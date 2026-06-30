package com.wisatakita.app

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import com.wisatakita.app.data.TravelLocalRepository
import com.wisatakita.app.databinding.ActivityReviewBinding
import kotlinx.coroutines.launch
import java.io.File

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var repository: TravelLocalRepository
    private var destination: Destination? = null
    private var pendingCameraUri: Uri? = null
    private var attachedPhotoUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraUri?.let { setAttachedPhoto(it) }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { setAttachedPhoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = TravelLocalRepository(this)

        val destinationId = intent.getStringExtra("DESTINATION_ID").orEmpty()
        setupInteractions(destinationId)
        lifecycleScope.launch {
            destination = DestinationRepository(this@ReviewActivity).getDestinationById(destinationId)
            destination?.let { bindDestination(it) } ?: finish()
        }
    }

    private fun setupInteractions(destinationId: String) {
        binding.btnBack.bounceClick()
        binding.btnBack.setOnClickListener {
            HapticUtil.click(it)
            finish()
        }
        binding.ratingGunung.onRatingChanged = { rating ->
            binding.tvRatingValue.text = "$rating/5"
        }
        binding.etReview.setOnFocusChangeListener { _, hasFocus ->
            binding.reviewUnderline.animate()
                .scaleX(if (hasFocus) 1f else 0.72f)
                .alpha(if (hasFocus) 1f else 0.55f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(180L)
                .start()
        }
        binding.btnCamera.bounceClick()
        binding.btnCamera.setOnClickListener {
            HapticUtil.click(it)
            pendingCameraUri = createPhotoUri()
            pendingCameraUri?.let { uri -> takePicture.launch(uri) }
        }
        binding.btnGallery.bounceClick()
        binding.btnGallery.setOnClickListener {
            HapticUtil.click(it)
            pickImage.launch("image/*")
        }
        binding.btnDeletePhoto.bounceClick()
        binding.btnDeletePhoto.setOnClickListener {
            HapticUtil.click(it)
            attachedPhotoUri = null
            binding.cardPhotoPreview.visibility = View.GONE
        }
        binding.btnSubmitReview.bounceClick()
        binding.btnSubmitReview.setOnClickListener {
            HapticUtil.click(it)
            submitReview(destinationId)
        }
    }

    private fun bindDestination(destination: Destination) {
        binding.ivDestination.transitionName = "destination_${destination.id}"
        binding.tvDestinationName.text = destination.name
        binding.tvDestinationLocation.text = destination.location
        Glide.with(this)
            .load(destination.imageUrl)
            .override(360, 360)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(GlidePlaceholders.batik(this))
            .into(binding.ivDestination)
    }

    private fun createPhotoUri(): Uri? {
        val dir = File(getExternalFilesDir("Pictures"), "reviews").apply { mkdirs() }
        val file = File(dir, "review_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    }

    private fun setAttachedPhoto(uri: Uri) {
        attachedPhotoUri = uri
        binding.cardPhotoPreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .placeholder(GlidePlaceholders.batik(this))
            .into(binding.ivAttachedPhoto)
    }

    private fun submitReview(destinationId: String) {
        val rating = binding.ratingGunung.rating
        val body = binding.etReview.text?.toString()?.trim().orEmpty()
        when {
            rating <= 0 -> {
                Toast.makeText(this, R.string.review_empty_rating, Toast.LENGTH_SHORT).show()
                return
            }
            body.isBlank() -> {
                Toast.makeText(this, R.string.review_empty_body, Toast.LENGTH_SHORT).show()
                return
            }
        }

        binding.btnSubmitReview.isEnabled = false
        lifecycleScope.launch {
            repository.addReview(destinationId, rating, body)
            Toast.makeText(this@ReviewActivity, R.string.review_success, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
