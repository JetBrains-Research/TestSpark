# Task 07 — Update plugin metadata & changelog

**Goal:** Reflect the new release and 2026.1 support in the plugin's
user-visible metadata. Purely mechanical; do it once the build is green.

**Risk:** Low.

## Files to change

- `gradle.properties`
  - Line 7: `pluginVersion = 0.4.2` → new version. Recommended `0.5.0` (a
    minor-version bump signals new IDE support); confirm the exact number with
    the team.
- `src/main/resources/META-INF/plugin.xml`
  - In the `<change-notes>` block (starts ~line 38), add a new top entry above
    the current `0.4.2` entry, matching the existing style:
    ```html
    <h4>0.5.0</h4>
        <ul>
            <li>Support IDE 261.* (IntelliJ 2026.1).</li>
        </ul>
    ```
    (Use whatever version number was chosen above. Include any other
    user-facing changes made during task 06 if relevant.)
- `CHANGELOG.md` (repo root)
  - Add a matching entry for the new version. Check how the Gradle Changelog
    plugin expects sections to be structured (the build reads the `Unreleased`
    section via `getChangelog`). Follow the existing format in the file.

## Steps

1. Decide the version number (default `0.5.0`) and set `pluginVersion`.
2. Add the `<change-notes>` entry in `plugin.xml`.
3. Add the `CHANGELOG.md` entry.
4. Keep the plugin `<name>`, `<id>` (`org.jetbrains.research.testgenie`), and
   `<vendor>` unchanged.

## Verify

```
j21
./gradlew properties --console=plain -q | grep "^version:"    # shows new version
./gradlew patchPluginXml --console=plain
./gradlew getChangelog --unreleased --no-header --console=plain -q
```
- `version:` reflects the new number.
- `patchPluginXml` succeeds and the generated
  `build/patchedPluginXml/plugin.xml` (or `build/resources`) contains the new
  change note.
- `getChangelog` prints the new entry without error (this exact command runs in
  CI, so it must succeed).

## Done when

Version, `plugin.xml` change-notes, and `CHANGELOG.md` all reference the new
release and 2026.1 support, and the changelog tasks run clean.
Commit: `docs: bump version and changelog for 2026.1 support (task 07)`.
