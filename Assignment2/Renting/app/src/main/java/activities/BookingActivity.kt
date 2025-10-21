package com.example.renting.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.renting.R
import com.example.renting.databinding.ActivityBookingBinding
import com.example.renting.models.Instrument
import com.example.renting.utils.ValidationHelper
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent

/**
 * Booking Activity - Handle instrument booking
 *
 * Features:
 * - Receive instrument via Parcelable Intent
 * - Display instrument details
 * - Collect customer information
 * - Validate all inputs
 * - Calculate remaining credits
 * - Save booking or cancel
 *
 * Uses ViewBinding for type-safe view access
 */
class BookingActivity : AppCompatActivity() {

    // ViewBinding instance
    private lateinit var binding: ActivityBookingBinding

    // Instrument being booked (received via Parcelable)
    private lateinit var instrument: Instrument

    // User's available credits
    private var availableCredits: Int = 0

    // Track if form has been modified
    private var hasModifications = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar with back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Display instrument details
        displayInstrumentSummary()

        // Setup input listeners
        setupInputListeners()

        // Setup button click listeners
        setupClickListeners()

        // Calculate and display credit summary
        updateCreditSummary()
    }

    /**
     * Retrieve instrument and credits data from Intent
     *
     * @return true if data retrieved successfully, false otherwise
     */
    private fun retrieveIntentData(): Boolean {
        // Get Parcelable instrument
        @Suppress("DEPRECATION")
        val receivedInstrument = intent.getParcelableExtra<Instrument>(
            MainActivity.EXTRA_INSTRUMENT
        )

        if (receivedInstrument == null) {
            return false
        }

        instrument = receivedInstrument

        // Get available credits
        availableCredits = intent.getIntExtra(
            MainActivity.EXTRA_AVAILABLE_CREDITS,
            0
        )

        return true
    }

    /**
     * Display instrument summary at top of form
     */
    private fun displayInstrumentSummary() {
        with(binding) {
            // Instrument image
            imageInstrumentThumb.setImageResource(instrument.imageResourceId)
            imageInstrumentThumb.contentDescription = getString(
                R.string.cd_instrument_image,
                instrument.name
            )

            // Instrument name
            textInstrumentNameSummary.text = instrument.name

            // Rating
            ratingBarSummary.rating = instrument.rating

            // Price
            textPriceSummary.text = instrument.getFormattedPrice()
        }
    }

    /**
     * Setup input field listeners for validation and change tracking
     */
    private fun setupInputListeners() {
        // Track modifications for back button confirmation
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasModifications = true
                // Clear errors when user starts typing
                binding.layoutCustomerName.error = null
                binding.layoutContact.error = null
            }
        }

        binding.editCustomerName.addTextChangedListener(textWatcher)
        binding.editContact.addTextChangedListener(textWatcher)
    }

    /**
     * Setup click listeners for action buttons
     */
    private fun setupClickListeners() {
        // Confirm booking button
        binding.buttonConfirmBooking.setOnClickListener {
            attemptBooking()
        }

        // Cancel booking button
        binding.buttonCancelBooking.setOnClickListener {
            handleCancel()
        }

        // Toolbar back button
        binding.toolbar.setNavigationOnClickListener {
            handleCancel()
        }
    }

    /**
     * Update credit summary display
     * Shows monthly cost, available credits, and remaining after booking
     */
    private fun updateCreditSummary() {
        with(binding) {
            // Monthly cost
            textMonthlyCost.text = getString(
                R.string.credits_value,
                instrument.pricePerMonth
            )

            // Available credits
            textAvailableCredits.text = getString(
                R.string.credits_value,
                availableCredits
            )

            // Remaining credits after booking
            val remaining = availableCredits - instrument.pricePerMonth
            textRemainingCredits.text = getString(
                R.string.credits_value,
                remaining
            )

            // Change color if insufficient
            if (remaining < 0) {
                textRemainingCredits.setTextColor(getColor(R.color.error))
            } else {
                textRemainingCredits.setTextColor(getColor(R.color.success))
            }
        }
    }

    /**
     * Attempt to process the booking
     * Validates all inputs and saves if valid
     */
    private fun attemptBooking() {
        // Get input values
        val customerName = binding.editCustomerName.text.toString()
        val contact = binding.editContact.text.toString()

        // Validate all inputs
        val validationResult = ValidationHelper.validateBookingForm(
            customerName = customerName,
            contact = contact,
            availableCredits = availableCredits,
            requiredCredits = instrument.pricePerMonth
        )

        if (!validationResult.isValid) {
            // Show specific error
            showValidationError(validationResult.errorMessage ?: "Invalid input")
            return
        }

        // All validation passed - process booking
        processBooking(customerName, contact)
    }

    /**
     * Show validation error to user
     * Determines which field has error and displays inline
     *
     * @param errorMessage The error message to display
     */
    private fun showValidationError(errorMessage: String) {
        // Determine which field to show error on
        val customerName = binding.editCustomerName.text.toString()
        val contact = binding.editContact.text.toString()

        when {
            ValidationHelper.validateCustomerName(customerName).let { !it.isValid } -> {
                binding.layoutCustomerName.error = errorMessage
                binding.editCustomerName.requestFocus()
            }
            ValidationHelper.validateContact(contact).let { !it.isValid } -> {
                binding.layoutContact.error = errorMessage
                binding.editContact.requestFocus()
            }
            else -> {
                // Credit error - show in Snackbar
                Snackbar.make(
                    binding.root,
                    errorMessage,
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(getColor(R.color.error))
                    .setTextColor(getColor(R.color.text_white))
                    .show()
            }
        }
    }
    private fun processBooking(customerName: String, contact: String) {
        // Sanitize inputs
        val sanitizedName = ValidationHelper.sanitizeInput(customerName)
        val sanitizedContact = ValidationHelper.sanitizeInput(contact)

        // Get current date for booking record
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date())

        // Update instrument with booking details
        instrument.bookInstrument(
            customerName = sanitizedName,
            contact = sanitizedContact,
            date = currentDate
        )

        // Create result intent with updated instrument
        val resultIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_INSTRUMENT, instrument)
        }

        // Set result and finish
        setResult(MainActivity.RESULT_BOOKED, resultIntent)

        // Show success message before closing
        showBookingSuccess(instrument.name)

        // Finish activity after short delay to show message
        binding.root.postDelayed({
            finish()
        }, 1000)
    }

    /**
     * Handle cancel button or back navigation
     * Shows confirmation dialog if form has modifications
     */
    private fun handleCancel() {
        if (hasModifications) {
            // Show confirmation dialog
            showCancelConfirmationDialog()
        } else {
            // No changes, cancel directly
            cancelBooking()
        }
    }

    /**
     * Show confirmation dialog when cancelling with unsaved changes
     */
    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_cancel_booking_title)
            .setMessage(R.string.dialog_cancel_booking_message)
            .setPositiveButton(R.string.dialog_button_yes) { dialog, _ ->
                dialog.dismiss()
                cancelBooking()
            }
            .setNegativeButton(R.string.dialog_button_no) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Cancel the booking and return to MainActivity
     */
    private fun cancelBooking() {
        // Set cancelled result
        setResult(MainActivity.RESULT_CANCELLED)

        // Finish activity
        finish()
    }

    /**
     * Show success Snackbar
     *
     * @param instrumentName Name of booked instrument
     */
    private fun showBookingSuccess(instrumentName: String) {
        Snackbar.make(
            binding.root,
            getString(R.string.success_booking, instrumentName),
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(getColor(R.color.success))
            .setTextColor(getColor(R.color.text_white))
            .show()
    }

    /**
     * Show error Snackbar
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
     * Handle up navigation from toolbar
     * Treats it same as cancel
     */
    override fun onSupportNavigateUp(): Boolean {
        handleCancel()
        return true
    }

    /**
     * Handle system back button
     * Treats it same as cancel
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleCancel()
    }

    /**
     * Save instance state for configuration changes
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("customer_name", binding.editCustomerName.text.toString())
        outState.putString("contact", binding.editContact.text.toString())
        outState.putBoolean("has_modifications", hasModifications)
    }

    /**
     * Restore instance state after configuration change
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.editCustomerName.setText(savedInstanceState.getString("customer_name", ""))
        binding.editContact.setText(savedInstanceState.getString("contact", ""))
        hasModifications = savedInstanceState.getBoolean("has_modifications", false)
    }
}
