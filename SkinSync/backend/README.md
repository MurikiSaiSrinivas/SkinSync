# Backend

SkinSync has **no custom server**. The backend is **Firebase (BaaS, free Spark plan)**:

| Need | Firebase service | Why no server code |
|------|------------------|--------------------|
| Hide Gemini key | **Firebase AI Logic** | Google-hosted proxy; key never in APK; App Check gates it |
| Profiles / saved looks | **Firestore** | Direct SDK + security rules below |
| Identify a device anonymously | **Anonymous Auth** | No login UI, no PII |
| Abuse protection | **App Check (Play Integrity)** | Blocks non-app traffic to AI/DB |
| Crash + usage | **Crashlytics + Analytics** | Free |

A custom backend would add hosting cost and a key to manage — it would break the
**$0 / no-secrets-in-app** rules. So this folder only holds Firebase *configuration*.

## Files
- `firebase.json` — Firebase CLI config (which rules/indexes to deploy).
- `firestore.rules` — security rules: a user can only read/write their own data.
- `firestore.indexes.json` — composite indexes for saved-looks queries.

## Deploy (you do this once, free)
1. Create the Firebase project (see `../../BLUEPRINT.md` §11).
2. `npm i -g firebase-tools` → `firebase login`.
3. From this folder: `firebase use --add` then
   `firebase deploy --only firestore:rules,firestore:indexes`.

No server is ever run or paid for.
