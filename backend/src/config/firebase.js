const admin = require('firebase-admin');
const path = require('path');

let db;

function initializeFirebase() {
  if (admin.apps.length === 0) {
    const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;

    if (serviceAccountPath && require('fs').existsSync(serviceAccountPath)) {
      const serviceAccount = require(path.resolve(serviceAccountPath));
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
      });
      console.log('[Firebase Admin] Initialized with Service Account Certificate');
    } else {
      // Default Application Credentials (for GCP / local emulator / development fallback)
      admin.initializeApp({
        projectId: process.env.FIREBASE_PROJECT_ID || 'digitalbalance-demo'
      });
      console.log('[Firebase Admin] Initialized with default project config');
    }
  }
  db = admin.firestore();
  return { admin, db };
}

module.exports = { initializeFirebase, admin };
