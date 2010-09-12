/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010 Fraunhofer Gesellschaft, Munich, Germany,
 * for its Fraunhofer Institute and Computer Architecture and Software
 * Technology (FIRST), Berlin, Germany and Technical University Berlin,
 * Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.ui.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.tests.refactoring.RefactoringTestSetup;
import org.eclipse.objectteams.otdt.ui.tests.util.JavaProjectHelper;

@SuppressWarnings("restriction")
public class OTRefactoringTestSetup extends RefactoringTestSetup {

	public OTRefactoringTestSetup(Test test) {
		super(test);
	}

//{ObjectTeams: changed project creation:
	protected IJavaProject createJavaProject(String string, String string2) throws CoreException {
		return JavaProjectHelper.createOTJavaProject("TestProject"+System.currentTimeMillis(), "bin");
// SH}
	}
}
