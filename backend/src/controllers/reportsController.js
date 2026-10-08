const { admin } = require('../config/firebase');

async function getReports(req, res, next) {
  try {
    const uid = req.user.uid;
    const db = admin.firestore();

    const snapshot = await db.collection('users').doc(uid).collection('reports').get();
    const reports = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

    return res.status(200).json({
      success: true,
      data: reports
    });
  } catch (error) {
    next(error);
  }
}

async function getRecommendations(req, res, next) {
  try {
    const uid = req.user.uid;
    const db = admin.firestore();

    const snapshot = await db.collection('users').doc(uid).collection('recommendations').get();
    const recs = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

    return res.status(200).json({
      success: true,
      data: recs
    });
  } catch (error) {
    next(error);
  }
}

module.exports = { getReports, getRecommendations };
