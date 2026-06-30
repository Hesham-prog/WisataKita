package com.wisatakita.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.databinding.ItemDestinationFeaturedBinding

class FeaturedDestinationAdapter(
    private val onItemClick: (Destination) -> Unit
) : ListAdapter<Destination, FeaturedDestinationAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Destination>() {
            override fun areItemsTheSame(a: Destination, b: Destination) = a.id == b.id
            override fun areContentsTheSame(a: Destination, b: Destination) = a == b
        }
    }

    inner class ViewHolder(
        private val binding: ItemDestinationFeaturedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: Destination) {
            binding.tvFeaturedName.text = destination.name
            binding.tvFeaturedCategory.text = destination.category
            binding.tvFeaturedLocation.text = destination.location
            binding.tvFeaturedRating.text = "%.1f".format(destination.rating)

            Glide.with(binding.root)
                .load(destination.imageUrl)
                .override(320, 440)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.color.darker_gray)
                .into(binding.ivFeaturedImage)

            binding.root.setOnClickListener { v ->
                HapticUtil.click(v)
                // Scale press animation
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    onItemClick(destination)
                }.start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDestinationFeaturedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
