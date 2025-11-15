package com.ngkhhuyen.daily.data.repository

import androidx.lifecycle.LiveData
import com.ngkhhuyen.daily.data.local.MoodDao
import com.ngkhhuyen.daily.data.models.*
import com.ngkhhuyen.daily.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MoodRepository(
    private val apiService: ApiService,
    private val moodDao: MoodDao
) {
    // Get moods from local database (reactive with LiveData)
    fun getAllMoodsLive(): LiveData<List<MoodEntry>> {
        return moodDao.getAllMoods()
    }

    // Sync moods from backend to local database
    suspend fun syncMoodsFromBackend(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMoods()

            if (response.isSuccessful && response.body()?.success == true) {
                val apiMoods = response.body()?.data ?: emptyList()

                // Convert API moods to Room entities
                val localMoods = apiMoods.map { apiMood ->
                    MoodEntry(
                        serverId = apiMood.id,
                        moodType = MoodType.fromString(apiMood.moodType),
                        note = apiMood.note,
                        photoUrl = apiMood.photoUrl,
                        entryDate = apiMood.entryDate,
                        entryTime = apiMood.entryTime,
                        activities = apiMood.activities ?: emptyList(),
                        isSynced = true
                    )
                }

                // Clear old data and insert new
                moodDao.deleteAllMoods()
                moodDao.insertMoods(localMoods)

                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create mood (save locally first, then sync to backend)
    suspend fun createMood(mood: MoodEntry): Result<MoodEntry> = withContext(Dispatchers.IO) {
        try {
            //Save to local database first
            val localId = moodDao.insertMood(mood.copy(isSynced = false))

            //Try to sync with backend
            try {
                val request = MoodRequest(
                    moodType = mood.moodType.name.lowercase(),
                    note = mood.note,
                    photoUrl = mood.photoUrl,
                    entryDate = mood.entryDate,
                    entryTime = mood.entryTime,
                    activities = null // TODO: Add activities support
                )

                val response = apiService.createMood(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val serverId = response.body()?.data?.get("id")

                    // Update local entry with server ID
                    val syncedMood = mood.copy(
                        id = localId.toInt(),
                        serverId = serverId,
                        isSynced = true
                    )
                    moodDao.updateMood(syncedMood)

                    Result.success(syncedMood)
                } else {
                    // API failed but local save succeeded
                    Result.success(mood.copy(id = localId.toInt(), isSynced = false))
                }
            } catch (e: Exception) {
                // Network error - keep local copy
                Result.success(mood.copy(id = localId.toInt(), isSynced = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update mood
    suspend fun updateMood(mood: MoodEntry): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Update locally first
            moodDao.updateMood(mood.copy(isSynced = false))

            // Try to sync with backend
            if (mood.serverId != null) {
                try {
                    val request = MoodRequest(
                        moodType = mood.moodType.name.lowercase(),
                        note = mood.note,
                        photoUrl = mood.photoUrl,
                        entryDate = mood.entryDate,
                        entryTime = mood.entryTime,
                        activities = null
                    )

                    val response = apiService.updateMood(mood.serverId, request)

                    if (response.isSuccessful) {
                        moodDao.updateMood(mood.copy(isSynced = true))
                    }
                } catch (e: Exception) {
                    // Network error - keep local changes
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete mood
    suspend fun deleteMood(mood: MoodEntry): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete locally first
            moodDao.deleteMood(mood)

            // Try to delete from backend
            if (mood.serverId != null) {
                try {
                    apiService.deleteMood(mood.serverId)
                } catch (e: Exception) {
                    // Network error - already deleted locally
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync unsynced moods to backend
    suspend fun syncUnsyncedMoods(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val unsyncedMoods = moodDao.getUnsyncedMoods()
            var syncedCount = 0

            unsyncedMoods.forEach { mood ->
                try {
                    val request = MoodRequest(
                        moodType = mood.moodType.name.lowercase(),
                        note = mood.note,
                        photoUrl = mood.photoUrl,
                        entryDate = mood.entryDate,
                        entryTime = mood.entryTime,
                        activities = null
                    )

                    val response = apiService.createMood(request)

                    if (response.isSuccessful && response.body()?.success == true) {
                        val serverId = response.body()?.data?.get("id")
                        moodDao.updateMood(mood.copy(serverId = serverId, isSynced = true))
                        syncedCount++
                    }
                } catch (e: Exception) {
                    // Skip this mood, continue with others
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to get current date
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Helper function to get current time
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}