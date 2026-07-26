# VeroStay Demo

Apartment booking demo Android app, built to spec for testing the **VeroFlow** AI-powered automated testing platform. Kotlin + Jetpack Compose + Material 3, fully offline with in-memory mock data — no backend, no APIs.

## How to build

This project was generated as source only (no Android SDK available in the generation environment). To produce `app-debug.apk`:

1. Open the `VeroStayDemo/` folder in **Android Studio** (Koala or newer) and let it sync Gradle, **or**
2. From the command line, with Android SDK + JDK 17 installed:
   ```bash
   cd VeroStayDemo
   ./gradlew assembleDebug
   ```
   (If `gradlew` isn't present, open once in Android Studio first — it generates the wrapper — or run `gradle wrapper` with a local Gradle install.)
3. The output APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## What's implemented

All 31 screens from the spec, wired together with Navigation Compose, covering:
- Auth flow (splash, onboarding, welcome, register, login, forgot password) with real validation
- Home, search with filters, sortable results, and a genuine empty-search state
- Apartment details, image gallery (swipe + pinch/double-tap zoom), amenities, calendar, guest counters
- Booking summary → payment → confirmation → my bookings → booking details, with cancel flow
- Host profile, chat (send/camera/gallery/attachment/long-press delete/copy/forward), notifications, favorites, reviews
- Profile, edit profile, settings (dark mode, notifications, location, auto-login, language), help & support, about, logout dialog

## Testability hooks for VeroFlow

- **Deterministic payment outcome** — card number `4000000000000002` always declines; any other well-formed number succeeds. See `MockDataRepository.DECLINE_CARD_NUMBER`.
- **Deterministic empty state** — searching with a destination that resolves to no matches shows a real "no results" screen with a Clear Filters CTA.
- **Data reset** — `MockDataRepository.reset()` restores all mock data (apartments, bookings, favorites, chats, notifications) to a fixed seed, so repeated automated runs start from a known baseline. Wire this to a debug-only menu action or call it at test-harness startup.
- **Deep-link entry point** — tapping a Notification with a related booking navigates straight to Booking Details, bypassing the normal Home → Bookings path (also reachable via `verostay://booking` intent filter).
- **Accessible Map View** — implemented as plain Compose UI (icons/buttons with `contentDescription`), not an embedded live Maps SDK, so it stays reachable through the accessibility tree that Appium reads.
- Almost every interactive element has an explicit `contentDescription` (e.g. "Login Submit Button", "Favorite Icon apt_004", "Zoom In Button") so VeroFlow can locate elements reliably even where resource IDs alone would be ambiguous.

## Known simplifications (demo scope)

- Camera/gallery/file actions are stubbed (no real intents fired) — they update mock state to keep flows testable without device permissions blocking automation.
- Booking length is fixed at 3 nights for pricing simplicity; the Calendar screen still fully supports date selection and validates check-out > check-in.
- No test resource-ID collisions or artificial loading delays are included yet — see the FYP feedback notes on adding "hard" automation cases (duplicate IDs, forced crash trigger, stacked dialogs) if you want to extend this app further for Phase 2 evaluation.
