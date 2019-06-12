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

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmFunctionVisitor
import kotlinx.metadata.KmPropertyVisitor
import kotlinx.metadata.KmTypeParameterVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
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
import javax.lang.model.element.ExecutableElement
import kotlin.reflect.KClass
import kotlinx.metadata.ClassName as KmClassName

// TODO
//  * "ABI generation"
//  * Accept configuration for what types to parse. Default all.

inline fun KClass<*>.readKmType(): TypeSpec = java.readKmType()
inline fun Class<*>.readKmType(): TypeSpec = onAnnotation<Metadata>(::getAnnotation).readKmType()
inline fun Element.readKmType(): TypeSpec = onAnnotation<Metadata>(::getAnnotation).readKmType()

inline fun Metadata.readKmType(): TypeSpec {
  val metadata = KotlinClassMetadata.read(asClassHeader())
  checkNotNull(metadata) {
    "Could not parse metadata! This should only happen if you're using Kotlin <1.1."
  }
  return when (metadata) {
    is KotlinClassMetadata.Class -> {
      metadata.readClassData()
    }
    is FileFacade -> TODO()
    is SyntheticClass -> TODO()
    is MultiFileClassFacade -> TODO()
    is MultiFileClassPart -> TODO()
    is Unknown -> throw RuntimeException("Recorded unknown metadata type! $metadata")
  }
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

internal fun KmVariance.asKModifier(): KModifier? {
  return when (this) {
    IN -> KModifier.IN
    OUT -> KModifier.OUT
    INVARIANT -> null
  }
}

/**
 * Resolves the TypeName of this type as it would be seen in the source code, including nullability
 * and generic type parameters.
 *
 * @param flags the [Flags] associated with this type
 * @param [getTypeParameter] a function that returns the type parameter for the given index. **Only
 *     called if [TypeNameKmTypeVisitor.visitTypeParameter] is called**
 * @param useTypeAlias indicates whether or not to use type aliases or resolve their underlying
 *     types
 */
internal class TypeNameKmTypeVisitor(
    flags: Flags,
    private val getTypeParameter: ((index: Int) -> TypeName)? = null,
    private val useTypeAlias: Boolean = false,
    private val receiver: (TypeName) -> Unit
) : KmTypeVisitor() {

  private val nullable = flags.isNullableType
  private val isSuspend = flags.isSuspendType // TODO KotlinPoet only exposes this for lambdas
  private var className: String? = null
  private var typeAliasName: String? = null
  private var typeAliasType: TypeName? = null
  private var flexibleTypeUpperBound: TypeName? = null
  private var outerType: TypeName? = null
  private var typeParameter: TypeName? = null
  private val argumentList = mutableListOf<TypeName>()

  override fun visitAbbreviatedType(flags: Flags): KmTypeVisitor? {
    if (!useTypeAlias) {
      return null
    }
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      typeAliasType = it
    }
  }

  override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      argumentList.add(
          when (variance) {
            IN -> WildcardTypeName.consumerOf(it)
            OUT -> {
              if (it == ANY) {
                // This becomes a *, which we actually don't want here.
                // List<Any> works with List<*>, but List<*> doesn't work with List<Any>
                it
              } else {
                WildcardTypeName.producerOf(it)
              }
            }
            INVARIANT -> it
          }
      )
    }
  }

  override fun visitClass(name: KmClassName) {
    className = name
  }

  override fun visitFlexibleTypeUpperBound(flags: Flags,
      typeFlexibilityId: String?): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      flexibleTypeUpperBound = WildcardTypeName.producerOf(it)
    }
  }

  override fun visitOuterType(flags: Flags): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      outerType = WildcardTypeName.consumerOf(it)
    }
  }

  override fun visitStarProjection() {
    argumentList.add(STAR)
  }

  override fun visitTypeAlias(name: KmClassName) {
    if (!useTypeAlias) {
      return
    }
    typeAliasName = name
  }

  override fun visitTypeParameter(id: Int) {
    typeParameter = (getTypeParameter
        ?: throw IllegalStateException("Visiting TypeParameter when there are no type parameters!")
        ).invoke(id)
  }

  override fun visitEnd() {
    var finalType = flexibleTypeUpperBound ?: outerType ?: typeParameter
    if (finalType == null) {
      if (useTypeAlias) {
        finalType = typeAliasType ?: typeAliasName?.let {
          ClassName.bestGuess(it.replace("/", "."))
        }
      }
      if (finalType == null) {
        finalType = className?.let { ClassName.bestGuess(it.replace("/", ".")) }
            ?: throw IllegalStateException("No valid typename found!")
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
              LambdaTypeName.get(
                  receiver = null, // TODO wut
                  parameters = *parameters,
                  returnType = returnType
              ).copy(suspending = isSuspend)
            }
          } else {
            finalType = (finalType as ClassName).parameterizedBy(*argumentList.toTypedArray())
          }
        }
      }
    }

    receiver(finalType.copy(nullable = nullable))
  }
}

