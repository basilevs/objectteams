/*******************************************************************************
 * Copyright (c) 2011 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation 
 *******************************************************************************/
package org.eclipse.objectteams.internal.jdt.nullity;

import static org.eclipse.jdt.core.compiler.IProblem.ImportRelated;
import static org.eclipse.jdt.core.compiler.IProblem.Internal;
import static org.eclipse.jdt.core.compiler.IProblem.MethodRelated;
import static org.eclipse.jdt.core.compiler.IProblem.TypeRelated;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

@SuppressWarnings("restriction")
public class Constants {

	/** Additional constants for {@link org.eclipse.jdt.internal.compiler.lookup.TagBits}. */
	static interface TagBits extends org.eclipse.jdt.internal.compiler.lookup.TagBits {
		long HasBoundArguments = ASTNode.Bit13; // for method bindings to avoid duplicate invocation of bindArguments()
		 // the following two should be added to TagBits.AllStandardAnnotationsMask:
		long AnnotationNullable = ASTNode.Bit56L;
		long AnnotationNonNull = ASTNode.Bit57L;
		long AnnotationNullableByDefault = ASTNode.Bit58L;
		long AnnotationNonNullByDefault = ASTNode.Bit59L;
	}
	
	// ASTNode:
	// for annotation reference:
	public static final int IsSynthetic = ASTNode.Bit7;
	
	/** Additional constants for {@link org.eclipse.jdt.internal.compiler.lookup.TypeIds}. */
	static interface TypeIds {
		final int T_ConfiguredAnnotationNullable = 63;
		final int T_ConfiguredAnnotationNonNull = 64;
		final int T_ConfiguredAnnotationNullableByDefault = 65;
		final int T_ConfiguredAnnotationNonNullByDefault = 66;
	}

	/** Additional constants for {@link org.eclipse.jdt.core.compiler.IProblem}. */
	public static interface IProblem {
		/** @since 3.7 */
		int RequiredNonNullButProvidedNull = TypeRelated + 910;
		/** @since 3.7 */
		int RequiredNonNullButProvidedPotentialNull = TypeRelated + 911;
		/** @since 3.7 */
		int RequiredNonNullButProvidedUnknown = TypeRelated + 912;
		/** @since 3.7 */
		int MissingNullAnnotationType = ImportRelated + 913;
		/** @since 3.7 */
		int IllegalReturnNullityRedefinition = MethodRelated + 914;
		/** @since 3.7 */
		int IllegalRedefinitionToNonNullParameter = MethodRelated + 915;
		/** @since 3.7 */
		int IllegalDefinitionToNonNullParameter = MethodRelated + 916;
		/** @since 3.7 */
		int ParameterLackingNonNullAnnotation = MethodRelated + 917;
		/** @since 3.7 */
		int PotentialNullMessageSendReference = Internal + 918;
		/** @since 3.7 */
		int RedundantNullCheckOnNonNullMessageSend = Internal + 919;
	}
	
	/** Translate from a nullness annotation to the corresponding tag bit or 0L. */
	public static long getNullnessTagbit(TypeBinding nullnessAnnotation) {
		switch (nullnessAnnotation.id) {
		case TypeIds.T_ConfiguredAnnotationNonNull : 
			return TagBits.AnnotationNonNull;
		case TypeIds.T_ConfiguredAnnotationNullable : 
			return TagBits.AnnotationNullable;
		default: 
			return 0L;
		}
	}
	
	/** 
	 * Translate from a nullness default (like <code>@NonNullByDefault</code>)
	 * to the corresponding concrete nullness (like <code>@NonNull</code>),
	 * both sides being represented by their tag bit.
	 * @param defaultTagbit given set of tag bits
	 * @return one of {@link TagBits#AnnotationNonNull}, {@link TagBits#AnnotationNullable} or 0L.
	 */
	public static long applyDefaultNullnessTagbit(long defaultTagbit) {
		if ((defaultTagbit & TagBits.AnnotationNonNullByDefault) != 0L)
			return TagBits.AnnotationNonNull;
		if ((defaultTagbit & TagBits.AnnotationNullableByDefault) != 0L)
			return TagBits.AnnotationNullable;
		return 0L;
	}
}
