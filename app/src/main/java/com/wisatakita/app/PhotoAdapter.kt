package com.wisatakita.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.databinding.ItemPhotoBinding

class PhotoAdapter(
    private val items: MutableList<String>,
    private val onLongClick: (String) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: String) {
            Glide.with(binding.root.context)
                .load(Uri.parse(uri))
                .override(400, 400)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.color.colorPrimaryLight)
                .into(binding.ivPhoto)

            binding.root.setOnLongClickListener { onLongClick(uri); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val size = parent.width / 3
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = RecyclerView.LayoutParams(size, size)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun updateData(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
