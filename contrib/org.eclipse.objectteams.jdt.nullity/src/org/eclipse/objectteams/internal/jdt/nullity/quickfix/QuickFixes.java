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
package org.eclipse.objectteams.internal.jdt.nullity.quickfix;

import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.RequiredNonNullButProvidedNull;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.RequiredNonNullButProvidedPotentialNull;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.RequiredNonNullButProvidedUnknown;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.IllegalDefinitionToNonNullParameter;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.IllegalRedefinitionToNonNullParameter;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.IllegalReturnNullityRedefinition;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.ParameterLackingNonNullAnnotation;
import static org.eclipse.objectteams.internal.jdt.nullity.Constants.IProblem.ParameterLackingNullableAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.text.correction.proposals.FixCorrectionProposal;
import org.eclipse.jdt.ui.cleanup.ICleanUpFix;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.objectteams.internal.jdt.nullity.NullCompilerOptions;
import org.eclipse.swt.graphics.Image;

/**
 * Quickfixes for null-annotation related problems.
 * 
 * @author stephan
 */
@SuppressWarnings("restriction")
public class QuickFixes implements org.eclipse.jdt.ui.text.java.IQuickFixProcessor {

	/** Small adaptation just to make available the 'compilationUnit' passed at instantiation time. */
	static class MyCURewriteOperationsFix extends CompilationUnitRewriteOperationsFix {
		CompilationUnit cu;
		public MyCURewriteOperationsFix(String name, CompilationUnit compilationUnit, CompilationUnitRewriteOperation[] operations) 
		{
			super(name, compilationUnit, operations);
			this.cu = compilationUnit;
		}
	}

