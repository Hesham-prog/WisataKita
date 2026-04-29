package com.wisatakita.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.DestinationData
import com.wisatakita.app.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val destinationId = intent.getStringExtra("DESTINATION_ID") ?: ""
        val destination = DestinationData.getById(destinationId) ?: run { finish(); return }

        Glide.with(this)
            .load(destination.imageUrl)
            .override(1080, 720)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.color.colorPrimaryLight)
            .into(binding.ivDetailImage)

        binding.tvDetailTitle.text = destination.name
        binding.tvDetailLocation.text = destination.location
        binding.tvDetailAddress.text = destination.address
        binding.tvDetailDescription.text = destination.description

        val density = resources.displayMetrics.density
        destination.funFacts.forEachIndexed { index, fact ->
            val tv = TextView(this).apply {
                text = "✦  $fact"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorTextSecondary))
                val bottomPad = if (index < destination.funFacts.size - 1) (14 * density).toInt() else 0
                setPadding(0, 0, 0, bottomPad)
                setLineSpacing(4 * density, 1f)
            }
            binding.llFunFacts.addView(tv)
        }

        val photoW = (160 * density).toInt()
        val photoH = (112 * density).toInt()
        val photoMargin = (8 * density).toInt()

        destination.galleryImages.forEach { url ->
            val card = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(photoW, photoH).apply {
                    marginEnd = photoMargin
                }
                radius = 10 * density
                cardElevation = 0f
            }
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            card.addView(iv)
            Glide.with(this)
                .load(url)
                .override(480, 336)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.color.colorPrimaryLight)
                .into(iv)
            binding.llGallery.addView(card)
        }

        binding.btnGoogleMaps.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination.geoUri)))
        }

        binding.btnPesanTiket.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination.ticketUrl)))
        }
    }
}
