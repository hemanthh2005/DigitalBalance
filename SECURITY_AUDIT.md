# DigitalBalance Security Audit & Threat Model Report

**Project Title:** DigitalBalance: An AI-Based Social Media Addiction Monitoring and Intelligent Child Safety System  
**Workspace:** `C:\Users\heman\OneDrive\Desktop\DigitalBalanceFinal`  
**Audit Date:** October 6, 2026  
**Status:** Audit Completed • Threats Evaluated • Security Enforcements Verified

---

## 1. Threat Model & Security Architecture

| Security Domain | Vulnerability / Threat | Mitigation Implemented in Code | Status |
| :--- | :--- | :--- | :--- |
| **Guardian Authentication** | Hardcoded PIN / Plaintext PIN exposure | Hashed with SHA-256 in [SecurityManager.kt](file:///C:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/app/src/main/java/com/digitalbalance/app/security/SecurityManager.kt). PINs are never logged or stored in plaintext. | **VERIFIED** |
| **Protected App Authorization** | Permanent lock bypass or token persistence | Single-launch authorization token model in [AppProtectionAccessibilityService.kt](file:///C:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/app/src/main/java/com/digitalbalance/app/services/protection/AppProtectionAccessibilityService.kt). Token is cleared upon app exit/switch. | **VERIFIED** |
| **Bypass via Android Back Button** | Pressing Back on overlay allows access to app underneath | Intercepted via `OnBackPressedCallback` in [RestrictionOverlayActivity.kt](file:///C:/Users/heman/OneDrive/Desktop/DigitalBalanceFinal/app/src/main/java/com/digitalbalance/app/ui/screens/childmode/RestrictionOverlayActivity.kt) to redirect to Home Launcher. | **VERIFIED** |
| **Camera & Biometric Privacy** | Facial image leak or biometric template storage | Frames are analyzed transiently in RAM via ML Kit `ImageAnalysis`. Zero image frames, face embeddings, or identity templates are saved. | **VERIFIED** |
| **PDF Report File Sharing** | Unsafe `file://` URI exposure / Storage permission leak | Android `FileProvider` (`content://com.digitalbalance.app.fileprovider/...`) configured in `AndroidManifest.xml` and `res/xml/file_paths.xml`. | **VERIFIED** |
| **Backend & Cloud Sync** | Client-supplied UID impersonation / Unauthorized sync | Node.js Express backend validates Firebase Bearer ID Tokens using Firebase Admin SDK (`auth.js`). Authenticated UID is extracted exclusively from token. | **VERIFIED** |

---

## 2. Mandatory Truthfulness Disclosures

1. **Camera Adult Verification:** Verification uses transient face presence detection (ML Kit `FaceDetection`) combined with SHA-256 Guardian PIN authorization. No biometric identity recognition or facial database storage is performed.
2. **Accessibility Service:** The Accessibility Service is restricted solely to detecting foreground package transitions (`TYPE_WINDOW_STATE_CHANGED`). No keystrokes, personal messages, or private screen contents are inspected.
