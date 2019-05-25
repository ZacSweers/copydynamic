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
package io.sweers.copydynamic.metadata;

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmPropertyVisitor
import kotlinx.metadata.KmTypeParameterVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

internal fun Element.readMetadata(): KotlinClassHeader? {
  return getAnnotation(Metadata::class.java)?.asClassHeader()
}

internal fun Class<*>.readMetadata(): KotlinClassHeader? {
  return getAnnotation(Metadata::class.java)?.asClassHeader()
}

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

internal fun KotlinClassHeader.readKotlinClassMetadata(): KotlinClassMetadata? {
  return KotlinClassMetadata.read(this)
}

internal fun KmVariance.asKModifier(): KModifier? {
  return when (this) {
    KmVariance.IN -> KModifier.IN
    KmVariance.OUT -> KModifier.OUT
    KmVariance.INVARIANT -> null
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
            KmVariance.IN -> WildcardTypeName.consumerOf(it)
            KmVariance.OUT -> {
              if (it == ANY) {
                // This becomes a *, which we actually don't want here.
                // List<Any> works with List<*>, but List<*> doesn't work with List<Any>
                it
              } else {
                WildcardTypeName.producerOf(it)
              }
            }
            KmVariance.INVARIANT -> it
          }
      )
    }
  }

  override fun visitClass(name: kotlinx.metadata.ClassName) {
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

  override fun visitTypeAlias(name: kotlinx.metadata.ClassName) {
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
          finalType = (finalType as ClassName).parameterizedBy(*argumentList.toTypedArray())
        }
      }
    }

    receiver(finalType.copy(nullable = nullable))
  }
}

internal fun KotlinClassMetadata.Class.readClassData(): KmClass {
  @Suppress("RedundantExplicitType")
  var classFlags: Flags = 0
  lateinit var className: String
  val constructors = mutableListOf<KmConstructor>()
  var companionObjectName: String? = null
  val typeParameters = LinkedHashMap<Int, TypeVariableName>()
  val typeParamResolver = { id: Int -> typeParameters[id]!! }
  val superTypes = mutableListOf<TypeName>()
  val properties = mutableListOf<KmProperty>()
  accept(object : KmClassVisitor() {
    override fun visit(flags: Flags, name: kotlinx.metadata.ClassName) {
      super.visit(flags, name)
      className = name
      classFlags = flags
    }

    override fun visitTypeParameter(flags: Flags,
        name: String,
        id: Int,
        variance: KmVariance): KmTypeParameterVisitor? {
      return object : KmTypeParameterVisitor() {
        val upperBoundList = mutableListOf<TypeName>()
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
          typeParameters[id] = typeVariableName.copy(reified = flags.isReifiedTypeParameter)
        }
      }
    }

    override fun visitCompanionObject(name: String) {
      companionObjectName = name
    }

    override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
      return object : KmConstructorVisitor() {
        val params = mutableListOf<KmParameter>()
        override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
          val parameterFlags = flags
          return object : KmValueParameterVisitor() {
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
              params += KmParameter(parameterFlags, name, type, isVarArg, varargElementType)
            }
          }
        }

        override fun visitEnd() {
          constructors += KmConstructor(flags, params)
        }
      }
    }

    override fun visitSupertype(flags: Flags): KmTypeVisitor? {
      return TypeNameKmTypeVisitor(flags, typeParamResolver) {
        superTypes += it
      }
    }

    override fun visitProperty(flags: Flags,
        name: String,
        getterFlags: Flags,
        setterFlags: Flags): KmPropertyVisitor? {
      return object : KmPropertyVisitor() {
        lateinit var type: TypeName
        override fun visitEnd() {
          properties += KmProperty(flags, name, type)
        }

        override fun visitReturnType(flags: Flags): KmTypeVisitor? {
          return TypeNameKmTypeVisitor(flags, typeParamResolver) {
            type = it
          }
        }
      }
    }
  })

  return KmClass(className.replace("/", "."),
      classFlags,
      companionObjectName,
      constructors,
      superTypes,
      typeParameters.values.toList(),
      properties)
}

internal data class KmClass(
    val name: String,
    override val flags: Flags,
    val companionObjectName: String?,
    val constructors: List<KmConstructor>,
    val superTypes: MutableList<TypeName>,
    val typeVariables: List<TypeVariableName>,
    val kmProperties: List<KmProperty>
) : KmCommon {
  fun getPropertyForAnnotationHolder(methodElement: ExecutableElement): KmProperty? {
    return methodElement.simpleName.toString()
        .takeIf { it.endsWith(KOTLIN_PROPERTY_ANNOTATIONS_FUN_SUFFIX) }
        ?.substringBefore(KOTLIN_PROPERTY_ANNOTATIONS_FUN_SUFFIX)
        ?.let { propertyName -> kmProperties.firstOrNull { propertyName == it.name } }
  }

  companion object {
    /**
     * Postfix of the method name containing the [kotlin.Metadata] annotation for the relative property.
     * @see [getPropertyForAnnotationHolder]
     */
    const val KOTLIN_PROPERTY_ANNOTATIONS_FUN_SUFFIX = "\$annotations"
  }
}

internal data class KmConstructor(
    override val flags: Flags,
    val kmParameters: List<KmParameter>
) : KmCommon

internal data class KmParameter(
    override val flags: Flags,
    val name: String,
    val type: TypeName,
    val isVarArg: Boolean = false,
    val varargElementType: TypeName? = null
) : KmCommon

internal data class KmProperty(
    override val flags: Flags,
    val name: String,
    val type: TypeName
) : KmCommon

interface KmCommon {
  val flags: Flags
}
