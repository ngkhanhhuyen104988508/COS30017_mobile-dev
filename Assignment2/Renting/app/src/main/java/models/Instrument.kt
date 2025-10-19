package models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// id: Unique identifier for the instrument
// name: Display name of the instrument
// rating: Rating from 0-5 stars
// condition: Current condition (Excellent, Good, Fair, etc.)
//  pricePerMonth: Monthly rental price in credits
// imageResourceId: Drawable resource ID for the instrument image
// description: Detailed description of the instrument
// category: Category of instrument (String, Percussion, Keyboard, etc.)
// isBooked: Whether the instrument is currently booked
//  bookedByName: Name of the customer who booked (null if not booked)
// bookedContact: Contact information of customer
// bookedDate: Date when booking was made

@Parcelize
data class Instrument(
    val id: Int,
    val name: String,
    val rating: Float, // 0.0f to 5.0f
    val condition: String, // Multi-choice attribute
    val pricePerMonth: Int,
    val imageResourceId: Int,
    val description: String,
    val category: String,
    var isBooked: Boolean = false,
    var bookedByName: String? = null,
    var bookedContact: String? = null,
    var bookedDate: String? = null
) : Parcelable {


    // Returns a formatted price string with "credits" suffix

    fun getFormattedPrice(): String = "$pricePerMonth credits/month"

    // Returns booking status as readable string
    fun getBookingStatus(): String {
        return if (isBooked) {
            "Booked by: $bookedByName"
        } else {
            "Available"
        }
    }

    // Books this instrument with customer details
    fun bookInstrument(customerName: String, contact: String, date: String) {
        isBooked = true
        bookedByName = customerName
        bookedContact = contact
        bookedDate = date
    }

    // Cancels the booking and clears customer details
    fun cancelBooking() {
        isBooked = false
        bookedByName = null
        bookedContact = null
        bookedDate = null
    }
}