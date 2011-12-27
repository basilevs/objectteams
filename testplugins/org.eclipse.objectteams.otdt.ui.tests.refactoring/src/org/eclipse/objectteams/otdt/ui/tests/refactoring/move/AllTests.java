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
package org.eclipse.objectteams.otdt.ui.tests.refactoring.move;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author brcan
 * 
 * Runs all OT-specific refactoring tests and corresponding eclipse refactoring tests
 */
public class AllTests
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(
                "All OT-Move-Refactoring Tests");

        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.move.MoveInstanceMethodTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.move.MoveStaticMethodTests.suite());
        suite.addTest(org.eclipse.objectteams.otdt.ui.tests.refactoring.move.MoveRoleTests.suite());
        
        return suite;
    }
}
