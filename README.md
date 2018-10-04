CopyDynamic
===========

[![Build Status](https://travis-ci.org/hzsweers/copydynamic.svg?branch=master)](https://travis-ci.org/hzsweers/copydynamic)

Prototype of generating `copyDynamic` extension functions for data classes, such that you can do this:

```kotlin
@CopyDynamic
data class Foo(val bar: String = "bar", val baz: String = "baz", val fizz: String = "fizz")
```

And have a `copyDynamic` extension function generated to allow for dynamic setting of variables in a copy call.

```kotlin
val foo = Foo()

val newFoo = foo.copyDynamic {
  bar = "newBar"
  if (someCondition) baz = "newBaz"
}
```

**Configuration**: You can optionally specify a `copydynamic.generated` annotation processing option 
with a value of either `"javax.annotation.processing.Generated"` (JDK 9+) or `"javax.annotation.Generated"` (JDK <9).

```gradle
kapt {
  arguments {
    arg("copydynamic.generated", "javax.annotation.Generated")
  }
}
```

**Caveats**
* The generated intermediate builder has `internal` visibility for its constructor, which can be considered a bit of a leaky API. If you use this, it's recommended to put 
your models in a separate module to avoid leaking this.
* Properties must be `internal` or `public` visibility.

Download
--------

[![Maven Central](https://img.shields.io/maven-central/v/io.sweers.copydynamic/copydynamic.svg)](https://mvnrepository.com/artifact/io.sweers.copydynamic/copydynamic)
```gradle
kapt 'io.sweers.copydynamic:copydynamic:x.y.z'
compileOnly 'io.sweers.copydynamic:copydynamic-annotations:x.y.z'
```

KDocs can be found here: https://hzsweers.github.io/copydynamic/0.x

License
-------

    Copyright (C) 2018 Zac Sweers

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
