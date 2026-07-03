# TestSpark — IntelliJ 2026.1 Support Upgrade Plan

Goal: extend TestSpark (currently supporting IntelliJ up to 2025.3) to also
support the 2026.1 release. **Planning only — no code changed yet.**

## Current state

| Setting | Location | Current value |
|---|---|---|
| `pluginSinceBuild` | `gradle.properties:14` | `242` |
| `pluginUntilBuild` | `gradle.properties:15` | `253.*` (2025.3) |
| `platformVersion` (compile-against) | `gradle.properties:19` | `2024.3` |
| `platformType` | `gradle.properties:18` | `IC` |
| `javaVersion` / `jvmToolchainVersion` | `gradle.properties:26,36` | `17` |
| IntelliJ Platform Gradle Plugin | `build.gradle.kts:30` | `2.1.0` |
| Kotlin JVM plugin | `build.gradle.kts:28` | `2.1.0` |
| Gradle | `gradle.properties:29` + wrapper | `8.14` |

`platformVersion`/`platformType` propagate into the subprojects (`core` has no
platform dependency; `java`, `kotlin`, `langwrappers` each call `create(...)`
from the root properties), and `pluginUntilBuild` is reused in the
`pluginVerification` block (`build.gradle.kts:258-259`).

## Key external facts (verified)

- **2026.1 = build branch `261`** (EAP was `261.17801.55`). `pluginUntilBuild`
  must become `261.*`.
- **2025.1+ requires Java 21** for source/target bytecode and a JDK 21 build
  JDK. This is the biggest change — TestSpark is on Java 17.
- **`instrumentationTools()` was removed** in later 2.x releases of the Gradle
  plugin. It is called in all four platform-using build files and must be
  deleted when the plugin is bumped.
- **2026.1 extracted several bundled plugins into bundled modules** (own
  classloaders); some `bundledPlugin(...)` may need to become
  `bundledModule(...)`. Must be confirmed at build time.

## Proposed steps

### 1. Bump the toolchain to Java 21 (`gradle.properties`)
- `javaVersion = 21`, `jvmToolchainVersion = 21`. Flows to all modules via
  `jvmToolchain(...)` and the root `JavaCompile`/`KotlinCompile` config.
  Requires a JDK 21 to build.
- Note: EvoSuite/Kex *target-project* Java support is unrelated — that is the
  analyzed project's JDK, not the plugin's build JDK.

### 2. Bump the IntelliJ Platform Gradle Plugin (`build.gradle.kts:30`)
- From `2.1.0` to a current 2.x (e.g. `2.15.0`+).
- Consider bumping the Kotlin JVM plugin (`build.gradle.kts:28`) to align with
  2026.1's bundled Kotlin.

### 3. Remove `instrumentationTools()`
- `build.gradle.kts:147`, `java/build.gradle.kts:20`,
  `kotlin/build.gradle.kts:22`, `langwrappers/build.gradle.kts:20`.
- Removed in newer 2.x; no replacement needed.

### 4. Update the platform version & build range (`gradle.properties`)
- `platformVersion = 2026.1`, `pluginUntilBuild = 261.*`.
- Decide on `pluginSinceBuild`: keep `242` to retain broad compatibility (the
  verifier will flag any newer-API usage), or raise it if dropping old IDEs.

### 5. Verify Gradle / foojay compatibility
- Gradle 8.14 is likely fine for a current 2.x plugin, but confirm the minimum
  Gradle version the chosen plugin release requires (bump `gradleVersion` +
  wrapper if needed).
- Consider bumping `foojay-resolver-convention` (`settings.gradle.kts:2`,
  currently `0.5.0`) for reliable JBR-21 toolchain resolution.

### 6. Fix compilation / API breakages
- Building against `2026.1` after `2024.3` crosses the entire 2025.*
  incompatible-changes set. Compile each module and resolve removed/changed
  APIs; convert any affected `bundledPlugin(...)` to `bundledModule(...)` as the
  compiler/verifier demands.
- Can only be fully enumerated by an actual build.

### 7. Update metadata
- `pluginVersion` bump in `gradle.properties` (e.g. `0.4.3` or `0.5.0`).
- Add a `<change-notes>` entry in `plugin.xml:38` ("Support IDE 261.*") and a
  matching `CHANGELOG.md` entry.

### 8. Update CI (`.github/workflows/`)
- `build.yml` / `release.yml` set up Java 17 → change to **21**.
- `run-ui-tests.yml` uses Java 11 (Linux/macOS) and 17 (Windows) → align to
  **21**.

### 9. Validate
- `./gradlew clean test verifyPlugin buildPlugin`.
- Confirm the verifier passes against `261` and the plugin loads in a 2026.1 IDE
  (`runIde`).

## Recommended sequencing
Steps 1–4 are the coordinated version bumps (do together), then iterate on step
6 (build → fix → repeat) since it only surfaces at compile/verify time.
Metadata (7) and CI (8) are mechanical once the build is green.

Main risk concentrates in **step 6** (API breakage across 2024.3 → 2026.1) and
**step 1** (Java 21 migration touching the whole toolchain and CI).

## Sources
- [Incompatible Changes in IntelliJ Platform and Plugins API 2025.*](https://plugins.jetbrains.com/docs/intellij/api-changes-list-2025.html)
- [IntelliJ IDEA 2026.1 EAP 1 (261.17801.55) Release Notes](https://youtrack.jetbrains.com/articles/IDEA-A-2100662609/IntelliJ-IDEA-2026.1-EAP-1-261.17801.55-build-Release-Notes)
- [IntelliJ Platform Gradle Plugin (2.x)](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)
- [Dependencies Extension (bundledModule, instrumentationTools removal)](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html)
- [Creating a Plugin Gradle Project (Java 21 for 2025.1+)](https://plugins.jetbrains.com/docs/intellij/creating-plugin-project.html)
