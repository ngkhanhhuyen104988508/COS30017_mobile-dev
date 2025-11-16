package com.ngkhhuyen.daily.data.repository

import com.ngkhhuyen.daily.data.models.StatsData
import com.ngkhhuyen.daily.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatsRepository(private val apiService: ApiService) {

    suspend fun getMoodStats(period: String = "7d"): Result<StatsData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMoodStats(period)

            if (response.isSuccessful && response.body()?.success == true) {
                val stats = response.body()?.data
                if (stats != null) {
                    Result.success(stats)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}