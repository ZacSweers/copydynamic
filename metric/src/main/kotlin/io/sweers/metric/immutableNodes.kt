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

@file:Suppress("MemberVisibilityCanBePrivate", "unused")
@file:JvmName("ImmutableKmTypes")

package io.sweers.metric

import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmConstantValue
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmContract
import kotlinx.metadata.KmEffect
import kotlinx.metadata.KmEffectExpression
import kotlinx.metadata.KmEffectInvocationKind
import kotlinx.metadata.KmEffectType
import kotlinx.metadata.KmFlexibleTypeUpperBound
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmLambda
import kotlinx.metadata.KmPackage
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeAlias
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmTypeProjection
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.KmVariance
import kotlinx.metadata.KmVersion
import kotlinx.metadata.KmVersionRequirement
import kotlinx.metadata.KmVersionRequirementLevel
import kotlinx.metadata.KmVersionRequirementVersionKind
import kotlinx.metadata.KmVersionRequirementVisitor
import java.util.Collections

/** @return an immutable instance of this [KmClass]. */
@JvmName("immutableOf")
fun KmClass.immutable(): ImmutableKmClass {
  return ImmutableKmClass(
      flags,
      name,
      typeParameters.unmodifiable(),
      supertypes.unmodifiable(),
      functions.unmodifiable(),
      properties.unmodifiable(),
      typeAliases.unmodifiable(),
      constructors.unmodifiable(),
      companionObject,
      nestedClasses.unmodifiable(),
      enumEntries.unmodifiable(),
      sealedSubclasses.unmodifiable(),
      versionRequirements
  )
}

/**
 * Immutable representation of [KmClass].
 *
 * Represents a Kotlin class.
 *
 * @property flags Class flags, consisting of [Flag.HAS_ANNOTATIONS], visibility flag, modality flag and [Flag.Class] flags.
 * @property name Name of the class.
 * @property typeParameters Type parameters of the class.
 * @property supertypes Supertypes of the class. The first element is the superclass (or [Any])
 * @property functions Functions in the class.
 * @property properties Properties in the class.
 * @property typeAliases Type aliases in the class.
 * @property constructors Constructors of the class.
 * @property companionObject Name of the companion object of this class, if it has one.
 * @property nestedClasses Names of nested classes of this class.
 * @property enumEntries Names of enum entries, if this class is an enum class.
 * @property sealedSubclasses Names of direct subclasses of this class, if this class is `sealed`.
 * @property versionRequirements Version requirements on this class.
 */
data class ImmutableKmClass internal constructor(
    val flags: Flags,
    val name: ClassName,
    val typeParameters: List<KmTypeParameter>,
    val supertypes: List<KmType>,
    val functions: List<KmFunction>,
    val properties: List<KmProperty>,
    val typeAliases: List<KmTypeAlias>,
    val constructors: List<KmConstructor>,
    val companionObject: String?,
    val nestedClasses: List<String>,
    val enumEntries: List<String>,
    val sealedSubclasses: List<ClassName>,
    val versionRequirements: List<KmVersionRequirement>
) {
  fun mutable(): KmClass {
    return KmClass().apply {
      flags = this@ImmutableKmClass.flags
      name = this@ImmutableKmClass.name
      typeParameters += this@ImmutableKmClass.typeParameters
      supertypes += this@ImmutableKmClass.supertypes
      functions += this@ImmutableKmClass.functions
      properties += this@ImmutableKmClass.properties
      typeAliases += this@ImmutableKmClass.typeAliases
      constructors += this@ImmutableKmClass.constructors
      companionObject = this@ImmutableKmClass.companionObject
      nestedClasses += this@ImmutableKmClass.nestedClasses
      enumEntries += this@ImmutableKmClass.enumEntries
      sealedSubclasses += this@ImmutableKmClass.sealedSubclasses
      versionRequirements += this@ImmutableKmClass.versionRequirements
    }
  }
}

