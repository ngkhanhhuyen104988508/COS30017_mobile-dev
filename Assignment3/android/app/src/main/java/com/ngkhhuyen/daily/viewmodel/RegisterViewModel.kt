package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngkhhuyen.daily.data.models.ApiResponse
import com.ngkhhuyen.daily.data.models.AuthData
import com.ngkhhuyen.daily.data.models.RegisterRequest
import com.ngkhhuyen.daily.data.repository.MainRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val registerRepository = MainRepository();
    private val _registerState = MutableLiveData<ApiResponse<AuthData>>()
    val registerState: LiveData<ApiResponse<AuthData>> = _registerState
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            val response = registerRepository.registerUser(request)
            println(response.code())
            println(response.body())
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        _registerState.postValue(apiResponse)
                    }
                }
            }
        }

    }
}