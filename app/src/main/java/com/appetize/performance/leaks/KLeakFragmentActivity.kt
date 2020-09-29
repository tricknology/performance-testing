package com.appetize.performance.leaks

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_leak.*
import java.lang.RuntimeException

class KLeakFragment : Fragment(), CommonViewInterface {

    private lateinit var presenter: LeakyPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        println("onCreate called in KLeakFragment")
        super.onCreate(savedInstanceState)
        presenter = LeakyPresenter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        println("onCreateView called in KLeakFragment")
        val root = inflater.inflate(R.layout.fragment_leak, container, false)
        presenter = LeakyPresenter(this)
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button_close_fragment.setOnClickListener {
            println("Button clicked - button_close_fragment")
            presenter.onFinishedWithFragment()
        }

        button_leak_in_fragment.setOnClickListener {
            println("Button clicked - button_leak_in_fragment")
            presenter.leakTheViewWithAnonymousRunnable()
            println("Finish fragment")
            presenter.onFinishedWithFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanup()
    }

    /*
   Everything below could be moved to a Helper class because the Activity and Fragment
   share the same logic, but for clarity and demonstration purposes they are copied
    */


    override fun getSomeTextFromContext(): String {
        return getString(R.string.app_name)
    }


    override fun cleanup() {
        //oops
    }

    override fun removeCurrentFragment(): Boolean {
        return activity?.onBackPressed() != null
    }


    override fun changeViewText(message: String) {
        activity
            ?.findViewById<Button>(R.id.button_close_fragment)
            ?.text = message
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
    End shared code
     */


    companion object {
        val TAG = "KLeakFragment"

        fun newInstance(): KLeakFragment {
            val args = Bundle()
            val fragment = KLeakFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun loadLeakyFragment() {
        //unimplemented, this is fragment
    }
}