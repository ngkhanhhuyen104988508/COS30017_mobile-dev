package com.ngkhhuyen.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ngkhhuyen.daily.data.local.MoodDao
import com.ngkhhuyen.daily.data.models.MoodEntry
import kotlinx.coroutines.launch

class MoodViewModel(private val moodDao: MoodDao) : ViewModel() {

    // Observe all moods
    val allMoods: LiveData<List<MoodEntry>> = moodDao.getAllMoods()

    // Observe recent moods (for home screen preview)
    val recentMoods: LiveData<List<MoodEntry>> = moodDao.getAllMoods()

    // Insert new mood entry
    fun insertMood(mood: MoodEntry) {
        viewModelScope.launch {
            moodDao.insertMood(mood)
        }
    }

    // Update mood entry
    fun updateMood(mood: MoodEntry) {
        viewModelScope.launch {
            moodDao.updateMood(mood)
        }
    }

    // Delete mood entry
    fun deleteMood(mood: MoodEntry) {
        viewModelScope.launch {
            moodDao.deleteMood(mood)
        }
    }

    // Delete mood by ID
    fun deleteMoodById(id: Int) {
        viewModelScope.launch {
            moodDao.deleteMoodById(id)
        }
    }

    // Get moods by date
    fun getMoodsByDate(date: String): LiveData<List<MoodEntry>> {
        return moodDao.getMoodsByDate(date)
    }
}
