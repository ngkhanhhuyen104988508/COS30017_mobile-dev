package com.example.renting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renting.activities.MainActivity
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testAppLaunchesAndDisplaysFirstInstrument() {
        // Verify toolbar title
        onView(withText("Available Instruments"))
            .check(matches(isDisplayed()))

        // Verify instrument name is displayed
        onView(withId(R.id.textInstrumentName))
            .check(matches(isDisplayed()))
            .check(matches(withText("Acoustic Guitar Pro")))

        // Verify page indicator shows first page
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("1 / 4")))

        // Verify borrow button exists and is enabled
        onView(withId(R.id.buttonBorrow))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    //Test first instrument w pre button disabled, next button enabled

    @Test
    fun testPreviousButtonDisabledOnFirstInstrument() {
        onView(withId(R.id.buttonPrevious))
            .check(matches(isDisplayed()))
            .check(matches(not(isEnabled())))
    }


    @Test
    fun testNextButtonEnabledOnFirstInstrument() {
        onView(withId(R.id.buttonNext))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testNavigationToNextInstrument() {
        // Click Next button
        onView(withId(R.id.buttonNext))
            .perform(click())

        // Verify instrument changed (name should not be first instrument)
        onView(withId(R.id.textInstrumentName))
            .check(matches(isDisplayed()))
            .check(matches(not(withText("Acoustic Guitar Pro"))))

        // Verify page indicator updated
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("2 / 4")))

        // Verify Previous button now enabled
        onView(withId(R.id.buttonPrevious))
            .check(matches(isEnabled()))
    }

    @Test
    fun testNavigationThroughAllInstruments() {
        // Navigate to last instrument
        onView(withId(R.id.buttonNext)).perform(click()) // 2/4
        onView(withId(R.id.buttonNext)).perform(click()) // 3/4
        onView(withId(R.id.buttonNext)).perform(click()) // 4/4

        // Verify we're on last page
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("4 / 4")))

        // Verify Next button now disabled
        onView(withId(R.id.buttonNext))
            .check(matches(not(isEnabled())))

        // Verify Previous still enabled
        onView(withId(R.id.buttonPrevious))
            .check(matches(isEnabled()))
    }

    @Test
    fun testNavigationBackwards() {
        // Go to second instrument
        onView(withId(R.id.buttonNext)).perform(click())

        // Go back to first
        onView(withId(R.id.buttonPrevious)).perform(click())

        // Verify back on first instrument
        onView(withId(R.id.textPageIndicator))
            .check(matches(withText("1 / 4")))

        onView(withId(R.id.textInstrumentName))
            .check(matches(withText("Acoustic Guitar Pro")))

        // Previous button disabled again
        onView(withId(R.id.buttonPrevious))
            .check(matches(not(isEnabled())))
    }

    @Test
    fun testPriceDisplayed() {
        onView(withId(R.id.textPrice))
            .check(matches(isDisplayed()))
            .check(matches(withText("50 credits/month")))
    }

    @Test
    fun testDescriptionDisplayed() {
        onView(withId(R.id.textDescription))
            .check(matches(isDisplayed()))
            .check(matches(not(withText(""))))
    }

    @Test
    fun testBookingStatusShowsAvailable() {
        onView(withId(R.id.textBookingStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("✅ Available")))
    }

    @Test
    fun testBorrowButtonClickableOnAvailableInstrument() {
        onView(withId(R.id.buttonBorrow))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
    }

    @Test
    fun testAllRequiredElementsPresent() {
        // Image
        onView(withId(R.id.imageInstrument))
            .check(matches(isDisplayed()))

        // Name
        onView(withId(R.id.textInstrumentName))
            .check(matches(isDisplayed()))

        // Rating bar
        onView(withId(R.id.ratingBarInstrument))
            .check(matches(isDisplayed()))

        // Radio group for condition
        onView(withId(R.id.radioGroupCondition))
            .check(matches(isDisplayed()))

        // Price
        onView(withId(R.id.textPrice))
            .check(matches(isDisplayed()))

        // Description
        onView(withId(R.id.textDescription))
            .check(matches(isDisplayed()))

        // Navigation buttons
        onView(withId(R.id.buttonPrevious))
            .check(matches(isDisplayed()))

        onView(withId(R.id.buttonNext))
            .check(matches(isDisplayed()))

        // Borrow button
        onView(withId(R.id.buttonBorrow))
            .check(matches(isDisplayed()))
    }
}