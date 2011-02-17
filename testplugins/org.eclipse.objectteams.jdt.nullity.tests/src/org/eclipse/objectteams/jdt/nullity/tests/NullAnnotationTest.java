/*******************************************************************************
 * Copyright (c) 2010, 2011 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation 
 *******************************************************************************/
package org.eclipse.objectteams.jdt.nullity.tests;


import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractComparableTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.objectteams.internal.jdt.nullity.NullCompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
public class NullAnnotationTest extends AbstractComparableTest {

public NullAnnotationTest(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
		TESTS_NAMES = new String[] { "test_default_nullness_003a" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

public static Test suite() {
	return buildComparableTestSuite(testClass());
}

public static Class testClass() {
	return NullAnnotationTest.class;
}

// Conditionally augment problem detection settings
static boolean setNullRelatedOptions = true;
protected Map getCompilerOptions() {
    Map defaultOptions = super.getCompilerOptions();
    if (setNullRelatedOptions) {
    	defaultOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.ENABLED);
		
		defaultOptions.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);

		// enable null annotations:
		defaultOptions.put(NullCompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED);
		defaultOptions.put(NullCompilerOptions.OPTION_EmulateNullAnnotationTypes, CompilerOptions.ENABLED);
		// leave other new options at these defaults:
//		defaultOptions.put(CompilerOptions.OPTION_ReportNullContractViolation, CompilerOptions.ERROR);
//		defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullContractViolation, CompilerOptions.ERROR);
//		defaultOptions.put(CompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.WARNING);
		
//		defaultOptions.put(CompilerOptions.OPTION_NullableAnnotationName, "org.eclipse.jdt.annotation.Nullable");
//		defaultOptions.put(CompilerOptions.OPTION_NonNullAnnotationName, "org.eclipse.jdt.annotation.NonNull");
    }
    return defaultOptions;
}
// a nullable argument is dereferenced without a check
public void test_nullable_paramter_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@Nullable Object o) {\n" +
			  "        System.out.print(o.toString());\n" +
			  "    }\n" +
			  "}\n"},
	    "----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	System.out.print(o.toString());\n" + 
		"	                 ^\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// a null value is passed to a nullable argument
public void test_nullable_paramter_002() {
	runConformTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@Nullable Object o) {\n" +
			  "        // nop\n" +
			  "    }\n" +
			  "    void bar() {\n" +
			  "        foo(null);\n" +
			  "    }\n" +
			  "}\n"},
	    "");
}

// a non-null argument is checked for null
public void test_nonnull_parameter_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@NonNull Object o) {\n" +
			  "        if (o != null)\n" +
			  "              System.out.print(o.toString());\n" +
			  "    }\n" +
			  "}\n"},
	    "----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	if (o != null)\n" + 
		"	    ^\n" + 
		"Redundant null check: The variable o cannot be null at this location\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null argument is dereferenced without a check
