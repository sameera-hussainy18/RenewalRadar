# RenewalRadar

A simple Android app that tracks your subscriptions (Spotify, iCloud, gym, campus bills…) and shows how much you'll pay this month.

Built with **Java + XML layouts + Room** for Android Studio Chipmunk (AGP 7.2.1, Gradle 7.3.3, compileSdk 32, minSdk 24).

## Features
- **Create account / Sign in** (accounts are stored locally in Room, and passwords are saved as SHA-256 hashes)
- Stays signed in (SharedPreferences), with **Log out** in the ⋮ menu
- Subscription list (RecyclerView), sorted by the next charge date
- Add / edit form with `TextInputLayout` fields and a `ChipGroup` for the billing cycle (Weekly / Monthly / Yearly / Custom)
- Status chips: Active / Free trial / Paused
- "This month" total card in a `CoordinatorLayout` + collapsing toolbar, so the total stays visible while scrolling
- Recurrence engine with month-end handling (Jan 31 → Feb 28 → Mar 31), plus the next 6 charge dates
- Unit test for the date maths: `app/src/test/.../RecurrenceEngineTest.java`

## Run it
1. Clone or download this repo.
2. In Android Studio choose **File → Open** and select this folder (the one with `settings.gradle`).
3. Wait for Gradle sync, then press **Run ▶** on an emulator or a phone.

## Project structure
All Java code is in `app/src/main/java/com/example/renewalradar/`:

| File | Purpose |
|---|---|
| `LoginActivity`, `RegisterActivity`, `SessionManager` | Sign in, create account, remember the signed-in user |
| `User`, `UserDao` | `users` table |
| `Subscription`, `SubscriptionDao` | `subscriptions` table |
| `AppDatabase` | Room database |
| `RecurrenceEngine` | Next due dates and monthly total |
| `MainActivity`, `SubscriptionAdapter` | Home screen and list |
| `AddEditActivity` | Add / edit form |

## Download the app (APK)
Every push to `main` builds the app automatically (see `.github/workflows/build-apk.yml`).

**Latest APK:** https://github.com/sameera-hussainy18/RenewalRadar/releases/latest/download/RenewalRadar.apk

To install it, open the link on your Android phone, tap the downloaded file, and allow **Install unknown apps** when asked.
