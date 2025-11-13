
const express = require('express');
const router = express.Router();
const moodController = require('../controllers/moodController');
const authMiddleware = require('../middleware/authMiddleware');

// All mood routes require authentication
router.use(authMiddleware);

router.post('/', moodController.createMood);
router.get('/', moodController.getMoods);
router.get('/:id', moodController.getMoodById);
router.put('/:id', moodController.updateMood);
router.delete('/:id', moodController.deleteMood);

module.exports = router;