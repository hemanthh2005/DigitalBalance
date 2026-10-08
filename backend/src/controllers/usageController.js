const { admin } = require('../config/firebase');

/**
 * Idempotent daily app usage sync endpoint.
 * Document ID format: {date}_{packageName}
 */
async function syncUsage(req, res, next) {
  try {
    const uid = req.user.uid;
    const { usageRecords } = req.body;

    if (!Array.isArray(usageRecords) || usageRecords.length === 0) {
      return res.status(400).json({
        success: false,
        error: 'Invalid payload: usageRecords must be a non-empty array'
      });
    }

    const db = admin.firestore();
    const batch = db.batch();

    usageRecords.forEach(record => {
      const { date, packageName, appName, category, foregroundTimeMs, sessionCount, isSocialMedia } = record;
      if (!date || !packageName) return;

      // Deterministic Idempotent Document ID
      const docId = `${date}_${packageName.replace(/\./g, '_')}`;
      const docRef = db.collection('users').doc(uid).collection('usage').doc(docId);

      batch.set(docRef, {
        date,
        packageName,
        appName: appName || packageName,
        category: category || 'OTHER',
        foregroundTimeMs: foregroundTimeMs || 0,
        sessionCount: sessionCount || 1,
        isSocialMedia: !!isSocialMedia,
        syncedAt: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });
    });

    await batch.commit();

    return res.status(200).json({
      success: true,
      message: `Successfully synchronized ${usageRecords.length} usage records`,
      count: usageRecords.length
    });
  } catch (error) {
    next(error);
  }
}

async function getUsage(req, res, next) {
  try {
    const uid = req.user.uid;
    const { date } = req.query;

    const db = admin.firestore();
    let query = db.collection('users').doc(uid).collection('usage');

    if (date) {
      query = query.where('date', '==', date);
    }

    const snapshot = await query.get();
    const records = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

    return res.status(200).json({
      success: true,
      data: records
    });
  } catch (error) {
    next(error);
  }
}

module.exports = { syncUsage, getUsage };
