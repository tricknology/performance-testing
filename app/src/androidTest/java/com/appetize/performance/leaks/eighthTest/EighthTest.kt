package com.appetize.performance.leaks.eighthTest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.appetize.performance.leaks.LeakActivity
import com.appetize.performance.leaks.R

import org.junit.Test
import org.junit.Rule

class EighthTest {

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(LeakActivity::class.java)

    @Test
    @IgnoreLeaks("Ignore this")
    fun testIgnoreLeaks() {
        onView(withId(R.id.button_leak_in_activity)).perform(click())
    }

    @Test
    fun testLeaks() {
        onView(withId(R.id.button_leak_in_activity)).perform(click())
    }
}