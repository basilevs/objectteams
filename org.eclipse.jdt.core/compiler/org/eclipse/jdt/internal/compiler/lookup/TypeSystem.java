/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.DependentTypeBinding;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.ITeamAnchor;
import org.eclipse.objectteams.otdt.internal.core.compiler.lookup.RoleTypeBinding;

/* TypeSystem: An abstraction responsible for keeping track of types that undergo "derivation" of some sort and the derived types produced thus.
   Here we use the term derivation in the Pascal sense and not per object oriented parlance.
   
   As of Java SE8, a type can undergo derivation in a bunch of ways:
   
       - By being created arrays out of,
       - By being parameterized,
       - By being created raw forms of,
       - By being the generic type which a wildcard type or an intersection type parameterizes,
       - By being annotated.
       
   It is the responsibility of the TypeSystem to serve as the factory and ensure that unique types are created and maintained. Most of the
   compiler depends on object identity given the derivation parameters are the same. E.g: If we dole out non-unique ParameterizedTypeBinding's
   for two attempts to create List<String>, then one cannot be assigned to the other.
   
   Till Java SE7, we could manage to create a single binding for a type - not so with annotations coming into the picture. In order for
   two uses of the same type to be annotated differently, the bindings for them need to be distinct and cannot be shared. If we start
   doling out different bindings, then validating type identity and equivalence becomes an issue.
   
   What we do to solve the problem is produce different bindings when they need to be annotated differently, but stamp them with the
   same id (TypeBinding#id). Thus types that fail == or != could quickly be ascertained to be mere annotation variants by comparing
   the id field.
       
   This class is responsible for id stamping unique types. Only those types that are "derived from" in some form or participate in the 
   derivation in some form (by being type arguments say) get tracked and id'd here. A type which is not thus derived from in one form or 
   the other or participate in the derivation thus - we are completely oblivious to.
   
   TypeBinding.id computation: For primitive types and certain "well known" types, id assignment happens elsewhere. Here we start with an 
   id value that is suitably high and proceed monotonically upwards so we will not accidentally collide with the id space in use already. 
   id assignments happens in such a way that a naked type and its annotated variants - variously annotated - would all share the same id. 
   Example: @T1 Map<@T2 String, @T3 Object> and Map<@T4 String, @T5 Object> and @T6 Map<String, Object> and @T7 Map<String, @T8 Object> and 
   Map<String, @T9 Object> would all share the same id since the unadorned naked type in each case is the same: Map<String, Object>. None 
   of this would share the id with Map<String, String>. Briefly put, if you take a certain annotated type and strip it of all annotations 
   to come up with the naked type, that naked type and the annotated type would have the same id. Alternately, if you take a certain naked 
   type and arrive at the universe of all differently annotated types, they would all share the same id while their bindings could be different - 
   would be different unless they are identically annotated.
   
   Thus subsystems that are annotation agnostic could quickly ascertain binding equality by comparing the id field.
*/
public class TypeSystem {
	
	private int typeid = TypeIds.T_LastWellKnownTypeId;
	private TypeBinding [][] types; 
	private SimpleLookupTable annotationTypes; // cannot store in types, since AnnotationBinding is not a TypeBinding and we don't want types to operate at Binding level.
	private LookupEnvironment environment;
	
	public TypeSystem(LookupEnvironment environment) {
		this.environment = environment;
		this.annotationTypes = new SimpleLookupTable(16);
		this.typeid = TypeIds.T_LastWellKnownTypeId;
		this.types = new TypeBinding[TypeIds.T_LastWellKnownTypeId * 2][]; 
	}

	// Given a type, answer its unannotated aka naked prototype. This is also a convenient way to "register" a type with TypeSystem and have it id stamped.
	public final TypeBinding getUnannotatedType(TypeBinding type) {
		if (type.isUnresolvedType() && CharOperation.indexOf('$', type.sourceName()) > 0)
			type = BinaryTypeBinding.resolveType(type, this.environment, true); // to ensure unique id assignment (when enclosing type is parameterized, inner type is also) 
		if (type.id == TypeIds.NoId) {
			if (type.hasTypeAnnotations())
				throw new IllegalStateException();
			int typesLength = this.types.length;
			if (this.typeid == typesLength)
				System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
			this.types[type.id = this.typeid++] = new TypeBinding[4];
		} else {
			TypeBinding nakedType = this.types[type.id] == null ? null : this.types[type.id][0];
			if (type.hasTypeAnnotations() && nakedType == null)
				throw new IllegalStateException();
			if (nakedType != null)
				return nakedType;
			this.types[type.id] = new TypeBinding[4];  // well known type, assigned id elsewhere.
		}
	
		return this.types[type.id][0] = type;
	}
	
