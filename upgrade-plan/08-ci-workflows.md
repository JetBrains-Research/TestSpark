# Task 08 — Update CI workflows for Java 21

**Goal:** Bring the GitHub Actions workflows in line with the Java 21 build
requirement so CI matches local builds.

**Risk:** Low, but only fully verifiable once pushed (CI runs on GitHub).

## Files to change (`.github/workflows/`)

- `build.yml`
  - The `Setup Java` step uses `java-version: 17`. Change to `21`.
  - Leave `distribution: zulu` unless the team prefers another; JDK 21 must be
    available for that distribution (it is for Zulu).
- `release.yml`
  - The `Setup Java` step uses `java-version: 17`. Change to `21`.
- `run-ui-tests.yml`
  - Three jobs set up Java: Windows uses `17`, Linux and macOS use `11`.
  - Change all three to `21` so UI tests run on the same JDK as the build.

## Steps

1. Update each `java-version:` value listed above.
2. Grep to confirm none were missed:
   ```
   grep -rn "java-version" .github/workflows/
   ```
   Every occurrence relevant to building/testing TestSpark should read `21`.
3. Do not change unrelated action versions in this task unless one is broken.

## Verify

- **Locally:** nothing to run — these files only affect CI. Confirm YAML is
  well-formed:
  ```
  grep -rn "java-version" .github/workflows/    # all show 21
  ```
  There is the possibility for verification using the `act` command-line tool.
  See its [documentation](https://nektosact.com/).  Note that while `act` should
  be able to run the CI, it might also produce failures due to, e.g.,
  incompatible platforms. Take this with a grain of salt—if it passes, great; if
  not, this shall not be a blocker!
- **On CI (after push / in the PR):** the `Build` workflow's `Run Tests`,
  `Run Plugin Verification tasks`, and artifact steps must pass on Java 21.
  Watch the Actions run triggered by the PR.

## Done when

All build/test/release/UI-test workflows set up Java 21 and the `Build` workflow
passes on the PR.
Commit: `ci: build and test on Java 21 (task 08)`.
