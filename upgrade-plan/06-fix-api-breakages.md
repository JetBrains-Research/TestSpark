# Task 06 — Resolve compilation & API-compatibility breakages

**Goal:** Make the plugin compile and pass the plugin verifier against 2026.1,
fixing every API break introduced by crossing from platform 2024.3 (build 243)
to 2026.1 (build 261). This is an **iterative** task: build → read the first
error(s) → fix → rebuild, until green.

**Risk:** High and open-ended — the exact fixes cannot be known until the
compiler tells you. Work incrementally and keep changes minimal.

## Reference material

- [Incompatible Changes in IntelliJ Platform and Plugins API 2025.*](https://plugins.jetbrains.com/docs/intellij/api-changes-list-2025.html)
  — the authoritative list of removed/changed APIs across the 251–261 range.
  Search it for each symbol the compiler complains about.
- [Dependencies Extension — `bundledModule`](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html)

## The two categories of breakage

### A. Source compile errors (removed/renamed/relocated APIs)

1. Build and read errors from the top:
   ```
   j21
   ./gradlew clean compileKotlin --console=plain
   ```
   The IntelliJ MCP is very effective here:
   - `build_project` to get the structured error list.
   - `get_file_problems` on a specific file after editing.
   - `search_symbol` / `search_in_files_by_text` to find all usages of a
     removed API across modules.
2. For each error, look the symbol up in the 2025.* incompatible-changes page,
   apply the recommended replacement, and rebuild. Fix a few at a time and
   re-run — do not batch dozens of speculative edits.
3. Fix modules in dependency order if errors span several:
   `core` → `langwrappers` → `java` / `kotlin` → root module.

### B. `bundledPlugin` → `bundledModule` migrations

In 2026.1 several bundled plugins were extracted into separate bundled modules
with their own classloaders. If the verifier (or a runtime classloading error)
complains that API from such a module is not accessible, add an explicit
`bundledModule("<module-name>")` dependency instead of relying on the
`bundledPlugin(...)`.

- Current `platformPlugins` (in `gradle.properties:23`):
  `com.intellij.java, org.jetbrains.kotlin, org.jetbrains.idea.maven, com.intellij.gradle`.
- Current `bundledPlugins(...)` calls exist in `build.gradle.kts` and in the
  `java`/`kotlin`/`langwrappers` build files.
- Only convert an entry to `bundledModule(...)` if the verifier/compiler
  actually requires it — do not pre-emptively rewrite all of them. Record each
  conversion and why.

## Decision point: `pluginSinceBuild`

If the verifier reports that a currently-used API did not exist at build 242,
either:
- replace the usage with an older-compatible API, or
- raise `pluginSinceBuild` (task 05) and get team sign-off on dropping the older
  IDE support. Prefer the first option when feasible.

## Verify

```
j21
./gradlew clean compileKotlin --console=plain     # 0 errors
./gradlew test --console=plain                    # green (or same known baseline)
./gradlew verifyPlugin --console=plain            # verifier passes against 261
```
Inspect `build/reports/pluginVerifier` for any remaining
compatibility problems and resolve or explicitly justify each.

## Done when

Compilation, tests, and `verifyPlugin` all pass against platform 2026.1.
Commit incrementally, e.g. `fix: migrate <API> for platform 261 (task 06)`.
Squash or keep granular per team preference.
