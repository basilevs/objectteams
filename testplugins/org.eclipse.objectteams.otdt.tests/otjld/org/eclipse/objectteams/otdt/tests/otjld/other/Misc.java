/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 *
 * Copyright 2010 Stephan Herrmann
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
 *        Stephan Herrmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.otdt.tests.otjld.other;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.otdt.tests.otjld.AbstractOTJLDTest;

@SuppressWarnings("unchecked")
public class Misc extends AbstractOTJLDTest {

     public Misc(String name) {
             super(name);
     }

     // Static initializer to specify tests subset using TESTS_* static variables
     // All specified tests which does not belong to the class are skipped...
     static {
//        TESTS_NAMES = new String[] { "test04m_javadocBaseImportReference1"};
//        TESTS_NUMBERS = new int { 1459 };
//        TESTS_RANGE = new int { 1097, -1 };
     }

     public static Test suite() {
         return buildComparableTestSuite(testClass());
     }

     public static Class testClass() {
         return Misc.class;
     }

     // a non-abstract team tries to instantiate an abstract role
     // 0.m.1-otjld-abstract-relevant-role-instantiated-1
     public void test0m1_abstractRelevantRoleInstantiated1() {
         runNegativeTestMatching(
             new String[] {
 		"Team0m1arri1.java",
			    "\n" +
			    "public team class Team0m1arri1 {\n" +
			    "	abstract protected class Role {\n" +
			    "	}\n" +
			    "	void foo() {\n" +
			    "		Role r = new Role();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
             },
             "2.5(b)");
     }

     // an abstract team instantiates an abstract role
     // 0.m.1-otjld-abstract-relevant-role-instantiated-2
     public void test0m1_abstractRelevantRoleInstantiated2() {
        
        runConformTest(
             new String[] {
 		"Team0m1arri2_2.java",
			    "\n" +
			    "public team class Team0m1arri2_2 extends Team0m1arri2_1 {\n" +
			    "	protected class Role {\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		System.out.print((new Team0m1arri2_2()).foo());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team0m1arri2_1.java",
			    "\n" +
			    "public abstract team class Team0m1arri2_1 {\n" +
			    "	abstract protected class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	String foo() {\n" +
			    "		Role r = new Role();\n" +
			    "		return r.getValue();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
             },
             "OK");
     }

