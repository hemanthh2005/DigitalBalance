const express = require('express');
const router = express.Router();
const { verifyAuthToken } = require('../middleware/auth');
const { getReports, getRecommendations } = require('../controllers/reportsController');

router.use(verifyAuthToken);

router.get('/reports', getReports);
router.get('/recommendations', getRecommendations);

module.exports = router;
