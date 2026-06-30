package com.wisatakita.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Album
import com.wisatakita.app.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val items: MutableList<Album>,
    private val onClick: (Album) -> Unit,
    private val onLongClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.tvAlbumName.text = album.name
            binding.tvPhotoCount.text = binding.root.context.getString(R.string.gallery_photo_count, album.photoCount)

            if (album.coverUri != null) {
                Glide.with(binding.root.context)
                    .load(Uri.parse(album.coverUri))
                    .override(400, 300)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(GlidePlaceholders.batik(binding.root.context))
                    .into(binding.ivAlbumCover)
            } else {
                binding.ivAlbumCover.setImageResource(R.color.colorPrimaryLight)
            }

            binding.root.bounceClick()
            binding.root.setOnClickListener { onClick(album) }
            binding.root.setOnLongClickListener { onLongClick(album); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Album>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
