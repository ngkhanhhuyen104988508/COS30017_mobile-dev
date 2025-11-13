// backend/src/controllers/statsController.js
const db = require('../config/database');

// Get Mood Statistics
exports.getMoodStats = async (req, res) => {
  try {
    const userId = req.userId;
    const { period = '7d' } = req.query;

    let dateFilter = '';
    const params = [userId];

    if (period !== 'all') {
      const days = parseInt(period);
      dateFilter = `AND entry_date >= DATE_SUB(CURDATE(), INTERVAL ${days} DAY)`;
    }

    // Mood distribution
    const [distribution] = await db.query(
      `SELECT mood_type, COUNT(*) as count 
       FROM mood_entries 
       WHERE user_id = ? ${dateFilter}
       GROUP BY mood_type
       ORDER BY count DESC`,
      params
    );

    // Daily trend
    const [trend] = await db.query(
      `SELECT 
         entry_date,
         mood_type,
         COUNT(*) as count
       FROM mood_entries
       WHERE user_id = ? AND entry_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
       GROUP BY entry_date, mood_type
       ORDER BY entry_date ASC`,
      [userId]
    );

    // Top activities
    const [activities] = await db.query(
      `SELECT 
         a.name,
         a.icon,
         COUNT(*) as frequency
       FROM mood_activities ma
       JOIN activities a ON ma.activity_id = a.id
       JOIN mood_entries m ON ma.mood_entry_id = m.id
       WHERE m.user_id = ? ${dateFilter}
       GROUP BY a.id, a.name, a.icon
       ORDER BY frequency DESC
       LIMIT 5`,
      params
    );

    // Total entries
    const [total] = await db.query(
      `SELECT COUNT(*) as totalEntries 
       FROM mood_entries 
       WHERE user_id = ? ${dateFilter}`,
      params
    );

    res.json({
      success: true,
      data: {
        distribution,
        trend,
        topActivities: activities,
        totalEntries: total[0].totalEntries,
        period
      }
    });
  } catch (error) {
    console.error('Get stats error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch statistics'
    });
  }
};

// Get Activities
exports.getActivities = async (req, res) => {
  try {
    const [activities] = await db.query(
      'SELECT id, name, icon FROM activities ORDER BY name'
    );
    
    res.json({
      success: true,
      data: activities
    });
  } catch (error) {
    console.error('Get activities error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch activities'
    });
  }
};

// Get Summary
exports.getSummary = async (req, res) => {
  try {
    const userId = req.userId;

    // Total moods
    const [total] = await db.query(
      'SELECT COUNT(*) as total FROM mood_entries WHERE user_id = ?',
      [userId]
    );

    // This week
    const [thisWeek] = await db.query(
      `SELECT COUNT(*) as count 
       FROM mood_entries 
       WHERE user_id = ? 
         AND entry_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)`,
      [userId]
    );

    // This month
    const [thisMonth] = await db.query(
      `SELECT COUNT(*) as count 
       FROM mood_entries 
       WHERE user_id = ? 
         AND entry_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)`,
      [userId]
    );

    // Most frequent mood
    const [mostFrequent] = await db.query(
      `SELECT mood_type, COUNT(*) as count
       FROM mood_entries
       WHERE user_id = ?
       GROUP BY mood_type
       ORDER BY count DESC
       LIMIT 1`,
      [userId]
    );

    res.json({
      success: true,
      data: {
        totalMoods: total[0].total,
        thisWeek: thisWeek[0].count,
        thisMonth: thisMonth[0].count,
        mostFrequentMood: mostFrequent[0] || null
      }
    });
  } catch (error) {
    console.error('Get summary error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch summary'
    });
  }
};