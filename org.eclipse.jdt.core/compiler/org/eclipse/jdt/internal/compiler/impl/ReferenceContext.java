/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ReferenceContext.java 19873 2009-04-13 16:51:05Z stephan $
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

/*
 * Implementors are valid compilation contexts from which we can
 * escape in case of error:
 * For example: method, type, compilation unit or a lambda expression.
 */

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public interface ReferenceContext {

	void abort(int abortLevel, CategorizedProblem problem);

	CompilationResult compilationResult();

	CompilationUnitDeclaration getCompilationUnitDeclaration();

	boolean hasErrors();

	void tagAsHavingErrors();
	
	void tagAsHavingIgnoredMandatoryErrors(int problemId);

//{ObjectTeams: some errors will have to be removed
	void resetErrorFlag();
// SH}
}
