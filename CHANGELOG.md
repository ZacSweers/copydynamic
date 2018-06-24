Changelog
=========

Version 0.1.3
----------------------------

_2018-6-23_

**Fix:** Generated code wouldn't compile due to `source` name collision if a property was also called `source`.

**Enhancement:** The AutoCommon and KotlinPoet dependencies are now shaded, since they are both not stable APIs. This was wonky to get working correctly, so please report any issues!

Version 0.1.2
----------------------------

_2018-6-18_

**Fix:** Read class information from the primary constructor parameters list rather than raw properties, as `copy` is based on the constructor parameters!

**Enhancement:** Fail eagerly with useful error messages if properties don't have required `public` or `internal` visibility.

Version 0.1.1
----------------------------

_2018-6-16_

**Fix:** Make the generated builder and extension function match the visibility of the target class.

Version 0.1.0
----------------------------

_2018-6-15_

Initial release!
