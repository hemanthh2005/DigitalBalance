# DigitalBalance: An AI-Based Social Media Addiction Monitoring and Intelligent Child Safety System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_1.9.22-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Node.js_Express-339933?logo=node.js)](https://nodejs.org)
[![Firebase](https://img.shields.io/badge/Cloud-Firebase_Auth_Firestore-FFCA28?logo=firebase)](https://firebase.google.com)

DigitalBalance is a practical final-year Bachelor of Engineering (BE) Android digital-wellbeing and child-safety application designed to monitor smartphone usage patterns, assess behavioral risk transparently, provide healthy usage recommendations, and enforce application restriction via camera-assisted adult verification.

---

## 1. Core Objectives & Features

1. **Social Media Usage Monitoring:** Tracks active foreground application usage using Android `UsageStatsManager` while strictly excluding DigitalBalance self-usage and launcher idle time.
2. **Session Reconstruction & Midnight Splitting:** Reconstructs continuous foreground sessions and cleanly splits cross-midnight usage intervals (e.g., 23:50 to 00:20 split into Day A: 10m, Day B: 20m).
3. **Explainable Rule-Based Behavioral Risk Engine:** Computes a transparent 100-point risk score based on 3 indicators:
   - Screen Time Penalty (Max 40 pts)
   - High-Risk Category Share (Social/Video/Gaming) Penalty (Max 30 pts)
   - App Open Frequency Penalty (Max 30 pts)
4. **Intelligent Child Safety Mechanism:** Guardian vs Child mode toggle, protected application list, daily child limit enforcement, and AccessibilityService lock screen overlays.
5. **Camera-Assisted Adult Verification:** CameraX + Google ML Kit Face Detection transient presence verification required before guardian authorization.
6. **Secure Local-First Architecture:** Offline-first Room database combined with Node.js/Express REST backend powered by Firebase Admin SDK ID-token authentication.

---

## 2. Technical Stack

- **Android App:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt (DI), Room DB, Coroutines, StateFlow, WorkManager, CameraX, ML Kit Face Detection.
- **Backend:** Node.js, Express.js, Firebase Admin SDK, CORS, Helmet, Jest.
- **Cloud Infrastructure:** Firebase Authentication, Cloud Firestore (`users/{uid}/...`).

---

## 3. Master Architecture

```
Android Application (Jetpack Compose + Hilt + Room)
  ├── UsageStatsManager ──> Session Reconstruction & Midnight Splitting ──> Aggregation
  ├── CameraX + ML Kit ──> Transient Face Detection ──> Guardian Authorization
  ├── AccessibilityService ──> Protected App Detection ──> Lock Screen Overlay
  └── Offline-First Repository (Room DB) ──> WorkManager ──> Retrofit API
                                                                  │
                                                                  v
                                                     Node.js / Express Backend
                                                                  │
                                                     Firebase Admin SDK (ID Token Auth)
                                                                  │
                                                                  v
                                                        Firebase Firestore
```

---

## 4. Academic Truthfulness & Terminology Statement

- **Risk Engine:** Described strictly as *"A weighted rule-based behavioral risk assessment mechanism that estimates digital-wellbeing risk from observed smartphone usage indicators."*
- **No Clinical Claims:** The application does not make medical diagnoses.
- **Face Detection Scope:** Transient presence verification only. No biometric facial recognition, identity matching, or face database storage.

---

## 5. How to Build & Run

### Android Studio Setup:
1. Open the project root in Android Studio (Hedgehog 2023.1.1 or newer).
2. Ensure JDK 17 or JDK 22 is selected in Project Structure.
3. Sync Gradle and run `./gradlew assembleDebug` or `gradlew.bat assembleDebug`.

### Backend Setup:
1. Navigate to `backend/` directory:
   ```bash
   cd backend
   npm install
   npm test
   npm start
   ```
2. API running on `http://localhost:5000/api/health`.

---

## 6. Project Documentation Sitemap

- [`PROJECT_AUDIT.md`](file:///c:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/PROJECT_AUDIT.md) — Phase 0 audit & requirements mapping.
- [`DEVELOPMENT_PROGRESS.md`](file:///c:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/DEVELOPMENT_PROGRESS.md) — Checkpoint tracker.
- [`SECURITY_AUDIT.md`](file:///c:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/SECURITY_AUDIT.md) — Security threat model & verification.
- [`PERFORMANCE_AUDIT.md`](file:///c:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/PERFORMANCE_AUDIT.md) — Latency & memory optimization.
- [`DEMO_GUIDE.md`](file:///c:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/DEMO_GUIDE.md) — 5–10 minute viva demonstration script.
- [`FINAL_PROJECT_REPORT.md`](file:///c:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/FINAL_PROJECT_REPORT.md) — Comprehensive final project evaluation report.
