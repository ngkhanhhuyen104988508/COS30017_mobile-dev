package com.example.renting.utils

import android.util.Patterns
import java.util.regex.Pattern
//Helper class for input validation provides reusable validation logic for booking forms

object ValidationHelper {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun error(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Validates customer name
     * Rules:
     * - Must not be empty or blank
     * - Must be at least 2 characters
     * - Can only contain letters, spaces, hyphens, and apostrophes
     * @return ValidationResult with error message if invalid
     */
    fun validateCustomerName(name: String): ValidationResult {
        return when {
            name.isBlank() -> {
                ValidationResult.error("Customer name is required")
            }
            name.trim().length < 2 -> {
                ValidationResult.error("Name must be at least 2 characters")
            }
            !name.matches(Regex("^[a-zA-Z\\s\\-']+$")) -> {
                ValidationResult.error("Name can only contain letters, spaces, hyphens and apostrophes")
            }
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates contact information (email or phone)
     * Accepts either:
     * - Valid email address
     * - 10-digit phone number (with optional formatting)
     *
     * @param contact The contact string to validate
     * @return ValidationResult with error message if invalid
     */
    fun validateContact(contact: String): ValidationResult {
        if (contact.isBlank()) {
            return ValidationResult.error("Contact information is required")
        }

        val trimmedContact = contact.trim()

        // Check if it's an email
        if (isValidEmail(trimmedContact)) {
            return ValidationResult.success()
        }

        // Check if it's a phone number
        if (isValidPhone(trimmedContact)) {
            return ValidationResult.success()
        }

        return ValidationResult.error(
            "Please enter a valid email address or 10-digit phone number"
        )
    }

    /**
     * Validates credit availability
     * Checks if user has sufficient credits for the booking
     *
     * @param availableCredits User's current credit balance
     * @param requiredCredits Credits needed for the booking
     * @return ValidationResult with error message if insufficient
     */
    fun validateCredits(availableCredits: Int, requiredCredits: Int): ValidationResult {
        return if (availableCredits >= requiredCredits) {
            ValidationResult.success()
        } else {
            ValidationResult.error(
                "Insufficient credits. You need $requiredCredits credits but only have $availableCredits"
            )
        }
    }

    /**
     * Validates complete booking form
     * Combines all validation checks
     *
     * @param customerName Customer's name
     * @param contact Customer's contact (email or phone)
     * @param availableCredits User's available credits
     * @param requiredCredits Credits needed for booking
     * @return ValidationResult with first error encountered, or success if all valid
     */
    fun validateBookingForm(
        customerName: String,
        contact: String,
        availableCredits: Int,
        requiredCredits: Int
    ): ValidationResult {
        // Validate name first
        validateCustomerName(customerName).let {
            if (!it.isValid) return it
        }

        // Then validate contact
        validateContact(contact).let {
            if (!it.isValid) return it
        }

        // Finally validate credits
        return validateCredits(availableCredits, requiredCredits)
    }

    /**
     * Checks if a string is a valid email address
     * Uses Android's built-in Patterns utility
     *
     * @param email The email string to check
     * @return true if valid email format
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if a string is a valid phone number
     * Accepts formats:
     * - 1234567890
     * - 123-456-7890
     * - (123) 456-7890
     * - 123.456.7890
     *
     * @param phone The phone string to check
     * @return true if valid 10-digit phone
     */
    private fun isValidPhone(phone: String): Boolean {
        // Remove all non-digit characters
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        // Check if exactly 10 digits
        return digitsOnly.length == 10
    }

    /**
     * Formats a phone number for display
     * Converts 1234567890 to (123) 456-7890
     *
     * @param phone The phone number (digits only or formatted)
     * @return Formatted phone string, or original if invalid
     */
    fun formatPhoneNumber(phone: String): String {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")

        return if (digitsOnly.length == 10) {
            "(${digitsOnly.substring(0, 3)}) ${digitsOnly.substring(3, 6)}-${digitsOnly.substring(6)}"
        } else {
            phone
        }
    }

    /**
     * Sanitizes input by trimming whitespace
     * Useful before saving to repository
     *
     * @param input The string to sanitize
     * @return Trimmed string
     */
    fun sanitizeInput(input: String): String {
        return input.trim()
    }
}