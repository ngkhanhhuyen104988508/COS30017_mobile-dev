// android/app/src/main/java/com/yourname/dailybean/data/local/MoodDao.kt
package com.ngkhhuyen.daily.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ngkhhuyen.daily.data.models.MoodEntry

@Dao
interface MoodDao {
    // Get all moods (reactive with LiveData)
    @Query("SELECT * FROM mood_entries ORDER BY entryDate DESC, entryTime DESC")
    fun getAllMoods(): LiveData<List<MoodEntry>>

    // Get moods by date range
    @Query("""
        SELECT * FROM mood_entries 
        WHERE entryDate BETWEEN :startDate AND :endDate 
        ORDER BY entryDate DESC, entryTime DESC
    """)
    fun getMoodsByDateRange(startDate: String, endDate: String): LiveData<List<MoodEntry>>

    // Get moods by specific date
    @Query("SELECT * FROM mood_entries WHERE entryDate = :date ORDER BY entryTime DESC")
    fun getMoodsByDate(date: String): LiveData<List<MoodEntry>>

    // Get single mood by ID
    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getMoodById(id: Int): MoodEntry?

    // Insert new mood
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry): Long

    // Insert multiple moods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<MoodEntry>)

    // Update existing mood
    @Update
    suspend fun updateMood(mood: MoodEntry)

    // Delete mood
    @Delete
    suspend fun deleteMood(mood: MoodEntry)

    // Delete mood by ID
    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodById(id: Int)

    // Delete all moods
    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoods()

    // Get unsynced moods (for backend sync)
    @Query("SELECT * FROM mood_entries WHERE isSynced = 0")
    suspend fun getUnsyncedMoods(): List<MoodEntry>

    // Count total moods
    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getMoodCount(): Int
}