/** @return an immutable instance of this [KmPackage]. */
@JvmName("immutableOf")
fun KmPackage.immutable(): ImmutableKmPackage {
  return ImmutableKmPackage(
      functions.unmodifiable(),
      properties.unmodifiable(),
      typeAliases.unmodifiable()
  )
}

/**
 * Immutable representation of [KmPackage].
 *
 * Represents a Kotlin package fragment, including single file facades and multi-file class parts.
 *
 * @property functions Functions in the package fragment.
 * @property properties properties in the package fragment.
 * @property typeAliases typeAliases in the package fragment.
 */
data class ImmutableKmPackage internal constructor(
    val functions: List<KmFunction>,
    val properties: List<KmProperty>,
    val typeAliases: List<KmTypeAlias>
) {
  fun mutable(): KmPackage {
    return KmPackage().apply {
      functions += this@ImmutableKmPackage.functions
      properties += this@ImmutableKmPackage.properties
      typeAliases += this@ImmutableKmPackage.typeAliases
    }
  }
}

/** @return an immutable instance of this [KmLambda]. */
@JvmName("immutableOf")
fun KmLambda.immutable(): ImmutableKmLambda {
  return ImmutableKmLambda(function)
}

/**
 * Immutable representation of [KmLambda].
 *
 * Represents a synthetic class generated for a Kotlin lambda.
 *
 * @property function Signature of the synthetic anonymous function, representing the lambda.
 */
data class ImmutableKmLambda internal constructor(val function: KmFunction) {
  fun mutable(): KmLambda {
    return KmLambda().apply {
      function = this@ImmutableKmLambda.function
    }
  }
}

/** @return an immutable instance of this [KmConstructor]. */
@JvmName("immutableOf")
fun KmConstructor.immutable(): ImmutableKmConstructor {
  return ImmutableKmConstructor(
      flags = flags,
      valueParameters = valueParameters.unmodifiable(),
      versionRequirements = versionRequirements.unmodifiable()
  )
}

/**
 * Immutable representation of [KmConstructor].
 *
 * Represents a constructor of a Kotlin class.
 *
 * @property flags constructor flags, consisting of [Flag.HAS_ANNOTATIONS], visibility flag and [Flag.Constructor] flags
 * @property valueParameters Value parameters of the constructor.
 * @property versionRequirements Version requirements on the constructor.
 */
data class ImmutableKmConstructor internal constructor(
    val flags: Flags,
    val valueParameters: List<KmValueParameter>,
    val versionRequirements: List<KmVersionRequirement>
) {
  fun mutable(): KmConstructor {
    return KmConstructor(flags).apply {
      valueParameters += this@ImmutableKmConstructor.valueParameters
      versionRequirements += this@ImmutableKmConstructor.versionRequirements
    }
  }
}

/** @return an immutable instance of this [KmFunction]. */
@JvmName("immutableOf")
fun KmFunction.immutable(): ImmutableKmFunction {
  return ImmutableKmFunction(
      flags,
      name,
      typeParameters.unmodifiable(),
      receiverParameterType,
      valueParameters.unmodifiable(),
      returnType,
      versionRequirements.unmodifiable(),
      contract
  )
}

/**
 * Immutable representation of [KmFunction].
 *
 * Represents a Kotlin function declaration.
 *
 * @property flags function flags, consisting of [Flag.HAS_ANNOTATIONS], visibility flag, modality flag and [Flag.Function] flags
 * @property name the name of the function
 * @property typeParameters Type parameters of the function.
 * @property receiverParameterType Type of the receiver of the function, if this is an extension function.
 * @property valueParameters Value parameters of the function.
 * @property returnType Return type of the function.
 * @property versionRequirements Version requirements on the function.
 * @property contract Contract of the function.
 */
