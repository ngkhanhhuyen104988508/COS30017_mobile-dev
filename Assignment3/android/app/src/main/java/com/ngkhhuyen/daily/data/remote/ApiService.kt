package com.ngkhhuyen.daily.data.remote

import com.ngkhhuyen.daily.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    //  AUTH

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @GET("auth/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>

    @PUT("auth/password")
    suspend fun updatePassword(
        @Body request: Map<String, String>
    ): Response<ApiResponse<Unit>>

    //  MOODS

    @POST("moods")
    suspend fun createMood(
        @Body request: MoodRequest
    ): Response<ApiResponse<Map<String, Int>>>

    @GET("moods")
    suspend fun getMoods(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<MoodResponse>>>

    @GET("moods/{id}")
    suspend fun getMoodById(
        @Path("id") id: Int
    ): Response<ApiResponse<MoodResponse>>

    @PUT("moods/{id}")
    suspend fun updateMood(
        @Path("id") id: Int,
        @Body request: MoodRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("moods/{id}")
    suspend fun deleteMood(
        @Path("id") id: Int
    ): Response<ApiResponse<Unit>>

    //  STATISTICS

    @GET("stats/moods")
    suspend fun getMoodStats(
        @Query("period") period: String = "7d"
    ): Response<ApiResponse<StatsData>>

    @GET("stats/activities")
    suspend fun getActivities(): Response<ApiResponse<List<ActivityData>>>

    @GET("stats/summary")
    suspend fun getSummary(): Response<ApiResponse<Map<String, Any>>>

    //CHANGE PASS

    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>

    @GET("auth/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

}