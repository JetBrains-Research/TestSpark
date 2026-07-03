# Task 04 — Bump the IntelliJ Platform Gradle Plugin (+ Gradle / foojay / Kotlin)

**Goal:** Upgrade the IntelliJ Platform Gradle Plugin from `2.1.0` to a current
2.x release that supports resolving and verifying against the 2026.1 (build 261)
platform, and bring Gradle, the foojay toolchain resolver, and the Kotlin JVM
plugin up to compatible versions. Still compiling against platform 2024.3 here —
that flip happens in task 05.

**Risk:** Medium. A newer Gradle plugin can require a newer Gradle version and
can rename/remove DSL. Task 03 already removed the most common breaker
(`instrumentationTools()`).

## Research first (do not hardcode a stale version)

1. Find the current stable IntelliJ Platform Gradle Plugin 2.x version on the
   [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.jetbrains.intellij.platform)
   or the [GitHub releases](https://github.com/JetBrains/intellij-platform-gradle-plugin/releases).
   Note its **minimum required Gradle version** (in the release notes).
2. Note the Kotlin version bundled with IntelliJ 2026.1 so the `kotlin.jvm`
   plugin can be aligned (Kotlin `2.1.x`/`2.2.x` range is expected).

## Files to change

- `build.gradle.kts`
  - Line 30: `id("org.jetbrains.intellij.platform") version "2.1.0"` →
    chosen current 2.x version.
  - Line 28: `id("org.jetbrains.kotlin.jvm") version "2.1.0"` → aligned Kotlin
    version (only if needed for compatibility).
  - Line 32 has a commented-out `org.jetbrains.intellij.platform.migration`
    plugin. That migration plugin was removed in plugin 2.12.0 — since it is
    only a comment, leave it or delete the comment; do **not** enable it.
- `gradle.properties`
  - Line 29: `gradleVersion = 8.14` → bump **only if** the chosen plugin
    requires a newer Gradle.
- `gradle/wrapper/gradle-wrapper.properties`
  - Line 3 `distributionUrl` → must match `gradleVersion` above.
- `settings.gradle.kts`
  - Line 2: `org.gradle.toolchains.foojay-resolver-convention` `0.5.0` →
    consider bumping to a current release for reliable JDK/JBR 21 resolution.

## Steps

1. Update the plugin version(s) in `build.gradle.kts`.
2. If Gradle must be bumped: update `gradleVersion` in `gradle.properties`, then
   regenerate the wrapper:
   ```
   ./gradlew wrapper --gradle-version <new-version>
   ```
   (The `wrapper` task reads `gradleVersion`; passing `--gradle-version` is a
   belt-and-braces way to also rewrite `gradle-wrapper.properties`.)
3. Bump the foojay resolver in `settings.gradle.kts` if needed.
4. Trigger a Gradle sync / configuration and watch for removed-DSL errors.

## Verify

```
j21
./gradlew help --console=plain            # config phase resolves the new plugin
./gradlew clean compileKotlin --console=plain
./gradlew test --console=plain
```
- All `BUILD SUCCESSFUL`.
- If configuration fails with "unresolved reference" / "method not found" in the
  `intellijPlatform { }` DSL, consult the plugin's migration notes for the
  renamed API and fix. Common ones besides `instrumentationTools()`:
  `pluginVerification.ides.ide(...)`, `cachePath`, `useCustomCache` — none of
  which this project currently uses, but verify.

## Rollback

Revert the version strings and the wrapper properties; re-run `./gradlew help`.

## Done when

The project configures, compiles, and tests green on the new Gradle plugin
version while still on platform 2024.3.
Commit: `build: bump IntelliJ Platform Gradle Plugin and toolchain (task 04)`.