	public boolean hasCorrections(ICompilationUnit cu, int problemId) {
		switch (problemId) {
		case RequiredNonNullButProvidedNull:
		case RequiredNonNullButProvidedPotentialNull:
		case RequiredNonNullButProvidedUnknown:
		case IllegalReturnNullityRedefinition:
		case IllegalRedefinitionToNonNullParameter:
		case IllegalDefinitionToNonNullParameter:
		case ParameterLackingNonNullAnnotation:
		case ParameterLackingNullableAnnotation:
		case IProblem.NonNullLocalVariableComparisonYieldsFalse:
		case IProblem.RedundantNullCheckOnNonNullLocalVariable:
				return true;
		default:
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see IAssistProcessor#getCorrections(org.eclipse.jdt.internal.ui.text.correction.IAssistContext, org.eclipse.jdt.internal.ui.text.correction.IProblemLocation[])
	 */
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}

		HashSet<Integer> handledProblems= new HashSet<Integer>(locations.length);
		ArrayList<IJavaCompletionProposal> resultingCollections= new ArrayList<IJavaCompletionProposal>();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation curr= locations[i];
			Integer id= new Integer(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return (IJavaCompletionProposal[]) resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
	}

	void process(IInvocationContext context, IProblemLocation problem, Collection<IJavaCompletionProposal> proposals) {
		int id= problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		CompilationUnit astRoot= context.getASTRoot();
		ASTNode selectedNode= problem.getCoveringNode(astRoot);

		switch (id) {
		case IllegalReturnNullityRedefinition:
		case IllegalDefinitionToNonNullParameter:
		case IllegalRedefinitionToNonNullParameter:
			addNullAnnotationInSignatureProposal(context, problem, proposals, false);
			addNullAnnotationInSignatureProposal(context, problem, proposals, true);
			break;
		case RequiredNonNullButProvidedNull:
		case RequiredNonNullButProvidedPotentialNull:
		case RequiredNonNullButProvidedUnknown:
		case ParameterLackingNonNullAnnotation:
		case ParameterLackingNullableAnnotation:
		case IProblem.NonNullLocalVariableComparisonYieldsFalse:
		case IProblem.RedundantNullCheckOnNonNullLocalVariable:
			if (isComplainingAboutArgument(selectedNode)
				|| isComplainingAboutReturn(selectedNode))
				addNullAnnotationInSignatureProposal(context, problem, proposals, false);
			break;
		}
	}
		
	@SuppressWarnings("unchecked")
	void addNullAnnotationInSignatureProposal(IInvocationContext context,
											  IProblemLocation problem,
											  @SuppressWarnings("rawtypes") Collection proposals,
											  boolean modifyOverridden)
	{
		MyCURewriteOperationsFix fix= createNullAnnotationInSignatureFix(context.getASTRoot(), problem, modifyOverridden);
		
		if (fix != null) {
			Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
			Map<String, String> options= new Hashtable<String, String>();
			if (fix.cu != context.getASTRoot()) {
				// workaround: adjust the unit to operate on, depending on the findings of RewriteOperations.createAddAnnotationOperation(..)
				final CompilationUnit cu = fix.cu;
				final IInvocationContext originalContext = context;
				context = new IInvocationContext() {
					public int getSelectionOffset() {
						return originalContext.getSelectionOffset();
					}					
					public int getSelectionLength() {
						return originalContext.getSelectionLength();
					}
					public ASTNode getCoveringNode() {
						return originalContext.getCoveringNode();
					}					
					public ASTNode getCoveredNode() {
						return originalContext.getCoveredNode();
					}					
					public ICompilationUnit getCompilationUnit() {
						return (ICompilationUnit) cu.getJavaElement();
					}
					
					public CompilationUnit getASTRoot() {
						return cu;
					}
				};
			}
			int relevance = modifyOverridden ? 15 : 20; // TODO(SH): insert suitable values, just raise local change above change in overridden method
			FixCorrectionProposal proposal= new FixCorrectionProposal(fix, new NullAnnotationsCleanUp(options, this, problem.getProblemId()), relevance, image, context);
			proposals.add(proposal);
		}		
	}
	
	boolean isComplainingAboutArgument(ASTNode selectedNode) {
		if (!(selectedNode instanceof SimpleName)) {
			return false;
		}
		SimpleName nameNode= (SimpleName) selectedNode;
		IBinding binding = nameNode.resolveBinding();
		if (binding.getKind() == IBinding.VARIABLE && ((IVariableBinding) binding).isParameter())
			return true;
		VariableDeclaration argDecl = (VariableDeclaration) ASTNodes.getParent(selectedNode, VariableDeclaration.class);
		if (argDecl != null)
			binding = argDecl.resolveBinding();
		if (binding.getKind() == IBinding.VARIABLE && ((IVariableBinding) binding).isParameter())
			return true;
		return false;
	}
	
	boolean isComplainingAboutReturn(ASTNode selectedNode) {
		return selectedNode.getParent().getNodeType() == ASTNode.RETURN_STATEMENT;
	}

	MyCURewriteOperationsFix createNullAnnotationInSignatureFix(CompilationUnit compilationUnit, IProblemLocation problem, boolean modifyOverridden)
	{
		String nullableAnnotationName = getNullableAnnotationName(compilationUnit.getJavaElement(), false);
		String nonNullAnnotationName = getNonNullAnnotationName(compilationUnit.getJavaElement(), false);
		String annotationToAdd = nullableAnnotationName;
		String annotationToRemove = nonNullAnnotationName;
		
		switch (problem.getProblemId()) {
		case IllegalDefinitionToNonNullParameter:
		case IllegalRedefinitionToNonNullParameter:
		// case ParameterLackingNullableAnnotation: // never proposed with modifyOverridden
			if (modifyOverridden) {
				annotationToAdd = nonNullAnnotationName;
				annotationToRemove = nullableAnnotationName;
			}
			break;
		case ParameterLackingNonNullAnnotation:
		case IllegalReturnNullityRedefinition:
			if (!modifyOverridden) {
				annotationToAdd = nonNullAnnotationName;
				annotationToRemove = nullableAnnotationName;
			}
			break;
		// all others propose to add @Nullable
		}
		
		// when performing one change at a time we can actually modify another CU than the current one:
		RewriteOperations.SignatureAnnotationRewriteOperation operation = 
			RewriteOperations.createAddAnnotationOperation(
				compilationUnit, problem, annotationToAdd, annotationToRemove, null,
				false/*thisUnitOnly*/, true/*allowRemove*/, modifyOverridden);
		if (operation == null)
			return null;
		
		return new MyCURewriteOperationsFix(operation.getMessage(),
											operation.getCompilationUnit(), // note that this uses the findings from createAddAnnotationOperation(..)
											new RewriteOperations.SignatureAnnotationRewriteOperation[] {operation});
	}

	// Entry for NullAnnotationsCleanup:
	public ICleanUpFix createCleanUp(CompilationUnit compilationUnit, IProblemLocation[] locations, int problemID)
	{
		
		ICompilationUnit cu= (ICompilationUnit)compilationUnit.getJavaElement();
		if (!JavaModelUtil.is50OrHigher(cu.getJavaProject()))
			return null;
		
		List<CompilationUnitRewriteOperation> operations= new ArrayList<CompilationUnitRewriteOperation>();
		
		if (locations == null) {
			org.eclipse.jdt.core.compiler.IProblem[] problems= compilationUnit.getProblems();
			locations= new IProblemLocation[problems.length];
			for (int i= 0; i < problems.length; i++) {
				if (problems[i].getID() == problemID)
					locations[i]= new ProblemLocation(problems[i]);
			}
		}
		
		createAddNullAnnotationOperations(compilationUnit, locations, operations);
		
		if (operations.size() == 0)
			return null;
		
		CompilationUnitRewriteOperation[] operationsArray= operations.toArray(new CompilationUnitRewriteOperation[operations.size()]);
		return new MyCURewriteOperationsFix(FixMessages.QuickFixes_add_annotation_change_name, compilationUnit, operationsArray);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void createAddNullAnnotationOperations(CompilationUnit compilationUnit, IProblemLocation[] locations, List result) {
		String nullableAnnotationName = getNullableAnnotationName(compilationUnit.getJavaElement(), false);
		String nonNullAnnotationName = getNonNullAnnotationName(compilationUnit.getJavaElement(), false);
		Set<String> handledPositions = new HashSet<String>();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation problem= locations[i];
			if (problem == null) continue; // problem was filtered out by createCleanUp()
			String annotationToAdd = nullableAnnotationName;
			String annotationToRemove = nonNullAnnotationName;
			switch (problem.getProblemId()) {
			case IllegalDefinitionToNonNullParameter:
			case IllegalRedefinitionToNonNullParameter:
			case ParameterLackingNonNullAnnotation:
			case IllegalReturnNullityRedefinition:
				annotationToAdd = nonNullAnnotationName;
				annotationToRemove = nullableAnnotationName;
				// all others propose to add @Nullable
			}
			// when performing multiple changes we can only modify the one CU that the CleanUp infrastructure provides to the operation.
			CompilationUnitRewriteOperation fix = RewriteOperations.createAddAnnotationOperation(
						compilationUnit, problem, annotationToAdd, annotationToRemove,
						handledPositions, true/*thisUnitOnly*/, false/*allowRemove*/, false/*modifyOverridden*/);
			if (fix != null)
				result.add(fix);
		}
	}
	
	public static boolean isMissingNullAnnotationProblem(int id) {
		return id == RequiredNonNullButProvidedNull || id == RequiredNonNullButProvidedPotentialNull 
				|| id == IllegalReturnNullityRedefinition
				|| mayIndicateParameterNullcheck(id);
	}
	
	public static boolean mayIndicateParameterNullcheck(int problemId) {
		return problemId == IProblem.NonNullLocalVariableComparisonYieldsFalse || problemId == IProblem.RedundantNullCheckOnNonNullLocalVariable;
	}
	
	public static boolean hasExplicitNullAnnotation(ICompilationUnit compilationUnit, int offset) {
// FIXME(SH): check for existing annotations disabled due to lack of precision:
//		      should distinguish what is actually annotated (return? param? which?)
//		try {
//			IJavaElement problemElement = compilationUnit.getElementAt(offset);
//			if (problemElement.getElementType() == IJavaElement.METHOD) {
//				IMethod method = (IMethod) problemElement;
//				String nullable = getNullableAnnotationName(compilationUnit, true);
//				String nonnull = getNonNullAnnotationName(compilationUnit, true);
//				for (IAnnotation annotation : method.getAnnotations()) {
//					if (   annotation.getElementName().equals(nonnull)
//						|| annotation.getElementName().equals(nullable))
//						return true;
//				}
//			}
//		} catch (JavaModelException jme) {
//			/* nop */
//		}
		return false;
	}

	public static String getNullableAnnotationName(IJavaElement javaElement, boolean makeSimple) {
		String qualifiedName = javaElement.getJavaProject().getOption(NullCompilerOptions.OPTION_NullableAnnotationName, true);
		int lastDot;
		if (makeSimple && qualifiedName != null && (lastDot = qualifiedName.lastIndexOf('.')) != -1)
			return qualifiedName.substring(lastDot+1);
		return qualifiedName;
	}

	public static String getNonNullAnnotationName(IJavaElement javaElement, boolean makeSimple) {
		String qualifiedName = javaElement.getJavaProject().getOption(NullCompilerOptions.OPTION_NonNullAnnotationName, true);
		int lastDot;
		if (makeSimple && qualifiedName != null && (lastDot = qualifiedName.lastIndexOf('.')) != -1)
			return qualifiedName.substring(lastDot+1);
		return qualifiedName;
	}
}
