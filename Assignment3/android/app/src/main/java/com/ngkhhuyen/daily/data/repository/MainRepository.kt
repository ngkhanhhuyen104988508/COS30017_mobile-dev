package com.ngkhhuyen.daily.data.repository

import com.ngkhhuyen.daily.data.models.ApiResponse
import com.ngkhhuyen.daily.data.models.AuthData
import com.ngkhhuyen.daily.data.models.RegisterRequest
import retrofit2.Response

class MainRepository {
    private val api = RetrofitClient.apiService

    suspend fun registerUser(request: RegisterRequest): Response<ApiResponse<AuthData>> = api.register(request)

}