public void test_nonnull_parameter_002() {
	runConformTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@NonNull Object o) {\n" +
			  "        System.out.print(o.toString());\n" +
			  "    }\n" +
			  "    public static void main(String... args) {\n" +
			  "        new X().foo(\"OK\");\n" +
			  "    }\n" +
			  "}\n"},
	    "OK");
}
// passing null to nonnull parameter 
public void test_nonnull_parameter_003() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(@NonNull Object o) {\n" +
			  "        System.out.print(o.toString());\n" +
			  "    }\n" +
			  "    void bar() {\n" +
			  "        foo(null);\n" +
			  "    }\n" +
			  "}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	foo(null);\n" + 
		"	    ^^^^\n" + 
		"Null contract violation: passing null to a parameter declared as @NonNull\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// passing potential null to nonnull parameter - target method is consumed from .class
public void test_nonnull_parameter_004() {
	runConformTest(
			new String[] {
				"Lib.java",
					"import org.eclipse.jdt.annotation.*;\n" +
				"public class Lib {\n" +
				"    void setObject(@NonNull Object o) { }\n" +
				"}\n"
			});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "    void bar(Lib l, boolean b) {\n" +
			  "        Object o = null;\n" +
			  "        if (b) o = new Object();\n" +
			  "        l.setObject(o);\n" +
			  "    }\n" +
			  "}\n"},
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	l.setObject(o);\n" + 
		"	            ^\n" + 
		"Null contract violation: potentially passing null to a parameter declared as @NonNull\n" + 
		"----------\n",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// passing unknown value to nonnull parameter  - target method is consumed from .class
public void test_nonnull_parameter_005() {
	runConformTest(
			new String[] {
				"Lib.java",
				"import org.eclipse.jdt.annotation.*;\n" +
				"public class Lib {\n" +
				"    void setObject(@NonNull Object o) { }\n" +
				"}\n"
			});
	runConformTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "    void bar(Lib l, Object o) {\n" +
			  "        l.setObject(o);\n" +
			  "    }\n" +
			  "}\n"},
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	l.setObject(o);\n" + 
		"	            ^\n" + 
		"Potential null contract violation: insufficient nullness information regarding a value that is passed to a parameter declared as @NonNull\n" + 
		"----------\n",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// assigning potential null to a nonnull local variable
public void test_nonnull_local_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			  "import org.eclipse.jdt.annotation.*;\n" +
			  "public class X {\n" +
			  "    void foo(boolean b, Object p) {\n" +
			  "        @NonNull Object o1 = b ? null : new Object();\n" +
			  "        @NonNull String o2 = \"\";\n" +
			  "        o2 = null;\n" +
			  "        @NonNull Object o3 = p;\n" +
			  "    }\n" +
			  "}\n"},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	@NonNull Object o1 = b ? null : new Object();\n" + 
		"	                     ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Null contract violation: potentially assigning null to local variable o1, which is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	o2 = null;\n" + 
		"	     ^^^^\n" + 
		"Null contract violation: assigning null to local variable o2, which is declared as @NonNull\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 7)\n" + 
		"	@NonNull Object o3 = p;\n" + 
		"	                     ^\n" + 
		"Potential null contract violation: insufficient nullness information regarding a value that is assigned to local variable o3, which is declared as @NonNull\n" + 
		"----------\n",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// a method tries to tighten the null contract, super declares parameter o as @Nullable
// other parameters: s is redefined from not constrained to @Nullable which is OK
//                   third is redefined from not constrained to @NonNull which is bad, too
public void test_parameter_contract_inheritance_001() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    void foo(String s, @Nullable Object o, Object third) { }\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }\n" + 
		"	                                             ^\n" + 
		"Cannot tighten null contract for parameter o, inherited method from Lib declares this parameter as @Nullable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }\n" + 
		"	                                                                ^^^^^\n" + 
		"Cannot tighten null contract for parameter third, inherited method from Lib does not constrain this parameter\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a method body fails to handle the inherited null contract, super declares parameter as @Nullable
