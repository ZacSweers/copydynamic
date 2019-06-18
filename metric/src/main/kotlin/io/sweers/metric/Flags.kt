/*
 * Copyright (c) 2019 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sweers.metric

import com.squareup.kotlinpoet.KModifier
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmValueParameter

// Common flags for any element with flags.
internal val Flags.hasAnnotations: Boolean get() = Flag.HAS_ANNOTATIONS(this)
internal val Flags.isAbstract: Boolean get() = Flag.IS_ABSTRACT(this)
internal val Flags.isFinal: Boolean get() = Flag.IS_FINAL(this)
internal val Flags.isInternal: Boolean get() = Flag.IS_INTERNAL(this)
internal val Flags.isLocal: Boolean get() = Flag.IS_LOCAL(this)
internal val Flags.isOpen: Boolean get() = Flag.IS_OPEN(this)
internal val Flags.isPrivate: Boolean get() = Flag.IS_PRIVATE(this)
internal val Flags.isPrivate_to_this: Boolean get() = Flag.IS_PRIVATE_TO_THIS(this)
internal val Flags.isProtected: Boolean get() = Flag.IS_PROTECTED(this)
internal val Flags.isPublic: Boolean get() = Flag.IS_PUBLIC(this)
internal val Flags.isSealed: Boolean get() = Flag.IS_SEALED(this)
internal val KmClass.hasAnnotations: Boolean get() = flags.hasAnnotations
internal val KmClass.isAbstract: Boolean get() = flags.isAbstract
internal val KmClass.isFinal: Boolean get() = flags.isFinal
internal val KmClass.isInternal: Boolean get() = flags.isInternal
internal val KmClass.isLocal: Boolean get() = flags.isLocal
internal val KmClass.isOpen: Boolean get() = flags.isOpen
internal val KmClass.isPrivate: Boolean get() = flags.isPrivate
internal val KmClass.isPrivate_to_this: Boolean get() = flags.isPrivate_to_this
internal val KmClass.isProtected: Boolean get() = flags.isProtected
internal val KmClass.isPublic: Boolean get() = flags.isPublic
internal val KmClass.isSealed: Boolean get() = flags.isSealed
internal val ImmutableKmClass.hasAnnotations: Boolean get() = flags.hasAnnotations
internal val ImmutableKmClass.isAbstract: Boolean get() = flags.isAbstract
internal val ImmutableKmClass.isFinal: Boolean get() = flags.isFinal
internal val ImmutableKmClass.isInternal: Boolean get() = flags.isInternal
internal val ImmutableKmClass.isLocal: Boolean get() = flags.isLocal
internal val ImmutableKmClass.isOpen: Boolean get() = flags.isOpen
internal val ImmutableKmClass.isPrivate: Boolean get() = flags.isPrivate
internal val ImmutableKmClass.isPrivate_to_this: Boolean get() = flags.isPrivate_to_this
internal val ImmutableKmClass.isProtected: Boolean get() = flags.isProtected
internal val ImmutableKmClass.isPublic: Boolean get() = flags.isPublic
internal val ImmutableKmClass.isSealed: Boolean get() = flags.isSealed

// Type flags.
internal val Flags.isNullableType: Boolean get() = Flag.Type.IS_NULLABLE(this)
internal val Flags.isSuspendType: Boolean get() = Flag.Type.IS_SUSPEND(this)

// Class flags.
internal val Flags.isAnnotationClass: Boolean get() = Flag.Class.IS_ANNOTATION_CLASS(this)
internal val Flags.isClass: Boolean get() = Flag.Class.IS_CLASS(this)
internal val Flags.isCompanionObjectClass: Boolean get() = Flag.Class.IS_COMPANION_OBJECT(this)
internal val Flags.isDataClass: Boolean get() = Flag.Class.IS_DATA(this)
internal val Flags.isEnumClass: Boolean get() = Flag.Class.IS_ENUM_CLASS(this)
internal val Flags.isEnumEntryClass: Boolean get() = Flag.Class.IS_ENUM_ENTRY(this)
internal val Flags.isExpectClass: Boolean get() = Flag.Class.IS_EXPECT(this)
internal val Flags.isExternalClass: Boolean get() = Flag.Class.IS_EXTERNAL(this)
internal val Flags.isInlineClass: Boolean get() = Flag.Class.IS_INLINE(this)
internal val Flags.isInnerClass: Boolean get() = Flag.Class.IS_INNER(this)
internal val Flags.isObjectClass: Boolean get() = Flag.Class.IS_OBJECT(this)
internal val Flags.isInterface: Boolean get() = Flag.Class.IS_INTERFACE(this)
internal val KmClass.isAnnotation: Boolean get() = flags.isAnnotationClass
internal val KmClass.isClass: Boolean get() = flags.isClass
internal val KmClass.isCompanionObject: Boolean get() = flags.isCompanionObjectClass
internal val KmClass.isData: Boolean get() = flags.isDataClass
internal val KmClass.isEnum: Boolean get() = flags.isEnumClass
internal val KmClass.isEnumEntry: Boolean get() = flags.isEnumEntryClass
internal val KmClass.isExpect: Boolean get() = flags.isExpectClass
internal val KmClass.isExternal: Boolean get() = flags.isExternalClass
internal val KmClass.isInline: Boolean get() = flags.isInlineClass
internal val KmClass.isInner: Boolean get() = flags.isInnerClass
internal val KmClass.isObject: Boolean get() = flags.isObjectClass
internal val KmClass.isInterface: Boolean get() = flags.isInterface
internal val KmType.isSuspend: Boolean get() = flags.isSuspendType
internal val KmType.isNullable: Boolean get() = flags.isNullableType
internal val ImmutableKmClass.isAnnotation: Boolean get() = flags.isAnnotationClass
internal val ImmutableKmClass.isClass: Boolean get() = flags.isClass
internal val ImmutableKmClass.isCompanionObject: Boolean get() = flags.isCompanionObjectClass
internal val ImmutableKmClass.isData: Boolean get() = flags.isDataClass
internal val ImmutableKmClass.isEnum: Boolean get() = flags.isEnumClass
internal val ImmutableKmClass.isEnumEntry: Boolean get() = flags.isEnumEntryClass
internal val ImmutableKmClass.isExpect: Boolean get() = flags.isExpectClass
internal val ImmutableKmClass.isExternal: Boolean get() = flags.isExternalClass
internal val ImmutableKmClass.isInline: Boolean get() = flags.isInlineClass
internal val ImmutableKmClass.isInner: Boolean get() = flags.isInnerClass
internal val ImmutableKmClass.isObject: Boolean get() = flags.isObjectClass
internal val ImmutableKmClass.isInterface: Boolean get() = flags.isInterface
internal val ImmutableKmType.isSuspend: Boolean get() = flags.isSuspendType
internal val ImmutableKmType.isNullable: Boolean get() = flags.isNullableType

// Constructor flags.
internal val Flags.isPrimaryConstructor: Boolean get() = Flag.Constructor.IS_PRIMARY(this)
internal val KmConstructor.isPrimary: Boolean get() = flags.isPrimaryConstructor
internal val KmConstructor.isSecondary: Boolean get() = !isPrimary
internal val ImmutableKmConstructor.isPrimary: Boolean get() = flags.isPrimaryConstructor
internal val ImmutableKmConstructor.isSecondary: Boolean get() = !isPrimary

// Function flags.
internal val Flags.isDeclarationFunction: Boolean get() = Flag.Function.IS_DECLARATION(this)
internal val Flags.isFakeOverrideFunction: Boolean get() = Flag.Function.IS_FAKE_OVERRIDE(this)
internal val Flags.isDelegationFunction: Boolean get() = Flag.Function.IS_DELEGATION(this)
internal val Flags.isSynthesizedFunction: Boolean get() = Flag.Function.IS_SYNTHESIZED(this)
internal val Flags.isOperatorFunction: Boolean get() = Flag.Function.IS_OPERATOR(this)
internal val Flags.isInfixFunction: Boolean get() = Flag.Function.IS_INFIX(this)
internal val Flags.isInlineFunction: Boolean get() = Flag.Function.IS_INLINE(this)
internal val Flags.isTailRecFunction: Boolean get() = Flag.Function.IS_TAILREC(this)
internal val Flags.isExternalFunction: Boolean get() = Flag.Function.IS_EXTERNAL(this)
internal val Flags.isSuspendFunction: Boolean get() = Flag.Function.IS_SUSPEND(this)
internal val Flags.isExpectFunction: Boolean get() = Flag.Function.IS_EXPECT(this)
internal val KmFunction.isDeclaration: Boolean get() = flags.isDeclarationFunction
internal val KmFunction.isFakeOverride: Boolean get() = flags.isFakeOverrideFunction
internal val KmFunction.isDelegation: Boolean get() = flags.isDelegationFunction
internal val KmFunction.isSynthesized: Boolean get() = flags.isSynthesizedFunction
internal val KmFunction.isOperator: Boolean get() = flags.isOperatorFunction
internal val KmFunction.isInfix: Boolean get() = flags.isInfixFunction
internal val KmFunction.isInline: Boolean get() = flags.isInlineFunction
internal val KmFunction.isTailRec: Boolean get() = flags.isTailRecFunction
internal val KmFunction.isExternal: Boolean get() = flags.isExternalFunction
internal val KmFunction.isSuspend: Boolean get() = flags.isSuspendFunction
internal val KmFunction.isExpect: Boolean get() = flags.isExpectFunction
internal val ImmutableKmFunction.isDeclaration: Boolean get() = flags.isDeclarationFunction
internal val ImmutableKmFunction.isFakeOverride: Boolean get() = flags.isFakeOverrideFunction
internal val ImmutableKmFunction.isDelegation: Boolean get() = flags.isDelegationFunction
internal val ImmutableKmFunction.isSynthesized: Boolean get() = flags.isSynthesizedFunction
internal val ImmutableKmFunction.isOperator: Boolean get() = flags.isOperatorFunction
internal val ImmutableKmFunction.isInfix: Boolean get() = flags.isInfixFunction
internal val ImmutableKmFunction.isInline: Boolean get() = flags.isInlineFunction
internal val ImmutableKmFunction.isTailRec: Boolean get() = flags.isTailRecFunction
internal val ImmutableKmFunction.isExternal: Boolean get() = flags.isExternalFunction
internal val ImmutableKmFunction.isSuspend: Boolean get() = flags.isSuspendFunction
internal val ImmutableKmFunction.isExpect: Boolean get() = flags.isExpectFunction

// Parameter flags.
internal val KmValueParameter.declaresDefaultValue: Boolean get() = Flag.ValueParameter.DECLARES_DEFAULT_VALUE(flags)
internal val KmValueParameter.isCrossInline: Boolean get() = Flag.ValueParameter.IS_CROSSINLINE(flags)
internal val KmValueParameter.isNoInline: Boolean get() = Flag.ValueParameter.IS_NOINLINE(flags)
internal val ImmutableKmValueParameter.declaresDefaultValue: Boolean get() = Flag.ValueParameter.DECLARES_DEFAULT_VALUE(flags)
internal val ImmutableKmValueParameter.isCrossInline: Boolean get() = Flag.ValueParameter.IS_CROSSINLINE(flags)
internal val ImmutableKmValueParameter.isNoInline: Boolean get() = Flag.ValueParameter.IS_NOINLINE(flags)

// Property flags.
internal val Flags.isOverrideProperty: Boolean get() = Flag.Property.IS_FAKE_OVERRIDE(this)
internal val KmProperty.hasConstant: Boolean get() = Flag.Property.HAS_CONSTANT(flags)
internal val KmProperty.hasGetter: Boolean get() = Flag.Property.HAS_GETTER(flags)
internal val KmProperty.hasSetter: Boolean get() = Flag.Property.HAS_SETTER(flags)
internal val KmProperty.isConst: Boolean get() = Flag.Property.IS_CONST(flags)
internal val KmProperty.isDeclaration: Boolean get() = Flag.Property.IS_DECLARATION(flags)
internal val KmProperty.isDelegated: Boolean get() = Flag.Property.IS_DELEGATED(flags)
internal val KmProperty.isDelegation: Boolean get() = Flag.Property.IS_DELEGATION(flags)
internal val KmProperty.isExpect: Boolean get() = Flag.Property.IS_EXPECT(flags)
internal val KmProperty.isExternal: Boolean get() = Flag.Property.IS_EXTERNAL(flags)
internal val KmProperty.isOverride: Boolean get() = flags.isOverrideProperty
internal val KmProperty.isLateinit: Boolean get() = Flag.Property.IS_LATEINIT(flags)
internal val KmProperty.isSynthesized: Boolean get() = Flag.Property.IS_SYNTHESIZED(flags)
internal val KmProperty.isVar: Boolean get() = Flag.Property.IS_VAR(flags)
internal val KmProperty.isVal: Boolean get() = !isVar
internal val ImmutableKmProperty.hasConstant: Boolean get() = Flag.Property.HAS_CONSTANT(flags)
internal val ImmutableKmProperty.hasGetter: Boolean get() = Flag.Property.HAS_GETTER(flags)
internal val ImmutableKmProperty.hasSetter: Boolean get() = Flag.Property.HAS_SETTER(flags)
internal val ImmutableKmProperty.isConst: Boolean get() = Flag.Property.IS_CONST(flags)
internal val ImmutableKmProperty.isDeclaration: Boolean get() = Flag.Property.IS_DECLARATION(flags)
internal val ImmutableKmProperty.isDelegated: Boolean get() = Flag.Property.IS_DELEGATED(flags)
internal val ImmutableKmProperty.isDelegation: Boolean get() = Flag.Property.IS_DELEGATION(flags)
internal val ImmutableKmProperty.isExpect: Boolean get() = Flag.Property.IS_EXPECT(flags)
internal val ImmutableKmProperty.isExternal: Boolean get() = Flag.Property.IS_EXTERNAL(flags)
internal val ImmutableKmProperty.isOverride: Boolean get() = flags.isOverrideProperty
internal val ImmutableKmProperty.isLateinit: Boolean get() = Flag.Property.IS_LATEINIT(flags)
internal val ImmutableKmProperty.isSynthesized: Boolean get() = Flag.Property.IS_SYNTHESIZED(flags)
internal val ImmutableKmProperty.isVar: Boolean get() = Flag.Property.IS_VAR(flags)
internal val ImmutableKmProperty.isVal: Boolean get() = !isVar

// Property Accessor Flags
internal val Flags.isPropertyAccessorExternal: Boolean
  get() = Flag.PropertyAccessor.IS_EXTERNAL(this)
internal val Flags.isPropertyAccessorInline: Boolean get() = Flag.PropertyAccessor.IS_INLINE(this)
internal val Flags.isPropertyAccessorNotDefault: Boolean
  get() = Flag.PropertyAccessor.IS_NOT_DEFAULT(this)

// TypeParameter flags.
internal val KmTypeParameter.isReified: Boolean get() = Flag.TypeParameter.IS_REIFIED(flags)
internal val ImmutableKmTypeParameter.isReified: Boolean get() = Flag.TypeParameter.IS_REIFIED(flags)

internal val Flags.propertyAccessorFlags: Set<KModifier>
  get() = setOf {
    if (isPropertyAccessorExternal) {
      add(KModifier.EXTERNAL)
    }
    if (isPropertyAccessorInline) {
      add(KModifier.INLINE)
    }
    if (isPropertyAccessorNotDefault) {
//                add(KModifier.wat) // TODO
    }
  }

//* * visibility flags: [IS_INTERNAL], [IS_PRIVATE], [IS_PROTECTED], [IS_PUBLIC],
//
// [IS_PRIVATE_TO_THIS], [IS_LOCAL] not supported
val Flags.visibility: KModifier
  get() = when {
    isInternal -> KModifier.INTERNAL
    isPrivate -> KModifier.PRIVATE
    isProtected -> KModifier.PROTECTED
    else -> KModifier.PUBLIC
  }

//* * modality flags: [IS_FINAL], [IS_OPEN], [IS_ABSTRACT], [IS_SEALED]
val Flags.modalities: Set<KModifier>
  get() = setOf {
    if (isFinal) {
      add(KModifier.FINAL)
    }
    if (isOpen) {
      add(KModifier.OPEN)
    }
    if (isAbstract) {
      add(KModifier.ABSTRACT)
    }
    if (isSealed) {
      add(KModifier.SEALED)
    }
  }


internal inline fun <E> setOf(body: MutableSet<E>.() -> Unit): Set<E> {
  return mutableSetOf<E>().apply(body).toSet()
}
