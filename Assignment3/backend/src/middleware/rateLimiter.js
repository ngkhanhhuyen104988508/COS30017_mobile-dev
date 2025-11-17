// backend/src/middleware/rateLimiter.js

// Simple in-memory rate limiter
class RateLimiter {
  constructor(windowMs, maxRequests) {
    this.windowMs = windowMs;
    this.maxRequests = maxRequests;
    this.requests = new Map();
  }

  middleware() {
    return (req, res, next) => {
      const key = req.ip || req.connection.remoteAddress;
      const now = Date.now();

      if (!this.requests.has(key)) {
        this.requests.set(key, []);
      }

      const userRequests = this.requests.get(key);
      
      // Remove old requests outside the time window
      const validRequests = userRequests.filter(
        timestamp => now - timestamp < this.windowMs
      );

      if (validRequests.length >= this.maxRequests) {
        return res.status(429).json({
          success: false,
          message: 'Too many requests. Please try again later.',
          retryAfter: Math.ceil(this.windowMs / 1000)
        });
      }

      validRequests.push(now);
      this.requests.set(key, validRequests);

      next();
    };
  }

  // Clean up old entries periodically
  cleanup() {
    const now = Date.now();
    for (const [key, timestamps] of this.requests.entries()) {
      const validRequests = timestamps.filter(
        timestamp => now - timestamp < this.windowMs
      );
      if (validRequests.length === 0) {
        this.requests.delete(key);
      } else {
        this.requests.set(key, validRequests);
      }
    }
  }
}

// Rate limiters for different endpoints
const authLimiter = new RateLimiter(
  15 * 60 * 1000, // 15 minutes
  5 // 5 requests per window
);

const apiLimiter = new RateLimiter(
  60 * 1000, // 1 minute
  30 // 30 requests per window
);

// Cleanup every 5 minutes
setInterval(() => {
  authLimiter.cleanup();
  apiLimiter.cleanup();
}, 5 * 60 * 1000);

module.exports = {
  authLimiter: authLimiter.middleware(),
  apiLimiter: apiLimiter.middleware()
};