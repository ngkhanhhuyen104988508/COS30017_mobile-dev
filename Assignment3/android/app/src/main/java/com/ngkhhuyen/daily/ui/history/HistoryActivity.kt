// android/app/src/main/java/com/yourname/dailybean/ui/history/HistoryActivity.kt
package com.ngkhhuyen.daily.ui.history

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.local.AppDatabase
import com.ngkhhuyen.daily.databinding.ActivityHistoryBinding
import com.ngkhhuyen.daily.ui.home.MoodHistoryAdapter
import com.ngkhhuyen.daily.viewmodel.MoodViewModel
import com.ngkhhuyen.daily.viewmodel.MoodViewModelFactory

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: MoodViewModel
    private lateinit var adapter: MoodHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val database = AppDatabase.getInstance(this)
        val factory = MoodViewModelFactory(database.moodDao())
        viewModel = ViewModelProvider(this, factory)[MoodViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = MoodHistoryAdapter { mood ->
            // Show options dialog when item clicked
            showMoodOptionsDialog(mood)
        }

        binding.rvMoodHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }

        // Add swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val mood = adapter.currentList[position]

                // Show confirmation dialog
                AlertDialog.Builder(this@HistoryActivity)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this mood entry?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteMood(mood)
                        Toast.makeText(
                            this@HistoryActivity,
                            getString(R.string.success_mood_deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // Restore the item
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        // Restore the item if dialog dismissed
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvMoodHistory)
    }

    private fun observeData() {
        viewModel.allMoods.observe(this) { moods ->
            if (moods.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvMoodHistory.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvMoodHistory.visibility = View.VISIBLE
                adapter.submitList(moods)
            }
        }
    }

    private fun showMoodOptionsDialog(mood: com.ngkhhuyen.daily.data.models.MoodEntry) {
        val options = arrayOf("View Details", "Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Mood Entry")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showMoodDetails(mood)
                    1 -> editMood(mood)
                    2 -> deleteMood(mood)
                }
            }
            .show()
    }

    private fun showMoodDetails(mood: com.ngkhhuyen.daily.data.models.MoodEntry) {
        AlertDialog.Builder(this)
            .setTitle("${mood.moodType.emoji} ${mood.moodType.name}")
            .setMessage("""
                Date: ${mood.getDisplayDate()}
                Time: ${mood.getDisplayTime()}
                Note: ${mood.note ?: "No note"}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun editMood(mood: com.ngkhhuyen.daily.data.models.MoodEntry) {
        // TODO: Implement edit functionality in future
        Toast.makeText(this, "Edit - Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun deleteMood(mood: com.ngkhhuyen.daily.data.models.MoodEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMood(mood)
                Toast.makeText(this, getString(R.string.success_mood_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}