// android/app/src/main/java/com/yourname/dailybean/ui/home/MoodHistoryAdapter.kt
package com.ngkhhuyen.daily.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ngkhhuyen.daily.data.models.MoodEntry
import com.ngkhhuyen.daily.data.models.MoodType
import com.ngkhhuyen.daily.databinding.ItemMoodHistoryBinding

class MoodHistoryAdapter(
    private val onItemClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodHistoryAdapter.MoodViewHolder>(MoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoodViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MoodViewHolder(
        private val binding: ItemMoodHistoryBinding,
        private val onItemClick: (MoodEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mood: MoodEntry) {
            binding.apply {
                tvEmoji.text = mood.moodType.emoji
                tvMoodName.text = mood.moodType.name.lowercase().capitalize()
                tvDate.text = mood.getDisplayDate()
                tvTime.text = mood.getDisplayTime()
                tvNote.text = mood.note ?: "No note"

                // Set mood color
                cardMood.setCardBackgroundColor(getMoodColor(mood.moodType))

                root.setOnClickListener {
                    onItemClick(mood)
                }
            }
        }

        private fun getMoodColor(mood: MoodType): Int {
            return when (mood) {
                MoodType.HAPPY -> Color.parseColor("#FFD54F")
                MoodType.SAD -> Color.parseColor("#64B5F6")
                MoodType.ANGRY -> Color.parseColor("#EF5350")
                MoodType.CALM -> Color.parseColor("#81C784")
                MoodType.ANXIOUS -> Color.parseColor("#BA68C8")
            }
        }
    }

    private class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}