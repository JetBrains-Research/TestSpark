# Task 03 — Remove deprecated `instrumentationTools()`

**Goal:** Delete every `instrumentationTools()` call. This helper was removed in
later 2.x releases of the IntelliJ Platform Gradle Plugin; the required
instrumentation dependencies are now resolved automatically. Removing it now
(while still on plugin 2.1.0, where it is optional and harmless) makes the
plugin bump in task 04 clean.

**Risk:** Low. The call is a no-op-equivalent convenience helper; removing it
does not change instrumentation behavior on 2.x.

## Files to change (remove the single `instrumentationTools()` line in each)

- `build.gradle.kts` — inside `dependencies { intellijPlatform { ... } }` (~line 147)
- `java/build.gradle.kts` — (~line 20)
- `kotlin/build.gradle.kts` — (~line 22)
- `langwrappers/build.gradle.kts` — (~line 20)

Confirm the exact set first:
```
grep -rn "instrumentationTools" --include=*.kts .
```
Remove **only** those lines. Leave `pluginVerifier()`, `zipSigner()`,
`testFramework(...)`, and `bundledPlugins(...)` untouched.

## Steps

1. Run the grep above and delete each matching line.
2. Re-run the grep — expect zero matches.

## Verify

```
j21
./gradlew clean compileKotlin --console=plain
```
- `BUILD SUCCESSFUL`, no "unresolved reference: instrumentationTools" and no new
  warnings about it.

## Rollback

Re-add `instrumentationTools()` inside each `intellijPlatform { }` dependencies
block.

## Done when

No `instrumentationTools()` remains and the project still compiles.
Commit: `chore: remove deprecated instrumentationTools() (task 03)`.
