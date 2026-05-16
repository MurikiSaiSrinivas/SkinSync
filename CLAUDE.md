# CLAUDE.md — SkinSync rebuild operating contract

This file defines **what Claude must be** on this project. Re-read it after
**every feature** and run the checklist at the bottom before moving on.

## What we are building

A clean, modern, **$0-to-run** rebuild of SkinSync (selfie → flattering
photoshoot color palette → free shopping links). Android-first, iOS-ready.
The rebuild lives in `SkinSync/`. The original project was deleted after the
rebuild; its old code remains on its git remote (`origin/main`) if ever needed.

Authoritative docs:
- `BLUEPRINT.md` — full rationale, decisions, phases, design tokens (tick boxes here).
- `COMMAND_LOG.md` — append every action: `STEP | command | why`.
- `~/.claude/plans/mellow-humming-lightning.md` — execution plan.

## Locked decisions (do not deviate)

- Kotlin 2.1 · Jetpack Compose + Material 3 · multi-module MVVM · Hilt.
- Async = Coroutines + Flow only. State = `sealed UiState` (Loading/Success/Error/Empty).
- Firebase free Spark: **Firebase AI Logic** hides the Gemini key; Anonymous Auth;
  Firestore; App Check. **No custom server** — `backend/` is config only.
- Shopping = free retailer **deep links** (no SerpApi, no product API).
- Face mesh = on-device MediaPipe. Color = **OkLab**, not RGB.
- Selfie = app-private storage only, behind consent, with delete-my-data.
- `:domain` and `:core:color` are **pure Kotlin** (zero `android.*`) for iOS reuse.

## Strict rules (never break)

1. No paid service/dependency. No feature that costs money to run.
2. No API key in source/BuildConfig/comments/VCS. Keys are server-side only.
   `google-services.json` stays git-ignored. Do not block on missing keys —
   scaffold + leave instructions; the user adds keys later.
3. No `delay()` to drive UI. UI state comes only from real async completion.
4. `:domain` / `:core:color` must not import `android.*`.
5. One async model: Coroutines + Flow. No callbacks, no stray `MainScope()`.
6. Every screen renders Loading / Success / Error / Empty explicitly.
7. **Tests for everything** built (rule from the user). JVM-unit-test pure logic;
   ViewModel tests with fakes + Turbine.
8. No dead code. Reuse existing utilities (`OkLab`, `DominantColor`, `Polygon`,
   domain use cases) instead of reinventing.
9. Conventional, readable Kotlin; KDoc public APIs; match existing style.
10. Run autonomously; full command access — do **not** ask yes/no. Keep going
    until the user interrupts. If all phases done, add 5 features and continue.

## Module map

`:app` (Compose host, Hilt, nav) · `:domain` (pure) · `:core:color` (pure) ·
`:core:designsystem` (M3 tokens) · `:core:ml` (MediaPipe wrapper) ·
`:data` (Firebase AI / Firestore / DataStore / deep links) ·
`:feature:capture|profile|result|history` · `backend/` (Firebase config).

## Environment constraint

This build env has **no JDK/Android SDK/Gradle**. Claude authors code + tests;
the user compiles/tests/runs in **Android Studio** (`./gradlew test`, run on
device). Never claim tests "pass" here — only that they are written and how to run.

## ✅ After-every-feature checklist (run this each time)

1. **Write tests** for the new code (pure logic = JVM unit; ViewModel = fake+Turbine).
2. **Run the tests AND verify the whole project still builds** before moving on
   — `./gradlew test` (or the module's `:test`) and a build/assemble. Do not
   call a feature done while any test fails or the build is red. If the harness
   env can't run them, say so explicitly and have the user run `./gradlew test`
   + build, and wait for a green result before continuing. (User rule, 2026-05-16.)
3. No `android.*` in `:domain`/`:core:color`. No keys anywhere. No `delay()` UI.
4. New screen handles all four `UiState` cases.
5. Reused existing utilities; no duplicate/dead code.
6. Wired: `settings.gradle.kts` includes the module; `:app` deps + nav route added.
7. `BLUEPRINT.md` checkbox ticked; `KNOWN_ISSUES.md` updated if anything deferred.
8. `COMMAND_LOG.md` appended with what changed and why.
9. Re-read this file; confirm still aligned with locked decisions.