     // an abstract team instantiates an abstract role
     // 0.m.1-otjld-abstract-relevant-role-instantiated-3
     public void test0m1_abstractRelevantRoleInstantiated3() {
        
        runConformTest(
             new String[] {
 		"Team0m1arri3_2.java",
			    "\n" +
			    "public team class Team0m1arri3_2 extends Team0m1arri3_1 {\n" +
			    "	public class Role {\n" +
			    "	}\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team0m1arri3_1 t = new Team0m1arri3_2();\n" +
			    "		Role<@t> r = t.new Role();\n" +
			    "		System.out.print(r.getValue());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team0m1arri3_1.java",
			    "\n" +
			    "public abstract team class Team0m1arri3_1 {\n" +
			    "	public abstract class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "	String foo() {\n" +
			    "		Role r = new Role();\n" +
			    "		return r.getValue();\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
             },
             "OK");
     }

     // a non-abstract team instantiates an abstract role as externalized
     // 0.m.1-otjld-abstract-relevant-role-instantiated-4
     public void test0m1_abstractRelevantRoleInstantiated4() {
         runNegativeTestMatching(
             new String[] {
 		"T0m1arri4Main.java",
			    "\n" +
			    "public class T0m1arri4Main {\n" +
			    "	public static void main(String[] args) {\n" +
			    "		final Team0m1arri4 t = new Team0m1arri4();\n" +
			    "		Role<@t> r = t.new Role();\n" +
			    "		System.out.print(r.getValue());\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n",
		"Team0m1arri4.java",
			    "\n" +
			    "public team class Team0m1arri4 {\n" +
			    "	public abstract class Role {\n" +
			    "		public String getValue() { return \"OK\"; }\n" +
			    "	}\n" +
			    "}\n" +
			    "	\n"
             },
             "OTJLD 2.5(b)");
     }

     // 
     // 0.m.2-otjld-string-constant-limit-1
     public void test0m2_stringConstantLimit1() {
        
        runConformTest(
             new String[] {
 		"Team0m2scl1_2.java",
			    "\n" +
			    "public team class Team0m2scl1_2 extends Team0m2scl1_1 {\n" +
			    "    Team0m2scl1_2() {\n" +
			    "        R r = new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new Team0m2scl1_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team0m2scl1_1.java",
			    "\n" +
			    "public team class Team0m2scl1_1 {\n" +
			    "    protected class R {\n" +
			    "        String a=\"a\", b=\"b\", c=\"c\";\n" +
			    "        String d1=\"d1\", e1=\"e1\", f1=\"f1\", g1=\"g1\", h1=\"h1\", i1=\"i1\", j1=\"j1\", k1=\"k1\";\n" +
			    "        String d2=\"d2\", e2=\"e2\", f2=\"f2\", g2=\"g2\", h2=\"h2\", i2=\"i2\", j2=\"j2\", k2=\"k2\";\n" +
			    "        String d3=\"d3\", e3=\"e3\", f3=\"f3\", g3=\"g3\", h3=\"h3\", i3=\"i3\", j3=\"j3\", k3=\"k3\";\n" +
			    "        String d4=\"d4\", e4=\"e4\", f4=\"f4\", g4=\"g4\", h4=\"h4\", i4=\"i4\", j4=\"j4\", k4=\"k4\";\n" +
			    "        String d5=\"d5\", e5=\"e5\", f5=\"f5\", g5=\"g5\", h5=\"h5\", i5=\"i5\", j5=\"j5\", k5=\"k5\";\n" +
			    "        String d6=\"d6\", e6=\"e6\", f6=\"f6\", g6=\"g6\", h6=\"h6\", i6=\"i6\", j6=\"j6\", k6=\"k6\";\n" +
			    "        String d7=\"d7\", e7=\"e7\", f7=\"f7\", g7=\"g7\", h7=\"h7\", i7=\"i7\", j7=\"j7\", k7=\"k7\";\n" +
			    "        String ok=\"OK\";\n" +
			    "        protected void test() { System.out.print(ok); }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
             },
             "OK");
     }

     // an integer constant must remain within the range of short constant pool indices - read from bytecode
     // 0.m.2-otjld-string-constant-limit-2
     public void test0m2_stringConstantLimit2() {
        
        runConformTest(
             new String[] {
 		"Team0m2scl2_2.java",
			    "\n" +
			    "public team class Team0m2scl2_2 extends Team0m2scl2_1 {\n" +
			    "    protected class R {\n" +
			    "        String d1=\"d1\", e1=\"e1\", f1=\"f1\", g1=\"g1\", h1=\"h1\", i1=\"i1\", j1=\"j1\", k1=\"k1\";\n" +
			    "        String d2=\"d2\", e2=\"e2\", f2=\"f2\", g2=\"g2\", h2=\"h2\", i2=\"i2\", j2=\"j2\", k2=\"k2\";\n" +
			    "        String d3=\"d3\", e3=\"e3\", f3=\"f3\", g3=\"g3\", h3=\"h3\", i3=\"i3\", j3=\"j3\", k3=\"k3\";\n" +
			    "        String d4=\"d4\", e4=\"e4\", f4=\"f4\", g4=\"g4\", h4=\"h4\", i4=\"i4\", j4=\"j4\", k4=\"k4\";\n" +
			    "        String d5=\"d5\", e5=\"e5\", f5=\"f5\", g5=\"g5\", h5=\"h5\", i5=\"i5\", j5=\"j5\", k5=\"k5\";\n" +
			    "        String d6=\"d6\", e6=\"e6\", f6=\"f6\", g6=\"g6\", h6=\"h6\", i6=\"i6\", j6=\"j6\", k6=\"k6\";\n" +
			    "        String d7=\"d7\", e7=\"e7\", f7=\"f7\", g7=\"g7\", h7=\"h7\", i7=\"i7\", j7=\"j7\", k7=\"k7\";\n" +
			    "    }\n" +
			    "    Team0m2scl2_2() {\n" +
			    "        R r = new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new Team0m2scl2_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team0m2scl2_1.java",
			    "\n" +
			    "public team class Team0m2scl2_1 {\n" +
			    "    protected class R {\n" +
			    "        String a=\"a\", b=\"b\", c=\"c\";\n" +
			    "        String ok=\"OK\";\n" +
			    "	int much = 67000;\n" +
			    "        protected void test() { System.out.print(ok); System.out.print(much); }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
             },
             "OK67000");
     }

     // an integer constant must remain within the range of short constant pool indices - compile in one go (RoleModel lost its AST, still enable copying)
     // 0.m.2-otjld-string-constant-limit-3
     public void test0m2_stringConstantLimit3() {
        
        runConformTest(
             new String[] {
 		"Team0m2scl3_2.java",
			    "\n" +
			    "public team class Team0m2scl3_2 extends Team0m2scl3_1 {\n" +
			    "    protected class R {\n" +
			    "        String d1=\"d1\", e1=\"e1\", f1=\"f1\", g1=\"g1\", h1=\"h1\", i1=\"i1\", j1=\"j1\", k1=\"k1\";\n" +
			    "        String d2=\"d2\", e2=\"e2\", f2=\"f2\", g2=\"g2\", h2=\"h2\", i2=\"i2\", j2=\"j2\", k2=\"k2\";\n" +
			    "        String d3=\"d3\", e3=\"e3\", f3=\"f3\", g3=\"g3\", h3=\"h3\", i3=\"i3\", j3=\"j3\", k3=\"k3\";\n" +
			    "        String d4=\"d4\", e4=\"e4\", f4=\"f4\", g4=\"g4\", h4=\"h4\", i4=\"i4\", j4=\"j4\", k4=\"k4\";\n" +
			    "        String d5=\"d5\", e5=\"e5\", f5=\"f5\", g5=\"g5\", h5=\"h5\", i5=\"i5\", j5=\"j5\", k5=\"k5\";\n" +
			    "        String d6=\"d6\", e6=\"e6\", f6=\"f6\", g6=\"g6\", h6=\"h6\", i6=\"i6\", j6=\"j6\", k6=\"k6\";\n" +
			    "        String d7=\"d7\", e7=\"e7\", f7=\"f7\", g7=\"g7\", h7=\"h7\", i7=\"i7\", j7=\"j7\", k7=\"k7\";\n" +
			    "    }\n" +
			    "    Team0m2scl3_2() {\n" +
			    "        R r = new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new Team0m2scl3_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team0m2scl3_1.java",
			    "\n" +
			    "public team class Team0m2scl3_1 {\n" +
			    "    protected class R {\n" +
			    "        String a=\"a\", b=\"b\", c=\"c\";\n" +
			    "        String ok=\"OK\";\n" +
			    "	int much = 67000;\n" +
			    "        protected void test() { System.out.print(ok); System.out.print(much); }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
             },
             "OK67000");
     }

     // a long constant must remain within the range of short constant pool indices - read from bytecode
     // 0.m.2-otjld-string-constant-limit-4
     public void test0m2_stringConstantLimit4() {
        
        runConformTest(
             new String[] {
 		"Team0m2scl4_2.java",
			    "\n" +
			    "public team class Team0m2scl4_2 extends Team0m2scl4_1 {\n" +
			    "    protected class R {\n" +
			    "        String d1=\"d1\", e1=\"e1\", f1=\"f1\", g1=\"g1\", h1=\"h1\", i1=\"i1\", j1=\"j1\", k1=\"k1\";\n" +
			    "        String d2=\"d2\", e2=\"e2\", f2=\"f2\", g2=\"g2\", h2=\"h2\", i2=\"i2\", j2=\"j2\", k2=\"k2\";\n" +
			    "        String d3=\"d3\", e3=\"e3\", f3=\"f3\", g3=\"g3\", h3=\"h3\", i3=\"i3\", j3=\"j3\", k3=\"k3\";\n" +
			    "        String d4=\"d4\", e4=\"e4\", f4=\"f4\", g4=\"g4\", h4=\"h4\", i4=\"i4\", j4=\"j4\", k4=\"k4\";\n" +
			    "        String d5=\"d5\", e5=\"e5\", f5=\"f5\", g5=\"g5\", h5=\"h5\", i5=\"i5\", j5=\"j5\", k5=\"k5\";\n" +
			    "        String d6=\"d6\", e6=\"e6\", f6=\"f6\", g6=\"g6\", h6=\"h6\", i6=\"i6\", j6=\"j6\", k6=\"k6\";\n" +
			    "        String d7=\"d7\", e7=\"e7\", f7=\"f7\", g7=\"g7\", h7=\"h7\", i7=\"i7\", j7=\"j7\", k7=\"k7\";\n" +
			    "    }\n" +
			    "    Team0m2scl4_2() {\n" +
			    "        R r = new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new Team0m2scl4_2();\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n",
		"Team0m2scl4_1.java",
			    "\n" +
			    "public team class Team0m2scl4_1 {\n" +
			    "    protected class R {\n" +
			    "        String a=\"a\", b=\"b\", c=\"c\";\n" +
			    "        String ok=\"OK\";\n" +
			    "	long much = 67000000;\n" +
			    "        protected void test() { System.out.print(ok); System.out.print(much); }\n" +
			    "    }\n" +
			    "}    \n" +
			    "    \n"
             },
             "OK67000000");
     }

     // a float constant must remain within the range of short constant pool indices - compile in one go
     // 0.m.2-otjld-string-constant-limit-5
     public void test0m2_stringConstantLimit5() {
        
        runConformTest(
             new String[] {
 		"Team0m2scl5_2.java",
			    "\n" +
			    "public team class Team0m2scl5_2 extends Team0m2scl5_1 {\n" +
			    "    protected class R {\n" +
			    "        String d1=\"d1\", e1=\"e1\", f1=\"f1\", g1=\"g1\", h1=\"h1\", i1=\"i1\", j1=\"j1\", k1=\"k1\";\n" +
			    "        String d2=\"d2\", e2=\"e2\", f2=\"f2\", g2=\"g2\", h2=\"h2\", i2=\"i2\", j2=\"j2\", k2=\"k2\";\n" +
			    "        String d3=\"d3\", e3=\"e3\", f3=\"f3\", g3=\"g3\", h3=\"h3\", i3=\"i3\", j3=\"j3\", k3=\"k3\";\n" +
			    "        String d4=\"d4\", e4=\"e4\", f4=\"f4\", g4=\"g4\", h4=\"h4\", i4=\"i4\", j4=\"j4\", k4=\"k4\";\n" +
			    "        String d5=\"d5\", e5=\"e5\", f5=\"f5\", g5=\"g5\", h5=\"h5\", i5=\"i5\", j5=\"j5\", k5=\"k5\";\n" +
			    "        String d6=\"d6\", e6=\"e6\", f6=\"f6\", g6=\"g6\", h6=\"h6\", i6=\"i6\", j6=\"j6\", k6=\"k6\";\n" +
			    "        String d7=\"d7\", e7=\"e7\", f7=\"f7\", g7=\"g7\", h7=\"h7\", i7=\"i7\", j7=\"j7\", k7=\"k7\";\n" +
			    "    }\n" +
			    "    Team0m2scl5_2() {\n" +
			    "        R r = new R();\n" +
			    "        r.test();\n" +
			    "    }\n" +
			    "    public static void main (String[] args) {\n" +
			    "        new Team0m2scl5_2();\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n",
		"Team0m2scl5_1.java",
			    "\n" +
			    "public team class Team0m2scl5_1 {\n" +
			    "    protected class R {\n" +
			    "        String a=\"a\", b=\"b\", c=\"c\";\n" +
			    "        String ok=\"OK\";\n" +
			    "        float much = 6.7f;\n" +
			    "        protected void test() { System.out.print(ok); System.out.print(much); }\n" +
			    "    }\n" +
			    "}\n" +
			    "    \n"
             },
             "OK6.7");
     }

     // 
     // 0.m.3-otjld-mismatching-filename-1
     public void test0m3_mismatchingFilename1() {
         runNegativeTestMatching(
             new String[] {
 		"Team0m3mf1.java",
			    "\n" +
			    "public team class WrongName {\n" +
			    "    protected class R {}\n" +
			    "}    \n" +
			    "    \n"
             },
             "in its own");
     }

     // WITNESS for TPX-280
     // 0.m.4-otjld-private-toplevel-class-1
     public void test0m4_privateToplevelClass1() {
         runNegativeTestMatching(
             new String[] {
 		"T0m4ptc1_1.java",
			    "\n" +
			    "public class T0m4ptc1_1 {}\n" +
			    "private class T0m4ptc1_2 {}    \n" +
			    "    \n"
             },
             "Illegal modifier");
     }

     // javadoc references uses base import
     public void test04m_javadocBaseImportReference1() {
    	 Map customOptions = getCompilerOptions();
         customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
    	 customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
    	 customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
    	 customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PROTECTED);
    	 runConformTest(new String[] {
    	 "pteam/Team0m4jbir1.java",
    	 		"package pteam;\n" +
    	 		"import base pbase.T0m4jbir1;\n" +
    	 		"public team class Team0m4jbir1 {\n" +
    	 		"   /** Role for {@link T0m4jbir1} as its base.\n" +
    	 		"     * @see T0m4jbir1\n" +
    	 		"     */\n" +
    	 		"	protected class R playedBy T0m4jbir1 {} \n" +
    	 		"}\n",
    	 "pbase/T0m4jbir1.java",
    	 		"package pbase;\n" +
    	 		"public class T0m4jbir1 {}\n"
    	 	},
    	 	"",
    	 	null/*classLibraries*/,
    	 	false/*shouldFlushOutputDirectory*/,
    	 	null/*vmArguments*/,
    	 	customOptions,
    	 	null/*requestor*/);
     }
}
