// backend/src/routes/authRoutes.js
const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const authMiddleware = require('../middleware/authMiddleware');
const { registerValidation, loginValidation, passwordUpdateValidation } = require('../middleware/validation');

// Public routes
router.post('/register', registerValidation, authController.register);
router.post('/login', loginValidation, authController.login);

// Protected routes
router.get('/profile', authMiddleware, authController.getProfile);
router.put('/password', authMiddleware, passwordUpdateValidation, authController.updatePassword);
router.post('/change-password', authMiddleware, passwordUpdateValidation, authController.updatePassword);

module.exports = router;