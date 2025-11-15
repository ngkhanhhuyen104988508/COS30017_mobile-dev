package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngkhhuyen.daily.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Login success
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    // Register success
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    init {
        // Initialize token if user is already logged in
        repository.initializeToken()
    }

    // Register
    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.register(email, password, username)
                .onSuccess {
                    _registerSuccess.value = true
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Registration failed"
                    _registerSuccess.value = false
                    _isLoading.value = false
                }
        }
    }

    // Login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.login(email, password)
                .onSuccess {
                    _loginSuccess.value = true
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Login failed"
                    _loginSuccess.value = false
                    _isLoading.value = false
                }
        }
    }

    // Logout
    fun logout() {
        repository.logout()
    }

    // Check if logged in
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }

    // Clear error
    fun clearError() {
        _errorMessage.value = null
    }
}