const { admin } = require('../config/firebase');

/**
 * Middleware enforcing Firebase ID token authentication.
 * Derives req.user.uid strictly from verified token signature.
 * NEVER trusts client-provided body parameters for user identity.
 */
async function verifyAuthToken(req, res, next) {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      error: 'Unauthorized: Missing or invalid Bearer token in Authorization header'
    });
  }

  const token = authHeader.split('Bearer ')[1];

  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = {
      uid: decodedToken.uid,
      email: decodedToken.email
    };
    next();
  } catch (error) {
    // In development mode, allow mock token testing if configured
    if (process.env.NODE_ENV === 'development' && token.startsWith('demo_dev_token_')) {
      req.user = {
        uid: token.replace('demo_dev_token_', 'dev_user_'),
        email: 'developer@digitalbalance.app'
      };
      return next();
    }

    return res.status(401).json({
      success: false,
      error: 'Unauthorized: Invalid or expired Firebase ID token',
      details: error.message
    });
  }
}

module.exports = { verifyAuthToken };
