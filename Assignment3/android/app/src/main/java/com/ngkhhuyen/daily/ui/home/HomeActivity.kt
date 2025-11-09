package com.ngkhhuyen.daily.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.local.AppDatabase
import com.ngkhhuyen.daily.data.models.MoodEntry
import com.ngkhhuyen.daily.data.models.MoodType
import com.ngkhhuyen.daily.databinding.ActivityHomeBinding
import com.ngkhhuyen.daily.ui.history.HistoryActivity
import com.ngkhhuyen.daily.viewmodel.MoodViewModel
import com.ngkhhuyen.daily.viewmodel.MoodViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: MoodViewModel
    private var selectedMood: MoodType? = null
    private lateinit var moodAdapter: MoodHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val database = AppDatabase.getInstance(this)
        val factory = MoodViewModelFactory(database.moodDao())
        viewModel = ViewModelProvider(this, factory)[MoodViewModel::class.java]

        setupToolbar()
        setupMoodButtons()
        setupClickListeners()
        setupRecyclerView()
        observeData()
        updateDateDisplay()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupMoodButtons() {
        val moods = listOf(
            MoodType.HAPPY,
            MoodType.SAD,
            MoodType.ANGRY,
            MoodType.CALM,
            MoodType.ANXIOUS
        )

        moods.forEach { mood ->
            val moodView = createMoodButton(mood)
            binding.layoutMoodButtons.addView(moodView)
        }
    }

    private fun createMoodButton(moodType: MoodType): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_mood_button, binding.layoutMoodButtons, false)

        val card = view.findViewById<MaterialCardView>(R.id.cardMood)
        val emoji = view.findViewById<TextView>(R.id.tvEmoji)
        val name = view.findViewById<TextView>(R.id.tvMoodName)

        emoji.text = moodType.emoji
        name.text = moodType.name.lowercase().capitalize()

        // Set background color
        card.setCardBackgroundColor(getMoodColor(moodType))

        // Click listener
        card.setOnClickListener {
            onMoodSelected(moodType, card)
        }

        // Layout params for equal spacing
        val params = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        view.layoutParams = params

        return view
    }

    private fun onMoodSelected(mood: MoodType, selectedCard: MaterialCardView) {
        // Reset all cards
        for (i in 0 until binding.layoutMoodButtons.childCount) {
            val child = binding.layoutMoodButtons.getChildAt(i)
            val card = child.findViewById<MaterialCardView>(R.id.cardMood)
            card.strokeWidth = 0
        }

        // Highlight selected
        selectedCard.strokeWidth = 8
        selectedCard.strokeColor = Color.parseColor("#6C63FF")

        selectedMood = mood
        binding.cardNoteInput.visibility = View.VISIBLE
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveMoodEntry()
        }

        binding.btnAddPhoto.setOnClickListener {
            // TODO: Implement photo picker
            Toast.makeText(this, "Photo picker - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    false
                }
                R.id.nav_statistics -> {
                    Toast.makeText(this, "Statistics - Coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings - Coming soon!", Toast.LENGTH_SHORT).show()
                    false
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodHistoryAdapter { mood ->
            // Handle mood item click
            Toast.makeText(this, "Mood: ${mood.moodType}", Toast.LENGTH_SHORT).show()
        }

        binding.rvRecentMoods.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = moodAdapter
        }
    }

    private fun observeData() {
        viewModel.recentMoods.observe(this) { moods ->
            moodAdapter.submitList(moods.take(3)) // Show only 3 recent
        }
    }

    private fun saveMoodEntry() {
        val mood = selectedMood ?: return
        val note = binding.etNote.text.toString().trim()

        val entry = MoodEntry(
            moodType = mood,
            note = note.ifEmpty { null },
            entryDate = getCurrentDate(),
            entryTime = getCurrentTime()
        )

        viewModel.insertMood(entry)

        Toast.makeText(this, getString(R.string.success_mood_saved), Toast.LENGTH_SHORT).show()

        // Reset form
        binding.etNote.text?.clear()
        binding.cardNoteInput.visibility = View.GONE
        selectedMood = null

        // Reset mood buttons
        for (i in 0 until binding.layoutMoodButtons.childCount) {
            val child = binding.layoutMoodButtons.getChildAt(i)
            val card = child.findViewById<MaterialCardView>(R.id.cardMood)
            card.strokeWidth = 0
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
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