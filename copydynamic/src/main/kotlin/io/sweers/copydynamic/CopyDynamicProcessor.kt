/*
 * Copyright (c) 2018 Zac Sweers
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

package io.sweers.copydynamic

import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import io.sweers.copydynamic.CopyDynamicProcessor.Companion.OPTION_GENERATED
import io.sweers.copydynamic.annotations.CopyDynamic
import io.sweers.copydynamic.metadata.KmClass
import io.sweers.copydynamic.metadata.readClassData
import io.sweers.copydynamic.metadata.readKotlinClassMetadata
import io.sweers.copydynamic.metadata.readMetadata
import kotlinx.metadata.jvm.KotlinClassMetadata
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@SupportedOptions(OPTION_GENERATED)
@AutoService(Processor::class)
class CopyDynamicProcessor : AbstractProcessor() {

  companion object {
    /**
     * This annotation processing argument can be specified to have a `@Generated` annotation
     * included in the generated code. It is not encouraged unless you need it for static analysis
     * reasons and not enabled by default.
     *
     * Note that this can only be one of the following values:
     *   * `"javax.annotation.processing.Generated"` (JRE 9+)
     *   * `"javax.annotation.Generated"` (JRE <9)
     */
    const val OPTION_GENERATED = "copydynamic.generated"
    private val POSSIBLE_GENERATED_NAMES = setOf(
        "javax.annotation.processing.Generated",
        "javax.annotation.Generated"
    )
    private val ALLOWABLE_PROPERTY_VISIBILITY = setOf(INTERNAL, PUBLIC)
  }

  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var options: Map<String, String>
  private var generatedAnnotation: AnnotationSpec? = null

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    filer = processingEnv.filer
    messager = processingEnv.messager
    elements = processingEnv.elementUtils
    types = processingEnv.typeUtils
    options = processingEnv.options
    generatedAnnotation = options[OPTION_GENERATED]?.let {
      require(it in POSSIBLE_GENERATED_NAMES) {
        "Invalid option value for $OPTION_GENERATED. Found $it, allowable values are $POSSIBLE_GENERATED_NAMES."
      }
      elements.getTypeElement(it)
    }?.let {
      AnnotationSpec.builder(it.asClassName())
          .addMember("value = [%S]", CopyDynamicProcessor::class.java.canonicalName)
          .addMember("comments = %S", "https://github.com/hzsweers/copydynamic")
          .build()
    }
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
  }

  override fun getSupportedAnnotationTypes(): Set<String> {
    return setOf(CopyDynamic::class.java.canonicalName)
  }

  override fun process(annotations: Set<TypeElement>,
      roundEnv: RoundEnvironment): Boolean {

    roundEnv.getElementsAnnotatedWith(CopyDynamic::class.java)
        .asSequence()
        .map { it as TypeElement }
        .associate { it to (it.readMetadata()?.readKotlinClassMetadata() ?: throw IllegalStateException("CopyDynamic can only be applied to Kotlin classes!")) }
        .filterValues { it is KotlinClassMetadata.Class }
        .mapValues { (_, value) -> (value as KotlinClassMetadata.Class).readClassData() }
        .forEach { (element, classData) ->
          createType(element, classData)
        }

    return true
  }

  private fun createType(element: TypeElement,
      classData: KmClass ) {

    // Find the primary constructor
    val constructor = classData.primaryConstructor
    if (constructor == null) {
      messager.printMessage(
          ERROR, "@CopyDynamic can't be applied to $element: must have a primary constructor",
          element)
      return
    }

    val propertiesByName = classData.properties.associateBy { it.name }
    val parametersByName = constructor.parameters.associate {
      it.name to it
    }

    // Make sure parameters are public or internal
    if (parametersByName.keys.any { propertiesByName[it]?.visibility !in ALLOWABLE_PROPERTY_VISIBILITY }) {
      messager.printMessage(
          ERROR,
          "@CopyDynamic can't be applied to $element: constructor properties must have public or internal visibility",
          element)
      return
    }

    // Create a NameAllocator and fill its cache with existing parameter names so we don't collide
    // with our own parameter name
    val nameAllocator = NameAllocator()
    parametersByName.keys.forEach { nameAllocator.newName(it) }

    val packageName = MoreElements.getPackage(element).toString()
    val builderName = "${element.simpleName}DynamicBuilder"
    val typeParams = classData.typeVariables
    val sourceType = element.asClassName().let {
      if (typeParams.isNotEmpty()) {
        it.parameterizedBy(*(typeParams.toTypedArray()))
      } else {
        it
      }
    }
    val visibility = classData.visibility
    val sourceParam = ParameterSpec.builder(nameAllocator.newName("source"), sourceType).build()
    val properties = mutableListOf<Pair<String, PropertySpec>>()
    val builderSpec = TypeSpec.classBuilder(builderName)
        .addOriginatingElement(element)
        .apply {
          generatedAnnotation?.let(::addAnnotation)
          if (visibility != PUBLIC) {
            addModifiers(visibility)
          }
          if (typeParams.isNotEmpty()) {
            addTypeVariables(typeParams)
          }
        }
        .primaryConstructor(FunSpec.constructorBuilder()
            .addModifiers(INTERNAL)
            .addParameter(sourceParam)
            .build())
        .addProperty(PropertySpec.builder(sourceParam.name, sourceType)
            .addModifiers(PRIVATE)
            .initializer("%N", sourceParam)
            .build())
        .apply {
          parametersByName.forEach { (name, parameter) ->
            addProperty(PropertySpec.builder(name,
                parameter.type)
                .mutable(true)
                .initializer("%N.%L", sourceParam, name)
                .build()
                .also { properties.add(name to it) }
            )
          }
        }
        .build()

    // Generate the extension fun
    val builderSpecKind = ClassName(packageName, builderName).let {
      if (typeParams.isNotEmpty()) {
        it.parameterizedBy(*(typeParams.toTypedArray()))
      } else {
        it
      }
    }
    val copyBlockParam = ParameterSpec.builder("copyBlock",
        LambdaTypeName.get(receiver = builderSpecKind,
            parameters = emptyList(),
            returnType = UNIT
        )).build()
    val copyCodeBlock = CodeBlock.of(
        "copy(${properties.joinToString(", ") { "${it.first} = %N" }})",
        *(properties.map(Pair<String, PropertySpec>::second).toTypedArray()))

    val extensionFun = FunSpec.builder("copyDynamic")
        .apply {
          generatedAnnotation?.let(::addAnnotation)
          if (visibility != PUBLIC) {
            addModifiers(visibility)
          }
          if (typeParams.isNotEmpty()) {
            addTypeVariables(typeParams)
          }
        }
        .receiver(sourceType)
        .returns(sourceType)
        .addParameter(copyBlockParam)
        .addStatement("return %T(this).also·{ %N(it) }.run·{ %L }", builderSpecKind, copyBlockParam, copyCodeBlock)
        .build()

    FileSpec.builder(packageName, builderName)
        .indent("  ")
        .addComment("Code generated by copydynamic. Do not edit.")
        .addType(builderSpec)
        .addFunction(extensionFun)
        .build()
        .writeTo(filer)
  }
}