	// Given a type, return all its variously annotated versions.
	public TypeBinding[] getAnnotatedTypes(TypeBinding type) {
		return Binding.NO_TYPES;
	}

	/* Note: parameters will not have type type annotations if lookup environment directly uses TypeSystem as its typeSystem. When ATS is used however
	   they may be annotated and we need to materialize the unannotated versions and work on them.
	*/ 
	public ArrayBinding getArrayType(TypeBinding leafType, int dimensions) {
		TypeBinding unannotatedLeafType = getUnannotatedType(leafType);
		TypeBinding[] derivedTypes = this.types[unannotatedLeafType.id];
		int i, length = derivedTypes.length;
		for (i = 0; i < length; i++) {
			TypeBinding derivedType = derivedTypes[i];
			if (derivedType == null) 
				break;
			if (!derivedType.isArrayType() || derivedType.hasTypeAnnotations())
				continue;
			if (derivedType.leafComponentType() == unannotatedLeafType && derivedType.dimensions() == dimensions) //$IDENTITY-COMPARISON$
				return (ArrayBinding) derivedType;
		}
		if (i == length) {
			System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedLeafType.id] = derivedTypes;
		}
		TypeBinding arrayType = derivedTypes[i] = new ArrayBinding(unannotatedLeafType, dimensions, this.environment);
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (ArrayBinding) (this.types[arrayType.id = this.typeid++][0] = arrayType);
	}
	
	public ArrayBinding getArrayType(TypeBinding leafComponentType, int dimensions, AnnotationBinding[] annotations) {
		return getArrayType(leafComponentType, dimensions);
	}

	public ReferenceBinding getMemberType(ReferenceBinding memberType, ReferenceBinding enclosingType) {
		return memberType;  // nothing to do for plain vanilla type system, they are already hooked.
	}

	/* Note: parameters will not have type type annotations if lookup environment directly uses TypeSystem. When AnnotatableTypeSystem is in use
	   they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType) {
//{ObjectTeams: more arguments for role types:
		return getParameterizedType(genericType, typeArguments, null, -1, enclosingType);
	}
	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments,
										ITeamAnchor teamAnchor, int valueParamPosition, ReferenceBinding enclosingType) {
		if (teamAnchor == null && genericType instanceof DependentTypeBinding)
			teamAnchor = ((DependentTypeBinding) genericType)._teamAnchor;
// SH}

		ReferenceBinding unannotatedGenericType = (ReferenceBinding) getUnannotatedType(genericType);
		int typeArgumentsLength = typeArguments == null ? 0: typeArguments.length;
		TypeBinding [] unannotatedTypeArguments = typeArguments == null ? null : new TypeBinding[typeArgumentsLength];
		for (int i = 0; i < typeArgumentsLength; i++) {
			unannotatedTypeArguments[i] = getUnannotatedType(typeArguments[i]);
		}
		ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding) getUnannotatedType(enclosingType);
		
		TypeBinding[] derivedTypes = this.types[unannotatedGenericType.id];
		int i, length = derivedTypes.length;
		for (i = 0 ; i < length; i++) {
			TypeBinding derivedType = derivedTypes[i];
			if (derivedType == null) 
				break;
//{ObjectTeams: parameterized and/or anchored?
/* orig:
			if (!derivedType.isParameterizedType() || derivedType.actualType() != unannotatedGenericType || derivedType.hasTypeAnnotations()) //$IDENTITY-COMPARISON$
				continue;
  :giro */
			if (!(derivedType instanceof ParameterizedTypeBinding)) // roles might answer 'false' to isParameterized(), still they are valid candidates, here
				continue;
			if (derivedType.actualType() != unannotatedGenericType) //$IDENTITY-COMPARISON$
				continue;
			if (derivedType.isRawType() && unannotatedTypeArguments != null)
				continue;
			// also match team anchor if given:
			if (!isRoleTypeMatch(teamAnchor, valueParamPosition, derivedType))
				continue;
