
const express = require('express');
const router = express.Router();
const statsController = require('../controllers/statsController');
const authMiddleware = require('../middleware/authMiddleware');

// All stats routes require authentication
router.use(authMiddleware);

router.get('/moods', statsController.getMoodStats);
router.get('/activities', statsController.getActivities);
router.get('/summary', statsController.getSummary);

module.exports = router;