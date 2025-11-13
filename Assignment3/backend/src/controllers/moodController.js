// backend/src/controllers/moodController.js
const db = require('../config/database');

const VALID_MOODS = ['happy', 'sad', 'angry', 'calm', 'anxious'];

// Create Mood
exports.createMood = async (req, res) => {
  try {
    const { moodType, note, photoUrl, entryDate, entryTime, activities } = req.body;
    const userId = req.userId;

    if (!moodType || !entryDate) {
      return res.status(400).json({
        success: false,
        message: 'Mood type and entry date are required'
      });
    }

    if (!VALID_MOODS.includes(moodType.toLowerCase())) {
      return res.status(400).json({
        success: false,
        message: 'Invalid mood type'
      });
    }

    const timeToUse = entryTime || new Date().toTimeString().split(' ')[0];

    // Insert mood
    const [result] = await db.query(
      'INSERT INTO mood_entries (user_id, mood_type, note, photo_url, entry_date, entry_time) VALUES (?, ?, ?, ?, ?, ?)',
      [userId, moodType.toLowerCase(), note || null, photoUrl || null, entryDate, timeToUse]
    );

    const moodId = result.insertId;

    // Add activities
    if (activities && Array.isArray(activities) && activities.length > 0) {
      const activityValues = activities.map(actId => [moodId, actId]);
      await db.query(
        'INSERT INTO mood_activities (mood_entry_id, activity_id) VALUES ?',
        [activityValues]
      );
    }

    res.status(201).json({
      success: true,
      message: 'Mood entry created successfully',
      data: { id: moodId }
    });
  } catch (error) {
    console.error('Create mood error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to create mood entry'
    });
  }
};

// Get All Moods
exports.getMoods = async (req, res) => {
  try {
    const userId = req.userId;
    const { startDate, endDate, limit = 50, offset = 0 } = req.query;

    let query = `
      SELECT m.*, GROUP_CONCAT(a.name) as activities
      FROM mood_entries m
      LEFT JOIN mood_activities ma ON m.id = ma.mood_entry_id
      LEFT JOIN activities a ON ma.activity_id = a.id
      WHERE m.user_id = ?
    `;
    const params = [userId];

    if (startDate) {
      query += ' AND m.entry_date >= ?';
      params.push(startDate);
    }
    if (endDate) {
      query += ' AND m.entry_date <= ?';
      params.push(endDate);
    }

    query += ' GROUP BY m.id ORDER BY m.entry_date DESC, m.entry_time DESC LIMIT ? OFFSET ?';
    params.push(parseInt(limit), parseInt(offset));

    const [moods] = await db.query(query, params);

    const formattedMoods = moods.map(mood => ({
      id: mood.id,
      moodType: mood.mood_type,
      note: mood.note,
      photoUrl: mood.photo_url,
      entryDate: mood.entry_date,
      entryTime: mood.entry_time,
      activities: mood.activities ? mood.activities.split(',') : [],
      createdAt: mood.created_at
    }));

    res.json({
      success: true,
      data: formattedMoods
    });
  } catch (error) {
    console.error('Get moods error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch moods'
    });
  }
};

// Get Mood by ID
exports.getMoodById = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.userId;

    const [moods] = await db.query(
      `SELECT m.*, GROUP_CONCAT(a.name) as activities
       FROM mood_entries m
       LEFT JOIN mood_activities ma ON m.id = ma.mood_entry_id
       LEFT JOIN activities a ON ma.activity_id = a.id
       WHERE m.id = ? AND m.user_id = ?
       GROUP BY m.id`,
      [id, userId]
    );

    if (moods.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Mood entry not found'
      });
    }

    const mood = moods[0];
    res.json({
      success: true,
      data: {
        id: mood.id,
        moodType: mood.mood_type,
        note: mood.note,
        photoUrl: mood.photo_url,
        entryDate: mood.entry_date,
        entryTime: mood.entry_time,
        activities: mood.activities ? mood.activities.split(',') : [],
        createdAt: mood.created_at
      }
    });
  } catch (error) {
    console.error('Get mood by ID error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch mood'
    });
  }
};

// Update Mood
exports.updateMood = async (req, res) => {
  try {
    const { id } = req.params;
    const { moodType, note, photoUrl } = req.body;
    const userId = req.userId;

    const updates = [];
    const params = [];

    if (moodType) {
      if (!VALID_MOODS.includes(moodType.toLowerCase())) {
        return res.status(400).json({
          success: false,
          message: 'Invalid mood type'
        });
      }
      updates.push('mood_type = ?');
      params.push(moodType.toLowerCase());
    }
    if (note !== undefined) {
      updates.push('note = ?');
      params.push(note);
    }
    if (photoUrl !== undefined) {
      updates.push('photo_url = ?');
      params.push(photoUrl);
    }

    if (updates.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'No fields to update'
      });
    }

    params.push(id, userId);

    const [result] = await db.query(
      `UPDATE mood_entries SET ${updates.join(', ')} WHERE id = ? AND user_id = ?`,
      params
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        success: false,
        message: 'Mood entry not found'
      });
    }

    res.json({
      success: true,
      message: 'Mood entry updated successfully'
    });
  } catch (error) {
    console.error('Update mood error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to update mood entry'
    });
  }
};

// Delete Mood
exports.deleteMood = async (req, res) => {
  try {
    const { id } = req.params;
    const userId = req.userId;

    const [result] = await db.query(
      'DELETE FROM mood_entries WHERE id = ? AND user_id = ?',
      [id, userId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({
        success: false,
        message: 'Mood entry not found'
      });
    }

    res.json({
      success: true,
      message: 'Mood entry deleted successfully'
    });
  } catch (error) {
    console.error('Delete mood error:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to delete mood entry'
    });
  }
};