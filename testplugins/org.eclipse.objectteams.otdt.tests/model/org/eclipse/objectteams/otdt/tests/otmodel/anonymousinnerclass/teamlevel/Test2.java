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
 * $Id: Test2.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Fraunhofer FIRST - Initial API and implementation
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otmodel.anonymousinnerclass.teamlevel;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.objectteams.otdt.tests.otmodel.FileBasedModelTest;

/**
 * $Id: Test2.java 23494 2010-02-05 23:06:44Z stephan $
 *
 * testcase:
 * a team class with a method
 * instantiation of an anonymous class inside the method
 * the anonymous class is an ordinary class
 */
public class Test2 extends FileBasedModelTest
{
    private IType _teamJavaElem = null;
    
    public Test2(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        if (true)
        {
            return new Suite(Test2.class);
        }
        junit.framework.TestSuite suite = new Suite(Test2.class
            .getName());
        return suite;
    }
    
    public void setUpSuite() throws Exception
    {
        setTestProjectDir("AnonymousInnerclass");
        super.setUpSuite();
    }
    
    protected void setUp() throws Exception
    {
		super.setUp();
        try
        {
            ICompilationUnit teamUnit = getCompilationUnit(
                "AnonymousInnerclass",
                "teamlevel",
                "teamlevel.teampkg",
                "Test2_SampleTeam.java");
            _teamJavaElem = teamUnit.getType("Test2_SampleTeam");
            
            System.out.println("\nAnonymousInnerclassTeamLevelTest2:");
            System.out.println("Teamklasse: " +_teamJavaElem);
        }
        catch (JavaModelException ex)
        {
            ex.printStackTrace();
        }
    } 
    
    public void testExistenceOfAnonymousType() throws JavaModelException
    {
        assertNotNull(_teamJavaElem);
        assertTrue(_teamJavaElem.exists());
        
        IMethod teamlevelMethod = _teamJavaElem.getMethod("teamlevelMethod", new String[0]);
        assertNotNull(teamlevelMethod);
        assertTrue(teamlevelMethod.exists());
        
        IType anonymousType = teamlevelMethod.getType("",1);
        assertNotNull(anonymousType);
        assertTrue(anonymousType.exists());
    }  
 
    
    private IType getAnonymousType() throws JavaModelException
    {
        if ((_teamJavaElem != null) && (_teamJavaElem.exists()))
        {
            IMethod teamlevelMethod = _teamJavaElem.getMethod("teamlevelMethod", new String[0]);
            
            if ((teamlevelMethod != null) && (teamlevelMethod.exists()))
            {
                IType anonymousType = teamlevelMethod.getType("",1);
                
                if ((anonymousType != null) && (anonymousType.exists()))
                {
                    return anonymousType;
                }
            }
        }
        return null;
    }    
    
    public void testContainmentOfMethodInAnonymousType() throws JavaModelException
    {
        IType anonymousType = getAnonymousType();
        assertNotNull(anonymousType);
        
        IMethod method = anonymousType.getMethod("additionalMethod", new String[0]);
        assertNotNull(method);
        assertTrue(method.exists());
    }
}
