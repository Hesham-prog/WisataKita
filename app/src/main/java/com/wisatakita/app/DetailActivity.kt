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
import androidx.core.content.res.ResourcesCompat
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        setupMotionScroll()
        setupWeatherObserver()
        setupMusicOrb()
        keepControlsAboveScroll()
        binding.btnBack.bounceClick()
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
            NotificationScheduler.scheduleReviewNudge(this@DetailActivity, destination.id)
            loadPexelsPhotos(destination)
            loadWeather(destination)
            loadNearbyPlaces(destination)
            maybeShowLantern()
            setupFavoriteButton(destination)
        }
    }

    private fun setupFavoriteButton(destination: Destination) {
        val repo = TravelLocalRepository(this)
        lifecycleScope.launch {
            val isFav = repo.isFavorite(destination.id)
            setFavoriteIconInitial(isFav)
        }
        binding.btnFavorite.bounceClick()
        binding.btnFavorite.setOnClickListener {
            HapticUtil.click(it)
            lifecycleScope.launch {
                val isNowFav = repo.toggleFavorite(destination.id)
                playFavoriteAnimation(isNowFav)
                val msg = if (isNowFav) getString(R.string.favorite_added, destination.name) else getString(R.string.favorite_removed, destination.name)
                android.widget.Toast.makeText(this@DetailActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFavoriteIconInitial(isFavorite: Boolean) {
        binding.btnFavorite.setIconResource(if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        binding.btnFavorite.setIconTintResource(if (isFavorite) R.color.gold_primary else R.color.cream_primary)
    }

    private fun playFavoriteAnimation(isFavorite: Boolean) {
        if (isFavorite) {
            // Heart pop animation
            binding.btnFavorite.setIconResource(R.drawable.ic_heart_filled)
            binding.btnFavorite.setIconTintResource(R.color.gold_primary)
            binding.btnFavorite.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(150)
                .withEndAction {
                    binding.btnFavorite.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }.start()
        } else {
            // Heartbreak animation
            binding.btnFavorite.setIconResource(R.drawable.ic_heart_broken)
            binding.btnFavorite.setIconTintResource(android.R.color.holo_red_light)
            
            // Shake effect
            binding.btnFavorite.animate()
                .rotation(15f).setDuration(50)
                .withEndAction {
                    binding.btnFavorite.animate().rotation(-15f).setDuration(50).withEndAction {
                        binding.btnFavorite.animate().rotation(0f).setDuration(50).withEndAction {
                            // Fade back to normal outline
                            binding.btnFavorite.postDelayed({
                                binding.btnFavorite.setIconResource(R.drawable.ic_heart_outline)
                                binding.btnFavorite.setIconTintResource(R.color.cream_primary)
                            }, 300)
                        }.start()
                    }.start()
                }.start()
        }
    }

    private fun keepControlsAboveScroll() {
        binding.btnBack.bringToFront()
        binding.musicOrb.bringToFront()
        binding.tvDetailTitle.bringToFront()
        binding.layoutFabActions.bringToFront()
        binding.btnPesanTiket.bringToFront()
    }


    private fun setupMusicOrb() {
        binding.musicOrb.setPlaying(MusicService.isPlaying)
        binding.musicOrb.bounceClick()
        binding.musicOrb.setOnClickListener {
            HapticUtil.click(it)
            val intent = Intent(this, MusicService::class.java)
            if (MusicService.isPlaying) {
                intent.action = MusicService.ACTION_PAUSE
                MusicService.isPlaying = false
            } else {
                intent.action = MusicService.ACTION_RESUME
                MusicService.isPlaying = true
            }
            startService(intent)
            binding.musicOrb.setPlaying(MusicService.isPlaying)
        }
    }

    private fun setupMotionScroll() {
        // OnSwipe in scene_detail.xml now drives the transition automatically.
        // We just need to prime the MotionLayout to the correct transition.
        binding.motionDetail.setTransition(R.id.start, R.id.end)
        binding.motionDetail.progress = 0f
    }

    private fun setupWeatherObserver() {
        weatherViewModel.weatherState.observe(this) { state ->
            if (state.loading) {
                binding.tvWeatherIcon.text = "⏳"
                binding.tvWeatherInfo.text = getString(R.string.weather_loading)
                return@observe
            }
            val weather = state.info
            binding.tvWeatherInfo.text = weather?.let {
                getString(
                    R.string.detail_weather_format,
                    it.temperature.toInt(),
                    it.description,
                    it.humidity,
                    it.windSpeed
                )
            } ?: getString(R.string.weather_unavailable)

            when (state.mood) {
                WeatherViewModel.Mood.SUNNY -> {
                    // Clear sunny sky — golden sun icon, warm card tint
                    binding.tvWeatherIcon.text = "☀️"
                    binding.tvWeatherIcon.textSize = 28f
                    binding.cardWeather.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.weather_sunny_tint)
                    )
                    binding.heroGoldOverlay.animate().alpha(0.22f).setDuration(280L).start()
                    binding.rainOverlay.visibility = View.GONE
                }
                WeatherViewModel.Mood.RAIN -> {
                    // Rainy — blue cloud-rain icon, cool blue card tint
                    binding.tvWeatherIcon.text = "🌧️"
                    binding.tvWeatherIcon.textSize = 28f
                    binding.cardWeather.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.weather_rain_tint)
                    )
                    binding.heroGoldOverlay.animate().alpha(0.02f).setDuration(280L).start()
                    binding.rainOverlay.visibility = View.VISIBLE
                }
                WeatherViewModel.Mood.NEUTRAL -> {
                    // Cloudy / neutral — soft grey cloud icon
                    binding.tvWeatherIcon.text = "⛅"
                    binding.tvWeatherIcon.textSize = 28f
                    binding.cardWeather.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.weather_neutral_tint)
                    )
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
            .placeholder(GlidePlaceholders.batik(this))
            .into(binding.ivDetailImage)

        binding.tvDetailTitle.text = destination.name
        binding.tvDetailLocation.text = destination.location
        binding.tvDetailAddress.text = destination.address
        binding.tvDetailMeta.text = getString(
            R.string.detail_meta_format,
            destination.category,
            destination.rating,
            destination.reviewCount
        )
        binding.tvTicketInfo.text = "${destination.ticketPrice} - ${destination.openingHours}"
        binding.tvWeatherInfo.text = getString(R.string.detail_weather_realtime_loading)
        binding.tvDetailDescription.text = destination.description
        binding.tvPromoInfo.text = if (destination.promoTitle.isNotBlank()) {
            "${destination.promoTitle}\n${destination.promoDescription}"
        } else {
            getString(R.string.detail_no_promo)
        }
        binding.tvTransportInfo.text = getString(
            R.string.detail_transport_format,
            destination.transportInfo.ifBlank { getString(R.string.detail_transport_unavailable) }
        )
        binding.tvEmergencyInfo.text = getString(
            R.string.detail_emergency_format,
            destination.emergencyContact.ifBlank { getString(R.string.detail_emergency_unavailable) }
        )
        binding.tvReviews.text = if (destination.reviews.isNotEmpty()) {
            getString(R.string.detail_reviews_title) + "\n" + destination.reviews.joinToString("\n") { "• $it" }
        } else {
            getString(R.string.detail_no_reviews)
        }

        renderFunFacts(destination.funFacts)
        renderGallery(destination.galleryImages.ifEmpty { listOf(destination.imageUrl) })

        binding.btnGoogleMaps.bounceClick()
        binding.btnGoogleMaps.setOnClickListener {
            HapticUtil.click(it)
            if (destination.latitude != 0.0 && destination.longitude != 0.0) {
                LinkUtil.openMapPin(this, destination.latitude, destination.longitude, destination.name)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination.geoUri)))
            }
        }

        binding.btnPesanTiket.bounceClick()
        binding.btnPesanTiket.setOnClickListener {
            HapticUtil.click(it)
            LinkUtil.openTicketUrl(this, destination.ticketUrl)
        }

        binding.btnWriteReview.bounceClick()
        binding.btnWriteReview.setOnClickListener {
            HapticUtil.click(it)
            startActivity(Intent(this, ReviewActivity::class.java).apply {
                putExtra("DESTINATION_ID", destination.id)
            })
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out_scale)
        }

        binding.btnShareDestination.bounceClick()
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
                text = "• $fact"
                textSize = 14f
                typeface = ResourcesCompat.getFont(this@DetailActivity, R.font.plus_jakarta_sans)
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
                .placeholder(GlidePlaceholders.batik(this))
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
                    .placeholder(GlidePlaceholders.batik(this@DetailActivity))
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
            NearbyPlace(
                getString(R.string.detail_nearby_unavailable_name),
                getString(R.string.detail_nearby_unavailable_address)
            )
        )
        displayPlaces.forEach { place ->
            val tv = TextView(this).apply {
                text = "• ${place.name}\n  ${place.address}"
                textSize = 13.5f
                typeface = ResourcesCompat.getFont(this@DetailActivity, R.font.plus_jakarta_sans)
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.colorTextSecondary))
                setPadding(0, 0, 0, (14 * resources.displayMetrics.density).toInt())
                setLineSpacing(4 * resources.displayMetrics.density, 1f)
            }
            binding.llNearbyPlaces.addView(tv)
        }
    }

    private fun maybeShowLantern() {
        val message = intent.getStringExtra(EXTRA_LANTERN_MESSAGE) ?: return
        SnackbarLantern(this).show(binding.motionDetail, message)
    }
}
