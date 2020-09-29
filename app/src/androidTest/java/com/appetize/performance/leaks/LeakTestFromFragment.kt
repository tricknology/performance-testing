package com.appetize.performance.leaks

import android.os.Debug
import android.widget.Button
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.appetize.performance.leaks.eighthTest.IgnoreLeaks
import com.appetize.performance.leaks.ninthTest.LeakTest
import kotlinx.android.synthetic.main.fragment_leak.*
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

/**
 * Created by Zach Nwabudike on 9/28/20.
 */
class LeakTestFromFragment {

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(KotlinLeakActivity::class.java)

    @Test
    @IgnoreLeaks("Ignore this")
    fun testIgnoreLeaks() {
        // inflate the fragment
        onView(withId(R.id.button_start_fragment)).perform(click())
    }

    @Test
    @LeakTest
    fun testFragmentLeaksActivityInterface() {
        Debug.enableEmulatorTraceOutput()
        Debug.startMethodTracing(null);

        println("Performing click on button_start_fragment")

        //inflate the fragment
        onView(withId(R.id.button_start_fragment)).perform(click())
        checkFragmentExists()

        // cause the leak
        println("Performing click on button_leak_in_fragment")
        onView(withId(R.id.button_leak_in_fragment)).perform(click())

        Debug.stopMethodTracing();

        println("Finished Test")

    }


    @Ignore(" This is not working at the moment")
    @Test
    @LeakTest
    fun detectLeakWithActivityScenario() {
        Debug.enableEmulatorTraceOutput()
        Debug.startMethodTracing(null);

        ActivityScenario.launch(KotlinLeakActivity::class.java)
            .use { scenario ->
                scenario.moveToState(Lifecycle.State.RESUMED);    // Moves the activity state to State.RESUMED.
                scenario.moveToState(Lifecycle.State.STARTED);    // Moves the activity state to State.STARTED.
                scenario.moveToState(Lifecycle.State.CREATED);    // Moves the activity state to State.CREATED.
                scenario.onActivity { activity: KotlinLeakActivity ->
                    activity.loadLeakyFragment()
                    activity.findViewById<Button>(R.id.button_leak_in_fragment)
                    activity.removeCurrentFragment()
                }
                scenario.moveToState(Lifecycle.State.DESTROYED);  // Moves the activity state to State.DESTROYED

                scenario.close()
                Debug.stopMethodTracing();
            }
    }

    @Ignore(" This is not working at the moment")
    @Test
    @LeakTest
    fun fragmentScenarioLeakTest() {
        val fragmentScenario = FragmentScenario.launch(KLeakFragment::class.java, null)
            .onFragment {
                //calls the presenter to leak an item in the view
                //then calls the presenter to onFinishFragment()
                onView(withId(R.id.button_leak_in_fragment)).perform(click())
            }

        with(fragmentScenario) {
            moveToState(Lifecycle.State.RESUMED);    // Moves the activity state to State.RESUMED.
            moveToState(Lifecycle.State.STARTED);    // Moves the activity state to State.STARTED.
            moveToState(Lifecycle.State.CREATED);    // Moves the activity state to State.CREATED.

            moveToState(Lifecycle.State.DESTROYED);  // Moves the activity state to State.DESTROYED
        }
    }


    private fun checkFragmentExists() {
        val fragmentManager = mainActivityActivityTestRule.activity.supportFragmentManager

        val fragment: KLeakFragment? =
            fragmentManager.findFragmentByTag(KLeakFragment.TAG) as KLeakFragment?

        assert(fragment != null) { "Fragment null" }
    }


}