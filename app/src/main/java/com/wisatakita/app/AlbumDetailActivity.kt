package com.wisatakita.app

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.wisatakita.app.data.GalleryData
import com.wisatakita.app.databinding.ActivityAlbumDetailBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbumDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumDetailBinding
    private lateinit var galleryData: GalleryData
    private lateinit var adapter: PhotoAdapter
    private var albumId: String = ""
    private var pendingCameraUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            galleryData.addPhoto(albumId, it.toString())
            refreshPhotos()
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let {
                galleryData.addPhoto(albumId, it.toString())
                refreshPhotos()
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else Toast.makeText(this, R.string.gallery_camera_permission, Toast.LENGTH_SHORT).show()
    }

    private val requestGalleryPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pickImage.launch("image/*") else Toast.makeText(this, R.string.gallery_media_permission, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        albumId = intent.getStringExtra("ALBUM_ID") ?: run { finish(); return }
        galleryData = GalleryData(this)

        adapter = PhotoAdapter(mutableListOf()) { uri -> showDeletePhotoDialog(uri) }

        binding.recyclerPhotos.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerPhotos.adapter = adapter

        binding.btnBack.bounceClick()
        binding.btnBack.setOnClickListener { finish() }

        binding.btnDeleteAlbum.bounceClick()
        binding.btnDeleteAlbum.setOnClickListener {
            val album = galleryData.getAlbum(albumId)
            showDeleteAlbumDialog(album?.name ?: "")
        }

        binding.fabAddPhoto.bounceClick()
        binding.fabAddPhoto.setOnClickListener { showAddPhotoDialog() }
    }

    override fun onResume() {
        super.onResume()
        refreshPhotos()
    }

    private fun refreshPhotos() {
        val album = galleryData.getAlbum(albumId) ?: run { finish(); return }
        binding.tvAlbumTitle.text = album.name
        binding.tvPhotoCountHeader.text = getString(R.string.gallery_photo_count, album.photoCount)
        adapter.updateData(album.photoUris)

        if (album.photoUris.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerPhotos.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerPhotos.visibility = View.VISIBLE
        }
    }

    private fun showAddPhotoDialog() {
        val options = arrayOf(getString(R.string.gallery_take_photo), getString(R.string.gallery_pick_photo))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gallery_add_photo))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraAndLaunch()
                    1 -> checkGalleryAndLaunch()
                }
            }
            .show()
    }

    private fun checkCameraAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryAndLaunch() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> pickImage.launch("image/*")
            else -> requestGalleryPermission.launch(permission)
        }
    }

    private fun launchCamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timestamp.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        pendingCameraUri = uri
        takePicture.launch(uri)
    }

    private fun showDeletePhotoDialog(uri: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gallery_delete_photo))
            .setMessage(getString(R.string.gallery_delete_photo_message))
            .setPositiveButton(getString(R.string.gallery_delete_photo)) { _, _ ->
                galleryData.deletePhoto(albumId, uri)
                refreshPhotos()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeleteAlbumDialog(albumName: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gallery_delete_album))
            .setMessage(getString(R.string.gallery_delete_album_message, albumName))
            .setPositiveButton(getString(R.string.gallery_delete_album)) { _, _ ->
                galleryData.deleteAlbum(albumId)
                Toast.makeText(this, R.string.gallery_album_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
