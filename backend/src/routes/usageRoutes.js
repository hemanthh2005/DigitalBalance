const express = require('express');
const router = express.Router();
const { verifyAuthToken } = require('../middleware/auth');
const { syncUsage, getUsage } = require('../controllers/usageController');

router.use(verifyAuthToken);

router.post('/sync', syncUsage);
router.get('/', getUsage);

module.exports = router;