data class ImmutableKmFunction internal constructor(
    val flags: Flags,
    val name: String,
    val typeParameters: List<KmTypeParameter>,
    val receiverParameterType: KmType?,
    val valueParameters: List<KmValueParameter>,
    val returnType: KmType,
    val versionRequirements: List<KmVersionRequirement>,
    val contract: KmContract?
) {
  fun mutable(): KmFunction {
    return KmFunction(flags, name).apply {
      typeParameters += this@ImmutableKmFunction.typeParameters
      receiverParameterType = this@ImmutableKmFunction.receiverParameterType
      valueParameters += this@ImmutableKmFunction.valueParameters
      returnType = this@ImmutableKmFunction.returnType
      versionRequirements += this@ImmutableKmFunction.versionRequirements
      contract = this@ImmutableKmFunction.contract
    }
  }
}

/** @return an immutable instance of this [KmProperty]. */
@JvmName("immutableOf")
fun KmProperty.immutable(): ImmutableKmProperty {
  return ImmutableKmProperty(
      flags,
      name,
      getterFlags,
      setterFlags,
      typeParameters.unmodifiable(),
      receiverParameterType,
      setterParameter,
      returnType,
      versionRequirements.unmodifiable()
  )
}

/**
 * Immutable representation of [KmProperty].
 *
 * Represents a Kotlin property declaration.
 *
 * @property flags property flags, consisting of [Flag.HAS_ANNOTATIONS], visibility flag, modality flag and [Flag.Property] flags
 * @property name the name of the property
 * @property getterFlags property accessor flags, consisting of [Flag.HAS_ANNOTATIONS], visibility flag, modality flag
 *   and [Flag.PropertyAccessor] flags
 * @property setterFlags property accessor flags, consisting of [Flag.HAS_ANNOTATIONS], visibility flag, modality flag
 *   and [Flag.PropertyAccessor] flags
 * @property typeParameters Type parameters of the property.
 * @property receiverParameterType Type of the receiver of the property, if this is an extension property.
 * @property setterParameter Value parameter of the setter of this property, if this is a `var` property.
 * @property returnType Type of the property.
 * @property versionRequirements Version requirements on the property.
 */
data class ImmutableKmProperty internal constructor(
    val flags: Flags,
    val name: String,
    val getterFlags: Flags,
    val setterFlags: Flags,
    val typeParameters: List<KmTypeParameter>,
    val receiverParameterType: KmType?,
    val setterParameter: KmValueParameter?,
    val returnType: KmType,
    val versionRequirements: List<KmVersionRequirement>
) {
  fun mutable(): KmProperty {
    return KmProperty(flags, name, getterFlags, setterFlags).apply {
      typeParameters += this@ImmutableKmProperty.typeParameters
      receiverParameterType = this@ImmutableKmProperty.receiverParameterType
      setterParameter = this@ImmutableKmProperty.setterParameter
      returnType = this@ImmutableKmProperty.returnType
      versionRequirements += this@ImmutableKmProperty.versionRequirements
    }
  }
}

/** @return an immutable instance of this [KmTypeAlias]. */
@JvmName("immutableOf")
fun KmTypeAlias.immutable(): ImmutableKmTypeAlias {
  return ImmutableKmTypeAlias(
      flags,
      name,
      typeParameters.unmodifiable(),
      underlyingType,
      expandedType,
      annotations.unmodifiable(),
      versionRequirements.unmodifiable()
  )
}

/**
 * Immutable representation of [KmTypeAlias].
 *
 * Represents a Kotlin type alias declaration.
 *
 * @property flags type alias flags, consisting of [Flag.HAS_ANNOTATIONS] and visibility flag
 * @property name the name of the type alias
 * @property typeParameters Type parameters of the type alias.
 * @property underlyingType Underlying type of the type alias, i.e. the type in the right-hand side of the type alias declaration.
 * @property expandedType Expanded type of the type alias, i.e. the full expansion of the underlying
 *                        type, where all type aliases are substituted with their expanded types. If
 *                        no type aliases are used in the underlying type, expanded type is equal to
 *                        the underlying type.
 * @property annotations Annotations on the type alias.
 * @property versionRequirements Version requirements on the type alias.
 */
