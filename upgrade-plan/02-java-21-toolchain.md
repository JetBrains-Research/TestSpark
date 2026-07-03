# Task 02 — Migrate the build toolchain to Java 21

**Goal:** Move the plugin's build/compile toolchain from Java 17 to Java 21.
IntelliJ Platform 2025.1+ (and therefore 2026.1) requires Java 21 bytecode.
Doing this *before* the platform bump keeps the change isolated and verifiable
against the current platform (2024.3 already runs on JBR 21).

**Risk:** Low. This is a version-string change plus using JDK 21 to build.

## Files to change

- `gradle.properties`
  - Line 26: `javaVersion = 17` → `javaVersion = 21`
  - Line 36: `jvmToolchainVersion = 17` → `jvmToolchainVersion = 21`

That's it. Both values fan out automatically:
- `javaVersion` drives `sourceCompatibility` / `targetCompatibility` and Kotlin
  `jvmTarget` in the root `build.gradle.kts` (the `properties("javaVersion").let { ... }`
  block, ~lines 300–308).
- `jvmToolchainVersion` drives `jvmToolchain(...)` in `core`, `java`, `kotlin`,
  and `langwrappers` build files.

## Steps

1. Edit the two lines in `gradle.properties` as above.
2. Search for any other hardcoded `17` build targets that do NOT come from these
   properties (there should be none, but verify):
   ```
   grep -rn "17" --include=*.kts --include=*.properties . | grep -iE "jvmTarget|sourceCompat|targetCompat|toolchain|languageVersion"
   ```
   The `JUnitRunner` module compiles code that is executed inside *target*
   projects — check its build file's Java level and leave it alone unless it
   explicitly references `javaVersion`. Do not lower any target-runtime Java
   level here; this task is only about the plugin's own build JDK.

## Verify

Build with JDK 21:
```
j21
java -version          # confirm 21.x
./gradlew clean compileKotlin --console=plain
./gradlew test --console=plain
```

- Both commands `BUILD SUCCESSFUL`.
- Optionally confirm the produced bytecode is 21: inspect a compiled class, or
  trust `targetCompatibility`. The IntelliJ MCP `build_project` can also be used
  to confirm no compile problems.

## Rollback

Revert the two lines in `gradle.properties`.

## Done when

Project compiles and tests pass under JDK 21, still against platform 2024.3.
Commit: `chore: migrate build toolchain to Java 21 (task 02)`.
