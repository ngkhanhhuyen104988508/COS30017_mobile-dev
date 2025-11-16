package com.ngkhhuyen.daily.ui.history

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.local.AppDatabase
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.data.repository.MoodRepository
import com.ngkhhuyen.daily.databinding.ActivityHistoryBinding
import com.ngkhhuyen.daily.ui.home.MoodHistoryAdapter
import com.ngkhhuyen.daily.viewmodel.MoodViewModel
import com.ngkhhuyen.daily.viewmodel.MoodViewModelFactory
import java.io.File

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: MoodViewModel
    private lateinit var adapter: MoodHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel with Repository
        val database = AppDatabase.getInstance(this)
        val moodRepository = MoodRepository(RetrofitClient.apiService, database.moodDao())
        val factory = MoodViewModelFactory(moodRepository)
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
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood_detail, null)

        // Set mood info
        dialogView.findViewById<TextView>(R.id.tvMoodTitle).text =
            "${mood.moodType.emoji} ${mood.moodType.name}"
        dialogView.findViewById<TextView>(R.id.tvDate).text =
            "Date: ${mood.getDisplayDate()}"
        dialogView.findViewById<TextView>(R.id.tvTime).text =
            "Time: ${mood.getDisplayTime()}"
        dialogView.findViewById<TextView>(R.id.tvNote).text =
            mood.note ?: "No note"

        // Load photo if available
        val photoView = dialogView.findViewById<ImageView>(R.id.ivPhoto)
        if (!mood.photoUrl.isNullOrEmpty()) {
            val file = File(mood.photoUrl)
            if (file.exists()) {
                photoView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(file)
                    .centerCrop()
                    .into(photoView)
            } else {
                photoView.visibility = View.GONE
            }
        } else {
            photoView.visibility = View.GONE
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .setNeutralButton("Edit") { _, _ ->
                editMood(mood)
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteMood(mood)
            }
            .show()
    }

    private fun editMood(mood: com.ngkhhuyen.daily.data.models.MoodEntry) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_mood, null)
        val moodPicker = dialogView.findViewById<LinearLayout>(R.id.layoutMoodPicker)
        val noteInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNote)

        // Set current note
        noteInput.setText(mood.note)

        // Create mood buttons
        var selectedMoodType = mood.moodType
        val moods = listOf(
            com.ngkhhuyen.daily.data.models.MoodType.HAPPY,
            com.ngkhhuyen.daily.data.models.MoodType.SAD,
            com.ngkhhuyen.daily.data.models.MoodType.ANGRY,
            com.ngkhhuyen.daily.data.models.MoodType.CALM,
            com.ngkhhuyen.daily.data.models.MoodType.ANXIOUS
        )

        val moodButtons = mutableListOf<TextView>()
        moods.forEach { moodType ->
            val button = TextView(this).apply {
                text = moodType.emoji
                textSize = 32f
                setPadding(24, 24, 24, 24)
                setBackgroundResource(if (moodType == selectedMoodType)
                    R.color.primary else android.R.color.transparent)
                setOnClickListener {
                    selectedMoodType = moodType
                    moodButtons.forEach { btn ->
                        btn.setBackgroundResource(android.R.color.transparent)
                    }
                    setBackgroundResource(R.color.primary)
                }
            }
            moodButtons.add(button)
            moodPicker.addView(button)
        }
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedMood = mood.copy(
                    moodType = selectedMoodType,
                    note = noteInput.text.toString().trim().ifEmpty { null }
                )
                viewModel.updateMood(updatedMood)
                Toast.makeText(this, "Mood updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
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