//SH}

			if (derivedType.enclosingType() == unannotatedEnclosingType && Util.effectivelyEqual(derivedType.typeArguments(), unannotatedTypeArguments)) //$IDENTITY-COMPARISON$
				return (ParameterizedTypeBinding) derivedType;
		}

		if (i == length) {
			System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedGenericType.id] = derivedTypes;
		}
//{ObjectTeams: dependent type?
/* orig:	
		TypeBinding parameterizedType = derivedTypes[i] = new ParameterizedTypeBinding(unannotatedGenericType, unannotatedTypeArguments, unannotatedEnclosingType, this.environment);
  :giro */
		ParameterizedTypeBinding parameterizedType;
		if (teamAnchor == null) {
			parameterizedType = new ParameterizedTypeBinding(unannotatedGenericType,unannotatedTypeArguments, unannotatedEnclosingType, this.environment);
		} else {
			if (genericType.isRole()) {
				parameterizedType = new RoleTypeBinding(unannotatedGenericType, unannotatedTypeArguments, teamAnchor, unannotatedEnclosingType, this.environment);
			} else {
				parameterizedType = new DependentTypeBinding(unannotatedGenericType, unannotatedTypeArguments, teamAnchor, valueParamPosition, unannotatedEnclosingType, this.environment);
			}
		}
		derivedTypes[i] = parameterizedType;
// SH}

	
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (ParameterizedTypeBinding) (this.types[parameterizedType.id = this.typeid++][0] = parameterizedType);
	}
	
