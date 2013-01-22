/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2012 Oliver Frank and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 *		Oliver Frank - Initial API and implementation
 *		Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otredyn.transformer.names;

import java.util.HashSet;
import java.util.List;

import org.eclipse.objectteams.otredyn.runtime.TeamManager;
import org.objectteams.IBoundBase;
import org.objectteams.ITeam;
import org.objectteams.ImplicitTeamActivation;
import org.objectteams.SneakyException;
import org.objectteams.Team;

/**
 * Container for class names used in the bytecode manipulating classes
 * @author Oliver Frank
 */
public abstract class ClassNames {
	public final static String I_BOUND_BASE_SLASH = IBoundBase.class.getName().replace('.', '/');
	public final static String OBJECT_SLASH = Object.class.getName().replace('.', '/');
	public final static String CLASS_SLASH = Class.class.getName().replace('.', '/');
	public final static String I_BOUND_BASE_DOT = IBoundBase.class.getName();
	public final static String TEAM_MANAGER_SLASH = TeamManager.class.getName().replace('.', '/');
	public final static String ITEAM_SLASH = ITeam.class.getName().replace('.', '/');
	public final static String TEAM_SLASH = Team.class.getName().replace('.', '/');
	public final static String LIST_SLASH = List.class.getName().replace('.', '/');
	public final static String HASH_SET_SLASH = HashSet.class.getName().replace('.', '/');
	public final static String IMPLICIT_ACTIVATION = ImplicitTeamActivation.class.getName().replace('.', '/');

	public static final String SNEAKY_EXCEPTION_SLASH = SneakyException.class.getName().replace('.', '/');
	// member of SneakyException:
	public static final String RETHROW_SELECTOR = "rethrow";
	public static final String RETHROW_SIGNATURE = "()V"; 
}