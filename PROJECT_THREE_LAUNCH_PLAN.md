# CS 360 Project Three - App Launch Plan

## 1) App Store Listing Strategy

### App Name
**Inventory Tracker CS360**

### App Description (Store Listing)
Inventory Tracker CS360 helps users manage item counts quickly with a simple login, item grid, and full CRUD controls.

Key features included in the launch build:
- User registration and login backed by persistent SQLite storage
- Inventory item management (create, read, update, delete)
- Grid-based inventory view for quick scanning
- Optional SMS low-stock alert support when permission is granted
- Core functionality remains fully available even when SMS permission is denied

### App Icon Plan
Use a clean, high-contrast icon showing a **box/checklist + small barcode or count badge** to communicate inventory tracking at a glance.
- Foreground: stylized storage box/checklist symbol
- Accent: numeric badge (e.g., "3") representing quantity tracking
- Design goal: readable at small launcher sizes and accessible color contrast

## 2) Android Version Support Plan

The current project configuration supports:
- **Minimum Android version:** API 24 (Android 7.0 Nougat)
- **Target Android version:** API 36
- **Compile SDK:** 36

Launch testing matrix should include:
- Minimum supported device/emulator (API 24)
- Mid-range modern API level (for compatibility behavior)
- Latest available Android API in Android Studio emulator

This approach balances broad compatibility with modern platform behavior and permission handling.

## 3) Permission Review Plan

The app currently requests only one runtime-sensitive permission:
- `android.permission.SEND_SMS`

Why this permission is needed:
- Enables optional SMS alerts when low inventory is detected.

Privacy/minimization plan:
- Request permission only when user taps the SMS permission button.
- If denied, app continues all non-SMS features (login + database CRUD + grid display).
- Do not request unrelated permissions (microphone, camera, location, contacts, etc.).

## 4) Monetization Plan

### Phase 1 (initial launch)
- **No paywall and no ads** to reduce friction for classroom/demo launch and early user adoption.

### Phase 2 (future roadmap)
Choose one model after usage validation:
1. **Freemium:** free core app + optional paid upgrade (advanced reports/export)
2. **Ad-supported free tier:** only if analytics show engagement can support ads without hurting usability

Recommendation for this course launch: start with **no ads/no purchase** and gather feedback before monetization.

## 5) Launch Readiness Checklist

Before publishing:
1. Execute full regression tests for login/register and all CRUD operations.
2. Validate SMS permission flows:
   - Granted path sends alert
   - Denied path keeps app functional without crash
3. Verify persistent database behavior across app restarts.
4. Finalize app icon assets and store screenshots.
5. Produce release notes and privacy disclosures consistent with permissions used.

