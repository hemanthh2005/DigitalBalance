require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const { initializeFirebase } = require('./config/firebase');
const { errorHandler } = require('./middleware/errorHandler');
const usageRoutes = require('./routes/usageRoutes');
const reportsRoutes = require('./routes/reportsRoutes');

initializeFirebase();

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '10mb' }));
if (process.env.NODE_ENV !== 'test') {
  app.use(morgan('dev'));
}

// Health Check Endpoint
app.get('/api/health', (req, res) => {
  res.status(200).json({
    status: 'ONLINE',
    system: 'DigitalBalance Backend Node.js API',
    timestamp: new Date().toISOString()
  });
});

// API Routes
app.use('/api/usage', usageRoutes);
app.use('/api', reportsRoutes);

// Centralized Error Handling
app.use(errorHandler);

module.exports = app;
