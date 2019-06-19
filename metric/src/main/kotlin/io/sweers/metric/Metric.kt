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
package io.sweers.metric;

import com.google.auto.common.MoreElements
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmClassifier.TypeAlias
import kotlinx.metadata.KmClassifier.TypeParameter
import kotlinx.metadata.KmVariance
import kotlinx.metadata.KmVariance.IN
import kotlinx.metadata.KmVariance.INVARIANT
import kotlinx.metadata.KmVariance.OUT
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.KotlinClassMetadata.FileFacade
import kotlinx.metadata.jvm.KotlinClassMetadata.MultiFileClassFacade
import kotlinx.metadata.jvm.KotlinClassMetadata.MultiFileClassPart
import kotlinx.metadata.jvm.KotlinClassMetadata.SyntheticClass
import kotlinx.metadata.jvm.KotlinClassMetadata.Unknown
import javax.lang.model.element.Element
import kotlin.reflect.KClass
import kotlinx.metadata.ClassName as KmClassName

inline fun KClass<*>.asTypeSpec(): TypeSpec = java.asTypeSpec()
inline fun Class<*>.asTypeSpec(): TypeSpec = onAnnotation<Metadata>(::getAnnotation).asTypeSpec()
inline fun Element.asTypeSpec(): TypeSpec = onAnnotation<Metadata>(::getAnnotation).asTypeSpec()
inline fun KClass<*>.asFileSpec(): FileSpec = java.asFileSpec()
inline fun Class<*>.asFileSpec(): FileSpec = FileSpec.get(`package`.name, asTypeSpec())
inline fun Element.asFileSpec(): FileSpec = FileSpec.get(
    packageName = MoreElements.getPackage(this).toString(),
    typeSpec = asTypeSpec()
)

fun Metadata.asTypeSpec(): TypeSpec {
  return when (val metadata = readKotlinClassMetadata()) {
    //  return when (metadata) {
    is KotlinClassMetadata.Class -> {
      metadata.toImmutableKmClass().asTypeSpec()
    }
    is FileFacade -> TODO()
    is SyntheticClass -> TODO()
    is MultiFileClassFacade -> TODO()
    is MultiFileClassPart -> TODO()
    is Unknown -> throw RuntimeException("Recorded unknown metadata type! $metadata")
  }
}

internal fun Metadata.readKotlinClassMetadata(): KotlinClassMetadata {
  val metadata = KotlinClassMetadata.read(asClassHeader())
  checkNotNull(metadata) {
    "Could not parse metadata! This should only happen if you're using Kotlin <1.1."
  }
  return metadata
}

@PublishedApi
internal inline fun <reified T : Annotation> onAnnotation(lookup: ((Class<T>) -> T?)): T {
  return checkNotNull(lookup.invoke(T::class.java)) {
    "No Metadata annotation found! Must be Kotlin code built with the standard library on the classpath."
  }
}

@PublishedApi
internal fun Metadata.asClassHeader(): KotlinClassHeader {
  return KotlinClassHeader(
      kind = kind,
      metadataVersion = metadataVersion,
      bytecodeVersion = bytecodeVersion,
      data1 = data1,
      data2 = data2,
      extraString = extraString,
      packageName = packageName,
      extraInt = extraInt
  )
}

val ImmutableKmClass.primaryConstructor: ImmutableKmConstructor?
  get() = constructors.find { it.isPrimary }

internal fun KmVariance.asKModifier(): KModifier? {
  return when (this) {
    IN -> KModifier.IN
    OUT -> KModifier.OUT
    INVARIANT -> null
  }
}

internal fun ImmutableKmTypeProjection.asTypeName(
    typeParamResolver: ((index: Int) -> TypeName)
): TypeName {
  val typename = type?.asTypeName(typeParamResolver) ?: STAR
  return when (variance) {
    IN -> WildcardTypeName.consumerOf(typename)
    OUT -> {
      if (typename == ANY) {
        // This becomes a *, which we actually don't want here.
        // List<Any> works with List<*>, but List<*> doesn't work with List<Any>
        typename
      } else {
        WildcardTypeName.producerOf(typename)
      }
    }
    INVARIANT -> typename
    null -> STAR
  }
}

internal fun ImmutableKmType.asTypeName(
    typeParamResolver: ((index: Int) -> TypeName),
    useTypeAlias: Boolean = false
): TypeName {
  val argumentList = arguments.map { it.asTypeName(typeParamResolver) }
  val type: TypeName = when (val valClassifier = classifier) {
    is TypeParameter -> {
      typeParamResolver(valClassifier.id)
    }
    is KmClassifier.Class -> {
      flexibleTypeUpperBound?.asTypeName(typeParamResolver)?.let { return it }
      outerType?.asTypeName(typeParamResolver)?.let { return it }
      var finalType: TypeName = ClassName.bestGuess(valClassifier.name.replace("/", "."))
      if (argumentList.isNotEmpty()) {
        val finalTypeString = finalType.toString()
        if (finalTypeString.startsWith("kotlin.Function")) {
          // It's a lambda type!
          finalType = if (finalTypeString == "kotlin.FunctionN") {
            TODO("unclear how to express this one since it has arity")
          } else {
            val (parameters, returnType) = if (isSuspend) {
              // Coroutines always adds an `Any?` return type, but we kind of just want the
              // source representation, so we trick it here and ignore the last.
              argumentList.dropLast(2).toTypedArray() to argumentList.dropLast(1).last().let {
                // Coroutines makes these a `Continuation<T>` of the type, so we want the parameterized type
                check(it is ParameterizedTypeName)
                it.typeArguments[0]
              }
            } else {
              argumentList.dropLast(1).toTypedArray() to argumentList.last()
            }
            val lambdaType = if (isExtensionType) {
              // Extension function type! T.(). First parameter is actually the receiver.
              LambdaTypeName.get(
                  receiver = parameters[0],
                  parameters = *parameters.drop(1).toTypedArray(),
                  returnType = returnType
              )
            } else {
              LambdaTypeName.get(
                  receiver = null,
                  parameters = *parameters,
                  returnType = returnType
              )
            }
            lambdaType.copy(suspending = isSuspend)
          }
        } else {
          finalType = (finalType as ClassName).parameterizedBy(*argumentList.toTypedArray())
        }
      }
      finalType
    }
    is TypeAlias -> {
      if (useTypeAlias) {
        ClassName.bestGuess(valClassifier.name)
      } else {
        checkNotNull(abbreviatedType).asTypeName(typeParamResolver)
      }
    }
  }

  return type.copy(nullable = isNullable)
}

internal fun ImmutableKmTypeParameter.asTypeVariableName(
    typeParamResolver: ((index: Int) -> TypeName)
): TypeVariableName {
  val finalVariance = variance.asKModifier().let {
    if (it == KModifier.OUT) {
      // We don't redeclare out variance here
      null
    } else {
      it
    }
  }
  val typeVariableName = if (upperBounds.isEmpty()) {
    TypeVariableName(
        name = name,
        variance = finalVariance
    )
  } else {
    TypeVariableName(
        name = name,
        bounds = *(upperBounds.map { it.asTypeName(typeParamResolver) }.toTypedArray()),
        variance = finalVariance
    )
  }
  return typeVariableName.copy(reified = isReified)
}

private fun ImmutableKmFlexibleTypeUpperBound.asTypeName(
    typeParamResolver: ((index: Int) -> TypeName)
): TypeName {
  // TODO tag typeFlexibilityId somehow?
  return WildcardTypeName.producerOf(type.asTypeName(typeParamResolver))
}
