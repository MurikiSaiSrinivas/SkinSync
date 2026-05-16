# SkinSync — Known Issues / Backlog

Status: **app installs and runs on device** (2026-05-16). The items below are
deferred — to be worked on starting next session. Not blocking install.

---

## 1. Failing unit tests (2) — fix first

`./gradlew test` → `:core:color:test` 2 of 30 failing.

### 1a. `ColorNamerTest > red` — `ColorNamerTest.kt:8`
- `ColorNamer.name(0xFFD32F2F)` is expected to return `"red"` but returns
  something else (ComparisonFailure).
- Cause: OkLab hue of `#D32F2F` falls outside the `hue < 20 || hue >= 345`
  red band in `ColorNamer.kt` (likely lands in `orange` or `pink`).
- Fix options: widen the red hue band (e.g. `>= 340` / `< 25`) **or** adjust
  the test's expected sample. Re-run `:core:color:test` after.

### 1b. `SeasonalAnalyzerTest > cool deep features classify as a Winter` — `SeasonalAnalyzerTest.kt:21`
- `SeasonalAnalyzer.analyze(skin=#6B4A52, lip=#8E2B45, eye=#20242E)` expected
  to end with `"Winter"`; the warmth/depth thresholds in `SeasonalAnalyzer.kt`
  classified it otherwise (AssertionError).
- Cause: heuristic `warmth`/`deep` cutoffs don't match the chosen sample.
- Fix options: tune the thresholds (the `warmth >= 0` / `s.l < 0.62` cutoffs)
  **or** pick a more representative cool-deep sample in the test. Re-run after.

> Both are heuristic-vs-expectation mismatches in pure `:core:color` code —
> deterministic and quick to reconcile. No production impact yet (these
> features are wired but the heuristics just need calibration).

---

## 2. Build warnings (non-blocking)

- **KSP / Hilt incremental warning** (feature:profile/history/result):
  `w: [ksp] No dependencies reported for generated source
  *_HiltModules_*_LazyMapKey.java which will prevent incremental compilation.`
  Known upstream KSP+Hilt issue (issuetracker 413107). Harmless; only slows
  incremental builds. Revisit when bumping Hilt/KSP.
- **Gradle 9 deprecation notice** — forward-compat only; on Gradle 8.11.1.

---

## 3. Runtime warning on device — 16 KB page size

On-device dialog "Android App Compatibility": app isn't 16 KB-aligned. Native
libs flagged:
`libsurface_util_jni.so`, `libimage_processing_util_jni.so` (CameraX),
`libdatastore_shared_counter.so` (DataStore), `libmediapipe_tasks_vision_jni.so`
(MediaPipe), `libandroidx.graphics.path.so`.

- This is a **forward-compat warning** for Android 15+ devices with 16 KB
  pages; the app still runs. "Debuggable app" part is normal for debug builds.
- Fix later: bump to 16 KB-aligned library versions when available
  (CameraX, MediaPipe tasks-vision, DataStore, androidx.graphics) and/or set
  AGP packaging accordingly. Track Google's 16 KB guidance:
  https://developer.android.com/16kb-page-size
- Low priority until targeting Play with 16 KB requirement.

---

## 4. UI / UX overhaul (planned)

Current screens are functional but placeholder-grade (the home is literally
`HomePlaceholder`). Backlog:
- Proper home/dashboard, real branding, imagery, iconography.
- Polished capture screen (framing guide overlay, not just a text banner).
- Result screen layout, palette presentation, animations.
- Profile screen visual design; onboarding visuals.
- Component library in `:core:designsystem` (buttons, cards, app bar, etc.).
- Empty/loading/error state visuals.
- Rename `HomePlaceholder` → real `HomeScreen`.

(Detailed UI plan to be defined in a working session.)

---

## 5. Other deferred items / next-loop features

- B7 — Localized strings (i18n: move user-facing text to string resources).
- B9 — Saved-look detail screen (open a look → full palette, reshop, re-share).
- App Check debug provider for emulator (if App Check ever enforced in dev).
- Firestore composite-index review if new sorted queries are added.
- `git push --force` to origin still pending (user runs it; harness blocks it).

---

## Working order (suggested)
1. Fix the 2 failing tests (§1) → `./gradlew test` fully green.
2. UI overhaul plan (§4).
3. B7 / B9 (§5).
4. 16 KB alignment (§3) before any Play release.
