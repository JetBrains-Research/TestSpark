# TestSpark → IntelliJ 2026.1 — Task Breakdown

This folder splits [`../upgrade-plan.md`](../upgrade-plan.md) into small, ordered,
independently verifiable tasks. **Do them one after another, in order.** Each
task ends in a green build, so if something breaks you know which task caused it.

## Ground rules for every task

- **Work on a branch.** The repo default working branch is `development`.  The
  repo is now already in a working branch
  `stephanlukasczyk/chore/ij-2026-compatibility`, which shall be used.
- **Commit after each completed task** with a message naming the task
  (e.g. `chore: migrate build toolchain to Java 21 (task 02)`). This gives you a
  clean rollback point per task.
- **One task = one verification.** Do not start the next task until the current
  task's "Verify" section passes.
- **Ask before guessing.** If a step's expected result does not match reality
  (e.g. a file:line reference has drifted), stop and confirm rather than forcing
  a change.

## Switching JDKs on this machine

`JAVA_HOME` can be changed temporarily per shell:

| Command | JDK |
|---|---|
| `j17` | JDK 17 |
| `j21` | JDK 21 |
| `j25` | JDK 25 |

Run these **before** invoking `./gradlew` so Gradle uses the right JDK. Confirm
with `java -version` after switching.

## Tooling available

- **`./gradlew <task>`** — primary build/verify tool. Common tasks used here:
  `compileKotlin`, `test`, `verifyPlugin`, `buildPlugin`, `runIde`,
  `properties`, `dependencies`.
- **IntelliJ MCP server (`mcp__idea__*`)** — useful for inspecting the project
  without leaving the IDE:
  - `build_project` — build and get compile errors.
  - `get_file_problems` — inspection/compile problems for a specific file.
  - `get_project_dependencies` / `get_project_modules` — verify resolved
    platform version and modules.
  - `search_in_files_by_text` / `search_symbol` — locate API usages that break.
- **mcp-steroid server (`mcp__mcp-steroid__*`)** — drive/observe a running IDE
  (e.g. after `runIde`): `steroid_open_project`, `steroid_take_screenshot`,
  `steroid_list_windows` to confirm the plugin loads and the TestSpark tool
  window appears.

## Task list (do in order)

| # | Task | File |
|---|---|---|
| 01 | Establish a green baseline & confirm JDKs | [`01-baseline.md`](01-baseline.md) |
| 02 | Migrate the build toolchain to Java 21 | [`02-java-21-toolchain.md`](02-java-21-toolchain.md) |
| 03 | Remove deprecated `instrumentationTools()` | [`03-remove-instrumentationtools.md`](03-remove-instrumentationtools.md) |
| 04 | Bump the IntelliJ Platform Gradle Plugin (+ Gradle/foojay/Kotlin) | [`04-bump-gradle-plugin.md`](04-bump-gradle-plugin.md) |
| 05 | Switch compile target to platform 2026.1 & build range 261 | [`05-platform-2026.1.md`](05-platform-2026.1.md) |
| 06 | Resolve compilation & API-compatibility breakages | [`06-fix-api-breakages.md`](06-fix-api-breakages.md) |
| 07 | Update plugin metadata & changelog | [`07-metadata-changelog.md`](07-metadata-changelog.md) |
| 08 | Update CI workflows for Java 21 | [`08-ci-workflows.md`](08-ci-workflows.md) |
| 09 | Full validation & plugin verifier against 261 | [`09-final-validation.md`](09-final-validation.md) |

## Why this order

Tasks 02–04 are low-risk mechanical bumps, each verifiable while still compiling
against the *old* platform (2024.3). Task 05 flips to 2026.1 and is the point
where API breakages surface; task 06 fixes them iteratively. Metadata (07) and
CI (08) are mechanical once the build is green, and task 09 is the final gate.
