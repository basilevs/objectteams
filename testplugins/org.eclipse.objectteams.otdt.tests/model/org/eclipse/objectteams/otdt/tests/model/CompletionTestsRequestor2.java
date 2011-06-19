/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: CompletionTestsRequestor2.java 23494 2010-02-05 23:06:44Z stephan $
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fraunhofer FIRST - extended API and implementation
 *     Technical University Berlin - extended API and implementation
 *******************************************************************************/
package org.eclipse.objectteams.otdt.tests.model;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.core.JavaElement;

// OT_COPY_PASTE with modifications (SH)
public class CompletionTestsRequestor2 extends CompletionRequestor {
	private final char[] NULL_LITERAL = "null".toCharArray();//$NON-NLS-1$
	
	private CompletionContext context;
	public int proposalsPtr = -1;
	private final static int PROPOSALS_INCREMENT = 10;
	private CompletionProposal[] proposals = new CompletionProposal[PROPOSALS_INCREMENT];
	private IProblem problem;
	
	private boolean showParameterNames;
	private boolean showUniqueKeys;
	private boolean showPositions;
	private boolean showTokenPositions;
	private boolean shortContext;
	private boolean showMissingTypes;
	
	private boolean computeVisibleElements;
	private boolean computeEnclosingElement;
	private String assignableType;
	
	public boolean fDebug = false;

