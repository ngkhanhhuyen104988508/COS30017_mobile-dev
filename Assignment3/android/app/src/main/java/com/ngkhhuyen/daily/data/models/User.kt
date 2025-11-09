
package com.ngkhhuyen.daily.data.models

// User data from API response
data class User(
    val userId: Int,
    val email: String,
    val username: String,
    val token: String
)

// API Request models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

// API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)