/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2008 Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DebugUIDialogAdaptor2.java 23456 2010-02-04 20:44:45Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.internal.debug.adaptor.launching;

import base org.eclipse.jdt.internal.debug.ui.launcher.LocalJavaApplicationTabGroup;

/**
 * Apply adaptations of the super team to a class from org.eclipse.jdt.debug.ui.
 * 
 * @author stephan
 * @since 1.2.1
 */
@SuppressWarnings("restriction")
public team class DebugUIDialogAdaptor2 extends DebugUIDialogAdaptor {
	protected class JDTTabGroup
			extends LaunchConfigurationTabGroup
			playedBy LocalJavaApplicationTabGroup 
	{
		// empty role, just advise OT/Equinox to weave into one more base class. See Trac #145
	}
}
