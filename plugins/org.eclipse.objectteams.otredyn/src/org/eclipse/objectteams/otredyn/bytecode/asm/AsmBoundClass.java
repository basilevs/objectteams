/**********************************************************************
 * This file is part of "Object Teams Dynamic Runtime Environment"
 * 
 * Copyright 2009, 2012 Oliver Frank and others.
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

import org.eclipse.objectteams.otredyn.bytecode.AbstractBoundClass;
import org.eclipse.objectteams.otredyn.bytecode.AbstractTeam;
import org.eclipse.objectteams.otredyn.bytecode.IBytecodeProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * This class implements the bytecode parsing for {@link AbstractBoundClass}.
 * It parses the bytecode with ASM.
 * @author Oliver Frank
 */
abstract class AsmBoundClass extends AbstractTeam {
	
	public static final int ASM_API = Opcodes.ASM5;

	private boolean parsed;
	private IBytecodeProvider bytecodeProvider;
	
	/**
	 * just a temporary cache for the bytecode
	 */
	private byte[] bytecode;
	
	/**
	 * ordered list of qualified callin labels
	 */
	public String[] precedences;

	protected AsmBoundClass(String name, String id, IBytecodeProvider bytecodeProvider, ClassLoader loader) {
		super(name, id, loader);
		this.bytecodeProvider = bytecodeProvider;
	}

	/**
	 * Parses the bytecode of a class and uses the set/add... Methods (e.g. addMethod)
	 * of {@link AbstractBoundClass} to set the information
	 */
	@Override
	protected synchronized void parseBytecode() {
		if (parsed) {
			// Already parsed, nothing to do
			return;
		}
		
		bytecode = bytecodeProvider.getBytecode(getId());
		if (bytecode == null) {
			//Class is not loaded yet.
			return;
		}
		
		// Don't parse another time
		parsed = true;
		AsmClassVisitor cv = new AsmClassVisitor(this);
		ClassReader cr = null;
		cr = new ClassReader(bytecode);
		
		cr.accept(cv, Attributes.attributes, 0);
		
		//release the bytecode
		bytecode = null;
	}
	
	/**
	 * Returns the bytecode of this class and cache it temporary.
	 * This method is only needed, if getBytecode of the {@link IBytecodeProvider}
	 * is an expensive operation.
	 * @return
	 */
	protected byte[] allocateAndGetBytecode() {
		if (bytecode == null) {
			bytecode = getBytecode();
		}
		return bytecode;
	}
	
	/**
	 * Get the bytecode directly from the {@link IBytecodeProvider}.
	 * This method can be used, if getBytecode of the {@link IBytecodeProvider}
	 * is not an expensive operation.
	 * @return 
	 */
	@Override
	public byte[] getBytecode() {
		return bytecodeProvider.getBytecode(getId());
	}
	
	/**
	 * Releases the bytecode, if it's cached, an set it in the {@link IBytecodeProvider}
	 */
	protected void releaseBytecode() {
		bytecodeProvider.setBytecode(getId(), bytecode);
		bytecode = null;
	}
	
	/**
	 * Returns the {@link IBytecodeProvider} used for this class
	 * @return
	 */
	protected IBytecodeProvider getBytecodeProvider() {
		return bytecodeProvider;
	}
	
	/**
	 * Sets the bytecode. 
	 * If the bytecode is temporary cached, the cache is used.
	 * Otherwise this method give the bytecode directly to the {@link IBytecodeProvider}
	 * @param bytecode
	 */
	protected void setBytecode(byte[] bytecode) {
		//Is the bytecode temporary cached?
		if (this.bytecode == null) {
			// no, then save the bytecode directly in the bytecode provider
			bytecodeProvider.setBytecode(getId(), bytecode);
		} else {
			// yes, then use the cache
			this.bytecode = bytecode;
		}
	}
	
	public int compare(String callinLabel1, String callinLabel2) {
		boolean label1Seen = false, label2Seen = false;
		if (this.precedences != null) {
			for (String label : this.precedences) {
				if (label.equals(callinLabel1)) {
					if (label2Seen)
						return -1; // saw two then one: one has lower priority than two
					label1Seen = true;
				} else if (label.equals(callinLabel2)) {
					if (label1Seen)
						return 1; // saw one then two: one has higher priority than two
					label2Seen = true;
				}
			}
		}
		AbstractBoundClass enclosingClass = getEnclosingClass();
		if (enclosingClass != null) {
			String singleName = getInternalName();
			int pos = singleName.lastIndexOf('$');
			singleName = singleName.substring(pos+1);
			if (singleName.startsWith("__OT__"))
				singleName = singleName.substring("__OT__".length());
			// check for precedence at outer level:
			return enclosingClass.compare(singleName+'.'+callinLabel1, singleName+'.'+callinLabel2);
		}
		return callinLabel1.compareTo(callinLabel2);
	}
}
