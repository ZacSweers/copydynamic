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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import io.sweers.copydynamic.CopyDynamicProcessor.Companion.OPTION_GENERATED
import io.sweers.copydynamic.annotations.CopyDynamic
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadata
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.visibility
import java.io.File
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
  }

  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var options: Map<String, String>
  private lateinit var outputDir: File
  private var generatedAnnotation: AnnotationSpec? = null

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    filer = processingEnv.filer
    messager = processingEnv.messager
    elements = processingEnv.elementUtils
    types = processingEnv.typeUtils
    options = processingEnv.options
    outputDir = options["kapt.kotlin.generated"]?.let(::File) ?: throw IllegalStateException(
        "No kapt.kotlin.generated option provided")
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
        .forEach { element ->
          val typeMetadata: KotlinMetadata? = element.kotlinMetadata
          if (typeMetadata !is KotlinClassMetadata) {
            messager.printMessage(
                ERROR, "@CopyDynamic can't be applied to $element: must be a Kotlin class", element)
            return@forEach
          }

          val proto = typeMetadata.data.classProto
          if (!proto.isDataClass) {
            messager.printMessage(
                ERROR, "@CopyDynamic can't be applied to $element: must be a data class", element)
            return@forEach
          }
          val nameResolver = typeMetadata.data.nameResolver
          createType(element, proto, nameResolver)
        }

    return true
  }

  private fun createType(element: TypeElement, classData: ProtoBuf.Class,
      nameResolver: NameResolver) {
    val packageName = MoreElements.getPackage(element).toString()
    val builderName = "${element.simpleName}DynamicBuilder"
    val typeParams = classData.typeParameterList
        .map { it.asTypeName(nameResolver, classData::getTypeParameter, true) }
    val sourceType = element.asClassName().let {
      if (typeParams.isNotEmpty()) {
        ParameterizedTypeName.get(it, *(typeParams.toTypedArray()))
      } else {
        it
      }
    }
    val visibility = classData.visibility?.asKModifier()
    val sourceParam = ParameterSpec.builder("source", sourceType).build()
    val properties = mutableListOf<Pair<String, PropertySpec>>()
    val builderSpec = TypeSpec.classBuilder(builderName)
        .apply {
          generatedAnnotation?.let(::addAnnotation)
          visibility?.let { addModifiers(it) }
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
          classData.propertyList.forEach { property ->
            val propertyName = nameResolver.getString(property.name)
            addProperty(PropertySpec.varBuilder(propertyName,
                property.returnType.asTypeName(nameResolver, classData::getTypeParameter, true))
                .initializer("%N.%L", sourceParam, propertyName)
                .build()
                .also { properties.add(propertyName to it) }
            )
          }
        }
        .addFunction(FunSpec.builder("build")
            .addModifiers(INTERNAL)
            .returns(sourceType)
            .addStatement(
                "return %N.copy(${properties.joinToString(", ") { "${it.first} = %N" }})",
                sourceParam,
                *(properties.map(Pair<String, PropertySpec>::second).toTypedArray()))
            .build())
        .build()

    // Generate the extension fun
    val builderSpecKind = ClassName(packageName, builderName).let {
      if (typeParams.isNotEmpty()) {
        ParameterizedTypeName.get(it, *(typeParams.toTypedArray()))
      } else {
        it
      }
    }
    val copyBlockParam = ParameterSpec.builder("copyBlock",
        LambdaTypeName.get(receiver = builderSpecKind,
            parameters = emptyList(),
            returnType = UNIT
        )).build()
    val extensionFun = FunSpec.builder("copyDynamic")
        .apply {
          generatedAnnotation?.let(::addAnnotation)
          visibility?.let { addModifiers(it) }
          if (typeParams.isNotEmpty()) {
            addTypeVariables(typeParams)
          }
        }
        .receiver(sourceType)
        .returns(sourceType)
        .addParameter(copyBlockParam)
        .addStatement("return %T(this).also { %N(it) }.build()", builderSpecKind, copyBlockParam)
        .build()

    FileSpec.builder(packageName, builderName)
        .indent("  ")
        .addComment("Code generated by copydynamic. Do not edit.")
        .addType(builderSpec)
        .addFunction(extensionFun)
        .build()
        .writeTo(outputDir)
  }
}
