package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngkhhuyen.daily.data.models.StatsData
import com.ngkhhuyen.daily.data.repository.StatsRepository
import kotlinx.coroutines.launch

class StatsViewModel(private val repository: StatsRepository) : ViewModel() {

    private val _statsData = MutableLiveData<StatsData>()
    val statsData: LiveData<StatsData> = _statsData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadStats(period: String = "7d") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getMoodStats(period)
                .onSuccess { stats ->
                    _statsData.value = stats
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Failed to load statistics"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

// ViewModel Factory
class StatsViewModelFactory(
    private val repository: StatsRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}