data class ImmutableKmTypeAlias internal constructor(
    val flags: Flags,
    val name: String,
    val typeParameters: List<KmTypeParameter>,
    val underlyingType: KmType,
    val expandedType: KmType,
    val annotations: List<KmAnnotation>,
    val versionRequirements: List<KmVersionRequirement>
) {
  fun mutable(): KmTypeAlias {
    return KmTypeAlias(flags, name).apply {
      typeParameters += this@ImmutableKmTypeAlias.typeParameters
      underlyingType = this@ImmutableKmTypeAlias.underlyingType
      expandedType = this@ImmutableKmTypeAlias.expandedType
      annotations += this@ImmutableKmTypeAlias.annotations
      versionRequirements += this@ImmutableKmTypeAlias.versionRequirements
    }
  }
}

/** @return an immutable instance of this [KmValueParameter]. */
@JvmName("immutableOf")
fun KmValueParameter.immutable(): ImmutableKmValueParameter {
  return ImmutableKmValueParameter(
      flags,
      name,
      type,
      varargElementType
  )
}

/**
 * Immutable representation of [KmValueParameter].
 *
 * Represents a value parameter of a Kotlin constructor, function or property setter.
 *
 * @property flags value parameter flags, consisting of [Flag.ValueParameter] flags
 * @property name the name of the value parameter
 * @property type Type of the value parameter, if this is **not** a `vararg` parameter.
 * @property varargElementType Type of the value parameter, if this is a `vararg` parameter.
 */
data class ImmutableKmValueParameter internal constructor(
    val flags: Flags,
    val name: String,
    val type: KmType?,
    val varargElementType: KmType?
) {
  fun mutable(): KmValueParameter {
    return KmValueParameter(flags, name).apply {
      type = this@ImmutableKmValueParameter.type
      varargElementType = this@ImmutableKmValueParameter.varargElementType
    }
  }
}

/** @return an immutable instance of this [KmTypeParameter]. */
@JvmName("immutableOf")
fun KmTypeParameter.immutable(): ImmutableKmTypeParameter {
  return ImmutableKmTypeParameter(
      flags,
      name,
      id,
      variance,
      upperBounds.unmodifiable()
  )
}

/**
 * Immutable representation of [KmTypeParameter].
 *
 * Represents a type parameter of a Kotlin class, function, property or type alias.
 *
 * @property flags type parameter flags, consisting of [Flag.TypeParameter] flags
 * @property name the name of the type parameter
 * @property id the id of the type parameter, useful to be able to uniquely identify the type parameter in different contexts where
 *           the name isn't enough (e.g. `class A<T> { fun <T> foo(t: T) }`)
 * @property variance the declaration-site variance of the type parameter
 * @property upperBounds Upper bounds of the type parameter.
 */
data class ImmutableKmTypeParameter internal constructor(
    val flags: Flags,
    val name: String,
    val id: Int,
    val variance: KmVariance,
    val upperBounds: List<KmType>
) {
  fun mutable(): KmTypeParameter {
    return KmTypeParameter(flags, name, id, variance).apply {
      upperBounds += this@ImmutableKmTypeParameter.upperBounds
    }
  }
}

/** @return an immutable instance of this [KmType]. */
@JvmName("immutableOf")
fun KmType.immutable(): ImmutableKmType {
  return ImmutableKmType(
      flags,
      classifier,
      arguments.unmodifiable(),
      abbreviatedType,
      outerType,
      flexibleTypeUpperBound
  )
}

/**
 * Immutable representation of [KmType].
 *
 * Represents a type.
 *
 * @property flags type flags, consisting of [Flag.Type] flags
 * @property classifier Classifier of the type.
 * @property arguments Arguments of the type, if the type's classifier is a class or a type alias.
 */