private class TypeVariableNameKmTypeParameterVisitor(
    private val flags: Flags,
    private val name: String,
    private val variance: KmVariance,
    private val onResolved: (TypeVariableName) -> Unit
) : KmTypeParameterVisitor() {
  private val upperBoundList = mutableListOf<TypeName>()
  override fun visitUpperBound(flags: Flags): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags) {
      upperBoundList += it
    }
  }

  override fun visitEnd() {
    val finalVariance = variance.asKModifier().let {
      if (it == KModifier.OUT) {
        // We don't redeclare out variance here
        null
      } else {
        it
      }
    }
    val typeVariableName = if (upperBoundList.isEmpty()) {
      TypeVariableName(
          name = name,
          variance = finalVariance
      )
    } else {
      TypeVariableName(
          name = name,
          bounds = *(upperBoundList.toTypedArray()),
          variance = finalVariance
      )
    }
    onResolved(typeVariableName.copy(reified = flags.isReifiedTypeParameter))
  }
}

private class MetricKmParameterVisitor(
    private val flags: Flags,
    private val name: String,
    private val typeParamResolver: ((index: Int) -> TypeName)?,
    private val onResolved: (KmParameter) -> Unit
) : KmValueParameterVisitor() {
  lateinit var type: TypeName
  var isVarArg = false
  var varargElementType: TypeName? = null
  override fun visitType(flags: Flags): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, typeParamResolver) {
      type = it
    }
  }

  override fun visitVarargElementType(flags: Flags): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, typeParamResolver) {
      isVarArg = true
      varargElementType = it
    }
  }

  override fun visitEnd() {
    val kmParameter = KmParameter(flags, name, type, isVarArg,
        varargElementType)
    onResolved(kmParameter)
  }
}

private class TypeSpecKmClassVisitor(private val receiver: (TypeSpec) -> Unit) : KmClassVisitor() {
  @Suppress("RedundantExplicitType")
  private var classFlags: Flags = 0
  private lateinit var className: String
  private val constructors = mutableListOf<KmConstructor>()
  private var companionObjectName: String? = null
  private val typeParameters = LinkedHashMap<Int, TypeVariableName>()
  private val typeParamResolver = { id: Int -> typeParameters[id]!! }
  private lateinit var superClass: TypeName
  private val superInterfaces = mutableListOf<TypeName>()
  private val properties = mutableListOf<KmProperty>()
  private val functions = mutableListOf<KmFunction>()

  override fun visit(flags: Flags, name: KmClassName) {
    super.visit(flags, name)
    className = name
    classFlags = flags
  }

  override fun visitTypeParameter(flags: Flags,
      name: String,
      id: Int,
      variance: KmVariance): KmTypeParameterVisitor? {
    return TypeVariableNameKmTypeParameterVisitor(flags, name, variance) {
      typeParameters[id] = it
    }
  }

  override fun visitCompanionObject(name: String) {
    companionObjectName = name
  }

  override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
    return object : KmConstructorVisitor() {
      val params = mutableListOf<KmParameter>()
      override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
        return MetricKmParameterVisitor(flags, name, typeParamResolver, params::plusAssign)
      }

