/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2015 Oliver Frank and others.
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
package org.eclipse.objectteams.otredyn.bytecode.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.objectteams.otredyn.bytecode.Method;
import org.eclipse.objectteams.otredyn.transformer.IWeavingContext;
import org.eclipse.objectteams.otredyn.transformer.names.ConstantMembers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;



/**
 * This class moves the code of a method to callOrig. 
 * @author Oliver Frank
 */
public class MoveCodeToCallOrigAdapter extends AbstractTransformableClassNode {
	private Method method;
	private int boundMethodId;
	private int firstArgIndex; // slot index of the first argument (0 (static) or 1 (non-static))
	private int argOffset; // used to skip synth args if the callOrig method itself is a statid role method
	private Method callOrig;
	private boolean superIsWeavable = true;
	
	public MoveCodeToCallOrigAdapter(AsmWritableBoundClass clazz, Method method, int boundMethodId, IWeavingContext weavingContext) {
		this.method = method;
		this.boundMethodId = boundMethodId;
		if (method.isStatic()) {
			firstArgIndex = 0;
			argOffset = clazz.isRole() ? 2 : 0;
			callOrig = clazz.getCallOrigStatic();
		} else {
			firstArgIndex = 1;
			callOrig = ConstantMembers.callOrig;
		}
		if (weavingContext != null)
			superIsWeavable = weavingContext.isWeavable(clazz.getSuperClassName());
	}
	
	public boolean transform() {
		MethodNode orgMethod = getMethod(method);
		if ((orgMethod.access & Opcodes.ACC_ABSTRACT) != 0) return false;
		
		MethodNode callOrig = getMethod(this.callOrig);
		
		Type returnType = Type.getReturnType(orgMethod.desc);
		
		
				
		InsnList newInstructions = new InsnList();
		
		
		//Unboxing arguments
		Type[] args = Type.getArgumentTypes(orgMethod.desc);
		
		int boundMethodIdSlot = firstArgIndex;
		
		if (args.length > 0) {
			// move boundMethodId to a higher slot, to make lower slots available for original locals
			newInstructions.add(new IntInsnNode(Opcodes.ILOAD, boundMethodIdSlot));
			boundMethodIdSlot = callOrig.maxLocals+1;
			newInstructions.add(new IntInsnNode(Opcodes.ISTORE, boundMethodIdSlot));
			
			newInstructions.add(new IntInsnNode(Opcodes.ALOAD, firstArgIndex + argOffset + 1));
			
			int slot = firstArgIndex + argOffset;
			for (int i = argOffset; i < args.length; i++) {
				if (i < args.length - 1) {
					newInstructions.add(new InsnNode(Opcodes.DUP));
				}
				newInstructions.add(createLoadIntConstant(i));
				newInstructions.add(new InsnNode(Opcodes.AALOAD));
				Type arg = args[i];
				if (arg.getSort() != Type.ARRAY && arg.getSort() != Type.OBJECT) {
					String objectType = AsmTypeHelper.getObjectType(arg);
					newInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, objectType));
					newInstructions.add(AsmTypeHelper.getUnboxingInstructionForType(arg, objectType));
				} else {
					newInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, arg.getInternalName()));
				}
				
				newInstructions.add(new IntInsnNode(args[i].getOpcode(Opcodes.ISTORE), slot));
				slot += arg.getSize();
			}
		}

		if (superIsWeavable)
			adjustSuperCalls(orgMethod.instructions, orgMethod.name, orgMethod.desc, args, returnType, boundMethodIdSlot);
		
		// replace return of the original method with areturn and box the result value if needed
		replaceReturn(orgMethod.instructions, returnType);
		
		newInstructions.add(orgMethod.instructions);
		
		addNewLabelToSwitch(callOrig.instructions, newInstructions, boundMethodId);
		
		// a minimum stacksize of 3 is needed to box the arguments
		callOrig.maxStack = Math.max(Math.max(callOrig.maxStack, orgMethod.maxStack), 3);
		
		// we have to increment the max. stack size, because we have to put NULL on the stack
		if (returnType.getSort() == Type.VOID) {
			callOrig.maxStack += 1;
		}
		callOrig.maxLocals = Math.max(callOrig.maxLocals, orgMethod.maxLocals);
		return true;
	}
	
	/** To avoid infinite recursion, calls super.m(a1, a2) must be translated to super.callOrig(boundMethodId, new Object[] {a1, a2}). */
	private void adjustSuperCalls(InsnList instructions, String selector, String descriptor, 
			Type[] args, Type returnType, final int boundMethodIdSlot) {

		// search:
		List<MethodInsnNode> toReplace = new ArrayList<MethodInsnNode>();
		ListIterator<AbstractInsnNode> orgMethodIter = instructions.iterator();
		while (orgMethodIter.hasNext()) {
			AbstractInsnNode orgMethodNode = orgMethodIter.next();
			if (orgMethodNode.getOpcode() == Opcodes.INVOKESPECIAL 
					&& ((MethodInsnNode)orgMethodNode).name.equals(selector)
					&& ((MethodInsnNode)orgMethodNode).desc.equals(descriptor))
				toReplace.add((MethodInsnNode) orgMethodNode);
		}
		if (toReplace.isEmpty())
			return;
		// replace:
		replaceSuperCallsWithCallToCallOrig(instructions, toReplace, true, new IBoundMethodIdInsnProvider() {
			@Override public AbstractInsnNode getLoadBoundMethodIdInsn(MethodInsnNode methodInsn) {
				return new IntInsnNode(Opcodes.ILOAD, boundMethodIdSlot);
			}
		});
	}
}
