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

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.tag

fun ImmutableKmClass.asTypeSpec(): TypeSpec {
  // Fill the parametersMap. Need to do sequentially and allow for referencing previously defined params
  val parametersMap = mutableMapOf<Int, TypeName>()
  val typeParamResolver = { id: Int -> parametersMap.getValue(id) }
  typeParameters.forEach { parametersMap[it.id] = it.asTypeVariableName(typeParamResolver) }

  val simpleName = name.substringAfterLast(".")
  val builder = when {
    isAnnotation -> TypeSpec.annotationBuilder(simpleName)
    isCompanionObject -> TypeSpec.companionObjectBuilder(simpleName)
    isEnum -> TypeSpec.enumBuilder(simpleName)
    isExpect -> TypeSpec.expectClassBuilder(simpleName)
    isObject -> TypeSpec.objectBuilder(simpleName)
    isInterface -> TypeSpec.interfaceBuilder(simpleName)
    else -> TypeSpec.classBuilder(simpleName)
  }
  builder.addModifiers(flags.visibility)
  builder.addModifiers(*flags.modalities
      .filterNot { it == KModifier.FINAL } // Default
      .toTypedArray()
  )
  if (isData) {
    builder.addModifiers(KModifier.DATA)
  }
  if (isExternal) {
    builder.addModifiers(KModifier.EXTERNAL)
  }
  if (isInline) {
    builder.addModifiers(KModifier.INLINE)
  }
  if (isInner) {
    builder.addModifiers(KModifier.INNER)
  }
  if (isEnumEntry) {
    // TODO handle typespec arg for complex enums
    enumEntries.forEach {
      builder.addEnumConstant(it)
    }
  }

  builder.addTypeVariables(typeParameters.map { it.asTypeVariableName(typeParamResolver) })
  supertypes.first().asTypeName(typeParamResolver).takeIf { it != ANY }?.let(builder::superclass)
  builder.addSuperinterfaces(supertypes.drop(1).map { it.asTypeName(typeParamResolver) })
  builder.addProperties(properties.map { it.asPropertySpec(typeParamResolver) })
  primaryConstructor?.takeIf { it.valueParameters.isNotEmpty() || flags.visibility != KModifier.PUBLIC }?.let {
    builder.primaryConstructor(it.asFunSpec(typeParamResolver))
  }
  constructors.filter { !it.isPrimary }.takeIf { it.isNotEmpty() }?.let { secondaryConstructors ->
    builder.addFunctions(secondaryConstructors.map { it.asFunSpec(typeParamResolver) })
  }
  companionObject?.let {
    builder.addType(TypeSpec.companionObjectBuilder(it).build())
  }
  builder.addFunctions(functions.map { it.asFunSpec(typeParamResolver) })

  return builder
      .tag(this)
      .build()
}

fun ImmutableKmConstructor.asFunSpec(
    typeParamResolver: ((index: Int) -> TypeName)
): FunSpec {
  return FunSpec.constructorBuilder()
      .apply {
        addModifiers(flags.visibility)
        addParameters(this@asFunSpec.valueParameters.map { it.asParameterSpec(typeParamResolver) })
      }
      .tag(this)
      .build()
}

fun ImmutableKmFunction.asFunSpec(
    typeParamResolver: ((index: Int) -> TypeName)
): FunSpec {
  return FunSpec.builder(name)
      .apply {
        addModifiers(flags.visibility)
        addParameters(this@asFunSpec.valueParameters.map { it.asParameterSpec(typeParamResolver) })
        if (isDeclaration) {
          // TODO
        }
        if (isFakeOverride) {
          addModifiers(KModifier.OVERRIDE)
        }
        if (isDelegation) {
          // TODO
        }
        if (isSynthesized) {
          addAnnotation(JvmSynthetic::class)
        }
        if (isOperator) {
          addModifiers(KModifier.OPERATOR)
        }
        if (isInfix) {
          addModifiers(KModifier.INFIX)
        }
        if (isInline) {
          addModifiers(KModifier.INLINE)
        }
        if (isTailRec) {
          addModifiers(KModifier.TAILREC)
        }
        if (isExternal) {
          addModifiers(KModifier.EXTERNAL)
        }
        if (isExpect) {
          addModifiers(KModifier.EXPECT)
        }
        if (isSuspend) {
          addModifiers(KModifier.SUSPEND)
        }
        val returnTypeName = returnType.asTypeName(typeParamResolver)
        if (returnTypeName != UNIT) {
          returns(returnTypeName)
          addStatement("TODO(\"Stub!\")")
        }
        receiverParameterType?.asTypeName(typeParamResolver)?.let { receiver(it) }
      }
      .tag(this)
      .build()
}