//{ObjectTeams: more parameters:
/* orig:
	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
		return getParameterizedType(genericType, typeArguments, enclosingType);
  :giro */
	public ParameterizedTypeBinding getParameterizedType(ReferenceBinding genericType, TypeBinding[] typeArguments,
					ITeamAnchor teamAnchor, int valueParamPosition, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
		return getParameterizedType(genericType, typeArguments, teamAnchor, valueParamPosition, enclosingType);
// SH}
	}

	/* Note: Parameters will not have type type annotations if lookup environment directly uses TypeSystem. However when AnnotatableTypeSystem is in use,
	   they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType) {
		ReferenceBinding unannotatedGenericType = (ReferenceBinding) getUnannotatedType(genericType);
		ReferenceBinding unannotatedEnclosingType = enclosingType == null ? null : (ReferenceBinding) getUnannotatedType(enclosingType);
	
		TypeBinding[] derivedTypes = this.types[unannotatedGenericType.id];
		int i, length = derivedTypes.length;
		for (i = 0; i < length; i++) {
			TypeBinding derivedType = derivedTypes[i];
			if (derivedType == null) 
				break;
			if (!derivedType.isRawType() || derivedType.actualType() != unannotatedGenericType || derivedType.hasTypeAnnotations()) //$IDENTITY-COMPARISON$
				continue;
			if (derivedType.enclosingType() == unannotatedEnclosingType) //$IDENTITY-COMPARISON$
				return (RawTypeBinding) derivedType;
		}

		if (i == length) {
			System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedGenericType.id] = derivedTypes;
		}
		
		TypeBinding rawTytpe = derivedTypes[i] = new RawTypeBinding(unannotatedGenericType, unannotatedEnclosingType, this.environment);
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (RawTypeBinding) (this.types[rawTytpe.id = this.typeid++][0] = rawTytpe);
	}
	
	public RawTypeBinding getRawType(ReferenceBinding genericType, ReferenceBinding enclosingType, AnnotationBinding[] annotations) {
		return getRawType(genericType, enclosingType);
	}

	/* Parameters will not have type type annotations if lookup environment directly uses TypeSystem. When AnnotatableTypeSystem is in use,
	   they may and we need to materialize the unannotated versions and work on them.
	*/ 
	public WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind) {
		if (genericType == null) // pseudo wildcard denoting composite bounds for lub computation
			genericType = ReferenceBinding.LUB_GENERIC;
		
		ReferenceBinding unannotatedGenericType = (ReferenceBinding) getUnannotatedType(genericType);
		int otherBoundsLength = otherBounds == null ? 0: otherBounds.length;
		TypeBinding [] unannotatedOtherBounds = otherBounds == null ? null : new TypeBinding[otherBoundsLength];
		for (int i = 0; i < otherBoundsLength; i++) {
			unannotatedOtherBounds[i] = getUnannotatedType(otherBounds[i]);
		}
		TypeBinding unannotatedBound = bound == null ? null : getUnannotatedType(bound);

		TypeBinding[] derivedTypes = this.types[unannotatedGenericType.id];  // by construction, cachedInfo != null now.
		int i, length = derivedTypes.length;
		for (i = 0; i < length; i++) {
			TypeBinding derivedType = derivedTypes[i];
			if (derivedType == null) 
				break;
			if (!derivedType.isWildcard() || derivedType.actualType() != unannotatedGenericType || derivedType.hasTypeAnnotations()) //$IDENTITY-COMPARISON$
				continue;
			if (derivedType.rank() != rank || derivedType.boundKind() != boundKind || derivedType.bound() != unannotatedBound) //$IDENTITY-COMPARISON$
				continue;
			if (Util.effectivelyEqual(derivedType.additionalBounds(), unannotatedOtherBounds))
				return (WildcardBinding) derivedType;
		}
		
		if (i == length) {
			System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
			this.types[unannotatedGenericType.id] = derivedTypes;
		}
		TypeBinding wildcard = derivedTypes[i] = new WildcardBinding(unannotatedGenericType, rank, unannotatedBound, unannotatedOtherBounds, boundKind, this.environment);
	
		int typesLength = this.types.length;
		if (this.typeid == typesLength)
			System.arraycopy(this.types, 0, this.types = new TypeBinding[typesLength * 2][], 0, typesLength);
		this.types[this.typeid] = new TypeBinding[1];
		return (WildcardBinding) (this.types[wildcard.id = this.typeid++][0] = wildcard);
	}
	
	public WildcardBinding getWildcard(ReferenceBinding genericType, int rank, TypeBinding bound, TypeBinding[] otherBounds, int boundKind, AnnotationBinding[] annotations) {
		return getWildcard(genericType, rank, bound, otherBounds, boundKind);
	}

	public TypeBinding getAnnotatedType(TypeBinding type, AnnotationBinding[][] annotations) {
		return type; // Nothing to do for plain vanilla type system.
	}
	
	protected final TypeBinding /* @NonNull */ [] getDerivedTypes(TypeBinding keyType) {
		keyType = getUnannotatedType(keyType);
		return this.types[keyType.id];
	}
	
	private TypeBinding cacheDerivedType(TypeBinding keyType, TypeBinding derivedType) {
		if (keyType == null || derivedType == null || keyType.id == TypeIds.NoId)
			throw new IllegalStateException();
		
		TypeBinding[] derivedTypes = this.types[keyType.id];
		int i = 0, length = derivedTypes.length;
		while (i < length && derivedTypes[i] != null) {
			i++;
		}
		if (i == length) {
			System.arraycopy(derivedTypes, 0, derivedTypes = new TypeBinding[length * 2], 0, length);
			this.types[keyType.id] = derivedTypes;
		}
		return derivedTypes[i] = derivedType;
	}
	
	protected final TypeBinding cacheDerivedType(TypeBinding keyType, TypeBinding nakedType, TypeBinding derivedType) {
		
		/* Cache the derived type, tagging it as a derivative of both the key type and the naked type.
		   E.g: int @NonNull [] would be tagged as a derived type of both int and int []. This is not
		   needed for correctness, but for annotated object reuse. We provide two alternate ways to
		   annotate a type: 
		   
		   Taking parameterized types as an example, a call to getParamaterizedType can be made with annotations
		   to create @NonNull List<@NonNull String> in one stroke. Or a parameterized type can be created first
		   and then annotated via getAnnotatedType. In the former case, the tables get looked up with List as
		   the key, in the latter with List<String> as the key.
		   
		   Binary vs source, substitutions, annotation re-attribution from SE7 locations etc trigger these
		   alternate code paths. Unless care is exercised, we will end up with duplicate objects (that share
		   the same TypeBinding.id => correctness is not an issue, but memory wastage is)
		*/
		cacheDerivedType(keyType, derivedType);
		if (nakedType.id != keyType.id) {
			cacheDerivedType(nakedType, derivedType);
		}
		return derivedType;
	}
	
	/* Return a unique annotation binding for an annotation with either no or all default element-value pairs.
	   We may return a resolved annotation when requested for unresolved one, but not vice versa. 
	*/
	public final AnnotationBinding getAnnotationType(ReferenceBinding annotationType, boolean requiredResolved) {
		AnnotationBinding annotation = (AnnotationBinding) this.annotationTypes.get(annotationType);
		if (annotation == null) {
			if (requiredResolved)
				annotation = new AnnotationBinding(annotationType, Binding.NO_ELEMENT_VALUE_PAIRS);
			else 
				annotation = new UnresolvedAnnotationBinding(annotationType, Binding.NO_ELEMENT_VALUE_PAIRS, this.environment);
			this.annotationTypes.put(annotationType, annotation);
		}
		if (requiredResolved)
			annotation.resolve();
		return annotation;
	}

	public boolean isAnnotatedTypeSystem() {
		return false;
	}

	public void reset() {
		this.annotationTypes = new SimpleLookupTable(16);
		this.typeid = TypeIds.T_LastWellKnownTypeId;
		this.types = new TypeBinding[TypeIds.T_LastWellKnownTypeId * 2][];
	}
	
	public void updateCaches(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
		final int unresolvedTypeId = unresolvedType.id;
		if (unresolvedTypeId != TypeIds.NoId) {
			if (this.types[unresolvedTypeId] != null && this.types[unresolvedTypeId][0] == unresolvedType) { //$IDENTITY-COMPARISON$
				resolvedType.id = unresolvedTypeId;
				this.types[unresolvedTypeId][0] = resolvedType;
			}
		}
		if (this.annotationTypes.get(unresolvedType) != null) { // update the key
			Object[] keys = this.annotationTypes.keyTable;
			for (int i = 0, l = keys.length; i < l; i++) {
				if (keys[i] == unresolvedType) {
					keys[i] = resolvedType; // hashCode is based on compoundName so this works.
					break;
				}
			}
		}
	}

	public final TypeBinding getIntersectionCastType(ReferenceBinding[] intersectingTypes) {
		int intersectingTypesLength = intersectingTypes == null ? 0 : intersectingTypes.length;
		if (intersectingTypesLength == 0)
			return null;
		TypeBinding keyType = intersectingTypes[0];
		if (keyType == null || intersectingTypesLength == 1)
			return keyType;
					
		TypeBinding[] derivedTypes = getDerivedTypes(keyType);
		int i, length = derivedTypes.length;
		next:
		for (i = 0; i < length; i++) {
			TypeBinding derivedType = derivedTypes[i];
			if (derivedType == null) 
				break;
			if (!derivedType.isIntersectionCastType())
				continue;
			ReferenceBinding [] priorIntersectingTypes = derivedType.getIntersectingTypes();
			if (priorIntersectingTypes.length != intersectingTypesLength)
				continue;
			for (int j = 0; j < intersectingTypesLength; j++) {
				if (intersectingTypes[j] != priorIntersectingTypes[j]) //$IDENTITY-COMPARISON$
					continue next;
			}	
			return derivedType;
		}
		return cacheDerivedType(keyType, new IntersectionCastTypeBinding(intersectingTypes, this.environment));
	}

//{ObjectTeams: compare role types:
	boolean isRoleTypeMatch(ITeamAnchor teamAnchor, int valueParamPosition, TypeBinding cachedType) {
		if (teamAnchor != null) {
			if (!(cachedType instanceof DependentTypeBinding))
				return false;
			if (!((DependentTypeBinding)cachedType)._teamAnchor.hasSameBestNameAs(teamAnchor))
				return false;
		}
		if (   valueParamPosition > -1 							// position specified, requires dependent type
		    && !(cachedType instanceof DependentTypeBinding))
			return false;
		if (   (cachedType instanceof DependentTypeBinding)	// dependent type specified, positions must match
		    && ((DependentTypeBinding)cachedType)._valueParamPosition != valueParamPosition)
			return false;
		if (teamAnchor == null && cachedType.isRoleType()) 		// role type found though not requested?
			return false;
		return true;
	}
//SH}

}