public void test_parameter_contract_inheritance_002() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    void foo(@Nullable Object o) { }\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    void foo(Object o) {\n" +
			"        System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	System.out.print(o.toString());\n" + 
		"	                 ^\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a method relaxes the parameter null contract, super interface declares parameter o as @NonNull
// other (first) parameter just repeats the inherited @NonNull
public void test_parameter_contract_inheritance_003() {
	runConformTest(
		new String[] {
			"IX.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IX {\n" +
			"    void foo(@NonNull String s, @NonNull Object o);\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X implements IX {\n" +
			"    public void foo(@NonNull String s, @Nullable Object o) { ; }\n" +
			"    void bar() { foo(\"OK\", null); }\n" +
			"}\n"
		},
		"");
}
// a method adds a @NonNull annotation, super interface has no null annotation
// changing other from unconstrained to @Nullable is OK
public void test_parameter_contract_inheritance_004() {
	runConformTest(
		new String[] {
			"IX.java",
			"public interface IX {\n" +
			"    void foo(Object o, Object other);\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X implements IX {\n" +
			"    public void foo(@NonNull Object o, @Nullable Object other) { System.out.print(o.toString()); }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	public void foo(@NonNull Object o, @Nullable Object other) { System.out.print(o.toString()); }\n" + 
		"	                                ^\n" + 
		"Cannot tighten null contract for parameter o, inherited method from IX does not constrain this parameter\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a method tries to relax the null contract, super declares @NonNull return
public void test_parameter_contract_inheritance_005() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	@Nullable Object getObject() { return null; }\n" + 
		"	          ^^^^^^\n" + 
		"Cannot relax null contract for method return, inherited method from Lib is declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// super has no contraint for return, sub method confirms the null contract as @Nullable 
public void test_parameter_contract_inheritance_006() {
	runConformTest(
		new String[] {
			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return null; }\n" +
			"}\n"
		});
	runConformTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"}\n"
		},
		"",
		null/*classLibs*/,
		false /* flush output directory */,
		null/*vmArguments*/,
		null/*customOptions*/,
		null/*compilerRequestor*/);
}
// a method body violates the inherited null contract, super declares @NonNull return
public void test_parameter_contract_inheritance_007() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"public class X extends Lib {\n" +
			"    @Override\n" +
			"    Object getObject() { return null; }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	Object getObject() { return null; }\n" + 
		"	                     ^^^^^^^^^^^^\n" + 
		"Null contract violation: returning null from a method declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a client potentially violates the inherited null contract, super interface declares @NonNull parameter
public void test_parameter_contract_inheritance_008() {
	Map options = getCompilerOptions();
	options.put(NullCompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"IX.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public interface IX {\n" +
			"    void printObject(@NonNull Object o);\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"public class X implements IX {\n" +
			"    public void printObject(Object o) { System.out.print(o.toString()); }\n" +
			"}\n",
			"M.java",
			"public class M{\n" +
			"    void foo(X x, Object o) {\n" +
			"        x.printObject(o);\n" +
			"    }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		options,
		"----------\n" + 
		"1. ERROR in M.java (at line 3)\n" + 
		"	x.printObject(o);\n" + 
		"	              ^\n" + 
		"Potential null contract violation: insufficient nullness information regarding a value that is passed to a parameter declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a static method has a more relaxed null contract than a like method in the super class, but no overriding.
public void test_parameter_contract_inheritance_009() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull static Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X extends Lib {\n" +
			"    @Nullable static Object getObject() { return null; }\n" +
			"}\n"
		},
		"");
}
// a nullable return value is dereferenced without a check
public void test_nullable_return_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"    void foo() {\n" +
			"        Object o = getObject();\n" +
			"        System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	System.out.print(o.toString());\n" + 
		"	                 ^\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a nullable return value is dereferenced without a check, method is read from .class file
public void test_nullable_return_002() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @Nullable Object getObject() { return null; }\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(Lib l) {\n" +
			"        Object o = l.getObject();\n" +
			"        System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	System.out.print(o.toString());\n" + 
		"	                 ^\n" + 
		"Potential null pointer access: The variable o may be null at this location\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null return value is checked for null, method is read from .class file
public void test_nonnull_return_001() {
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n"
		});
	runNegativeTest(
		false /* flush output directory */,
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo(Lib l) {\n" +
			"        Object o = l.getObject();\n" +
			"        if (o != null)\n" +
			"            System.out.print(o.toString());\n" +
			"    }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	if (o != null)\n" + 
		"	    ^\n" + 
		"Redundant null check: The variable o cannot be null at this location\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null method returns null
public void test_nonnull_return_003() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(boolean b) {\n" +
			"        if (b)\n" +
			"            return null;\n" + // definite contract violation despite enclosing "if"
			"        return new Object();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	return null;\n" + 
		"	^^^^^^^^^^^^\n" + 
		"Null contract violation: returning null from a method declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null method potentially returns null
public void test_nonnull_return_004() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(@Nullable Object o) {\n" +
			"        return o;\n" + // 'o' is only potentially null
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	return o;\n" + 
		"	^^^^^^^^^\n" + 
		"Null contract violation: return value can be null but method is declared as @NonNull\n" + 
		"----------\n");
}
// a non-null method returns its non-null argument
public void test_nonnull_return_005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(@NonNull Object o) {\n" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null/*classLibs*/,
		true/*shouldFlushOutputDirectory*/,
		null/*vmArguments*/,
		customOptions,
		null/*compilerRequestor*/);
}
//a non-null method has insufficient nullness info for its return value
public void test_nonnull_return_006() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(Object o) {\n" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	return o;\n" + 
		"	^^^^^^^^^\n" + 
		"Potential null contract violation: insufficient nullness information regarding return value while the method is declared as @NonNull\n" + 
		"----------\n");
}
// a result from a nullable method is directly dereferenced
public void test_nonnull_return_007() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable Object getObject() {\n" +
			"        return null;\n" +
			"    }\n" +
			"    void test() {\n" +
			"        getObject().toString();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	getObject().toString();\n" + 
		"	^^^^^^^^^^^\n" + 
		"Potential null pointer access: The method getObject() may return null\n" + 
		"----------\n");
}
// a result from a nonnull method is directly checked for null: redundant
public void test_nonnull_return_008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true /* flushOutputDirectory*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject() {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        if (getObject() == null)\n" +
			"		     throw new RuntimeException();\n" +
			"    }\n" +
			"}\n"
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (getObject() == null)\n" + 
		"	    ^^^^^^^^^^^\n" + 
		"Redundant null check: The method getObject() cannot return null\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a result from a nonnull method is directly checked for null (from local): redundant
public void test_nonnull_return_009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true /* flushOutputDirectory*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject() {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        Object left = null;\n" +
			"        if (left != getObject())\n" +
			"		     throw new RuntimeException();\n" +
			"    }\n" +
			"}\n"
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	if (left != getObject())\n" + 
		"	    ^^^^\n" + 
		"Redundant null check: The variable left can only be null at this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	if (left != getObject())\n" + 
		"	            ^^^^^^^^^^^\n" + 
		"Redundant null check: The method getObject() cannot return null\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a result from a nullable method is assigned and checked for null (from local): not redundant
// see also Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
public void test_nonnull_return_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true /* flushOutputDirectory*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @Nullable X getX() {\n" +
			"        return new X();\n" +
			"    }\n" +
			"    void test() {\n" +
			"        X left = this;\n" +
			"        do {\n" +
			"            if (left == null) \n" +
			"	   	         throw new RuntimeException();\n" +
			"        } while ((left = left.getX()) != null);\n" +
			"    }\n" +
			"}\n"
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	if (left == null) \n" + 
		"	    ^^^^\n" + 
		"Null comparison always yields false: The variable left cannot be null at this location\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// mixed use of fully qualified name / explicit import
public void test_annotation_import_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullableAnnotationName, "org.foo.Nullable");
	customOptions.put(NullCompilerOptions.OPTION_NonNullAnnotationName, "org.foo.NonNull");
	runConformTest(
		new String[] {
			"Lib.java",
			"public class Lib {\n" +
			"    @org.foo.NonNull Object getObject() { return new Object(); }\n" + 	// FQN
			"}\n",
			"X.java",
			"import org.foo.NonNull;\n" +											// explicit import
			"public class X {\n" +
			"    @NonNull Object getObject(@NonNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null/*classLibs*/,
		true/*shouldFlushOutputDirectory*/,
		null/*vmArguments*/,
		customOptions,
		null/*compilerRequestor*/);
}

