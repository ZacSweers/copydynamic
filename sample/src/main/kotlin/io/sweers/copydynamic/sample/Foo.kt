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

@file:Suppress("UNUSED_VARIABLE", "unused", "ConstantConditionIf")

package io.sweers.copydynamic.sample

import io.sweers.copydynamic.annotations.CopyDynamic

typealias AliasedBar = String

@CopyDynamic
data class Foo(val bar: AliasedBar = "bar", val baz: String = "baz", val fizz: String = "fizz")

@CopyDynamic
data class FooGeneric<T>(val bar: String = "bar", val baz: String = "baz", val fizz: T)

fun example() {
  val foo = Foo()
  val someCondition = true
  val newFoo = foo.copyDynamic {
    bar = "newBar"
    if (someCondition) baz = "newBaz"
  }

  val fooGeneric = FooGeneric(fizz = "fizz")
  val newFooGeneric = fooGeneric.copyDynamic {
    bar = "newBar"
    if (someCondition) baz = "newBaz"
  }
}

fun main() {
  println("Ble")

  // Reflection works
  // FooDynamicBuilderBridge::class.java.methods[0].invoke(null, "", "", "", 0) works

  // Fails to compile if present due to unresolved reference FooDynamicBuilderBridge
  // This links partially out of the box. Will error with it not being found on classpath
  // Links fully if build/tmp/kapt3/classes are included to build.gradle's sourceSets
  FooDynamicBuilderBridge.constructorBridge("", "", "", 0)
}
