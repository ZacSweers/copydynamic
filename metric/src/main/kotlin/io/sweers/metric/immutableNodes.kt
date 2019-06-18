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

/** @return an immutable representation of this [KmClass]. */
@JvmName("immutableOf")
fun KmClass.immutable(): ImmutableKmClass {
  return ImmutableKmClass(
      flags,
      name,
      typeParameters.map { it.immutable() },
      supertypes.map { it.immutable() },
      functions.map { it.immutable() },
      properties.map { it.immutable() },
      typeAliases.map { it.immutable() },
      constructors.map { it.immutable() },
      companionObject,
      nestedClasses.unmodifiable(),
      enumEntries.unmodifiable(),
      sealedSubclasses.unmodifiable(),
      versionRequirements.map { it.immutable() }
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
    val typeParameters: List<ImmutableKmTypeParameter>,
    val supertypes: List<ImmutableKmType>,
    val functions: List<ImmutableKmFunction>,
    val properties: List<ImmutableKmProperty>,
    val typeAliases: List<ImmutableKmTypeAlias>,
    val constructors: List<ImmutableKmConstructor>,
    val companionObject: String?,
    val nestedClasses: List<String>,
    val enumEntries: List<String>,
    val sealedSubclasses: List<ClassName>,
    val versionRequirements: List<ImmutableKmVersionRequirement>
) {
  fun mutable(): KmClass {
    return KmClass().apply {
      flags = this@ImmutableKmClass.flags
      name = this@ImmutableKmClass.name
      typeParameters += this@ImmutableKmClass.typeParameters.map { it.mutable() }
      supertypes += this@ImmutableKmClass.supertypes.map { it.mutable() }
      functions += this@ImmutableKmClass.functions.map { it.mutable() }
      properties += this@ImmutableKmClass.properties.map { it.mutable() }
      typeAliases += this@ImmutableKmClass.typeAliases.map { it.mutable() }
      constructors += this@ImmutableKmClass.constructors.map { it.mutable() }
      companionObject = this@ImmutableKmClass.companionObject
      nestedClasses += this@ImmutableKmClass.nestedClasses
      enumEntries += this@ImmutableKmClass.enumEntries
      sealedSubclasses += this@ImmutableKmClass.sealedSubclasses
      versionRequirements += this@ImmutableKmClass.versionRequirements.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmPackage]. */
@JvmName("immutableOf")
fun KmPackage.immutable(): ImmutableKmPackage {
  return ImmutableKmPackage(
      functions.map { it.immutable() },
      properties.map { it.immutable() },
      typeAliases.map { it.immutable() }
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
    val functions: List<ImmutableKmFunction>,
    val properties: List<ImmutableKmProperty>,
    val typeAliases: List<ImmutableKmTypeAlias>
) {
  fun mutable(): KmPackage {
    return KmPackage().apply {
      functions += this@ImmutableKmPackage.functions.map { it.mutable() }
      properties += this@ImmutableKmPackage.properties.map { it.mutable() }
      typeAliases += this@ImmutableKmPackage.typeAliases.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmLambda]. */
@JvmName("immutableOf")
fun KmLambda.immutable(): ImmutableKmLambda {
  return ImmutableKmLambda(function.immutable())
}

/**
 * Immutable representation of [KmLambda].
 *
 * Represents a synthetic class generated for a Kotlin lambda.
 *
 * @property function Signature of the synthetic anonymous function, representing the lambda.
 */
data class ImmutableKmLambda internal constructor(val function: ImmutableKmFunction) {
  fun mutable(): KmLambda {
    return KmLambda().apply {
      function = this@ImmutableKmLambda.function.mutable()
    }
  }
}

/** @return an immutable representation of this [KmConstructor]. */
@JvmName("immutableOf")
fun KmConstructor.immutable(): ImmutableKmConstructor {
  return ImmutableKmConstructor(
      flags = flags,
      valueParameters = valueParameters.map { it.immutable() },
      versionRequirements = versionRequirements.map { it.immutable() }
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
    val valueParameters: List<ImmutableKmValueParameter>,
    val versionRequirements: List<ImmutableKmVersionRequirement>
) {
  fun mutable(): KmConstructor {
    return KmConstructor(flags).apply {
      valueParameters += this@ImmutableKmConstructor.valueParameters.map { it.mutable() }
      versionRequirements += this@ImmutableKmConstructor.versionRequirements.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmFunction]. */
@JvmName("immutableOf")
fun KmFunction.immutable(): ImmutableKmFunction {
  return ImmutableKmFunction(
      flags,
      name,
      typeParameters.map { it.immutable() },
      receiverParameterType,
      valueParameters.map { it.immutable() },
      returnType.immutable(),
      versionRequirements.map { it.immutable() },
      contract?.immutable()
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
    val typeParameters: List<ImmutableKmTypeParameter>,
    val receiverParameterType: KmType?,
    val valueParameters: List<ImmutableKmValueParameter>,
    val returnType: ImmutableKmType,
    val versionRequirements: List<ImmutableKmVersionRequirement>,
    val contract: ImmutableKmContract?
) {
  fun mutable(): KmFunction {
    return KmFunction(flags, name).apply {
      typeParameters += this@ImmutableKmFunction.typeParameters.map { it.mutable() }
      receiverParameterType = this@ImmutableKmFunction.receiverParameterType
      valueParameters += this@ImmutableKmFunction.valueParameters.map { it.mutable() }
      returnType = this@ImmutableKmFunction.returnType.mutable()
      versionRequirements += this@ImmutableKmFunction.versionRequirements.map { it.mutable() }
      contract = this@ImmutableKmFunction.contract?.mutable()
    }
  }
}

/** @return an immutable representation of this [KmProperty]. */
@JvmName("immutableOf")
fun KmProperty.immutable(): ImmutableKmProperty {
  return ImmutableKmProperty(
      flags,
      name,
      getterFlags,
      setterFlags,
      typeParameters.map { it.immutable() },
      receiverParameterType?.immutable(),
      setterParameter?.immutable(),
      returnType.immutable(),
      versionRequirements.map { it.immutable() }
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
    val typeParameters: List<ImmutableKmTypeParameter>,
    val receiverParameterType: ImmutableKmType?,
    val setterParameter: ImmutableKmValueParameter?,
    val returnType: ImmutableKmType,
    val versionRequirements: List<ImmutableKmVersionRequirement>
) {
  fun mutable(): KmProperty {
    return KmProperty(flags, name, getterFlags, setterFlags).apply {
      typeParameters += this@ImmutableKmProperty.typeParameters.map { it.mutable() }
      receiverParameterType = this@ImmutableKmProperty.receiverParameterType?.mutable()
      setterParameter = this@ImmutableKmProperty.setterParameter?.mutable()
      returnType = this@ImmutableKmProperty.returnType.mutable()
      versionRequirements += this@ImmutableKmProperty.versionRequirements.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmTypeAlias]. */
@JvmName("immutableOf")
fun KmTypeAlias.immutable(): ImmutableKmTypeAlias {
  return ImmutableKmTypeAlias(
      flags,
      name,
      typeParameters.map { it.immutable() },
      underlyingType.immutable(),
      expandedType.immutable(),
      annotations.unmodifiable(),
      versionRequirements.map { it.immutable() }
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
    val typeParameters: List<ImmutableKmTypeParameter>,
    val underlyingType: ImmutableKmType,
    val expandedType: ImmutableKmType,
    val annotations: List<KmAnnotation>,
    val versionRequirements: List<ImmutableKmVersionRequirement>
) {
  fun mutable(): KmTypeAlias {
    return KmTypeAlias(flags, name).apply {
      typeParameters += this@ImmutableKmTypeAlias.typeParameters.map { it.mutable() }
      underlyingType = this@ImmutableKmTypeAlias.underlyingType.mutable()
      expandedType = this@ImmutableKmTypeAlias.expandedType.mutable()
      annotations += this@ImmutableKmTypeAlias.annotations
      versionRequirements += this@ImmutableKmTypeAlias.versionRequirements.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmValueParameter]. */
@JvmName("immutableOf")
fun KmValueParameter.immutable(): ImmutableKmValueParameter {
  return ImmutableKmValueParameter(
      flags,
      name,
      type?.immutable(),
      varargElementType?.immutable()
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
    val type: ImmutableKmType?,
    val varargElementType: ImmutableKmType?
) {
  fun mutable(): KmValueParameter {
    return KmValueParameter(flags, name).apply {
      type = this@ImmutableKmValueParameter.type?.mutable()
      varargElementType = this@ImmutableKmValueParameter.varargElementType?.mutable()
    }
  }
}

/** @return an immutable representation of this [KmTypeParameter]. */
@JvmName("immutableOf")
fun KmTypeParameter.immutable(): ImmutableKmTypeParameter {
  return ImmutableKmTypeParameter(
      flags,
      name,
      id,
      variance,
      upperBounds.map { it.immutable() }
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
    val upperBounds: List<ImmutableKmType>
) {
  fun mutable(): KmTypeParameter {
    return KmTypeParameter(flags, name, id, variance).apply {
      upperBounds += this@ImmutableKmTypeParameter.upperBounds.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmType]. */
@JvmName("immutableOf")
fun KmType.immutable(): ImmutableKmType {
  return ImmutableKmType(
      flags,
      classifier,
      arguments.map { it.immutable() },
      abbreviatedType?.immutable(),
      outerType?.immutable(),
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
    val arguments: List<ImmutableKmTypeProjection>,
    /**
     * Abbreviation of this type. Note that all types are expanded for metadata produced by the Kotlin compiler. For example:
     *
     *     typealias A<T> = MutableList<T>
     *
     *     fun foo(a: A<Any>) {}
     *
     * The type of the `foo`'s parameter in the metadata is actually `MutableList<Any>`, and its abbreviation is `A<Any>`.
     */
    val abbreviatedType: ImmutableKmType?,
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
    val outerType: ImmutableKmType?,
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
      arguments += this@ImmutableKmType.arguments.map { it.mutable() }
      abbreviatedType = this@ImmutableKmType.abbreviatedType?.mutable()
      outerType = this@ImmutableKmType.outerType?.mutable()
      flexibleTypeUpperBound = this@ImmutableKmType.flexibleTypeUpperBound
    }
  }
}

/** @return an immutable representation of this [KmVersionRequirement]. */
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

/** @return an immutable representation of this [KmContract]. */
@JvmName("immutableOf")
fun KmContract.immutable(): ImmutableKmContract {
  return ImmutableKmContract(effects.map { it.immutable() })
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
data class ImmutableKmContract internal constructor(val effects: List<ImmutableKmEffect>) {
  fun mutable(): KmContract {
    return KmContract().apply {
      effects += this@ImmutableKmContract.effects.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmEffect]. */
@JvmName("immutableOf")
fun KmEffect.immutable(): ImmutableKmEffect {
  return ImmutableKmEffect(
      type,
      invocationKind,
      constructorArguments.map { it.immutable() },
      conclusion?.immutable()
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
    val constructorArguments: List<ImmutableKmEffectExpression>,
    val conclusion: ImmutableKmEffectExpression?
) {
  fun mutable(): KmEffect {
    return KmEffect(type, invocationKind).apply {
      constructorArguments += this@ImmutableKmEffect.constructorArguments.map { it.mutable() }
      conclusion = this@ImmutableKmEffect.conclusion?.mutable()
    }
  }
}

/** @return an immutable representation of this [KmEffectExpression]. */
@JvmName("immutableOf")
fun KmEffectExpression.immutable(): ImmutableKmEffectExpression {
  return ImmutableKmEffectExpression(
      flags,
      parameterIndex,
      constantValue,
      isInstanceType?.immutable(),
      andArguments.map { it.immutable() },
      orArguments.map { it.immutable() }
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
    val isInstanceType: ImmutableKmType?,
    val andArguments: List<ImmutableKmEffectExpression>,
    val orArguments: List<ImmutableKmEffectExpression>
) {
  fun mutable(): KmEffectExpression {
    return KmEffectExpression().apply {
      flags = this@ImmutableKmEffectExpression.flags
      parameterIndex = this@ImmutableKmEffectExpression.parameterIndex
      constantValue = this@ImmutableKmEffectExpression.constantValue
      isInstanceType = this@ImmutableKmEffectExpression.isInstanceType?.mutable()
      andArguments += this@ImmutableKmEffectExpression.andArguments.map { it.mutable() }
      orArguments += this@ImmutableKmEffectExpression.orArguments.map { it.mutable() }
    }
  }
}

/** @return an immutable representation of this [KmTypeProjection]. */
@JvmName("immutableOf")
fun KmTypeProjection.immutable(): ImmutableKmTypeProjection {
  return ImmutableKmTypeProjection(variance, type?.immutable())
}

/**
 * Immutable representation of [KmTypeProjection].
 *
 * Represents type projection used in a type argument of the type based on a class or on a type alias.
 * For example, in `MutableMap<in String?, *>`, `in String?` is the type projection which is the first type argument of the type.
 *
 * @property variance the variance of the type projection, or `null` if this is a star projection
 * @property type the projected type, or `null` if this is a star projection
 */
data class ImmutableKmTypeProjection(val variance: KmVariance?, val type: ImmutableKmType?) {

  fun mutable(): KmTypeProjection {
    return KmTypeProjection(variance, type?.mutable())
  }

  companion object {
    /**
     * Star projection (`*`).
     * For example, in `MutableMap<in String?, *>`, `*` is the star projection which is the second type argument of the type.
     */
    @JvmField
    val STAR = KmTypeProjection.STAR
  }
}

/** @return an immutable representation of this [KmFlexibleTypeUpperBound]. */
@JvmName("immutableOf")
fun KmFlexibleTypeUpperBound.immutable(): ImmutableKmFlexibleTypeUpperBound {
  return ImmutableKmFlexibleTypeUpperBound(type.immutable(), typeFlexibilityId)
}

/**
 * Immutable representation of [KmFlexibleTypeUpperBound].
 *
 * Represents an upper bound of a flexible Kotlin type.
 *
 * @property type upper bound of the flexible type
 * @property typeFlexibilityId id of the kind of flexibility this type has. For example, "kotlin.jvm.PlatformType" for JVM platform types,
 *                          or "kotlin.DynamicType" for JS dynamic type
 */
data class ImmutableKmFlexibleTypeUpperBound(val type: ImmutableKmType,
    val typeFlexibilityId: String?) {
  fun mutable(): KmFlexibleTypeUpperBound {
    return KmFlexibleTypeUpperBound(type.mutable(), typeFlexibilityId)
  }
}

private fun <E> List<E>.unmodifiable(): List<E> = Collections.unmodifiableList(this)
