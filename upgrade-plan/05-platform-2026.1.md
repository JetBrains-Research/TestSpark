# Task 05 — Switch compile target to platform 2026.1 & build range 261

**Goal:** Point the plugin at the 2026.1 platform and declare compatibility with
build branch 261. This is the pivotal change; expect compile errors afterwards —
they are addressed in task 06.

**Risk:** High (surfaces API breakages). But the change itself is a few property
edits.

## Files to change

- `gradle.properties`
  - Line 15: `pluginUntilBuild = 253.*` → `pluginUntilBuild = 261.*`
  - Line 19: `platformVersion = 2024.3` → `platformVersion = 2026.1`
    (use the exact resolvable version, e.g. `2026.1` or a specific `2026.1.x`;
    verify it resolves — see below).
  - Line 14: `pluginSinceBuild = 242` — **decision required**:
    - Keep `242` to retain the widest compatibility. The plugin verifier will
      flag any API you use that did not exist in 242.
    - Raise it (e.g. to `243` or higher) only if the team decides to drop older
      IDEs. Default recommendation: **keep 242** unless task 06 forces a bump.

The `platformVersion`/`platformType` values propagate to `java`, `kotlin`, and
`langwrappers` via `rootProject.properties[...]`, and `pluginUntilBuild` also
feeds the `pluginVerification` block in `build.gradle.kts:258-259`. No other
edits are needed to move the target version.

## Steps

1. Make the three edits above (leaving `pluginSinceBuild` at `242` for now).
2. Confirm the platform version resolves before compiling:
   ```
   j21
   ./gradlew dependencies --configuration intellijPlatformDependency --console=plain | head -n 40
   ```
   or use the IntelliJ MCP `get_project_dependencies` to confirm the resolved
   IDE artifact is `IC-2026.1`. If it does not resolve, pick an exact published
   version from the JetBrains repository and retry.

## Verify

```
j21
./gradlew clean compileKotlin --console=plain
```

Two possible outcomes, both acceptable for *this* task:
- **BUILD SUCCESSFUL** — great, no source changes needed; jump ahead but still
  run task 06's verifier step.
- **Compile errors** referencing removed/changed platform APIs — expected.
  **Capture the full error list** (save the output) and proceed to task 06. Do
  not fix errors in this task; keep the version bump isolated in one commit.

## Rollback

Revert `platformVersion` and `pluginUntilBuild` to `2024.3` / `253.*`.

## Done when

`gradle.properties` targets `2026.1` / `261.*`, the platform artifact resolves,
and you have a recorded list of any compile errors to hand to task 06.
Commit (even if it does not yet compile — it is an isolated, intentional step):
`build: target IntelliJ platform 2026.1 / build 261 (task 05)`.
