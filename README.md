CopyDynamic
===========

PoC prototype of generating `copyDynamic` extension functions for data classes, such that you can do this:

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
