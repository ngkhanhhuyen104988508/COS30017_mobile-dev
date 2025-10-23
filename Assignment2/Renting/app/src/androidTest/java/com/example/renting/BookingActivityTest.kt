package com.example.renting

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renting.activities.BookingActivity
import com.example.renting.activities.MainActivity
import com.example.renting.models.Instrument
import com.example.renting.repositories.InstrumentRepository
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingActivityTest {

    private lateinit var testInstrument: Instrument

    @Before
    fun setup() {
        // Initialize repository
        InstrumentRepository.initialize()
        // Get test instrument
        testInstrument = InstrumentRepository.getInstrumentAtIndex(0)!!
    }

    private fun launchBookingActivity(): ActivityScenario<BookingActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BookingActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_INSTRUMENT, testInstrument)
            putExtra(MainActivity.EXTRA_AVAILABLE_CREDITS, 120)
        }
        return ActivityScenario.launch(intent)
    }

    @Test
    fun testInstrumentDetailsDisplayed() {
        launchBookingActivity()

        // Verify instrument name
        onView(withId(R.id.textInstrumentNameSummary))
            .check(matches(withText("Acoustic Guitar Pro")))

        // Verify price
        onView(withId(R.id.textPriceSummary))
            .check(matches(withText("50 credits/month")))
    }

    /**
     * Test: Credit summary calculates correctly
     */
    @Test
    fun testCreditSummaryDisplayed() {
        launchBookingActivity()

        // Monthly cost
        onView(withId(R.id.textMonthlyCost))
            .check(matches(withText("50 credits")))

        // Available credits
        onView(withId(R.id.textAvailableCredits))
            .check(matches(withText("120 credits")))

        // Remaining credits (120 - 50 = 70)
        onView(withId(R.id.textRemainingCredits))
            .check(matches(withText("70 credits")))
    }

    /**
     * Test: Empty name field shows validation error
     */
    @Test
    fun testEmptyNameShowsError() {
        launchBookingActivity()

        // Leave name empty, enter valid contact
        onView(withId(R.id.editContact))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // Try to confirm
        onView(withId(R.id.buttonConfirmBooking))
            .perform(click())

        // Verify error appears (TextInputLayout shows error)
        onView(withId(R.id.layoutCustomerName))
            .check(matches(hasDescendant(withText(containsString("required")))))
    }

    @Test
    fun testInvalidEmailShowsError() {
        launchBookingActivity()

        // Enter valid name
        onView(withId(R.id.editCustomerName))
            .perform(typeText("John Doe"), closeSoftKeyboard())

        // Enter invalid email
        onView(withId(R.id.editContact))
            .perform(typeText("invalid"), closeSoftKeyboard())

        // Try to confirm
        onView(withId(R.id.buttonConfirmBooking))
            .perform(click())

        // Verify error appears
        onView(withId(R.id.layoutContact))
            .check(matches(hasDescendant(withText(containsString("valid")))))
    }

    @Test
    fun testValidPhoneNumberAccepted() {
        launchBookingActivity()

        // Enter valid name
        onView(withId(R.id.editCustomerName))
            .perform(typeText("John Doe"), closeSoftKeyboard())

        // Enter valid phone
        onView(withId(R.id.editContact))
            .perform(typeText("1234567890"), closeSoftKeyboard())

        // Confirm should process (activity will finish)
        onView(withId(R.id.buttonConfirmBooking))
            .perform(click())

    }

    @Test
    fun testCancelButtonDisplayed() {
        launchBookingActivity()

        onView(withId(R.id.buttonCancelBooking))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
    }
    @Test
    fun testAllInputFieldsDisplayed() {
        launchBookingActivity()

        // Customer name field
        onView(withId(R.id.editCustomerName))
            .check(matches(isDisplayed()))

        // Contact field
        onView(withId(R.id.editContact))
            .check(matches(isDisplayed()))

        // Confirm button
        onView(withId(R.id.buttonConfirmBooking))
            .check(matches(isDisplayed()))

        // Cancel button
        onView(withId(R.id.buttonCancelBooking))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testInputFieldsAcceptText() {
        launchBookingActivity()

        val testName = "Test User"
        val testEmail = "test@example.com"

        // Type in name field
        onView(withId(R.id.editCustomerName))
            .perform(typeText(testName), closeSoftKeyboard())

        // Verify text entered
        onView(withId(R.id.editCustomerName))
            .check(matches(withText(testName)))

        // Type in contact field
        onView(withId(R.id.editContact))
            .perform(typeText(testEmail), closeSoftKeyboard())

        // Verify text entered
        onView(withId(R.id.editContact))
            .check(matches(withText(testEmail)))
    }
}