# SkinSync — Rebuild Blueprint

> Single source of truth for the SkinSync rebuild. Explains the old app, why we are
> rebuilding, every decision, the architecture, the rules Claude follows, the phase
> plan, and exactly what only you (the human) must do.

---

## 1. What SkinSync is

A mobile app that helps a user pick flattering outfit colors for a photoshoot.

1. Take a selfie.
2. Detect the face and sample **skin, lip, and eye colors**.
3. Send those colors + age/gender + a **location** to an AI model.
4. Get back a palette of complementary colors, each with a reason and outfit ideas.
5. Tap a color → open shopping links for matching clothes.

---

## 2. The old app (audited) and why it is being redone

Native Android, Kotlin 1.9, XML Views, AGP 8.6, SDK 34. The original
`SkinSync Project/SkinSync/` folder was deleted after the rebuild (it remains
on its git remote `origin/main` if the old code is ever needed).

### Current features (old app)
- CameraX selfie capture.
- MediaPipe FaceLandmarker (478-point mesh).
- Custom RGB K-means dominant-color extraction for cheek / lip / left & right eye.
- Profile (name, age, gender, photo, colors) stored in SharedPreferences.
- Gemini 1.5 Flash (legacy SDK) returns a color palette as JSON via prompt text.
- SerpApi (paid) fetches shopping results per dress name; product cards with buy links.

### Critical problems found (drove the rebuild)
| # | Problem | Why it matters |
|---|---------|----------------|
| 1 | API keys hard-coded in `MainActivity` | Extractable from APK; quota theft |
| 2 | `GEMINI_API_KEY` BuildConfig wired from `SERP_API_KEY` | Plain bug |
| 3 | Deprecated GenAI SDK + retiring model | Will stop working |
| 4 | Spinner hidden by hard-coded `delay(5000)` | Not tied to real work |
| 5 | God-Activity: networking + parsing + UI in one file | Untestable |
| 6 | Mixed concurrency (MainScope, callbacks, viewModelScope) | Leaks, no cancellation |
| 7 | One SerpApi call **per dress** in a loop | Burns paid quota fast |
| 8 | Gemini JSON parsed with string `.replace("```")` hacks | Crashes on drift |
| 9 | Full-image `getPixel` loop + mask bitmap on main path | Jank / ANR |
| 10 | RGB K-means for skin tone | Perceptually wrong |
| 11 | Selfie written to public gallery, no consent | Privacy issue |
| 12 | Dead code: `ColorStorage`, `AmazonProductApi`, unused ML Kit dep | Bloat/confusion |
| 13 | No DI, no repository, no tests, SharedPreferences-as-DB | Not maintainable |
| 14 | targetSdk 34, JVM 1.8 | Below Play 2025+ requirements |

---

## 3. Hard constraints

- **$0 cost.** No paid service or dependency. SerpApi is dropped.
- **No secrets in the app.** API keys never ship in the APK or VCS.
- Reuse the free, good parts: on-device MediaPipe, Gemini free tier (via Firebase).

---

## 4. Decisions (locked)

| Area | Decision |
|------|----------|
| UI | Jetpack Compose + Material 3 (Android). iOS later = separate native UI |
| Platform | **Android first, iOS later.** Shared logic kept pure-Kotlin (KMP-friendly) |
| Compose↔iOS | Option **B**: per-platform UI; shared **domain + color** logic only |
| Backend | Firebase free **Spark** plan |
| AI | **Firebase AI Logic** proxies Gemini (`gemini-2.x-flash`); key stays server-side; App Check |
| Auth/DB | Firebase **Anonymous Auth** + **Firestore** (profile, saved looks) |
| Shopping | **No product API.** Gemini returns queries → app builds retailer **search deep links** opened externally |
| Face mesh | MediaPipe FaceLandmarker (on-device, free) |
| Color | Extract in **OkLab/CIELAB**, not RGB |
| Storage | Selfie in **app-private** storage, behind consent, with delete-my-data |
| Local store | DataStore (Proto) for profile/consent |
| Serialization | kotlinx.serialization (drop Gson) |
| Images | Coil 3 (drop Glide) |
| DI | Hilt |
| Crash/analytics | Firebase Crashlytics + Analytics (free) |

---

## 5. Target architecture

```
:app                Single Activity, DI graph (Hilt), Compose Navigation
:feature:capture    Camera + face-mesh screen + ViewModel
:feature:profile    Profile screen + ViewModel
:feature:result     Palette + reasons + shopping links + ViewModel
:feature:history    Saved looks (Firestore)
:core:designsystem   M3 theme, tokens, shared composables
:core:ml            MediaPipe FaceLandmarker wrapper (Android impl)
:core:color         OkLab dominant-color extraction      ← PURE KOTLIN (iOS reuses)
:domain             Models, use cases, repository interfaces ← PURE KOTLIN (iOS reuses)
:data               Repo impls: Firebase AI, Firestore, DataStore, deep-link builder
```

