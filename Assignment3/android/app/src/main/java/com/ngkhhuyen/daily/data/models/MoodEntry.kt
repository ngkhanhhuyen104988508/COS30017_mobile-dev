package com.ngkhhuyen.daily.data.models

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Mood types enum
enum class MoodType(val emoji: String, val colorRes: Int) {
    HAPPY("😊", R.color.holo_orange_light),
    SAD("😢", R.color.holo_blue_light),
    ANGRY("😠", R.color.holo_red_light),
    CALM("😌", R.color.holo_green_light),
    ANXIOUS("😰", R.color.holo_purple);

    companion object {
        fun fromString(value: String): MoodType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: CALM
        }
    }
}

// Room Entity for local database
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val serverId: Int? = null,          // ID from backend
    val moodType: MoodType,
    val note: String? = null,
    val photoUrl: String? = null,
    val entryDate: String,
    val entryTime: String,
    val activities: List<String> = emptyList(),
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper function to get display date
    fun getDisplayDate(): String {
        return try {
            val parts = entryDate.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            entryDate
        }
    }

    // Helper function to get display time
    fun getDisplayTime(): String {
        return try {
            entryTime.substring(0, 5)
        } catch (e: Exception) {
            entryTime
        }
    }
}

// Type converters for Room (to store complex types)
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMoodType(value: MoodType): String {
        return value.name
    }

    @TypeConverter
    fun toMoodType(value: String): MoodType {
        return MoodType.fromString(value)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}