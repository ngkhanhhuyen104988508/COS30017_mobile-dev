// backend/src/app.js - SIMPLE VERSION
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || '*',
  credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Log requests
app.use((req, res, next) => {
  console.log(`${req.method} ${req.path}`);
  next();
});

// Import database (test connection)
try {
  const db = require('./config/database');
  console.log('Database module loaded');
} catch (err) {
  console.error('Database error:', err.message);
}

// Import routes
let authRoutes, moodRoutes, statsRoutes;

try {
  authRoutes = require('./routes/authRoutes');
  console.log('Auth routes loaded');
} catch (err) {
  console.error('Auth routes error:', err.message);
}

try {
  moodRoutes = require('./routes/moodRoutes');
  console.log('Mood routes loaded');
} catch (err) {
  console.error('Mood routes error:', err.message);
}

try {
  statsRoutes = require('./routes/statsRoutes');
  console.log('Stats routes loaded');
} catch (err) {
  console.error('Stats routes error:', err.message);
}

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    message: '🎉 DailyBean API is running!',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
});

// API Routes
if (authRoutes) app.use('/api/auth', authRoutes);
if (moodRoutes) app.use('/api/moods', moodRoutes);
if (statsRoutes) app.use('/api/stats', statsRoutes);

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Route not found: ${req.method} ${req.path}`
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err.stack);
  res.status(err.status || 500).json({
    success: false,
    message: err.message || 'Internal Server Error'
  });
});

// Start server
app.listen(PORT, () => {
  console.log('================================');
  console.log('🚀 DailyBean Backend Server');
  console.log(`📍 Running on: http://localhost:${PORT}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log('================================');
  console.log('Available routes:');
  console.log('  GET  /health');
  console.log('  POST /api/auth/register');
  console.log('  POST /api/auth/login');
  console.log('  GET  /api/auth/profile');
  console.log('  PUT  /api/auth/password');
  console.log('  POST /api/auth/change-password');
  console.log('  POST /api/moods');
  console.log('  GET  /api/moods');
  console.log('================================');
});

module.exports = app;