data class ImmutableKmType internal constructor(
    val flags: Flags,
    val classifier: KmClassifier,
    val arguments: List<KmTypeProjection>,
    /**
     * Abbreviation of this type. Note that all types are expanded for metadata produced by the Kotlin compiler. For example:
     *
     *     typealias A<T> = MutableList<T>
     *
     *     fun foo(a: A<Any>) {}
     *
     * The type of the `foo`'s parameter in the metadata is actually `MutableList<Any>`, and its abbreviation is `A<Any>`.
     */
    val abbreviatedType: KmType?,
    /**
     * Outer type of this type, if this type's classifier is an inner class. For example:
     *
     *     class A<T> { inner class B<U> }
     *
     *     fun foo(a: A<*>.B<Byte?>) {}
     *
     * The type of the `foo`'s parameter in the metadata is `B<Byte>` (a type whose classifier is class `B`, and it has one type argument,
     * type `Byte?`), and its outer type is `A<*>` (a type whose classifier is class `A`, and it has one type argument, star projection).
     */
    val outerType: KmType?,
    /**
     * Upper bound of this type, if this type is flexible. In that case, all other data refers to the lower bound of the type.
     *
     * Flexible types in Kotlin include platform types in Kotlin/JVM and `dynamic` type in Kotlin/JS.
     */
    val flexibleTypeUpperBound: KmFlexibleTypeUpperBound?
) {
  fun mutable(): KmType {
    return KmType(flags).apply {
      classifier = this@ImmutableKmType.classifier
      arguments += this@ImmutableKmType.arguments
      abbreviatedType = this@ImmutableKmType.abbreviatedType
      outerType = this@ImmutableKmType.outerType
      flexibleTypeUpperBound = this@ImmutableKmType.flexibleTypeUpperBound
    }
  }
}

/** @return an immutable instance of this [KmVersionRequirement]. */
@JvmName("immutableOf")
fun KmVersionRequirement.immutable(): ImmutableKmVersionRequirement {
  return ImmutableKmVersionRequirement(
      kind,
      level,
      errorCode,
      message,
      version
  )
}

/**
 * Immutable representation of [KmVersionRequirement].
 *
 * Represents a version requirement on a Kotlin declaration.
 *
 * Version requirement is an internal feature of the Kotlin compiler and the standard Kotlin library,
 * enabled for example with the internal [kotlin.internal.RequireKotlin] annotation.
 *
 * @property kind Kind of the version that this declaration requires.
 * @property level Level of the diagnostic that must be reported on the usages of the declaration in case the version requirement is not satisfied.
 * @property errorCode Optional error code to be displayed in the diagnostic.
 * @property message Optional message to be displayed in the diagnostic.
 * @property version Version required by this requirement.
 */
data class ImmutableKmVersionRequirement internal constructor(
    val kind: KmVersionRequirementVersionKind,
    val level: KmVersionRequirementLevel,
    val errorCode: Int?,
    val message: String?,
    val version: KmVersion
) : KmVersionRequirementVisitor() {
  fun mutable(): KmVersionRequirement {
    return KmVersionRequirement().apply {
      kind = this@ImmutableKmVersionRequirement.kind
      level = this@ImmutableKmVersionRequirement.level
      errorCode = this@ImmutableKmVersionRequirement.errorCode
      message = this@ImmutableKmVersionRequirement.message
      version = this@ImmutableKmVersionRequirement.version
    }
  }
}

/** @return an immutable instance of this [KmContract]. */
@JvmName("immutableOf")
fun KmContract.immutable(): ImmutableKmContract {
  return ImmutableKmContract(effects.unmodifiable())
}

/**
 * Immutable representation of [KmContract].
 *
 * Represents a contract of a Kotlin function.
 *
 * Contracts are an internal feature of the standard Kotlin library, and their behavior and/or binary format
 * may change in a subsequent release.
 *
 * @property effects Effects of this contract.
 */