Flow: `Composable → ViewModel(StateFlow<UiState>) → UseCase → Repository(interface in :domain) → impl in :data`.
`UiState = Loading | Success(data) | Error(msg) | Empty`. Coroutines + Flow only.

---

## 6. Tech stack & versions

| Area | Choice |
|------|--------|
| Kotlin | 2.1.x (K2) |
| AGP | 8.7+ |
| compile / target / min SDK | 35 / 35 / 26 |
| JVM target | 17 |
| UI | Compose BOM (latest) + Material 3 + Navigation-Compose |
| DI | Hilt |
| AI | `com.google.firebase:firebase-ai` (Firebase AI Logic), structured output via `responseSchema` |
| Auth/DB | Firebase Auth (Anonymous) + Firestore + App Check (Play Integrity) |
| Local | DataStore Proto |
| JSON | kotlinx.serialization |
| Images | Coil 3 |
| Camera | CameraX 1.4.x |
| Face mesh | MediaPipe `tasks-vision` latest |
| Removed | SerpApi, Gson, Glide, legacy GenAI SDK, ML Kit face-detection, `ColorStorage`, `AmazonProductApi`, secrets/undercouch plugins |

---

## 7. Design tokens (Material 3, brand seed `#F584F5`)

**Light** — primary `#A23BA0` · onPrimary `#FFFFFF` · primaryContainer `#FFD6FA` ·
onPrimaryContainer `#37003A` · secondary `#6E5868` · tertiary `#82524A` ·
background `#FFF7FA` · surface `#FFF7FA` · surfaceVariant `#EFDEE8` ·
onSurface `#1F1A1D` · outline `#80747C` · error `#BA1A1A`

**Dark** — primary `#F8ABF0` · onPrimary `#5A1A58` · primaryContainer `#7E3A7C` ·
onPrimaryContainer `#FFD6FA` · secondary `#D9BFD2` · background `#161217` ·
surface `#161217` · surfaceVariant `#4F444C` · onSurface `#EAE0E4` · outline `#998D96`

**Spacing** 4·8·12·16·24·32·48 dp  ·  **Radius** sm 8 / md 12 / lg 16 / xl 28 / full
**Type** M3 scale (Display→Label)  ·  **Elevation** 0·1·3·6·8 dp

---

## 8. Gemini structured output (replaces string hacking)

Request with `responseMimeType = "application/json"` + `responseSchema`. Target model:

```
Suggestion(
  seasonalType: String,                 // e.g. "Soft Autumn"
  palette: List<ColorRec>
)
ColorRec(
  hexColor: String,                     // "#RRGGBB"
  name: String,                         // "Dusty Rose"
  reason: String,                       // why it flatters
  outfitQueries: List<String>           // e.g. "dusty rose midi dress women"
)
```

App builds, per query, deep links opened with `Intent.ACTION_VIEW`:
- `https://www.amazon.in/s?k=<q>`
- `https://www.myntra.com/<q>`
- `https://www.flipkart.com/search?q=<q>`
- `https://www.google.com/search?tbm=shop&q=<q>`

Zero API, zero key, zero cost.

---

## 9. Strict rules Claude follows while building

1. **No paid service, ever.** No dependency without a free tier covering expected use.
2. **No API key** in source, BuildConfig, comments, or VCS. Keys live server-side (Firebase AI Logic) only. `google-services.json` is git-ignored.
3. **No `delay()` to drive UI.** UI state derives only from real async completion via sealed `UiState`.
4. `:domain` and `:core:color` stay **pure Kotlin** (no `android.*` imports) so iOS can reuse them.
5. **One async model:** Kotlin Coroutines + Flow. No Retrofit callbacks, no stray `MainScope()`.
6. Every screen explicitly handles **Loading / Success / Error / Empty**.
7. The selfie is stored **app-private only**, behind explicit consent, with a working delete-my-data action.
8. Each phase ends building, with at least ViewModel/use-case **unit tests**.
9. No dead code committed. If something is unused, it is not added.
10. Conventional, readable Kotlin; match style across modules; KDoc public APIs.

---

## 10. Phase plan & progress

