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

package io.sweers.classgentesting.sample;

import io.sweers.copydynamic.annotations.CopyDynamic;

@CopyDynamic
public final class SimpleClass {
  public SimpleClass() {

  }

  public static void main(String[] args) {
    // This will not compile if you uncomment it even though it links
    //SimpleClass instance = io.sweers.classgentesting.sample.SimpleClassFactory.create();
  }
}
