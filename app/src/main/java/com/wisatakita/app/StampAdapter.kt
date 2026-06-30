package com.wisatakita.app

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wisatakita.app.data.db.JourneyStampEntity

class StampAdapter : RecyclerView.Adapter<StampAdapter.ViewHolder>() {

    private val items = mutableListOf<JourneyStampEntity>()

    class ViewHolder(val stampView: StampView) : RecyclerView.ViewHolder(stampView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val size = (112 * parent.resources.displayMetrics.density).toInt()
        return ViewHolder(
            StampView(parent.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                    setMargins(6, 6, 6, 14)
                }
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.stampView.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submit(stamps: List<JourneyStampEntity>) {
        items.clear()
        items.addAll(stamps)
        notifyDataSetChanged()
    }
}
