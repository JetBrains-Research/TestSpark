# Task 09 — Full validation & plugin verifier against 261

**Goal:** Prove end-to-end that the plugin builds, verifies, packages, and
actually loads and works in a 2026.1 IDE.

**Risk:** Low code risk; this is the acceptance gate.

## Steps & checks

1. **Clean full build with the release JDK:**
   ```
   j21
   ./gradlew clean test verifyPlugin buildPlugin --console=plain
   ```
   - `test`: green (or matches the known baseline from task 01).
   - `verifyPlugin`: no new compatibility problems against 261. Inspect
     `build/reports/pluginVerifier`.
   - `buildPlugin`: produces `build/distributions/TestSpark-<version>.zip`.

2. **Manual smoke test in a real 2026.1 IDE** via `runIde`:
   ```
   j21
   ./gradlew runIde --console=plain
   ```
   This launches a sandbox IDE on the 2026.1 platform. Then use the mcp-steroid
   MCP server to confirm the plugin loaded:
   - `steroid_list_windows` — find the sandbox IDE window.
   - `steroid_open_project` — open a small Java and a small Kotlin sample
     project.
   - `steroid_take_screenshot` — confirm the **TestSpark** tool window appears
     on the right and TestSpark settings exist under Settings → Tools.
   - Trigger a test-generation action on a simple class (LLM path needs a token;
     at minimum confirm the action/menu appears and the UI opens without
     errors). Check the IDE log for exceptions.

3. **Confirm the declared range** in the built plugin:
   - Unzip the artifact and open `TestSpark/lib/.../plugin.xml` (or check
     `build/patchedPluginXml`) and confirm `<idea-version since-build="242"
     until-build="261.*"/>` (or the chosen `since-build`).

4. **Verifier evidence:** archive `build/reports/pluginVerifier` output in the
   PR so reviewers can see 261 passes.

## Verify (acceptance criteria)

- `./gradlew clean test verifyPlugin buildPlugin` → `BUILD SUCCESSFUL`.
- Plugin verifier reports no unresolved/removed API problems for 261.
- `runIde` launches a 2026.1 IDE, TestSpark tool window loads, no plugin-init
  exceptions in the log, and a generation action opens its UI.

## Done when

All acceptance criteria pass. Open the PR against `development` summarizing the
version bumps, any API migrations from task 06, and attaching the verifier
report. This completes the 2026.1 support upgrade.
