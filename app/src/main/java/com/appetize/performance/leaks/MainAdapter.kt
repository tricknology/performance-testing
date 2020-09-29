package com.appetize.performance.leaks

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter : RecyclerView.Adapter<MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        (holder.itemView as TextView).text = "This is item $position"
        // Add sleep to recreate work on the main thread
//           Thread.sleep(150)
//        if (position % 20 == 0) {
//            try {
//                Thread.sleep(150)
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//        }
    }

    override fun getItemCount(): Int = 50

}