	public CompletionTestsRequestor2() {
		this(false, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames) {
		this(showParamNames, false, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys) {
		this(showParamNames, showUniqueKeys, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions) {
		this(showParamNames, showUniqueKeys, showPositions, true, false);
	}
	
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext) {
		this(showParamNames, showUniqueKeys, showPositions, shortContext, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext, boolean showMissingTypes) {
		this(showParamNames, showUniqueKeys, showPositions, shortContext, showMissingTypes, false);
	}
	public CompletionTestsRequestor2(boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext, boolean showMissingTypes, boolean showTokenPositions) {
		this(false, showParamNames, showUniqueKeys, showPositions, shortContext, showMissingTypes, showTokenPositions);
	}
	public CompletionTestsRequestor2(boolean ignoreAll, boolean showParamNames, boolean showUniqueKeys, boolean showPositions, boolean shortContext, boolean showMissingTypes, boolean showTokenPositions) {
		super(ignoreAll);
		this.showParameterNames = showParamNames;
		this.showUniqueKeys = showUniqueKeys;
		this.showPositions = showPositions;
		this.showTokenPositions =  showTokenPositions;
		this.shortContext = shortContext;
		this.showMissingTypes = showMissingTypes;
		
	}
	public void acceptContext(CompletionContext cc) {
		this.context = cc;
	}
	public void accept(CompletionProposal proposal) {
		int length = this.proposals.length;
		if (++this.proposalsPtr== length) {
			System.arraycopy(this.proposals, 0, this.proposals = new CompletionProposal[length+PROPOSALS_INCREMENT], 0, length);
		}
		this.proposals[this.proposalsPtr] = proposal;
	}
	
	public void allowAllRequiredProposals() {
		for (int i = CompletionProposal.ANONYMOUS_CLASS_DECLARATION; i <= CompletionProposal.TYPE_IMPORT; i++) {
			for (int j = CompletionProposal.ANONYMOUS_CLASS_DECLARATION; j <= CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER; j++) {
				this.setAllowsRequiredProposals(i, j, true);
			}
		}
	}

	public void completionFailure(IProblem p) {
		this.problem = p;
	}
	
	public String getContext() {
		if(this.context == null) return "";
		
		StringBuffer buffer = new StringBuffer();
		
		if(!this.shortContext) {
			buffer.append("completion offset=");
			buffer.append(context.getOffset());
			buffer.append('\n');
			
			buffer.append("completion range=[");
			buffer.append(context.getTokenStart());
			buffer.append(", ");
			buffer.append(context.getTokenEnd());
			buffer.append("]\n");
			
			char[] token = context.getToken();
			buffer.append("completion token=");
			if(token == null) {
				buffer.append("null");
			} else {
				buffer.append('\"');
				buffer.append(token);
				buffer.append('\"');
			}
			buffer.append('\n');
			
			buffer.append("completion token kind=");
			int tokenKind = context.getTokenKind();
			if(tokenKind == CompletionContext.TOKEN_KIND_STRING_LITERAL) {
				buffer.append("TOKEN_KIND_STRING_LITERAL");
			} else if(tokenKind == CompletionContext.TOKEN_KIND_NAME) {
				buffer.append("TOKEN_KIND_NAME");
			} else {
				buffer.append("TOKEN_KIND_UNKNOWN");
			}
			buffer.append('\n');
		}
		char[][] expectedTypesSignatures = this.context.getExpectedTypesSignatures();
		buffer.append("expectedTypesSignatures=");
		if(expectedTypesSignatures == null) {
			buffer.append(NULL_LITERAL);
		} else {
			buffer.append('{');
			for (int i = 0; i < expectedTypesSignatures.length; i++) {
				if(i > 0) buffer.append(',');
				buffer.append(expectedTypesSignatures[i]);
				
			}
			buffer.append('}');
		}
		buffer.append('\n');
		
		char[][] expectedTypesKeys = this.context.getExpectedTypesKeys();
		buffer.append("expectedTypesKeys=");
		if(expectedTypesSignatures == null) {
			buffer.append(NULL_LITERAL);
		} else {
			buffer.append('{');
			for (int i = 0; i < expectedTypesKeys.length; i++) {
				if(i > 0) buffer.append(',');
				buffer.append(expectedTypesKeys[i]);
				
			}
			buffer.append('}');
		}
		
		if(!this.shortContext) {
			buffer.append('\n');
			
			int locationType = this.context.getTokenLocation();
			if (locationType == 0) {
				buffer.append("completion token location=UNKNOWN"); //$NON-NLS-1$
			} else {
				buffer.append("completion token location={"); //$NON-NLS-1$
				boolean first = true;
				if ((locationType & CompletionContext.TL_MEMBER_START) != 0) {
					if (!first) buffer.append(',');
					buffer.append("MEMBER_START"); //$NON-NLS-1$
					first = false;
				}
				if ((locationType & CompletionContext.TL_STATEMENT_START) != 0) {
					if (!first) buffer.append(',');
					buffer.append("STATEMENT_START"); //$NON-NLS-1$
					first = false;
				}
				buffer.append('}');
			}
		}
		
		if (this.computeEnclosingElement) {
			buffer.append('\n');
			buffer.append("enclosingElement="); //$NON-NLS-1$
			JavaElement enclosingElement = (JavaElement)this.context.getEnclosingElement();
			if (enclosingElement == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				buffer.append(enclosingElement.toStringWithAncestors(true /*show resolved info*/));
			}
		}
		
		if (this.computeVisibleElements) {
			buffer.append('\n');
			
			IJavaElement[] visibleElements = this.context.getVisibleElements(this.assignableType);
			buffer.append("visibleElements="); //$NON-NLS-1$
			if (visibleElements == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else if (visibleElements.length == 0) {
				buffer.append("{}"); //$NON-NLS-1$
			} else {
				buffer.append('{');
				buffer.append('\n');
				for (int i = 0; i < visibleElements.length; i++) {
					JavaElement element = (JavaElement) visibleElements[i];
					buffer.append('\t');
					buffer.append(element.toStringWithAncestors(true /*show resolved info*/));
					buffer.append(",\n"); //$NON-NLS-1$
				}
				buffer.append('}');
			}
		}
		
		//buffer.append('\n');
		
		
		return buffer.toString();
	}
	public String getProblem() {
		return this.problem == null ? "" : this.problem.getMessage();
	}

	/*
	 * Get sorted results in ascending order
	 */
	public String getResults() {
		if(this.proposalsPtr < 0) return "";
		quickSort(this.proposals, 0, this.proposalsPtr);
		return getResultsWithoutSorting();
	}

	/*
	 * Get sorted results in ascending order
	 */
	@SuppressWarnings("unchecked")
	public String getReversedResults() {
		if(this.proposalsPtr < 0) return "";
		Arrays.sort(this.proposals, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (o1 == o2) 
					return 0;
				if (o1 == null)
					return 1;
				if (o2 == null)
					return -1;
				CompletionProposal p1 = (CompletionProposal) o1;
				CompletionProposal p2 = (CompletionProposal) o2;
				int relDif = p2.getRelevance() - p1.getRelevance();
				if(relDif != 0) return relDif;
				String name1 = getElementName(p1);
				String name2 = getElementName(p2);
				return name1.compareTo(name2);
			}
		});
		return getResultsWithoutSorting();
	}
	
	/*
	 * Get unsorted results (ie. same order as they were accepted by requestor)
	 */
	public String getResultsWithoutSorting() {
		if(this.proposalsPtr < 0) return "";
		StringBuffer buffer = printProposal(this.proposals[0]);
		for(int i = 1; i <=this.proposalsPtr; i++) {
			if(i > 0) buffer.append('\n');
			buffer.append(printProposal(this.proposals[i]));
		}
		return buffer.toString();
	}
	public String[] getStringsResult() {
		if(this.proposalsPtr < 0) {
			return new String[0];
		}
		String[] strings = new String[this.proposalsPtr+1];
		for (int i=0; i<=this.proposalsPtr; i++) {
			strings[i] =  printProposal(this.proposals[i]).toString();
		}
		return strings;
	}

	protected StringBuffer printProposal(CompletionProposal proposal) {
		StringBuffer buffer = new StringBuffer();
		return printProposal(proposal, 0, buffer);
	}
	
	protected StringBuffer printProposal(CompletionProposal proposal, int tab, StringBuffer buffer) {
		for (int i = 0; i < tab; i++) {
			buffer.append("   "); //$NON-NLS-1$
		}
		buffer.append(getElementName(proposal));
		buffer.append('[');
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				buffer.append("ANONYMOUS_CLASS_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.FIELD_REF :
				buffer.append("FIELD_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER :
				buffer.append("FIELD_REF_WITH_CASTED_RECEIVER"); //$NON-NLS-1$
				break;
			case CompletionProposal.KEYWORD :
				buffer.append("KEYWORD"); //$NON-NLS-1$
				break;
			case CompletionProposal.LABEL_REF :
				buffer.append("LABEL_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.LOCAL_VARIABLE_REF :
				buffer.append("LOCAL_VARIABLE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_DECLARATION :
				buffer.append("METHOD_DECLARATION"); //$NON-NLS-1$
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$
				}
				break;
			case CompletionProposal.METHOD_REF :
				buffer.append("METHOD_REF"); //$NON-NLS-1$
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$
				}
				break;
			case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER :
				buffer.append("METHOD_REF_WITH_CASTED_RECEIVER"); //$NON-NLS-1$
				if(proposal.isConstructor()) {
					buffer.append("<CONSTRUCTOR>"); //$NON-NLS-1$
				}
				break;
			case CompletionProposal.PACKAGE_REF :
				buffer.append("PACKAGE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.TYPE_REF :
				buffer.append("TYPE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.VARIABLE_DECLARATION :
				buffer.append("VARIABLE_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION :
				buffer.append("POTENTIAL_METHOD_DECLARATION"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_NAME_REFERENCE :
//{ObjectTeams: original "METHOD_IMPORT" is now misleading (used for rhs short method spec, too)				
				buffer.append("METHOD_NAME_REF"); //$NON-NLS-1$
// SH}				
				break;
//{ObjectTeams: new kinds:				
			case CompletionProposal.OT_METHOD_SPEC :
				buffer.append("METHOD_SPEC"); //$NON-NLS-1$
				break;
			case CompletionProposal.OT_FIELD_SPEC:
				buffer.append("FIELD_SPEC"); //$NON-NLS-1$
				break;
			case CompletionProposal.OT_CALLOUT_GET:
				buffer.append("CALLOUT_GET"); //$NON-NLS-1$
				break;
			case CompletionProposal.OT_CALLOUT_SET:
				buffer.append("CALLOUT_SET"); //$NON-NLS-1$
				break;
			case CompletionProposal.OT_CALLOUT_DECLARATION:
				buffer.append("CALLOUT_DECLARATION"); //$NON-NLS-1$
				break;				
			case CompletionProposal.OT_CALLOUT_OVERRIDE_DECLARATION:
				buffer.append("CALLOUT_OVERRIDE_DECLARATION"); //$NON-NLS-1$
				break;				
			case CompletionProposal.OT_CALLIN_DECLARATION:
				buffer.append("CALLIN_DECLARATION"); //$NON-NLS-1$
				break;				
// SH}
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF :
				buffer.append("ANNOTATION_ATTRIBUTE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_BLOCK_TAG :
				buffer.append("JAVADOC_BLOCK_TAG"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_INLINE_TAG :
				buffer.append("JAVADOC_INLINE_TAG"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_FIELD_REF:
				buffer.append("JAVADOC_FIELD_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_METHOD_REF :
				buffer.append("JAVADOC_METHOD_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_TYPE_REF :
				buffer.append("JAVADOC_TYPE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_PARAM_REF :
				buffer.append("JAVADOC_PARAM_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.JAVADOC_VALUE_REF :
				buffer.append("JAVADOC_VALUE_REF"); //$NON-NLS-1$
				break;
			case CompletionProposal.FIELD_IMPORT :
				buffer.append("FIELD_IMPORT"); //$NON-NLS-1$
				break;
			case CompletionProposal.METHOD_IMPORT :
				buffer.append("METHOD_IMPORT"); //$NON-NLS-1$
				break;
			case CompletionProposal.TYPE_IMPORT :
				buffer.append("TYPE_IMPORT"); //$NON-NLS-1$
				break;
			default :
				buffer.append("PROPOSAL"); //$NON-NLS-1$
				break;
				
		}
		buffer.append("]{");
		buffer.append(proposal.getCompletion() == null ? NULL_LITERAL : proposal.getCompletion());
		buffer.append(", ");
		buffer.append(proposal.getDeclarationSignature() == null ? NULL_LITERAL : proposal.getDeclarationSignature());  
		buffer.append(", ");
		buffer.append(proposal.getSignature() == null ? NULL_LITERAL : proposal.getSignature());
		
		char[] receiverSignature = proposal.getReceiverSignature();
		if (receiverSignature != null) {
			buffer.append(", ");
			buffer.append(receiverSignature);
		}
		
		if(this.showUniqueKeys) {
			buffer.append(", ");
			buffer.append(proposal.getDeclarationKey() == null ? NULL_LITERAL : proposal.getDeclarationKey());
			buffer.append(", ");
			buffer.append(proposal.getKey() == null ? NULL_LITERAL : proposal.getKey());
		}
		buffer.append(", ");
		buffer.append(proposal.getName() == null ? NULL_LITERAL : proposal.getName());
		if(this.showParameterNames) {
			char[][] parameterNames = proposal.findParameterNames(null);
			buffer.append(", ");
			if(parameterNames == null || parameterNames.length <= 0) {
				buffer.append(NULL_LITERAL);
			} else {
				buffer.append("(");
				for (int i = 0; i < parameterNames.length; i++) {
					if(i > 0) buffer.append(", ");
					buffer.append(parameterNames[i]);
				}
				buffer.append(")");
			}
		}
		
		if(this.showPositions) {
			buffer.append(", ");
			if(this.showTokenPositions || receiverSignature != null) buffer.append("replace");
			buffer.append("[");
			buffer.append(proposal.getReplaceStart());
			buffer.append(", ");
			buffer.append(proposal.getReplaceEnd());
			buffer.append("]");
		}
		if(this.showTokenPositions) {
			buffer.append(", token[");
			buffer.append(proposal.getTokenStart());
			buffer.append(", ");
			buffer.append(proposal.getTokenEnd());
			buffer.append("]");
		}
		if(this.showPositions && receiverSignature != null) {
			buffer.append(", receiver[");
			buffer.append(proposal.getReceiverStart());
			buffer.append(", ");
			buffer.append(proposal.getReceiverEnd());
			buffer.append("]");
		}
		buffer.append(", ");
		buffer.append(proposal.getRelevance());
		buffer.append('}');
		if(this.showMissingTypes) {
			CompletionProposal[] requiredProposals = proposal.getRequiredProposals();
			if (requiredProposals != null) {
				int length = requiredProposals.length;
				System.arraycopy(requiredProposals, 0, requiredProposals = new CompletionProposal[length], 0, length);
				quickSort(requiredProposals, 0, length - 1);
				for (int i = 0; i < length; i++) {
					buffer.append('\n');
					printProposal(requiredProposals[i], tab + 1, buffer);
				}
			}
		}
		return buffer;
	}

	protected CompletionProposal[] quickSort(CompletionProposal[] collection, int left, int right) {
		int original_left = left;
		int original_right = right;
		CompletionProposal mid = collection[ (left + right) / 2];
		do {
			while (compare(mid, collection[left]) > 0)
				// s[left] >= mid
				left++;
			while (compare(mid, collection[right]) < 0)
				// s[right] <= mid
				right--;
			if (left <= right) {
				CompletionProposal tmp = collection[left];
				collection[left] = collection[right];
				collection[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right)
			collection = quickSort(collection, original_left, right);
		if (left < original_right)
			collection = quickSort(collection, left, original_right);
		return collection;
	}
	
	protected int compare(CompletionProposal proposal1, CompletionProposal proposal2) {
		int relDif = proposal1.getRelevance() - proposal2.getRelevance();
		if(relDif != 0) return relDif;
		String name1 = getElementName(proposal1);
		String name2 = getElementName(proposal2);
		int nameDif = name1.compareTo(name2);
		if(nameDif != 0) return nameDif;
		int kindDif = proposal1.getKind() - proposal2.getKind();
		if(kindDif != 0) return kindDif;
		String completion1 = new String(proposal1.getCompletion());
		String completion2 = new String(proposal2.getCompletion());
		int completionDif = completion1.compareTo(completion2);
		if(completionDif != 0) return completionDif;
		char[] temp = proposal1.getSignature();
		String signature1 = temp == null ? null: new String(temp);
		temp = proposal2.getSignature();
		String signature2 = temp == null ? null: new String(temp);
		int signatureDif = 0;
		if(signature1 != null && signature2 != null) {
			signatureDif = signature1.compareTo(signature2);
		}
		if(signatureDif != 0) return signatureDif;
		temp = proposal1.getDeclarationSignature();
		String declarationSignature1 = temp == null ? null: new String(temp);
		temp = proposal2.getDeclarationSignature();
		String declarationSignature2 = temp == null ? null: new String(temp);
		int declarationSignatureDif = 0;
		if(declarationSignature1 != null && declarationSignature2 != null) {
			declarationSignatureDif = declarationSignature1.compareTo(declarationSignature2);
		}
		if(declarationSignatureDif != 0) return declarationSignatureDif;
		return 0;
	}
	
	protected String getElementName(CompletionProposal proposal) {
		switch(proposal.getKind()) {
			case CompletionProposal.ANONYMOUS_CLASS_DECLARATION :
				return new String(Signature.getSignatureSimpleName(proposal.getDeclarationSignature()));
			case CompletionProposal.TYPE_REF :
			case CompletionProposal.TYPE_IMPORT :
			case CompletionProposal.JAVADOC_TYPE_REF :
				return new String(Signature.getSignatureSimpleName(proposal.getSignature()));
			case CompletionProposal.FIELD_REF :
			case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER :
			case CompletionProposal.KEYWORD:
			case CompletionProposal.LABEL_REF:
			case CompletionProposal.LOCAL_VARIABLE_REF:
			case CompletionProposal.METHOD_REF:
			case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
			case CompletionProposal.METHOD_DECLARATION:
			case CompletionProposal.VARIABLE_DECLARATION:
			case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
			case CompletionProposal.METHOD_NAME_REFERENCE:
//{ObjectTeams: new kinds:
			case CompletionProposal.OT_METHOD_SPEC:
			case CompletionProposal.OT_FIELD_SPEC:
			case CompletionProposal.OT_CALLOUT_GET:
			case CompletionProposal.OT_CALLOUT_SET:
			case CompletionProposal.OT_CALLOUT_DECLARATION:
			case CompletionProposal.OT_CALLOUT_OVERRIDE_DECLARATION:
			case CompletionProposal.OT_CALLIN_DECLARATION:
// SH}
			case CompletionProposal.ANNOTATION_ATTRIBUTE_REF:
			case CompletionProposal.JAVADOC_BLOCK_TAG :
			case CompletionProposal.JAVADOC_INLINE_TAG :
			case CompletionProposal.JAVADOC_FIELD_REF:
			case CompletionProposal.JAVADOC_METHOD_REF :
			case CompletionProposal.JAVADOC_PARAM_REF :
			case CompletionProposal.JAVADOC_VALUE_REF :
			case CompletionProposal.FIELD_IMPORT :
			case CompletionProposal.METHOD_IMPORT :
				return new String(proposal.getName());
			case CompletionProposal.PACKAGE_REF:
				return new String(proposal.getDeclarationSignature());
		}
		return "";
	}
	public String toString() {
		return getResults();
	}
	public void setComputeVisibleElements(boolean computeVisibleElements) {
		this.computeVisibleElements = computeVisibleElements;
	}
	public void setAssignableType(String typeSignature) {
		this.assignableType = typeSignature;
	}
	public void setComputeEnclosingElement(boolean computeEnclosingElement) {
		this.computeEnclosingElement = computeEnclosingElement;
	}
}