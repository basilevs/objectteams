/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2013 GK Software AG
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.objectteams.org for updates and contact.
 * 
 * Contributors:
 * 	Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.internal.osgi.weaving;

import static org.eclipse.objectteams.otequinox.TransformerPlugin.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WovenClass;

/**
 * Each instance of this class represents the fact that a given base bundle has aspect bindings,
 * which require to load / instantiate / activate one or more teams at a suitable point in time.
 */
@NonNullByDefault
public class BaseBundleLoadTrigger {

	private AspectBindingRegistry aspectBindingRegistry;
	@SuppressWarnings("deprecation")
	private @Nullable org.osgi.service.packageadmin.PackageAdmin admin;

	private String baseBundleName;	
	private boolean otreAdded = false;
	private List<AspectBinding> aspectBindings = new ArrayList<>();

	public BaseBundleLoadTrigger(String bundleSymbolicName, AspectBindingRegistry aspectBindingRegistry, 
			@SuppressWarnings("deprecation") @Nullable org.osgi.service.packageadmin.PackageAdmin admin) 
	{
		this.baseBundleName = bundleSymbolicName;
		this.aspectBindingRegistry = aspectBindingRegistry;
		this.admin = admin;
	}
	
	/**
	 * Signal that the given class is being loaded and trigger any necessary steps:
	 * (1) add import to OTRE (now)
	 * (2) scan team (now)
	 * (3) add imports base->aspect (now)
	 * (4) load & instantiate & activate (now or later).
	 */
	@SuppressWarnings("deprecation") // uses deprecated PackageAdmin
	public void fire(WovenClass baseClass, Set<String> beingDefined, OTWeavingHook hook) {

		// (1) OTRE import added once per base bundle:
		synchronized(this) {
			if (!otreAdded) {
				otreAdded = true;
				log(IStatus.INFO, "Adding OTRE import to "+baseBundleName);
				List<String> imports = baseClass.getDynamicImports();
				imports.add("org.objectteams");
			}
		}
		
		// for each team in each aspect binding:
		if (aspectBindings.isEmpty())
			aspectBindings.addAll(aspectBindingRegistry.getAdaptingAspectBindings(baseBundleName));
		List<WaitingTeamRecord> deferredTeamClasses = new ArrayList<>();
		for (AspectBinding aspectBinding : aspectBindings) {
			if (!aspectBinding.hasScannedTeams)
				log(IStatus.INFO, "Preparing aspect binding for base bundle "+baseBundleName);
			final org.osgi.service.packageadmin.PackageAdmin admin2 = admin;
			if (admin2 == null) {
				log(IStatus.ERROR, "Cannot find aspect bundle "+aspectBinding.aspectPlugin);
				continue;					
			} else {
				Bundle aspectBundle = aspectBinding.aspectBundle;
				if (aspectBundle == null) {
					Bundle[] aspectBundles = admin2.getBundles(aspectBinding.aspectPlugin, null);
					if (aspectBundles == null || aspectBundles.length == 0) {
						log(IStatus.ERROR, "Cannot find aspect bundle "+aspectBinding.aspectPlugin);
						continue;
					}
					aspectBundle = aspectBundles[0];
					assert aspectBundle != null : "Package admin should not return a null array element";
				}
				// (2) scan all teams in affecting aspect bindings:
				if (!aspectBinding.hasScannedTeams)
					aspectBinding.scanTeamClasses(aspectBundle);
				
				// (3) add dependencies to the base bundle:
				aspectBinding.addImports(baseClass);

				// (4) try optional steps:
				TeamLoader loading = new TeamLoader(deferredTeamClasses, beingDefined);
				loading.loadTeamsForBase(aspectBundle, aspectBinding, baseClass);
			}
		}

		// if some had to be deferred collect them now:
		if (!deferredTeamClasses.isEmpty()) {
			hook.addDeferredTeamClasses(deferredTeamClasses);
		}

		// mark done for this base class 
		// (do outside the look in case multiple aspect bindings point to the same baseBundle)
		for (AspectBinding aspectBinding : aspectBindings) {
			String baseClassName = baseClass.getClassName();
			assert baseClassName != null : "WovenClass.getClassName() should not answer null";
			aspectBinding.cleanUp(baseClassName);
		}
	}

	public boolean isDone() {
		for (AspectBinding binding : aspectBindings)
			if (!binding.isDone())
				return false;
		return true;
	}
}