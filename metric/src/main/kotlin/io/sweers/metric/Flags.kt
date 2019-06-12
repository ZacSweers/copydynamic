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

 import kotlinx.metadata.Flag
import kotlinx.metadata.Flags

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
internal val KmCommon.hasAnnotations: Boolean get() = flags.hasAnnotations
internal val KmCommon.isAbstract: Boolean get() = flags.isAbstract
internal val KmCommon.isFinal: Boolean get() = flags.isFinal
internal val KmCommon.isInternal: Boolean get() = flags.isInternal
internal val KmCommon.isLocal: Boolean get() = flags.isLocal
internal val KmCommon.isOpen: Boolean get() = flags.isOpen
internal val KmCommon.isPrivate: Boolean get() = flags.isPrivate
internal val KmCommon.isPrivate_to_this: Boolean get() = flags.isPrivate_to_this
internal val KmCommon.isProtected: Boolean get() = flags.isProtected
internal val KmCommon.isPublic: Boolean get() = flags.isPublic
internal val KmCommon.isSealed: Boolean get() = flags.isSealed

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
internal val KmType.isSuspend: Boolean get() = flags.isNullableType
internal val KmType.isNullable: Boolean get() = flags.isSuspendType

 // Constructor flags.
internal val Flags.isPrimaryConstructor: Boolean get() = Flag.Constructor.IS_PRIMARY(this)
internal val KmConstructor.isPrimary: Boolean get() = flags.isPrimaryConstructor
internal val KmConstructor.isSecondary: Boolean get() = !isPrimary

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

 // Parameter flags.
internal val KmParameter.declaresDefaultValue: Boolean get() = Flag.ValueParameter.DECLARES_DEFAULT_VALUE(flags)
internal val KmParameter.isCrossInline: Boolean get() = Flag.ValueParameter.IS_CROSSINLINE(flags)
internal val KmParameter.isNoInline: Boolean get() = Flag.ValueParameter.IS_NOINLINE(flags)

 // Property flags.
internal val KmProperty.hasConstant: Boolean get() = Flag.Property.HAS_CONSTANT(flags)
internal val KmProperty.hasGetter: Boolean get() = Flag.Property.HAS_GETTER(flags)
internal val KmProperty.hasSetter: Boolean get() = Flag.Property.HAS_SETTER(flags)
internal val KmProperty.isConst: Boolean get() = Flag.Property.IS_CONST(flags)
internal val KmProperty.isDeclaration: Boolean get() = Flag.Property.IS_DECLARATION(flags)
internal val KmProperty.isDelegated: Boolean get() = Flag.Property.IS_DELEGATED(flags)
internal val KmProperty.isDelegation: Boolean get() = Flag.Property.IS_DELEGATION(flags)
internal val KmProperty.isExpect: Boolean get() = Flag.Property.IS_EXPECT(flags)
internal val KmProperty.isExternal: Boolean get() = Flag.Property.IS_EXTERNAL(flags)
internal val Flags.isOverrideProperty: Boolean get() = Flag.Property.IS_FAKE_OVERRIDE(this)
internal val KmProperty.isOverride: Boolean get() = flags.isOverrideProperty
internal val KmProperty.isLateinit: Boolean get() = Flag.Property.IS_LATEINIT(flags)
internal val KmProperty.isSynthesized: Boolean get() = Flag.Property.IS_SYNTHESIZED(flags)
internal val KmProperty.isVar: Boolean get() = Flag.Property.IS_VAR(flags)
internal val KmProperty.isVal: Boolean get() = !isVar

// Property Accessor Flags
internal val Flags.isPropertyAccessorExternal: Boolean get() = Flag.PropertyAccessor.IS_EXTERNAL(this)
internal val Flags.isPropertyAccessorInline: Boolean get() = Flag.PropertyAccessor.IS_INLINE(this)
internal val Flags.isPropertyAccessorNotDefault: Boolean get() = Flag.PropertyAccessor.IS_NOT_DEFAULT(this)

 // TypeParameter flags.
internal val Flags.isReifiedTypeParameter: Boolean get() = Flag.TypeParameter.IS_REIFIED(this)
