package com.example.renting.activities
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.renting.R
import com.example.renting.databinding.ActivityMainBinding
import com.example.renting.models.Instrument
import com.example.renting.repositories.InstrumentRepository
import com.google.android.material.snackbar.Snackbar

/**
 * - Display one instrument at a time
 * - Navigate between instruments (Previous/Next)
 * - Show booking status
 * - Launch booking activity
 *
 * Uses ViewBinding for type-safe view access
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding instance
    private lateinit var binding: ActivityMainBinding

    // Current instrument being displayed
    private var currentIndex = 0
    private var currentInstrument: Instrument? = null

    // User's available credits (in real app, this would come from user profile)
    private val userCredits = 120 // Hardcoded for demo purposes

    companion object {
        // Request codes for startActivityForResult
        const val REQUEST_CODE_BOOKING = 100

        // Result codes for booking activity
        const val RESULT_BOOKED = Activity.RESULT_FIRST_USER
        const val RESULT_CANCELLED = Activity.RESULT_FIRST_USER + 1

        // Intent extras
        const val EXTRA_INSTRUMENT = "extra_instrument"
        const val EXTRA_AVAILABLE_CREDITS = "extra_credits"

        private const val PREFS_NAME = "app_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository with sample data
        InstrumentRepository.initialize()

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Setup UI components
        setupClickListeners()

        // Display first instrument
        displayInstrument(currentIndex)

    }
    //Apply theme based on saved preference
    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)

        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
     override fun onCreateOptionsMenu(menu: Menu): Boolean {
         menuInflater.inflate(R.menu.menu_main, menu)
         return true
     }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                showThemeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //Show theme selection dialog
    private fun showThemeDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val currentTheme = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)

        val themes = arrayOf("Light Mode", "Dark Mode", "System Default")

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                // Save preference
                prefs.edit().putInt(KEY_THEME_MODE, which).apply()

                // Apply theme
                when (which) {
                    THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }

                dialog.dismiss()
            }
            .show()
    }

     //Setup click listeners for all interactive elements

    private fun setupClickListeners() {
        // Previous button
        binding.buttonPrevious.setOnClickListener {
            navigateToPrevious()
        }

        // Next button
        binding.buttonNext.setOnClickListener {
            navigateToNext()
        }

        // Borrow button
        binding.buttonBorrow.setOnClickListener {
            openBookingActivity()
        }
    }

    /**
     * Display instrument at given index
     * Updates all UI elements with instrument data
     *
     * @param index The position of instrument to display (0-based)
     */
    private fun displayInstrument(index: Int) {
        // Get instrument from repository
        val instrument = InstrumentRepository.getInstrumentAtIndex(index)

        if (instrument == null) {
            // Should not happen, but handle gracefully
            showError("Unable to load instrument")
            return
        }

        // Store current instrument
        currentInstrument = instrument
        currentIndex = index

        // Update UI elements
        with(binding) {
            // Image
            imageInstrument.setImageResource(instrument.imageResourceId)
            imageInstrument.contentDescription = getString(
                R.string.cd_instrument_image,
                instrument.name
            )

            // Name
            textInstrumentName.text = instrument.name

            // Rating
            ratingBarInstrument.rating = instrument.rating
            textRatingValue.text = String.format("%.1f", instrument.rating)

            // Condition - Set appropriate radio button
            when (instrument.condition) {
                "Excellent" -> radioGroupCondition.check(R.id.radioExcellent)
                "Good" -> radioGroupCondition.check(R.id.radioGood)
                "Fair" -> radioGroupCondition.check(R.id.radioFair)
            }

            // Price
            textPrice.text = instrument.getFormattedPrice()

            // Description
            textDescription.text = instrument.description

            // Booking status
            updateBookingStatus(instrument)

            // Page indicator
            val totalInstruments = InstrumentRepository.getInstrumentCount()
            textPageIndicator.text = getString(
                R.string.page_indicator,
                index + 1,
                totalInstruments
            )

            // Enable/disable navigation buttons
            updateNavigationButtons(index, totalInstruments)

            // Enable/disable borrow button based on booking status
            updateBorrowButton(instrument)
        }
    }

    /**
     * Update booking status display
     * Shows if instrument is available or already booked
     *
     * @param instrument The instrument to check status for
     */
    private fun updateBookingStatus(instrument: Instrument) {
        with(binding.textBookingStatus) {
            if (instrument.isBooked) {
                text = getString(R.string.status_booked, instrument.bookedByName ?: "Unknown")
                setTextColor(getColor(R.color.warning))
                // Apply StatusBooked style programmatically if needed
            } else {
                text = getString(R.string.status_available)
                setTextColor(getColor(R.color.success))
                // Apply StatusAvailable style programmatically if needed
            }
        }
    }

    /**
     * Update navigation button states
     * Disable Previous on first item, Next on last item
     *
     * @param currentIndex Current position
     * @param totalCount Total number of instruments
     */
    private fun updateNavigationButtons(currentIndex: Int, totalCount: Int) {
        binding.buttonPrevious.isEnabled = currentIndex > 0
        binding.buttonNext.isEnabled = currentIndex < totalCount - 1
    }

    /**
     * Update borrow button based on booking status
     * Disable if already booked, change text accordingly
     *
     * @param instrument The instrument to check
     */
    private fun updateBorrowButton(instrument: Instrument) {
        with(binding.buttonBorrow) {
            if (instrument.isBooked) {
                isEnabled = false
                text = "Already Booked"
                alpha = 0.5f
            } else {
                isEnabled = true
                text = getString(R.string.button_borrow)
                alpha = 1.0f
            }
        }
    }

    /**
     * Navigate to previous instrument
     */
    private fun navigateToPrevious() {
        if (currentIndex > 0) {
            displayInstrument(currentIndex - 1)
        }
    }

    /**
     * Navigate to next instrument
     */
    private fun navigateToNext() {
        val totalCount = InstrumentRepository.getInstrumentCount()
        if (currentIndex < totalCount - 1) {
            displayInstrument(currentIndex + 1)
        }
    }

    /**
     * Open BookingActivity to book current instrument
     * Passes instrument data via Parcelable
     */
    private fun openBookingActivity() {
        val instrument = currentInstrument

        if (instrument == null) {
            showError("No instrument selected")
            return
        }

        if (instrument.isBooked) {
            showError("This instrument is already booked")
            return
        }

        // Create Intent with Parcelable data
        val intent = Intent(this, BookingActivity::class.java).apply {
            putExtra(EXTRA_INSTRUMENT, instrument) // Parcelable object
            putExtra(EXTRA_AVAILABLE_CREDITS, userCredits)
        }

        // Start activity for result to handle booking confirmation
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_CODE_BOOKING)
    }

    /**
     * Handle result from BookingActivity
     * Updates UI based on booking success or cancellation
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_BOOKING) {
            when (resultCode) {
                RESULT_BOOKED -> {
                    // Booking was successful
                    val bookedInstrument = data?.getParcelableExtra<Instrument>(EXTRA_INSTRUMENT)

                    if (bookedInstrument != null) {
                        // Update repository with booked instrument
                        InstrumentRepository.updateInstrument(bookedInstrument)

                        // Refresh current display
                        displayInstrument(currentIndex)

                        // Show success message
                        showBookingSuccess(bookedInstrument.name)
                    }
                }

                RESULT_CANCELLED -> {
                    // Booking was cancelled
                    showBookingCancelled()
                }
            }
        }
    }

    /**
     * Show success message after booking
     * Uses Snackbar with action button
     *
     * @param instrumentName Name of booked instrument
     */
    private fun showBookingSuccess(instrumentName: String) {
        Snackbar.make(
            binding.root,
            getString(R.string.success_booking, instrumentName),
            Snackbar.LENGTH_LONG
        ).setAction(R.string.action_view_details) {
            // Action: Could navigate to booking details or history
            // For now, just show instrument details again
            displayInstrument(currentIndex)
        }.setAnchorView(binding.buttonBorrow) // Anchor to bottom button
            .show()
    }

    /**
     * Show cancellation message
     * Uses Snackbar for consistency
     */
    private fun showBookingCancelled() {
        Snackbar.make(
            binding.root,
            getString(R.string.info_booking_cancelled),
            Snackbar.LENGTH_SHORT
        ).setAnchorView(binding.buttonBorrow)
            .show()
    }

    /**
     * Show error message
     * Uses Snackbar for user feedback
     *
     * @param message Error message to display
     */
    private fun showError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(R.color.text_white))
            .show()
    }

    /**
     * Save instance state for configuration changes
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_index", currentIndex)
    }

    /**
     * Restore instance state after configuration change
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentIndex = savedInstanceState.getInt("current_index", 0)
        displayInstrument(currentIndex)
    }
}