// use of explicit imports throughout
public void test_annotation_import_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullableAnnotationName, "org.foo.Nullable");
	customOptions.put(NullCompilerOptions.OPTION_NonNullAnnotationName, "org.foo.NonNull");
	runConformTest(
		new String[] {
			"Lib.java",
			"import org.foo.NonNull;\n" +
			"public class Lib {\n" +
			"    @NonNull Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"import org.foo.NonNull;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(@org.foo.Nullable String dummy, @NonNull Lib l) {\n" +
			"        Object o = l.getObject();" +
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null/*classLibs*/,
		true/*shouldFlushOutputDirectory*/,
		null/*vmArguments*/,
		customOptions,
		null/*compilerRequestor*/);
}
// explicit import of existing annotation types
public void test_annotation_import_005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullableAnnotationName, "org.foo.MayBeNull");
	customOptions.put(NullCompilerOptions.OPTION_NonNullAnnotationName, "org.foo.MustNotBeNull");
	customOptions.put(NullCompilerOptions.OPTION_EmulateNullAnnotationTypes, CompilerOptions.DISABLED);
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"org/foo/MayBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface MayBeNull {}\n",
			
			"org/foo/MustNotBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface MustNotBeNull {}\n",

			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"import org.foo.*;\n" +
			"public class X {\n" +
			"    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n",
			
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	return l.getObject();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Potential null contract violation: insufficient nullness information regarding return value while the method is declared as @MustNotBeNull\n" +
//		"Potential null contract violation: insufficient nullness information for checking return value against declaration as @MustNotBeNull.\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null method returns a value obtained from an unannotated method, missing annotation types
public void test_annotation_import_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportNullContractInsufficientInfo, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullableAnnotationName, "org.foo.MayBeNull");
	customOptions.put(NullCompilerOptions.OPTION_NonNullAnnotationName, "org.foo.MustNotBeNull");
	customOptions.put(NullCompilerOptions.OPTION_EmulateNullAnnotationTypes, CompilerOptions.DISABLED);
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"Lib.java",
			"public class Lib {\n" +
			"    Object getObject() { return new Object(); }\n" +
			"}\n",
			"X.java",
			"public class X {\n" +
			"    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n"
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" + 
		"	@MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" + 
		"	 ^^^^^^^^^^^^^\n" + 
		"MustNotBeNull cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	@MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" + 
		"	                                 ^^^^^^^^^^^^^\n" + 
		"MustNotBeNull cannot be resolved to a type\n" + 
