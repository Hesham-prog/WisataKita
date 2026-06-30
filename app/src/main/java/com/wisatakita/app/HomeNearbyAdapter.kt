package com.wisatakita.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.databinding.ItemDestinationListBinding

/**
 * HomeNearbyAdapter — compact list-style cards for the "Terdekat Darimu" section on Home.
 * Uses item_destination_list.xml (the existing list item layout).
 */
class HomeNearbyAdapter(
    private val onItemClick: (Destination) -> Unit
) : ListAdapter<Destination, HomeNearbyAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Destination>() {
            override fun areItemsTheSame(a: Destination, b: Destination) = a.id == b.id
            override fun areContentsTheSame(a: Destination, b: Destination) = a == b
        }
    }

    inner class ViewHolder(
        private val binding: ItemDestinationListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: Destination) {
            binding.tvName.text = destination.name
            binding.tvLocation.text = destination.location
            binding.tvCategory.text = destination.category
            binding.tvRating.text = "%.1f".format(destination.rating)

            Glide.with(binding.root)
                .load(destination.imageUrl)
                .override(240, 240)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(GlidePlaceholders.batik(binding.root.context))
                .into(binding.ivThumbnail)

            binding.root.bounceClick()
            binding.root.setOnClickListener { v ->
                HapticUtil.click(v)
                onItemClick(destination)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDestinationListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