      override fun visitEnd() {
        constructors += KmConstructor(flags, params)
      }
    }
  }

  override fun visitSupertype(flags: Flags): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, typeParamResolver) {
      if (this::superClass.isInitialized) {
        superInterfaces += it
      } else {
        superClass = it
      }
    }
  }

  override fun visitProperty(flags: Flags,
      name: String,
      getterFlags: Flags,
      setterFlags: Flags): KmPropertyVisitor? {
    return object : KmPropertyVisitor() {
      lateinit var type: TypeName
      override fun visitEnd() {
        val getterResolver = {
          val visibility = getterFlags.visibility
          val modalities = getterFlags.modalities
              .filterNot { it == KModifier.FINAL && !flags.isOverrideProperty }
          val propertyAccessorFlags = getterFlags.propertyAccessorFlags
          if (visibility == KModifier.PUBLIC && modalities.isEmpty() && propertyAccessorFlags.isEmpty()) {
            null
          } else {
            FunSpec.getterBuilder()
                .apply {
                  addModifiers(visibility)
                  addModifiers(*modalities.toTypedArray())
                  addModifiers(*propertyAccessorFlags.toTypedArray())
                }
                .build()
          }
        }
        val setterResolver = {
          val visibility = setterFlags.visibility
          val modalities = setterFlags.modalities
              .filterNot { it == KModifier.FINAL && !flags.isOverrideProperty }
          val propertyAccessorFlags = setterFlags.propertyAccessorFlags
          if (visibility == KModifier.PUBLIC && modalities.isEmpty() && propertyAccessorFlags.isEmpty()) {
            null
          } else {
            FunSpec.setterBuilder()
                .apply {
                  addModifiers(visibility)
                  addModifiers(*modalities.toTypedArray())
                  addModifiers(*propertyAccessorFlags.toTypedArray())
                }
                .build()
          }
        }
        properties += KmProperty(flags, name, type, getterResolver, setterResolver)
      }

      private val Flags.propertyAccessorFlags: Set<KModifier>
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

      override fun visitReturnType(flags: Flags): KmTypeVisitor? {
        return TypeNameKmTypeVisitor(flags, typeParamResolver) {
          type = it
        }
      }
    }
  }

  override fun visitFunction(flags: Flags, name: String): KmFunctionVisitor? {
    // TODO What about intersection types?
    return object : KmFunctionVisitor() {
      private lateinit var returnType: TypeName
      private val functionTypeParameters = LinkedHashMap<Int, TypeVariableName>()
      private val functionTypeParamResolver = { id: Int ->
        // Try the function's type params first, then the class's
        functionTypeParameters[id] ?: typeParameters[id] ?: TODO("Unknown ID for scope! $id")
      }
      private val params = mutableListOf<KmParameter>()
      private var receiverType: TypeName? = null

      override fun visitEnd() {
        super.visitEnd()
        functions += KmFunction(
            flags,
            name,
            params,
            returnType,
            receiverType
        )
      }

      override fun visitReceiverParameterType(flags: Flags): KmTypeVisitor? {
        return TypeNameKmTypeVisitor(flags, functionTypeParamResolver) {
          receiverType = it
        }
      }

      override fun visitReturnType(flags: Flags): KmTypeVisitor? {
        return TypeNameKmTypeVisitor(flags, functionTypeParamResolver) {
          returnType = it
        }
      }

      override fun visitTypeParameter(flags: Flags,
          name: String,
          id: Int,
          variance: KmVariance): KmTypeParameterVisitor? {
        return TypeVariableNameKmTypeParameterVisitor(flags, name, variance) {
          functionTypeParameters[id] = it
        }
      }

      override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
        return MetricKmParameterVisitor(flags, name, functionTypeParamResolver, params::plusAssign)
      }
    }
  }

  override fun visitEnd() {
    super.visitEnd()
    receiver(
        KmClass(className.replace("/", "."),
            classFlags,
            companionObjectName,
            constructors,
            superClass,
            superInterfaces,
            typeParameters.values.toList(),
            properties,
            functions)
            .asTypeSpec()
    )
  }
}

@PublishedApi
internal fun KotlinClassMetadata.Class.readClassData(): TypeSpec {
  lateinit var finalSpec: TypeSpec
  accept(TypeSpecKmClassVisitor {
    finalSpec = it
  })
  return finalSpec
}

data class KmClass internal constructor(
    val name: String,
    override val flags: Flags,
    val companionObjectName: String?,
    val constructors: List<KmConstructor>,
    val superClass: TypeName,
    val superInterfaces: List<TypeName>,
    val typeVariables: List<TypeVariableName>,
    val properties: List<KmProperty>,
    val functions: List<KmFunction>
) : KmCommon, KmVisibilityOwner {

  val primaryConstructor: KmConstructor? by lazy { constructors.find { it.isPrimary } }

  fun getPropertyForAnnotationHolder(methodElement: ExecutableElement): KmProperty? {
    return methodElement.simpleName.toString()
        .takeIf {
          it.endsWith(KOTLIN_PROPERTY_ANNOTATIONS_FUN_SUFFIX)
        }
        ?.substringBefore(KOTLIN_PROPERTY_ANNOTATIONS_FUN_SUFFIX)
        ?.let { propertyName -> properties.firstOrNull { propertyName == it.name } }
  }

  companion object {
    /**
     * Postfix of the method name containing the [kotlin.Metadata] annotation for the relative property.
     * @see [getPropertyForAnnotationHolder]
     */
    const val KOTLIN_PROPERTY_ANNOTATIONS_FUN_SUFFIX = "\$annotations"
  }
}

data class KmConstructor internal constructor(
    override val flags: Flags,
    val parameters: List<KmParameter>
) : KmCommon, KmVisibilityOwner

data class KmFunction internal constructor(
    override val flags: Flags,
    val name: String,
    val parameters: List<KmParameter>,
    val returnType: TypeName,
    val receiverType: TypeName?
) : KmCommon, KmVisibilityOwner

data class KmParameter internal constructor(
    override val flags: Flags,
    val name: String,
    val type: TypeName,
    val isVarArg: Boolean = false,
    val varargElementType: TypeName? = null
) : KmCommon

data class KmProperty internal constructor(
    override val flags: Flags,
    val name: String,
    val type: TypeName,
    val getterResolver: () -> FunSpec?,
    val setterResolver: () -> FunSpec?
) : KmCommon, KmVisibilityOwner

interface KmCommon {
  val flags: Flags
}

interface KmType : KmCommon

interface KmVisibilityOwner : KmCommon {
  val visibility: KModifier get() = flags.visibility
  val modalities: Set<KModifier> get() = flags.modalities
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


private inline fun <E> setOf(body: MutableSet<E>.() -> Unit): Set<E> {
  return mutableSetOf<E>().apply(body).toSet()
}
