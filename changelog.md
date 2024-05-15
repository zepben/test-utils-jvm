# Test Utils JVM changelog

## [2.1.0] - UNRELEASED

### Breaking Changes

* None.

### New Features

* None.

### Enhancements

* None.

### Fixes

* Stopped `DefaultAnswer` from throwing `NullPointerException` for types that have not been supplied that can
  return `null`.

### Notes

* None.

## [2.0.0] - 2024-02-21

### Breaking Changes

* Update super pom to 0.34.x.
* Moved the Vert.x test utils into the vertx-utils library to remove a cyclic dependency.
* Updated remaining classes to Kotlin. Classes are the same, but accessors will have changed if you are in Kotlin.
* Removed redundant dependencies which will impact projects using these as transitive dependencies. Please import any
  missing dependencies directly.

### New Features

* None.

### Enhancements

* Added Kotlin helpers for `DefaultAnswer.of` and `DefaultAnswer.and` taking a type parameter rather than a class
  argument. e.g. `DefaultAnswer.of<Int>(100)` vs `DefaultAnswer.of(Int::class.java, 100)`

### Fixes

* None.

### Notes

* None.

---

## [1.2.0]

### Breaking Changes

* None.

### New Features

* None.

### Enhancements

* None.

### Fixes

* None.

### Notes

* None.

---

## [1.1.0]

### Breaking Changes

* None.

### New Features

* None.

### Enhancements

* None.

### Fixes

* None.

### Notes

* None.

---

## [1.0.0]

Initial release.