- [x] **Phase 0 — Scaffold:** Gradle project, version catalog, modules (`:app`, `:domain`, `:core:color`, `:core:designsystem`), Hilt, Compose, design-system + tokens, navigation skeleton, `backend/` Firebase config, command log. Domain & color have unit tests. *(Compile/run is done in Android Studio — no SDK in the build env.)*
- [x] **Phase 1 — Capture & color:** `:core:ml` MediaPipe wrapper + pure `FaceColorMapper`; `:data` DataStore `ProfileRepository` + `DeepLinkBuilder`; `:feature:capture` CameraX Compose screen, consent dialog, app-private storage, `CaptureViewModel`. Unit tests for mapper, profile mapping, deep links, ViewModel. Wired into app + nav.
- [x] **Phase 2 — Profile:** `:feature:profile` `ProfileViewModel` (draft + UiState) + `ProfileScreen` (name/age/gender, color swatches, delete-my-data w/ confirm). Domain `SelfieStore` + `ClearUserDataUseCase`; capture refactored to share the selfie path. Tests for ViewModel + use case. Wired + nav. *(Firestore sync deferred into Phase 3 alongside Firebase setup.)*
- [x] **Phase 3 — Recommendations:** `:data` `FirebaseAiSuggestionRepository` (Firebase AI Logic, key server-side), pure `PromptBuilder` + `SuggestionParser` (kotlinx.serialization, no string hacks) with tests. `:feature:result` `ResultViewModel` + `ResultScreen` (location → palette + reasons, all UiState cases). Wired + nav + home entry. *(User enables `google-services` plugin + adds `google-services.json` to run it.)*
- [x] **Phase 4 — Shopping deep links:** `DeepLinkBuilder` (Amazon/Myntra/Flipkart/Google Shopping, URL-encoded, tested) + `RetailerDialog` opening `Intent.ACTION_VIEW` from the result screen. Zero-cost SerpApi replacement.
- [x] **Phase 5 — Polish:** domain `SavedLook` + `LooksRepository` + Save/Observe/Delete use cases (tested); `:data` `FirestoreLooksRepository` + pure tested `LookMapping` + anonymous-auth; `:feature:history` `HistoryViewModel`+`HistoryScreen`; "Save look" on result; `SkinSyncApp` guarded App Check + anon auth; dark theme + error/empty states throughout. Wired + nav + home.
- [x] **Phase 6 — iOS readiness:** `:domain` + `:core:color` verified android-free (pure Kotlin JVM modules); extraction path documented. No iOS code yet (deferred by design).

### Bonus features (added after all phases finished — user instruction #5)
- [x] **B1 — Seasonal color analysis:** pure tested `SeasonalAnalyzer` (OkLab → e.g. "Warm Autumn"); shown on the profile screen (offline, deterministic).
- [x] **B2 — Favorites:** `SavedLook.favorite` + `ToggleFavoriteUseCase`; Firestore `setFavorite`; history star toggle, favorites sorted first. Mapping/use-case/ViewModel tests.
- [x] **B3 — Lighting guidance + white balance:** pure tested `GrayWorld` color-cast correction applied before sampling in the extractor; on-camera lighting coaching overlay.
- [x] **B4 — Share result card:** pure tested `ShareTextBuilder` + `ShareCardRenderer` (Canvas PNG) + FileProvider; "Share" button on result opens the system sheet.
- [x] **B5 — Wardrobe mode:** pure tested `Harmony` + `ColorNamer`; `GarmentColorExtractor` (downscale + gray-world + dominant); `:feature:wardrobe` `WardrobeViewModel`+`WardrobeScreen` (capture garment → matching palette → shop). Tests + wired + home.

### Bonus round 2 (loop continues until interrupted — instruction #6)
- [x] **B6 — Onboarding:** domain `OnboardingRepository` + use cases (tested); `:data` DataStore impl; `OnboardingScreen` + `StartViewModel` gate the nav start route on first run.
- [ ] **B7 — Localized strings:** move user-facing text to string resources (i18n-ready). *(Next iteration — large cross-module refactor, deliberately not rushed.)*
- [x] **B8 — Accessible swatches:** swatches now show hex text + `contentDescription` (profile & result), readable without color vision.
- [ ] **B9 — Saved-look detail:** open a look → full palette, reshop, re-share. *(Next iteration.)*
- [x] **B10 — Profile backup:** pure tested `ProfileBackup` JSON export/import in `:data`.

---

## 11. What only YOU can do (Claude cannot)

These need your Google account / Android Studio / a device — they are not codeable here:

1. **Create the Firebase project** (free Spark): console.firebase.google.com → add Android
   app with package `com.oo.skinsync` → download `google-services.json` into `:app/`.
2. **Enable in Firebase console:** Firebase AI Logic (Gemini), Anonymous Auth, Firestore,
   App Check (Play Integrity), Crashlytics.
3. **Open the project in Android Studio**, let it download the Android SDK / Gradle, and
   **Sync**.
4. **Run on an emulator or device** (camera + on-device ML need a real run).
5. Provide the **MediaPipe `face_landmarker.task`** model into `:app/src/main/assets/`
   (the build script will fetch it; confirm the download succeeds with internet).
6. App signing / Play Store steps when you publish.

Claude delivers: all source, build files, architecture, docs, and tests. Build/run +
Firebase console + signing are yours.

---

## 12. Old vs new — at a glance

| | Old | New |
|---|-----|-----|
| UI | XML Views | Jetpack Compose + M3 |
| Structure | 1 god Activity | Multi-module, MVVM + use cases |
| Keys | Hard-coded in app | Server-side (Firebase AI Logic) |
| AI SDK | Deprecated GenAI 0.9 | Firebase AI Logic, structured output |
| Shopping | Paid SerpApi | Free retailer deep links |
| Color | RGB K-means | OkLab extraction |
| Storage | SharedPreferences + public gallery | DataStore + app-private + Firestore |
| Async | Mixed/leaky | Coroutines + Flow + UiState |
| Tests | None | Per-phase unit tests |
| iOS | Impossible | Shared pure-Kotlin core |
