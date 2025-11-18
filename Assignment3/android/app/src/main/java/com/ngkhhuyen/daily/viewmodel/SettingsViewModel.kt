package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngkhhuyen.daily.data.repository.AuthRepository
import com.ngkhhuyen.daily.data.repository.StatsRepository
import com.ngkhhuyen.daily.utils.PreferenceManager
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val statsRepository: StatsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // User info LiveData
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> = _userId

    private val _userAvatarUrl = MutableLiveData<String?>()
    val userAvatarUrl: LiveData<String?> = _userAvatarUrl

    // Stats LiveData
    private val _recordedDays = MutableLiveData<Int>()
    val recordedDays: LiveData<Int> = _recordedDays

    private val _photoCount = MutableLiveData<Int>()
    val photoCount: LiveData<Int> = _photoCount

    // Settings LiveData
    private val _isPremiumUser = MutableLiveData<Boolean>()
    val isPremiumUser: LiveData<Boolean> = _isPremiumUser

    private val _themeCalendarSetting = MutableLiveData<String>()
    val themeCalendarSetting: LiveData<String> = _themeCalendarSetting

    // Load all information when screen is created
    fun loadAllInfo() {
        viewModelScope.launch {
            _isLoading.value = true

            // Load user info from PreferenceManager
            loadUserInfoFromPreferences()

            // Load stats from API
            loadStatsFromApi()

            // Load settings from PreferenceManager
            loadSettingsFromPreferences()

            _isLoading.value = false
        }
    }

    private fun loadUserInfoFromPreferences() {
        _userName.value = preferenceManager.getUsername() ?: "User"
        _userEmail.value = preferenceManager.getEmail() ?: ""
        _userId.value = preferenceManager.getUserId().toString()
        // Avatar URL can be added when profile picture feature is implemented
        _userAvatarUrl.value = null
    }

    private suspend fun loadStatsFromApi() {
        try {
            val result = statsRepository.getMoodStats("all")

            result.onSuccess { stats ->
                // Calculate recorded days (unique dates)
                val uniqueDates = stats.trend.map { it.entryDate }.distinct().size
                _recordedDays.value = uniqueDates

                // Calculate photo count
                // use total entries as a placeholder
                _photoCount.value = stats.totalEntries
            }.onFailure { error ->
                _errorMessage.value = "Failed to load statistics: ${error.message}"
                // Set default values
                _recordedDays.value = 0
                _photoCount.value = 0
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error loading statistics"
            _recordedDays.value = 0
            _photoCount.value = 0
        }
    }

    private fun loadSettingsFromPreferences() {
        // Load theme setting from PreferenceManager
        _themeCalendarSetting.value = preferenceManager.getThemeSetting()

        // Load premium status from PreferenceManager
        _isPremiumUser.value = preferenceManager.isPremiumUser()
    }

    // Save theme calendar setting
    fun saveThemeCalendarSetting(theme: String) {
        viewModelScope.launch {
            try {
                preferenceManager.saveThemeSetting(theme)
                _themeCalendarSetting.value = theme
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save theme setting"
            }
        }
    }

    // Logout function
    fun logout() {
        authRepository.logout()
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }
}