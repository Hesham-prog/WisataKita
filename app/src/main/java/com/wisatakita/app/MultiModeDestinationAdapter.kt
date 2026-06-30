package com.wisatakita.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.databinding.ItemDestinationCardBinding
import com.wisatakita.app.databinding.ItemDestinationGridBinding
import com.wisatakita.app.databinding.ItemDestinationListBinding

/**
 * MultiModeDestinationAdapter — supports List, Grid, and Card view modes.
 * Satisfies the academic requirement for RecyclerView in all three forms.
 */
class MultiModeDestinationAdapter(
    private var viewMode: PenjelajahFragment.ViewMode,
    private val onItemClick: (Destination) -> Unit
) : ListAdapter<Destination, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Destination>() {
            override fun areItemsTheSame(a: Destination, b: Destination) = a.id == b.id
            override fun areContentsTheSame(a: Destination, b: Destination) = a == b
        }
        private const val TYPE_LIST = 0
        private const val TYPE_GRID = 1
        private const val TYPE_CARD = 2
    }

    fun setViewMode(mode: PenjelajahFragment.ViewMode) {
        viewMode = mode
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (viewMode) {
        PenjelajahFragment.ViewMode.LIST -> TYPE_LIST
        PenjelajahFragment.ViewMode.GRID -> TYPE_GRID
        PenjelajahFragment.ViewMode.CARD -> TYPE_CARD
    }

    // ── List ViewHolder ──────────────────────────────────────────────

    inner class ListViewHolder(
        private val binding: ItemDestinationListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dest: Destination) {
            binding.tvName.text = dest.name
            binding.tvLocation.text = dest.location
            binding.tvCategory.text = dest.category
            binding.tvRating.text = "%.1f".format(dest.rating)
            Glide.with(binding.root).load(dest.imageUrl).override(240, 240)
                .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.ivThumbnail)
            binding.root.setOnClickListener { v ->
                HapticUtil.click(v)
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    onItemClick(dest)
                }.start()
            }
        }
    }

    // ── Grid ViewHolder ──────────────────────────────────────────────

    inner class GridViewHolder(
        private val binding: ItemDestinationGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dest: Destination) {
            binding.tvGridName.text = dest.name
            binding.tvGridRating.text = "%.1f".format(dest.rating)
            Glide.with(binding.root).load(dest.imageUrl).override(320, 320)
                .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.ivGridImage)
            binding.root.setOnClickListener { v ->
                HapticUtil.click(v)
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(180).start()
                    onItemClick(dest)
                }.start()
            }
        }
    }

    // ── Card ViewHolder ──────────────────────────────────────────────

    inner class CardViewHolder(
        private val binding: ItemDestinationCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dest: Destination) {
            binding.tvCardName.text = dest.name
            binding.tvCardLocation.text = dest.location
            binding.tvCardCategory.text = dest.category
            binding.tvCardRating.text = "%.1f".format(dest.rating)
            binding.tvCardDescription.text = dest.description
            binding.tvCardTicket.text = dest.ticketPrice
            Glide.with(binding.root).load(dest.imageUrl).override(720, 400)
                .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.ivCardImage)
            binding.root.setOnClickListener { v ->
                HapticUtil.click(v)
                onItemClick(dest)
            }
        }
    }

    // ── Factory ─────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_GRID -> GridViewHolder(ItemDestinationGridBinding.inflate(inflater, parent, false))
            TYPE_CARD -> CardViewHolder(ItemDestinationCardBinding.inflate(inflater, parent, false))
            else      -> ListViewHolder(ItemDestinationListBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dest = getItem(position)
        when (holder) {
            is ListViewHolder -> holder.bind(dest)
            is GridViewHolder -> holder.bind(dest)
            is CardViewHolder -> holder.bind(dest)
        }
    }
}
