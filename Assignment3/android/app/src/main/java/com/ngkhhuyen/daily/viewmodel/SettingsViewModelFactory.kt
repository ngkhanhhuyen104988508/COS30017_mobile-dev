package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ngkhhuyen.daily.data.repository.AuthRepository
import com.ngkhhuyen.daily.data.repository.StatsRepository
import com.ngkhhuyen.daily.utils.PreferenceManager

class SettingsViewModelFactory(
    private val authRepository: AuthRepository,
    private val statsRepository: StatsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(authRepository, statsRepository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
    