package com.wisatakita.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.wisatakita.app.data.GalleryData
import com.wisatakita.app.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding
    private lateinit var galleryData: GalleryData
    private lateinit var adapter: AlbumAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        galleryData = GalleryData(this)

        adapter = AlbumAdapter(
            mutableListOf(),
            onClick = { album ->
                startActivity(Intent(this, AlbumDetailActivity::class.java).apply {
                    putExtra("ALBUM_ID", album.id)
                })
            },
            onLongClick = { album -> showDeleteAlbumDialog(album.id, album.name) }
        )

        binding.recyclerAlbums.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerAlbums.adapter = adapter

        binding.btnBack.bounceClick()
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddAlbum.bounceClick()
        binding.fabAddAlbum.setOnClickListener { showCreateAlbumDialog() }
    }

    override fun onResume() {
        super.onResume()
        refreshAlbums()
    }

    private fun refreshAlbums() {
        val albums = galleryData.getAlbums()
        adapter.updateData(albums)
        val count = albums.size
        binding.tvAlbumCount.text = getString(R.string.gallery_album_count, count)
        if (albums.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerAlbums.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerAlbums.visibility = View.VISIBLE
        }
    }

    private fun showCreateAlbumDialog() {
        val editText = EditText(this).apply {
            hint = getString(R.string.gallery_album_name_hint)
            setPadding(48, 32, 48, 16)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gallery_create_album))
            .setView(editText)
            .setPositiveButton(getString(R.string.gallery_create)) { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    galleryData.createAlbum(name)
                    refreshAlbums()
                } else {
                    Toast.makeText(this, R.string.gallery_album_name_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeleteAlbumDialog(albumId: String, albumName: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gallery_delete_album))
            .setMessage(getString(R.string.gallery_delete_album_message, albumName))
            .setPositiveButton(getString(R.string.gallery_delete_album)) { _, _ ->
                galleryData.deleteAlbum(albumId)
                refreshAlbums()
                Toast.makeText(this, R.string.gallery_album_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