// TODO(SH): may want to report more specific error: 
//		"1. ERROR in Lib.java (at line 0)\n" + 
//		"	public class Lib {\n" + 
//		"	^\n" + 
//		"Buildpath problem: the type org.foo.MayBeNull which is configured as a null annotation type cannot be resolved.\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// emulation names conflict with existing types, DISABLED because we can't test the AbortCompilation exception.
public void _test_annotation_emulation_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullableAnnotationName, "libpack.Lib");
	customOptions.put(NullCompilerOptions.OPTION_NonNullAnnotationName, "libpack.Lib");
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"libpack/Lib.java",
			"package libpack;\n" +
			"public class Lib {\n" +
			"}\n",
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in libpack\\Lib.java (at line 0)\n" + 
		"	package libpack;\n" + 
		"	^\n" + 
		"Buildpath problem: emulation of type libpack.Lib is requested (for null annotations) but a type of this name exists on the build path\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// regular use (explicit import/FQN) of existing annotation types (=no emulation)
public void test_annotation_emulation_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportPotentialNullContractViolation, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullableAnnotationName, "org.foo.MayBeNull");
	customOptions.put(NullCompilerOptions.OPTION_NonNullAnnotationName, "org.foo.MustNotBeNull");
	customOptions.put(NullCompilerOptions.OPTION_EmulateNullAnnotationTypes, CompilerOptions.DISABLED);
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"org/foo/MayBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface MayBeNull {}\n",
			
			"org/foo/MustNotBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			"public @interface MustNotBeNull {}\n",
			
			"Lib.java",
			"public class Lib {\n" +
			"    @org.foo.MayBeNull Object getObject() { return new Object(); }\n" + 	// FQN
			"}\n",
			"X.java",
			"import org.foo.MustNotBeNull;\n" +											// explicit import
			"public class X {\n" +
			"    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {\n" +
			"        return l.getObject();\n" +
			"    }\n" +
			"}\n",

		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	return l.getObject();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Null contract violation: return value can be null but method is declared as @MustNotBeNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// a default null annotation is illegally used on a class:
