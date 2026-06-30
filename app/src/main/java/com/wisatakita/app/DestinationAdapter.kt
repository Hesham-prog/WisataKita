package com.wisatakita.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.databinding.ItemDestinationBinding

class DestinationAdapter(
    private val onItemClick: (Destination) -> Unit
) : RecyclerView.Adapter<DestinationAdapter.ViewHolder>() {

    private val items = mutableListOf<DestinationUiItem>()

    fun updateData(newItems: List<DestinationUiItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDestinationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DestinationUiItem) {
            val destination = item.destination
            binding.tvDestinationName.text = destination.name
            binding.tvDestinationLocation.text = destination.location
            binding.tvDestinationMeta.text = "${destination.category} - ${"%.1f".format(destination.rating)} (${destination.reviewCount})"
            binding.tvDestinationInfo.text = "${destination.ticketPrice} - ${destination.openingHours}"
            binding.tvDestinationDistance.text = item.distanceKm?.let {
                "${"%.1f".format(it)} km"
            } ?: "Aktifkan GPS"
            binding.tvPopularBadge.visibility = if (destination.reviewCount >= 15000 || destination.rating >= 4.8) {
                View.VISIBLE
            } else {
                View.GONE
            }
            Glide.with(binding.root.context)
                .load(destination.imageUrl)
                .override(360, 260)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.colorPrimaryLight)
                .into(binding.ivThumbnail)
            binding.root.setOnClickListener { onItemClick(destination) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDestinationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

data class DestinationUiItem(
    val destination: Destination,
    val distanceKm: Double? = null
)
