# SkinSync (v2 rebuild)

Find flattering photoshoot outfit colors from a selfie. Rebuilt clean, modern,
and **$0 to run**. Full rationale: [`../BLUEPRINT.md`](../BLUEPRINT.md).

## Stack
Kotlin 2.1 · Jetpack Compose + Material 3 · multi-module MVVM · Hilt ·
MediaPipe (on-device face mesh) · OkLab color extraction · Firebase AI Logic
(Gemini, key server-side) · Firestore · DataStore. iOS-shareable pure-Kotlin
`:domain` and `:core:color`.

## Modules
| Module | Type | Notes |
|--------|------|-------|
| `:app` | Android app | Single Activity, Compose, Hilt, navigation |
| `:domain` | Pure Kotlin | Models, use cases, repo interfaces (iOS-shareable) |
| `:core:color` | Pure Kotlin | OkLab dominant-color extraction (iOS-shareable) |
| `:core:designsystem` | Android lib | M3 theme + tokens |
| `:core:ml` | Android lib | MediaPipe wrapper *(Phase 1)* |
| `:data` | Android lib | Firebase AI / Firestore / DataStore / deep links *(Phase 2–4)* |
| `:feature:*` | Android lib | Compose screens + ViewModels *(Phase 1–5)* |
| `backend/` | Config only | Firebase rules/indexes — **no server** |

## Build
1. Open this folder in **Android Studio** (it provides the Android SDK + JDK 17).
2. Let it **Sync** (downloads Gradle 8.11.1 and dependencies).
3. Run on a device/emulator.

### Firebase (you, once, free — see BLUEPRINT §11)
Firebase is added in Phase 3. Create the project, drop `google-services.json`
into `app/`, then uncomment the Firebase plugins in `app/build.gradle.kts`.
API keys are never stored in the app.

## Tests
`./gradlew test` runs all JVM unit tests (`:domain`, `:core:color`, ViewModels).
