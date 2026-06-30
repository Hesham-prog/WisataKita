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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import com.wisatakita.app.data.MapDestinationHelper
import com.wisatakita.app.data.TravelLocalRepository
import com.wisatakita.app.data.remote.GeoapifyService
import com.wisatakita.app.data.remote.NearbyPlace
import com.wisatakita.app.data.remote.PexelsService
import com.wisatakita.app.data.remote.WeatherService
import com.wisatakita.app.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val destinationId = intent.getStringExtra("DESTINATION_ID") ?: ""
        lifecycleScope.launch {
            val destination = DestinationRepository(this@DetailActivity).getDestinationById(destinationId)
                ?: run {
                    finish()
                    return@launch
                }
            bindDestination(destination)
            TravelLocalRepository(this@DetailActivity).recordDestinationView(destination)
            loadPexelsPhotos(destination)
            loadWeather(destination)
            loadNearbyPlaces(destination)
        }
    }

    private fun bindDestination(destination: Destination) {
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
        binding.tvDetailMeta.text = "${destination.category} - Rating ${"%.1f".format(destination.rating)} (${destination.reviewCount} review)"
        binding.tvTicketInfo.text = "${destination.ticketPrice} - ${destination.openingHours}"
        binding.tvWeatherInfo.text = "Memuat cuaca real-time..."
        binding.tvDetailDescription.text = destination.description
        binding.tvPromoInfo.text = if (destination.promoTitle.isNotBlank()) {
            "${destination.promoTitle}\n${destination.promoDescription}"
        } else {
            "Promo belum tersedia untuk destinasi ini."
        }
        binding.tvTransportInfo.text = "Transportasi: ${destination.transportInfo.ifBlank { "Informasi transportasi belum tersedia." }}"
        binding.tvEmergencyInfo.text = "Kontak/Info penting: ${destination.emergencyContact.ifBlank { "Ikuti arahan petugas setempat." }}"
        binding.tvReviews.text = if (destination.reviews.isNotEmpty()) {
            "Review pengunjung:\n" + destination.reviews.joinToString("\n") { "- $it" }
        } else {
            "Belum ada review pengunjung."
        }

        renderFunFacts(destination.funFacts)
        renderGallery(destination.galleryImages.ifEmpty { listOf(destination.imageUrl) })

        binding.btnGoogleMaps.setOnClickListener {
            HapticUtil.click(it)
            if (destination.latitude != 0.0 && destination.longitude != 0.0) {
                LinkUtil.openMapPin(this, destination.latitude, destination.longitude, destination.name)
            } else {
                // Fallback to geoUri field from data
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination.geoUri)))
            }
        }

        binding.btnPesanTiket.setOnClickListener {
            HapticUtil.click(it)
            LinkUtil.openTicketUrl(this, destination.ticketUrl)
        }
    }

    private fun renderFunFacts(funFacts: List<String>) {
        val density = resources.displayMetrics.density
        binding.llFunFacts.removeAllViews()
        funFacts.forEachIndexed { index, fact ->
            val tv = TextView(this).apply {
                text = "- $fact"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorTextSecondary))
                val bottomPad = if (index < funFacts.size - 1) (14 * density).toInt() else 0
                setPadding(0, 0, 0, bottomPad)
                setLineSpacing(4 * density, 1f)
            }
            binding.llFunFacts.addView(tv)
        }
    }

    private fun renderGallery(images: List<String>) {
        val density = resources.displayMetrics.density
        val photoW = (160 * density).toInt()
        val photoH = (112 * density).toInt()
        val photoMargin = (8 * density).toInt()
        binding.llGallery.removeAllViews()

        images.forEach { url ->
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
    }

    private fun loadPexelsPhotos(destination: Destination) {
        lifecycleScope.launch {
            val pexelsImages = PexelsService().searchPhotos(destination.imageQuery, count = 5)
            if (pexelsImages.isNotEmpty()) {
                Glide.with(this@DetailActivity)
                    .load(pexelsImages.first())
                    .override(1080, 720)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.colorPrimaryLight)
                    .into(binding.ivDetailImage)
                renderGallery(pexelsImages.drop(1) + destination.galleryImages)
            }
        }
    }

    private fun loadWeather(destination: Destination) {
        lifecycleScope.launch {
            val weather = WeatherService().getCurrentWeather(destination.latitude, destination.longitude)
            binding.tvWeatherInfo.text = weather?.let {
                "Cuaca saat ini\n${it.asDisplayText()}"
            } ?: "Cuaca saat ini\nData cuaca belum tersedia."
        }
    }

    private fun loadNearbyPlaces(destination: Destination) {
        lifecycleScope.launch {
            val places = GeoapifyService().getNearbyPlaces(destination.latitude, destination.longitude)
            renderNearbyPlaces(places)
        }
    }

    private fun renderNearbyPlaces(places: List<NearbyPlace>) {
        binding.llNearbyPlaces.removeAllViews()
        val displayPlaces = if (places.isNotEmpty()) places else listOf(
            NearbyPlace("Data tempat sekitar belum tersedia", "Coba lagi saat koneksi internet aktif.")
        )
        displayPlaces.forEach { place ->
            val tv = TextView(this).apply {
                text = "- ${place.name}\n  ${place.address}"
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorTextSecondary))
                setPadding(0, 0, 0, (10 * resources.displayMetrics.density).toInt())
                setLineSpacing(3 * resources.displayMetrics.density, 1f)
            }
            binding.llNearbyPlaces.addView(tv)
        }
    }
}
