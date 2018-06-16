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

package io.sweers.copydynamic.sample.test

import com.google.common.truth.Truth.assertThat
import io.sweers.copydynamic.annotations.CopyDynamic
import org.junit.Test

typealias AliasedBar = String

class CopyDynamicTest {

  @Test
  fun regular() {
    val foo = Foo()
    val someCondition = true
    val newFoo = foo.copyDynamic {
      bar = "newBar"
      if (someCondition) baz = "newBaz"
    }
    assertThat(newFoo).isEqualTo(Foo(fizz = "fizz", bar = "newBar", baz = "newBaz"))
  }

  @CopyDynamic
  data class Foo(val bar: AliasedBar = "bar", val baz: String = "baz", val fizz: String = "fizz")

  @Test
  fun generics() {
    val fooGeneric = FooGeneric(fizz = "fizz")
    val someCondition = true
    val newFooGeneric = fooGeneric.copyDynamic {
      bar = "newBar"
      if (someCondition) baz = "newBaz"
    }

    assertThat(newFooGeneric).isEqualTo(FooGeneric(fizz = "fizz", bar = "newBar", baz = "newBaz"))
  }

  @CopyDynamic
  data class FooGeneric<T>(val bar: String = "bar", val baz: String = "baz", val fizz: T)
}


