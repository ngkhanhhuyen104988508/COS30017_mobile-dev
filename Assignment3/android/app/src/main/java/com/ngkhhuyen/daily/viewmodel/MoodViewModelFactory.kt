package com.ngkhhuyen.daily.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ngkhhuyen.daily.data.local.MoodDao

class MoodViewModelFactory(private val moodDao: MoodDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodViewModel(moodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
