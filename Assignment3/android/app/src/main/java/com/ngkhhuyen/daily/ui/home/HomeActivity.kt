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
import com.google.android.material.snackbar.Snackbar
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.local.AppDatabase
import com.ngkhhuyen.daily.data.models.MoodEntry
import com.ngkhhuyen.daily.data.models.MoodType
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.data.repository.MoodRepository
import com.ngkhhuyen.daily.databinding.ActivityHomeBinding
import com.ngkhhuyen.daily.ui.history.HistoryActivity
import com.ngkhhuyen.daily.viewmodel.MoodViewModel
import com.ngkhhuyen.daily.viewmodel.MoodViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.ngkhhuyen.daily.utils.ImagePickerHelper

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: MoodViewModel
    private var selectedMood: MoodType? = null
    private var selectedPhotoUri: Uri? = null
    private var selectedPhotoPath: String? = null
    private lateinit var moodAdapter: MoodHistoryAdapter

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            selectedPhotoPath = ImagePickerHelper.saveImageToInternalStorage(this, it)
            binding.btnAddPhoto.text = "✓ Photo Added"
            Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel with Repository
        val database = AppDatabase.getInstance(this)
        val moodRepository = MoodRepository(RetrofitClient.apiService, database.moodDao())
        val factory = MoodViewModelFactory(moodRepository)
        viewModel = ViewModelProvider(this, factory)[MoodViewModel::class.java]

        setupToolbar()
        setupMoodButtons()
        setupClickListeners()
        setupRecyclerView()
        observeData()
        updateDateDisplay()
    }

    //open image
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun checkPermissionAndOpenPicker() {

        openImagePicker()
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
        name.text = moodType.name.lowercase().replaceFirstChar { it.uppercase() }

        card.setCardBackgroundColor(getMoodColor(moodType))

        card.setOnClickListener {
            onMoodSelected(moodType, card)
        }

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
            //Toast.makeText(this, "Photo picker - Coming soon!", Toast.LENGTH_SHORT).show()
            checkPermissionAndOpenPicker()
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
                    Toast.makeText(this, "Statistics - Coming in Week 5!", Toast.LENGTH_SHORT).show()
                    false
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings - Coming in Week 6!", Toast.LENGTH_SHORT).show()
                    false
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodHistoryAdapter { mood ->
            Toast.makeText(this, "Mood: ${mood.moodType}", Toast.LENGTH_SHORT).show()
        }

        binding.rvRecentMoods.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = moodAdapter
        }
    }

    private fun observeData() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            // Show loading indicator if needed
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe success messages
        viewModel.successMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }

        // Observe moods from database
        viewModel.allMoods.observe(this) { moods ->
            moodAdapter.submitList(moods.take(3)) // Show only 3 recent
        }

        // Observe sync status
        viewModel.syncStatus.observe(this) { status ->
            // You can show sync status in toolbar or snackbar
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

        viewModel.createMood(entry)

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