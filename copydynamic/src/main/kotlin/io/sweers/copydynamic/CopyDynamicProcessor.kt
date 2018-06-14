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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import io.sweers.copydynamic.annotations.CopyDynamic
import kotlinx.metadata.impl.extensions.MetadataExtensions
import kotlinx.metadata.jvm.KotlinClassMetadata.Class
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@AutoService(Processor::class)
class CopyDynamicProcessor : AbstractProcessor() {

  // Workaround for https://youtrack.jetbrains.com/issue/KT-24881
  companion object {
    init {
      with(Thread.currentThread()) {
        val classLoader = contextClassLoader
        contextClassLoader = MetadataExtensions::class.java.classLoader
        try {
          MetadataExtensions.INSTANCES
        } finally {
          contextClassLoader = classLoader
        }
      }
    }
  }

  private lateinit var filer: Filer
  private lateinit var messager: Messager
  private lateinit var elements: Elements
  private lateinit var types: Types
  private lateinit var options: Map<String, String>
  private lateinit var outputDir: File

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    filer = processingEnv.filer
    messager = processingEnv.messager
    elements = processingEnv.elementUtils
    types = processingEnv.typeUtils
    options = processingEnv.options
    outputDir = options["kapt.kotlin.generated"]?.let(::File) ?: throw IllegalStateException(
        "No kapt.kotlin.generated option provided")
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
        .associate {
          it to ((it.readMetadata()?.readKotlinClassMetadata() as? Class)?.readClassData()
              ?: throw IllegalArgumentException("$it is not a kotlin class!"))
        }
        .forEach(this::createType)

    return true
  }

  private fun createType(element: TypeElement, classData: ClassData) {
    val packageName = MoreElements.getPackage(element).toString()
    val builderName = "${element.simpleName}Builder"
    val sourceType = element.asClassName()
    val sourceParam = ParameterSpec.builder("source", sourceType).build()
    val properties = mutableListOf<Pair<PropertyData, PropertySpec>>()
    val builderSpec = TypeSpec.classBuilder(builderName)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addModifiers(PRIVATE)
            .addParameter(sourceParam)
            .build())
        .addProperty(PropertySpec.builder(sourceParam.name, sourceType)
            .addModifiers(PRIVATE)
            .initializer("%N", sourceParam)
            .build())
        .apply {
          classData.properties.forEach { property ->
            addProperty(PropertySpec.varBuilder(property.name, property.type)
                .initializer("%N.%L", sourceParam, property.name)
                .build()
                .also { properties.add(property to it) }
            )
          }
        }
        .addFunction(FunSpec.builder("build")
            .addModifiers(PRIVATE)
            .returns(sourceType)
            .addStatement(
                "return %N.copy(${properties.joinToString(", ") { "${it.first.name} = %N" }})",
                sourceParam,
                *(properties.map { it.second }.toTypedArray()))
            .build())
        .build()

    // Generate the extension fun
    val builderSpecKind = ClassName(packageName, builderName)
    val copyBlockParam = ParameterSpec.builder("copyBlock",
        LambdaTypeName.get(receiver = builderSpecKind,
            parameters = emptyList(),
            returnType = UNIT
        )).build()
    val extensionFun = FunSpec.builder("copyDynamic")
        .receiver(sourceType)
        .returns(sourceType)
        .addParameter(copyBlockParam)
        .addStatement("return %T(this).also { %N(it) }.build()", builderSpecKind, copyBlockParam)
        .build()

    FileSpec.builder(packageName, builderName)
        .addType(builderSpec)
        .addFunction(extensionFun)
        .build()
        .writeTo(outputDir)
  }
}
