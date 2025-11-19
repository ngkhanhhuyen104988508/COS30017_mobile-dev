package com.ngkhhuyen.daily.data.models
import com.google.gson.annotations.SerializedName

// User data from API response
// Generic API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
// Auth Requests
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

// Auth Response
data class AuthData(
    val userId: Int,
    val email: String,
    val username: String,
    val token: String
)

// Mood Request
data class MoodRequest(
    val moodType: String,
    val note: String?,
    val photoUrl: String?,
    val entryDate: String,
    val entryTime: String,
    val activities: List<Int>?
)

// Mood Response from API
data class MoodResponse(
    val id: Int,
    val moodType: String,
    val note: String?,
    val photoUrl: String?,
    val entryDate: String,
    val entryTime: String,
    val activities: List<String>?,
    val createdAt: String?
)

// Statistics Response
data class StatsData(
    val distribution: List<MoodDistribution>,
    val trend: List<MoodTrend>,
    val topActivities: List<ActivityFrequency>,
    val totalEntries: Int,
    val period: String
)

data class MoodDistribution(
    @SerializedName("mood_type")
    val moodType: String,
    val count: Int
)

data class MoodTrend(
    @SerializedName("entry_date")
    val entryDate: String,
    @SerializedName("mood_type")
    val moodType: String,
    val count: Int
)

data class ActivityFrequency(
    val name: String,
    val icon: String,
    val frequency: Int
)

// Activity from API
data class ActivityData(
    val id: Int,
    val name: String,
    val icon: String
)
//change pass
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val createdAt: String
)

data class MessageResponse(
    val message: String
)
