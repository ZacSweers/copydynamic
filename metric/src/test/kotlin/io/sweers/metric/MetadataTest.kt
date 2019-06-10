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

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.junit.Test

@Suppress("unused", "UNUSED_PARAMETER")
class MetadataTest {

  @Test
  fun constructorData() {
    val classData = ConstructorClass::class.readKmType()

    assertThat(classData.primaryConstructor).isNotNull()
    assertThat(classData.primaryConstructor?.parameters).hasSize(2)
    val fooParam = classData.primaryConstructor!!.parameters[0]
    assertThat(fooParam.name).isEqualTo("foo")
    assertThat(fooParam.type).isEqualTo(String::class.asClassName())
    assertThat(fooParam.modifiers).doesNotContain(KModifier.VARARG)
    val barParam = classData.primaryConstructor!!.parameters[1]
    assertThat(barParam.name).isEqualTo("bar")
    assertThat(barParam.modifiers).contains(KModifier.VARARG)
    assertThat(barParam.type).isEqualTo(INT)
  }

  class ConstructorClass(val foo: String, vararg bar: Int) {
    // Secondary constructors are ignored, so we expect this constructor to not be the one picked
    // up in the test.
    constructor(bar: Int) : this("defaultFoo")
  }

  @Test
  fun supertype() {
    val classData = Supertype::class.readKmType()

    assertThat(classData.superclass).isEqualTo(BaseType::class.asClassName())
    assertThat(classData.superinterfaces).containsKey(BaseInterface::class.asClassName())
  }

  abstract class BaseType
  interface BaseInterface
  class Supertype : BaseType(), BaseInterface

  @Test
  fun properties() {
    val classData = Properties::class.readKmType()

    assertThat(classData.propertySpecs).hasSize(4)

    val fooProp = classData.propertySpecs.find { it.name == "foo" } ?: throw AssertionError("Missing foo property")
    assertThat(fooProp.type).isEqualTo(String::class.asClassName())
    assertThat(fooProp.mutable).isFalse()
    val barProp = classData.propertySpecs.find { it.name == "bar" } ?: throw AssertionError("Missing bar property")
    assertThat(barProp.type).isEqualTo(String::class.asClassName().copy(nullable = true))
    assertThat(barProp.mutable).isFalse()
    val bazProp = classData.propertySpecs.find { it.name == "baz" } ?: throw AssertionError("Missing baz property")
    assertThat(bazProp.type).isEqualTo(Int::class.asClassName())
    assertThat(bazProp.mutable).isTrue()
    val listProp = classData.propertySpecs.find { it.name == "aList" } ?: throw AssertionError("Missing baz property")
    assertThat(listProp.type).isEqualTo(List::class.parameterizedBy(Int::class))
    assertThat(listProp.mutable).isTrue()
  }

  class Properties {
    val foo: String = ""
    val bar: String? = null
    var baz: Int = 0
    var aList: List<Int> = emptyList()
  }

  @Test
  fun companionObject() {
    val classData = CompanionObject::class.readKmType()
    assertThat(classData.typeSpecs).hasSize(1)
    val companionObject = classData.typeSpecs.find { it.isCompanion }
    checkNotNull(companionObject)
    assertThat(companionObject.name).isEqualTo("Companion")
  }

  class CompanionObject {
    companion object
  }

  @Test
  fun namedCompanionObject() {
    val classData = NamedCompanionObject::class.readKmType()
    assertThat(classData.typeSpecs).hasSize(1)
    val companionObject = classData.typeSpecs.find { it.isCompanion }
    checkNotNull(companionObject)
    assertThat(companionObject.name).isEqualTo("Named")
  }

  class NamedCompanionObject {
    companion object Named
  }

  @Test
  fun generics() {
    val classData = Generics::class.readKmType()

    assertThat(classData.typeVariables).hasSize(3)
    val tType = classData.typeVariables[0]
    assertThat(tType.name).isEqualTo("T")
    assertThat(tType.isReified).isFalse()
    assertThat(tType.variance).isNull() // we don't redeclare out variance
    val rType = classData.typeVariables[1]
    assertThat(rType.name).isEqualTo("R")
    assertThat(rType.isReified).isFalse()
    assertThat(rType.variance).isEqualTo(KModifier.IN)
    val vType = classData.typeVariables[2]
    assertThat(vType.name).isEqualTo("V")
    assertThat(vType.isReified).isFalse()
    assertThat(vType.variance).isNull() // invariance is routed to null

    assertThat(classData.propertySpecs).hasSize(1)
    assertThat(classData.primaryConstructor?.parameters).hasSize(1)

    val param = classData.primaryConstructor!!.parameters[0]
    val property = classData.propertySpecs[0]

    assertThat(param.type).isEqualTo(tType)
    assertThat(property.type).isEqualTo(tType)
  }

  class Generics<out T, in R, V>(val genericInput: T)

  @Test
  fun typeAliases() {
    val classData = TypeAliases::class.readKmType()

    assertThat(classData.primaryConstructor?.parameters).hasSize(2)

    val (param1, param2) = classData.primaryConstructor!!.parameters
    // We always resolve the underlying type of typealiases
    assertThat(param1.type).isEqualTo(String::class.asClassName())
    assertThat(param2.type).isEqualTo(List::class.parameterizedBy(String::class))
  }

  class TypeAliases(val foo: TypeAliasName, val bar: GenericTypeAlias)

  @Test
  fun propertyMutability() {
    val classData = PropertyMutability::class.readKmType()

    assertThat(classData.primaryConstructor?.parameters).hasSize(2)

    val fooProp = classData.propertySpecs.find { it.name == "foo" } ?: throw AssertionError("foo property not found!")
    val mutableFooProp = classData.propertySpecs.find { it.name == "mutableFoo" } ?: throw AssertionError("mutableFoo property not found!")
    assertThat(fooProp.mutable).isFalse()
    assertThat(mutableFooProp.mutable).isTrue()
  }

  class PropertyMutability(val foo: String, var mutableFoo: String)

  @Test
  fun collectionMutability() {
    val classData = CollectionMutability::class.readKmType()

    assertThat(classData.primaryConstructor?.parameters).hasSize(2)

    val (immutableProp, mutableListProp) = classData.primaryConstructor!!.parameters
    assertThat(immutableProp.type).isEqualTo(List::class.parameterizedBy(String::class))
    assertThat(mutableListProp.type).isEqualTo(ClassName.bestGuess("kotlin.collections.MutableList").parameterizedBy(String::class.asTypeName()))
  }

  class CollectionMutability(val immutableList: List<String>, val mutableList: MutableList<String>)

  @Test
  fun suspendTypes() {
    val classData = SuspendTypes::class.readKmType()
    //language=kotlin
    assertThat(classData.toString().trim()).isEqualTo("""
      class SuspendTypes {
          val testProp: suspend (kotlin.Int, kotlin.Long) -> kotlin.String
      }
    """.trimIndent())
    println(classData)
  }

  class SuspendTypes {
    val testProp: suspend (Int, Long) -> String = { _, _ -> "" }

    fun testFun(body: suspend (Int, Long) -> String) {

    }

    suspend fun testSuspendFun(param1: String) {

    }

    suspend fun testComplexSuspendFun(body: suspend (Int, suspend (Long) -> String) -> String) {

    }
  }
}

typealias TypeAliasName = String
typealias GenericTypeAlias = List<String>
