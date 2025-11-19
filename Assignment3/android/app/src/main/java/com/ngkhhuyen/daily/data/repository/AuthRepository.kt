package com.ngkhhuyen.daily.data.repository

import com.ngkhhuyen.daily.data.models.AuthData
import com.ngkhhuyen.daily.data.models.ChangePasswordRequest
import com.ngkhhuyen.daily.data.models.LoginRequest
import com.ngkhhuyen.daily.data.models.RegisterRequest
import com.ngkhhuyen.daily.data.models.UserProfileResponse
import com.ngkhhuyen.daily.data.remote.ApiService
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    // Register new user
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(email, password, username)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()?.data

                if (authData != null) {
                    // Save user data and token
                    preferenceManager.saveUserData(
                        userId = authData.userId,
                        email = authData.email,
                        username = authData.username,
                        token = authData.token
                    )

                    // Set token for future API calls
                    RetrofitClient.setAuthToken(authData.token)

                    Result.success(authData)
                } else {
                    Result.failure(Exception("No data received"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "Registration failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login user
    suspend fun login(
        email: String,
        password: String
    ): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()?.data

                if (authData != null) {
                    // Save user data and token
                    preferenceManager.saveUserData(
                        userId = authData.userId,
                        email = authData.email,
                        username = authData.username,
                        token = authData.token
                    )

                    // Set token for future API calls
                    RetrofitClient.setAuthToken(authData.token)

                    Result.success(authData)
                } else {
                    Result.failure(Exception("No data received"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "Login failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //Change password
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<String> {
        return try {
            val response = apiService.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to change password"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return try {
            val response = apiService.getUserProfile()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout user
    fun logout() {
        preferenceManager.clearUserData()
        RetrofitClient.setAuthToken(null)
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return preferenceManager.isLoggedIn()
    }

    // Get saved token and set it for API calls
    fun initializeToken() {
        val token = preferenceManager.getToken()
        if (token != null) {
            RetrofitClient.setAuthToken(token)
        }
    }
}