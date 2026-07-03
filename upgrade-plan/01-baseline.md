# Task 01 — Establish a green baseline & confirm JDKs

**Goal:** Before changing anything, prove the project currently builds so that
any later breakage is clearly attributable to your changes. Also confirm the
JDK-switching commands work.

**Risk:** None (no code changes).

## Prerequisites

- A clean checkout is already present, no need to do it any more.

## Steps

1. Confirm the three JDKs are available:
   ```
   j17 && java -version    # expect: 17.x
   j21 && java -version    # expect: 21.x
   j25 && java -version    # expect: 25.x
   ```
   If any command is missing, stop and report it.

2. The project currently targets Java 17 (`gradle.properties` `javaVersion=17`).
   Select JDK 17 for the baseline build:
   ```
   j17
   ```

3. Read back what Gradle sees (sanity check of the values in the plan):
   ```
   ./gradlew properties --console=plain -q | grep -E "^(version|pluginName):"
   ```

4. Compile the project (compilation is enough for a baseline; a full `build`
   also runs the EvoSuite download + UI test wiring):
   ```
   ./gradlew clean compileKotlin --console=plain
   ```

5. Run the unit tests to record a known-good baseline:
   ```
   ./gradlew test --console=plain
   ```

## Verify

- `./gradlew clean compileKotlin` completes with `BUILD SUCCESSFUL`.
- `./gradlew test` completes with `BUILD SUCCESSFUL` (note any pre-existing
  failures — they are NOT yours to fix in this upgrade, but record them so you
  can tell new failures apart later).

## Record

Write down (in the PR description or a scratch note):
- Gradle version reported, current `pluginVersion`.
- Whether `test` was fully green, and if not, which tests already failed.

## Done when

You have a reproducible, documented green (or known-state) baseline on your
feature branch. Commit nothing yet — no files changed.
