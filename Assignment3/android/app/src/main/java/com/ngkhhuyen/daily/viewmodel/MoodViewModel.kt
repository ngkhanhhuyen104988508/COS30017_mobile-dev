package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngkhhuyen.daily.data.models.MoodEntry
import com.ngkhhuyen.daily.data.repository.MoodRepository
import kotlinx.coroutines.launch

class MoodViewModel(private val repository: MoodRepository) : ViewModel() {

    // LiveData for moods from local database
    val allMoods: LiveData<List<MoodEntry>> = repository.getAllMoodsLive()

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Success message
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // Sync status
    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    init {
        // Auto-sync on initialization
        syncMoodsFromBackend()
    }

    // Sync moods from backend
    fun syncMoodsFromBackend() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Syncing..."

            repository.syncMoodsFromBackend()
                .onSuccess {
                    _syncStatus.value = "Synced"
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = "Sync failed: ${e.message}"
                    _syncStatus.value = "Sync failed"
                    _isLoading.value = false
                }
        }
    }

    // Create new mood
    fun createMood(mood: MoodEntry) {
        viewModelScope.launch {
            _isLoading.value = true

            repository.createMood(mood)
                .onSuccess {
                    _successMessage.value = "Mood saved!"
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = "Failed to save: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    // Update mood
    fun updateMood(mood: MoodEntry) {
        viewModelScope.launch {
            _isLoading.value = true

            repository.updateMood(mood)
                .onSuccess {
                    _successMessage.value = "Mood updated!"
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = "Failed to update: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    // Delete mood
    fun deleteMood(mood: MoodEntry) {
        viewModelScope.launch {
            _isLoading.value = true

            repository.deleteMood(mood)
                .onSuccess {
                    _successMessage.value = "Mood deleted!"
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = "Failed to delete: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    // Sync unsynced moods
    fun syncUnsyncedMoods() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "Syncing unsynced moods..."

            repository.syncUnsyncedMoods()
                .onSuccess { count ->
                    _successMessage.value = "Synced $count moods"
                    _syncStatus.value = "Synced"
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _errorMessage.value = "Sync failed: ${e.message}"
                    _syncStatus.value = "Sync failed"
                    _isLoading.value = false
                }
        }
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }

    // Clear success message
    fun clearSuccess() {
        _successMessage.value = null
    }
}