data class ImmutableKmContract internal constructor(val effects: List<KmEffect>) {
  fun mutable(): KmContract {
    return KmContract().apply {
      effects += this@ImmutableKmContract.effects
    }
  }
}

/** @return an immutable instance of this [KmEffect]. */
@JvmName("immutableOf")
fun KmEffect.immutable(): ImmutableKmEffect {
  return ImmutableKmEffect(
      type,
      invocationKind,
      constructorArguments.unmodifiable(),
      conclusion
  )
}

/**
 * Immutable representation of [KmEffect].
 *
 * Represents an effect (a part of the contract of a Kotlin function).
 *
 * Contracts are an internal feature of the standard Kotlin library, and their behavior and/or binary format
 * may change in a subsequent release.
 *
 * @property type type of the effect
 * @property invocationKind optional number of invocations of the lambda parameter of this function,
 *   specified further in the effect expression
 * @property constructorArguments Arguments of the effect constructor, i.e. the constant value for
 *                                the [KmEffectType.RETURNS_CONSTANT] effect, or the parameter
 *                                reference for the [KmEffectType.CALLS] effect.
 * @property conclusion Conclusion of the effect. If this value is set, the effect represents an implication with this value as the right-hand side.
 */
data class ImmutableKmEffect internal constructor(
    val type: KmEffectType,
    val invocationKind: KmEffectInvocationKind?,
    val constructorArguments: List<KmEffectExpression>,
    val conclusion: KmEffectExpression?
) {
  fun mutable(): KmEffect {
    return KmEffect(type, invocationKind).apply {
      constructorArguments += this@ImmutableKmEffect.constructorArguments
      conclusion = this@ImmutableKmEffect.conclusion
    }
  }
}

/** @return an immutable instance of this [KmEffectExpression]. */
@JvmName("immutableOf")
fun KmEffectExpression.immutable(): ImmutableKmEffectExpression {
  return ImmutableKmEffectExpression(
      flags,
      parameterIndex,
      constantValue,
      isInstanceType,
      andArguments.unmodifiable(),
      orArguments.unmodifiable()
  )
}

/**
 * Immutable representation of [KmEffectExpression].
 *
 * Represents an effect expression, the contents of an effect (a part of the contract of a Kotlin function).
 *
 * Contracts are an internal feature of the standard Kotlin library, and their behavior and/or binary format
 * may change in a subsequent release.
 *
 * @property flags Effect expression flags, consisting of [Flag.EffectExpression] flags.
 * @property parameterIndex Optional 1-based index of the value parameter of the function, for
 *                          effects which assert something about the function parameters. The index
 *                          0 means the extension receiver parameter.
 * @property constantValue Constant value used in the effect expression.
 * @property isInstanceType Type used as the target of an `is`-expression in the effect expression.
 * @property andArguments Arguments of an `&&`-expression. If this list is non-empty, the resulting
 *                        effect expression is a conjunction of this expression and elements of the list.
 * @property orArguments Arguments of an `||`-expression. If this list is non-empty, the resulting
 *                       effect expression is a disjunction of this expression and elements of the list.
 */
data class ImmutableKmEffectExpression internal constructor(
    val flags: Flags,
    val parameterIndex: Int?,
    val constantValue: KmConstantValue?,
    val isInstanceType: KmType?,
    val andArguments: List<KmEffectExpression>,
    val orArguments: List<KmEffectExpression>
) {
  fun mutable(): KmEffectExpression {
    return KmEffectExpression().apply {
      flags = this@ImmutableKmEffectExpression.flags
      parameterIndex = this@ImmutableKmEffectExpression.parameterIndex
      constantValue = this@ImmutableKmEffectExpression.constantValue
      isInstanceType = this@ImmutableKmEffectExpression.isInstanceType
      andArguments += this@ImmutableKmEffectExpression.andArguments
      orArguments += this@ImmutableKmEffectExpression.orArguments
    }
  }
}

private fun <E> List<E>.unmodifiable(): List<E> = Collections.unmodifiableList(this)
