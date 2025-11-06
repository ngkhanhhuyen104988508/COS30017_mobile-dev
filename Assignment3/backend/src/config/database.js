// MySQL connection configuration
const mysql = require('mysql2/promise');

// Create connection pool
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME || 'dailybean',
  port: process.env.DB_PORT || 3306,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  enableKeepAlive: true,
  keepAliveInitialDelay: 0
});

// Test connection
(async () => {
  try {
    const connection = await pool.getConnection();
    console.log('✅ MySQL connected successfully');
    console.log(`📊 Database: ${process.env.DB_NAME}`);
    connection.release();
  } catch (err) {
    console.error('❌ MySQL connection failed:');
    console.error('Error:', err.message);
    console.error('Make sure:');
    console.error('1. MySQL server is running');
    console.error('2. Database "dailybean" exists');
    console.error('3. Credentials in .env are correct');
  }
})();

module.exports = pool;