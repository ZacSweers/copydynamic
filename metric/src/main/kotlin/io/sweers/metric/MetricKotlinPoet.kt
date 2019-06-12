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
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.tag

fun KmClass.asTypeSpec(): TypeSpec {
  val simpleName = name.substringAfterLast(".")
  val builder = when {
    isAnnotation -> TypeSpec.annotationBuilder(simpleName)
    isCompanionObject -> TypeSpec.companionObjectBuilder(companionObjectName)
    isEnum -> TypeSpec.enumBuilder(simpleName)
    isExpect -> TypeSpec.expectClassBuilder(simpleName)
    isObject -> TypeSpec.objectBuilder(simpleName)
    isInterface -> TypeSpec.interfaceBuilder(simpleName)
    else -> TypeSpec.classBuilder(simpleName)
  }
  builder.addModifiers(visibility)
  builder.addModifiers(*modalities
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
//      TODO()
  }

  builder.addTypeVariables(typeVariables)
  superClass.takeIf { it != ANY }?.let(builder::superclass)
  builder.addSuperinterfaces(superInterfaces)
  builder.addProperties(properties.map(KmProperty::asPropertySpec))
  primaryConstructor?.takeIf { it.parameters.isNotEmpty() || it.visibility != KModifier.PUBLIC }?.let {
    builder.primaryConstructor(it.asFunSpec())
  }
  constructors.filter { !it.isPrimary }.takeIf { it.isNotEmpty() }?.let {
    builder.addFunctions(it.map(KmConstructor::asFunSpec))
  }
  companionObjectName?.let {
    builder.addType(TypeSpec.companionObjectBuilder(it).build())
  }
  builder.addFunctions(functions.map { it.asFunSpec() })

  return builder
      .tag(this)
      .build()
}

fun KmConstructor.asFunSpec(): FunSpec {
  return FunSpec.constructorBuilder()
      .apply {
        addModifiers(visibility)
        addParameters(this@asFunSpec.parameters.map { it.asParameterSpec() })
      }
      .tag(this)
      .build()
}

fun KmFunction.asFunSpec(): FunSpec {
  return FunSpec.builder(name)
      .apply {
        addModifiers(visibility)
        addParameters(this@asFunSpec.parameters.map { it.asParameterSpec() })
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
        if (returnType != UNIT) {
          returns(returnType)
          addStatement("TODO(\"Stub!\")")
        }
        receiverType?.let { receiver(it) }
      }
      .tag(this)
      .build()
}

fun KmParameter.asParameterSpec(): ParameterSpec {
  return ParameterSpec.builder(name, varargElementType ?: type)
      .apply {
        if (isVarArg) {
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

fun KmProperty.asPropertySpec() = PropertySpec.builder(name, type)
    .apply {
      addModifiers(visibility)
      addModifiers(*modalities
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
        getterResolver()?.let(::getter)
      }
      if (hasSetter) {
        setterResolver()?.let(::setter)
      }
      // Available in tags
      //hasConstant
      //isDeclaration
      //isDelegation
    }
    .tag(this)
    .build()