fun ImmutableKmValueParameter.asParameterSpec(
    typeParamResolver: ((index: Int) -> TypeName)
): ParameterSpec {
  val paramType = varargElementType ?: type ?: throw IllegalStateException("No argument type!")
  return ParameterSpec.builder(name, paramType.asTypeName(typeParamResolver))
      .apply {
        if (varargElementType != null) {
          addModifiers(KModifier.VARARG)
        }
        if (isCrossInline) {
          addModifiers(KModifier.CROSSINLINE)
        }
        if (isNoInline) {
          addModifiers(KModifier.NOINLINE)
        }
        if (declaresDefaultValue) {
          defaultValue("TODO(\"Stub!\")")
        }
      }
      .tag(this)
      .build()
}

fun ImmutableKmProperty.asPropertySpec(
    typeParamResolver: ((index: Int) -> TypeName)
) = PropertySpec.builder(name, returnType.asTypeName(typeParamResolver))
    .apply {
      addModifiers(flags.visibility)
      addModifiers(*flags.modalities
          .filterNot { it == KModifier.FINAL && !isOverride }
          .toTypedArray())
      if (isOverride) {
        addModifiers(KModifier.OVERRIDE)
      }
      if (isConst) {
        addModifiers(KModifier.CONST)
      }
      if (isVar) {
        mutable(true)
      } else if (isVal) {
        mutable(false)
      }
      if (isDelegated) {
        delegate("") // Placeholder
      }
      if (isExpect) {
        addModifiers(KModifier.EXPECT)
      }
      if (isExternal) {
        addModifiers(KModifier.EXTERNAL)
      }
      if (isLateinit) {
        addModifiers(KModifier.LATEINIT)
      }
      if (isSynthesized) {
        addAnnotation(JvmSynthetic::class)
      }
      if (hasGetter) {
        val visibility = setterFlags.visibility
        val modalities = setterFlags.modalities
            .filterNot { it == KModifier.FINAL && !flags.isOverrideProperty }
        val propertyAccessorFlags = setterFlags.propertyAccessorFlags
        if (visibility != KModifier.PUBLIC || modalities.isNotEmpty() || propertyAccessorFlags.isNotEmpty()) {
          getter(FunSpec.setterBuilder()
              .apply {
                addModifiers(visibility)
                addModifiers(*modalities.toTypedArray())
                addModifiers(*propertyAccessorFlags.toTypedArray())
              }
              .build())
        }
      }
      if (hasSetter) {
        val visibility = setterFlags.visibility
        val modalities = setterFlags.modalities
            .filterNot { it == KModifier.FINAL && !flags.isOverrideProperty }
        val propertyAccessorFlags = setterFlags.propertyAccessorFlags
        if (visibility != KModifier.PUBLIC || modalities.isNotEmpty() || propertyAccessorFlags.isNotEmpty()) {
          setter(FunSpec.setterBuilder()
              .apply {
                addModifiers(visibility)
                addModifiers(*modalities.toTypedArray())
                addModifiers(*propertyAccessorFlags.toTypedArray())
              }
              .build())
        }
      }
      // Available in tags
      //hasConstant
      //isDeclaration
      //isDelegation
    }
    .tag(this)
    .build()
