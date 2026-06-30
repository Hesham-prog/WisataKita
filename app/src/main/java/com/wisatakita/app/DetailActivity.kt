package com.wisatakita.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import com.wisatakita.app.data.TravelLocalRepository
import com.wisatakita.app.data.remote.GeoapifyService
import com.wisatakita.app.data.remote.NearbyPlace
import com.wisatakita.app.data.remote.PexelsService
import com.wisatakita.app.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var weatherViewModel: WeatherViewModel

    companion object {
        const val EXTRA_LANTERN_MESSAGE = "LANTERN_MESSAGE"
        private const val SCROLL_PHASE_TWO = 0.3f
        private const val SCROLL_END = 0.7f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        setupMotionScroll()
        setupWeatherObserver()
        binding.btnBack.setOnClickListener {
            HapticUtil.click(it)
            finish()
        }

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
            maybeShowLantern()
        }
    }

    private fun setupMotionScroll() {
        binding.motionDetail.setTransition(R.id.start, R.id.phase2)
        binding.detailScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val range = (resources.displayMetrics.heightPixels * SCROLL_END).coerceAtLeast(1f)
            val globalProgress = (scrollY / range).coerceIn(0f, 1f)
            if (globalProgress <= SCROLL_PHASE_TWO) {
                binding.motionDetail.setTransition(R.id.start, R.id.phase2)
                binding.motionDetail.progress = globalProgress / SCROLL_PHASE_TWO
            } else {
                binding.motionDetail.setTransition(R.id.phase2, R.id.end)
                binding.motionDetail.progress =
                    ((globalProgress - SCROLL_PHASE_TWO) / (1f - SCROLL_PHASE_TWO)).coerceIn(0f, 1f)
            }
            binding.tvDetailTitle.textSize = 30f - (globalProgress * 12f)
        }
    }

    private fun setupWeatherObserver() {
        weatherViewModel.weatherState.observe(this) { state ->
            if (state.loading) {
                binding.tvWeatherIcon.text = "--"
                binding.tvWeatherInfo.text = getString(R.string.weather_loading)
                return@observe
            }
            val weather = state.info
            binding.tvWeatherIcon.text = weather?.temperature?.toInt()?.toString() ?: "NA"
            binding.tvWeatherInfo.text = weather?.let {
                "${it.temperature.toInt()} C  ${it.description}\nKelembapan ${it.humidity}% - Angin ${"%.1f".format(it.windSpeed)} m/s"
            } ?: getString(R.string.weather_unavailable)

            when (state.mood) {
                WeatherViewModel.Mood.SUNNY -> {
                    binding.heroGoldOverlay.animate().alpha(0.22f).setDuration(280L).start()
                    binding.rainOverlay.visibility = View.GONE
                }
                WeatherViewModel.Mood.RAIN -> {
                    binding.heroGoldOverlay.animate().alpha(0.02f).setDuration(280L).start()
                    binding.rainOverlay.visibility = View.VISIBLE
                }
                WeatherViewModel.Mood.NEUTRAL -> {
                    binding.heroGoldOverlay.animate().alpha(0.08f).setDuration(280L).start()
                    binding.rainOverlay.visibility = View.GONE
                }
            }
        }
    }

    private fun bindDestination(destination: Destination) {
        Glide.with(this)
            .load(destination.imageUrl)
            .override(1080, 720)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.color.charcoal_dark)
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
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination.geoUri)))
            }
        }

        binding.btnPesanTiket.setOnClickListener {
            HapticUtil.click(it)
            LinkUtil.openTicketUrl(this, destination.ticketUrl)
        }

        binding.btnShareDestination.setOnClickListener {
            HapticUtil.click(it)
            LinkUtil.shareDestination(
                this,
                destination.name,
                destination.location,
                destination.latitude,
                destination.longitude
            )
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
                .placeholder(R.color.charcoal_medium)
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
                    .placeholder(R.color.charcoal_dark)
                    .into(binding.ivDetailImage)
                renderGallery(pexelsImages.drop(1) + destination.galleryImages)
            }
        }
    }

    private fun loadWeather(destination: Destination) {
        weatherViewModel.load(destination.latitude, destination.longitude)
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

    private fun maybeShowLantern() {
        val message = intent.getStringExtra(EXTRA_LANTERN_MESSAGE) ?: return
        SnackbarLantern(this).show(binding.motionDetail, message)
    }
}
