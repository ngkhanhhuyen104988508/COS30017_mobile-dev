package com.example.renting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renting.activities.MainActivity
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


//Integration tests for complete user flows
//Tests end-to-end scenarios from MainActivity to BookingActivity and back

@RunWith(AndroidJUnit4::class)
class IntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

     //Tests the entire journey from browsing to booking

    @Test
    fun testCompleteBookingFlowWithValidData() {
        // Step 1: Verify we're on MainActivity showing first instrument
        onView(withId(R.id.textInstrumentName))
            .check(matches(withText("Acoustic Guitar Pro")))

        // Step 2: Click Borrow button
        onView(withId(R.id.buttonBorrow))
            .perform(click())

        // Step 3: Verify BookingActivity opened
        onView(withId(R.id.textInstrumentNameSummary))
            .check(matches(isDisplayed()))

        // Step 4: Fill in customer information
        onView(withId(R.id.editCustomerName))
            .perform(typeText("John Doe"), closeSoftKeyboard())

        onView(withId(R.id.editContact))
            .perform(typeText("john@example.com"), closeSoftKeyboard())

        // Step 5: Confirm booking
        onView(withId(R.id.buttonConfirmBooking))
            .perform(click())

        // Step 6: Verify returned to MainActivity
        // (Activity should finish and return to MainActivity)
        Thread.sleep(1500)

        // Step 7: Verify booking status updated
        onView(withId(R.id.textBookingStatus))
            .check(matches(withText(containsString("Booked by"))))

        // Step 8: Verify borrow button now disabled
        onView(withId(R.id.buttonBorrow))
            .check(matches(not(isEnabled())))
    }

    //Test: Browse multiple instruments and book one

    @Test
    fun testBrowseAndBookSecondInstrument() {
        // Navigate to second instrument
        onView(withId(R.id.buttonNext))
            .perform(click())

        // Verify we're on second instrument
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("2 / 4")))

        // Open booking
        onView(withId(R.id.buttonBorrow))
            .perform(click())

        // Verify correct instrument in booking screen
        onView(withId(R.id.textInstrumentNameSummary))
            .check(matches(withText("Digital Piano 88-Key")))

        // Fill and submit
        onView(withId(R.id.editCustomerName))
            .perform(typeText("Jane Smith"), closeSoftKeyboard())

        onView(withId(R.id.editContact))
            .perform(typeText("5551234567"), closeSoftKeyboard())

        onView(withId(R.id.buttonConfirmBooking))
            .perform(click())

        // Wait for return
        Thread.sleep(1500)

        // Verify still on second instrument and now booked
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("2 / 4")))

        onView(withId(R.id.textBookingStatus))
            .check(matches(withText(containsString("Jane Smith"))))
    }

     //Test: Cancel booking returns to MainActivity without changes
    @Test
    fun testCancelBookingReturnsWithoutChanges() {
        // Initial status should be available
        onView(withId(R.id.textBookingStatus))
            .check(matches(withText("✅ Available")))

        // Open booking
        onView(withId(R.id.buttonBorrow))
            .perform(click())

        // Enter some data
        onView(withId(R.id.editCustomerName))
            .perform(typeText("Test User"), closeSoftKeyboard())

        // Cancel
        onView(withId(R.id.buttonCancelBooking))
            .perform(click())

        // If dialog appears, confirm cancellation
        try {
            onView(withText("Yes, Cancel"))
                .perform(click())
        } catch (e: Exception) {
            //  continue
        }

        // Wait for return
        Thread.sleep(1000)

        // Verify back on MainActivity and status unchanged
        onView(withId(R.id.textBookingStatus))
            .check(matches(withText("✅ Available")))

        onView(withId(R.id.buttonBorrow))
            .check(matches(isEnabled()))
    }

     //Test: Validation prevents booking with empty fields

    @Test
    fun testValidationPreventsBookingWithEmptyFields() {
        // Open booking
        onView(withId(R.id.buttonBorrow))
            .perform(click())

        // Try to confirm without entering data
        onView(withId(R.id.buttonConfirmBooking))
            .perform(click())

        //still be on BookingActivity
        onView(withId(R.id.editCustomerName))
            .check(matches(isDisplayed()))

        // Error should be visible
        onView(withId(R.id.layoutCustomerName))
            .check(matches(isDisplayed()))
    }

    //Test: Navigation persists after booking

    @Test
    fun testNavigationPersistsAfterBooking() {
        // Navigate to third instrument
        onView(withId(R.id.buttonNext)).perform(click()) // 2/4
        onView(withId(R.id.buttonNext)).perform(click()) // 3/4

        // Verify position
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("3 / 4")))

        // Book it
        onView(withId(R.id.buttonBorrow)).perform(click())

        onView(withId(R.id.editCustomerName))
            .perform(typeText("Alice Brown"), closeSoftKeyboard())

        onView(withId(R.id.editContact))
            .perform(typeText("alice@test.com"), closeSoftKeyboard())

        onView(withId(R.id.buttonConfirmBooking)).perform(click())

        Thread.sleep(1500)

        // Should still be on third instrument
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("3 / 4")))

        // Can still navigate
        onView(withId(R.id.buttonNext)).perform(click())

        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("4 / 4")))
    }

     // Multiple bookings work

    @Test
    fun testMultipleBookings() {
        // Book first instrument
        bookCurrentInstrument("User One", "user1@test.com")

        // Navigate and book second
        onView(withId(R.id.buttonNext)).perform(click())
        bookCurrentInstrument("User Two", "user2@test.com")

        // Verify first is still booked
        onView(withId(R.id.buttonPrevious)).perform(click())
        onView(withId(R.id.textBookingStatus))
            .check(matches(withText(containsString("User One"))))

        // Verify second is booked
        onView(withId(R.id.buttonNext)).perform(click())
        onView(withId(R.id.textBookingStatus))
            .check(matches(withText(containsString("User Two"))))
    }

    private fun bookCurrentInstrument(name: String, contact: String) {
        onView(withId(R.id.buttonBorrow)).perform(click())

        onView(withId(R.id.editCustomerName))
            .perform(typeText(name), closeSoftKeyboard())

        onView(withId(R.id.editContact))
            .perform(typeText(contact), closeSoftKeyboard())

        onView(withId(R.id.buttonConfirmBooking)).perform(click())

        Thread.sleep(1500)
    }
}