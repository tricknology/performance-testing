package com.appetize.performance.leaks

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_leak.*

/**
 * Created by Zach Nwabudike on 9/28/20.
 *
 * This class was created to test com.appetize.performance.leaks in Kotlin
 */
class KotlinLeakActivity : FragmentActivity(), CommonViewInterface {

    //if this is referenced inside of an anonymous class it will be captured and leak
    private var leakyPresenter: LeakyPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        println("onCreate of KotlinLeakActivity called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leak)
        initializeView()
    }

    override fun onPause() {
        println("onPause of KotlinLeakActivity called")
        super.onPause()
        cleanup()
    }

    override fun cleanup() {
        //oops we forgot to cleanup the view
//        leakyPresenter?.destroy()
        // leakyPresenter = null
    }

    override fun removeCurrentFragment(): Boolean {
        println("called returnToMainActivity")

        return supportFragmentManager.let {
            val fragment =
                it.findFragmentByTag(KLeakFragment.TAG)
                    ?: return false
            it.beginTransaction().remove(fragment)
            it.executePendingTransactions()
            it.popBackStackImmediate().also { b ->
                println("popBackStackImmediate returns $b")
            }
        }
    }

    override fun loadLeakyFragment() {
        showKLeakFragment()
    }

    override fun getSomeTextFromContext(): String {
        return getString(R.string.app_name)
    }

    override fun changeViewText(message: String) {
        println("changeButtonText to $message")
        button_leak_in_activity.text = message
    }

    private fun initializeView() {
        leakyPresenter = LeakyPresenter(this)

        button_leak_in_activity.setOnClickListener {
            // this listener could leak too but it's implemented in View which is
            // attached to the Activity Lifecycle itself, it won't leak itself..
            // but it will leak the instance of the presenter
            // if we had to switch to another activity
            // (Think Samsung tablets, not Elo's)
            leakyPresenter?.leakTheViewWithAnonymousRunnable()
        }

        button_start_fragment.setOnClickListener {
            //this closure calls loadLeakyFragment, it is a capturing closure
            loadLeakyFragment()
        }

    }

    /**
     * Start work on a thread
     * This won't leak as-is because it is not referenced from a capture.
     * The Runnable is fine as it's running on a Thread that will eventually quit.
     * Still not good practice...
     */
    private fun startAsyncWork() {
        val work = Runnable { SystemClock.sleep(20000) }
        Thread(work).start()
    }

    /*
    End Sharable Code
     */

    // add fragment to top level of ViewGroup.
    // Activity still present in heap, may or may not be destroyed but will be paused
    private fun showKLeakFragment() {
        val fragment = KLeakFragment.newInstance()
        val transaction = supportFragmentManager?.beginTransaction()

        transaction.let {
            it.add(R.id.fragment_container, fragment, KLeakFragment.TAG)
            it.commitAllowingStateLoss()
        }

        button_leak_in_activity.visibility = View.GONE
        button_start_fragment.visibility = View.GONE
    }


}