public void test_illegal_annotation_001() {
	runNegativeTest(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNull public class X {\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	@NonNull public class X {\n" + 
		"	^^^^^^^^\n" + 
		"The annotation @NonNull is disallowed for this location\n" + 
		"----------\n");	
}
public void test_default_nullness_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportPotentialNullContractViolation, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullnessDefault, NullCompilerOptions.NULLABLE);
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    @NonNull Object getObject(Object o) {\n" +
			"        return o;\n" + // illegal due to default @Nullable of parameter
			"    }\n" +
			"}\n",

		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	return o;\n" + 
		"	^^^^^^^^^\n" + 
		"Null contract violation: return value can be null but method is declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test_default_nullness_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportPotentialNullContractViolation, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_NullnessDefault, NullCompilerOptions.NONNULL);
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			"    Object getObject(@Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
			"Y.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends X {\n" +
			"    @Override\n" +
			"    @Nullable Object getObject(Object o) {\n" + // don't complain of parameter: inherited has precedence over default
			"        return o;\n" +
			"    }\n" +
			"}\n",
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in Y.java (at line 4)\n" + 
		"	@Nullable Object getObject(Object o) {\n" + 
		"	          ^^^^^^\n" + 
		"Cannot relax null contract for method return, inherited method from X is declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test_default_nullness_003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportPotentialNullContractViolation, CompilerOptions.ERROR);
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected Object getObject(@Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"}\n",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    protected @Nullable Object getObject(Object o) {\n" + // don't complain of parameter: inherited has precedence over default
			"        bar(o);\n" +
			"        return o;\n" +
			"    }\n" +
			"	 void bar(Object o2) { }\n" + // parameter is nonnull per package default
			"}\n"
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in p2\\Y.java (at line 5)\n" + 
		"	protected @Nullable Object getObject(Object o) {\n" + 
		"	                    ^^^^^^\n" + 
		"Cannot relax null contract for method return, inherited method from X is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in p2\\Y.java (at line 6)\n" + 
		"	bar(o);\n" + 
		"	    ^\n" + 
		"Null contract violation: potentially passing null to a parameter declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void test_default_nullness_003a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(NullCompilerOptions.OPTION_ReportPotentialNullContractViolation, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
	"p1/X.java",
			"package p1;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    protected Object getObject(@Nullable Object o) {\n" +
			"        return new Object();\n" +
			"    }\n" +
			"	 protected void bar(Object o2) { }\n" + // parameter is nonnull per type default
			"}\n"});
	// check if default is visible from X.class.
	runNegativeTest(
		false/*shouldFlushOutputDirectory*/,
		new String[] {
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			"    protected @Nullable Object getObject(Object o) {\n" + // can't override inherited default nonnull 
			"        bar(o);\n" + // parameter is nonnull in super class's .class file
			"        return o;\n" +
			"    }\n" +
			"}\n"
		},
		null/*classLibs*/,
		customOptions,
		"----------\n" + 
		"1. ERROR in p2\\Y.java (at line 5)\n" + 
		"	protected @Nullable Object getObject(Object o) {\n" + 
		"	                    ^^^^^^\n" + 
		"Cannot relax null contract for method return, inherited method from X is declared as @NonNull\n" + 
		"----------\n" + 
		"2. ERROR in p2\\Y.java (at line 6)\n" + 
		"	bar(o);\n" + 
		"	    ^\n" + 
		"Null contract violation: potentially passing null to a parameter declared as @NonNull\n" + 
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
}
