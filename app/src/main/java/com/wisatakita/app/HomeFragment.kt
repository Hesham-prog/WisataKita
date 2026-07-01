package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var featuredAdapter: FeaturedDestinationAdapter
    private lateinit var nearbyAdapter: HomeNearbyAdapter
    private lateinit var viewModel: HomeViewModel

    private val heroHandler = Handler(Looper.getMainLooper())
    private var heroIndex = 0
    private var heroImages = listOf<String>()
    
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) fetchLocation()
    }
    
    private val voicePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchVoiceSearch()
        else android.widget.Toast.makeText(requireContext(), getString(R.string.voice_search_mic_permission), android.widget.Toast.LENGTH_SHORT).show()
    }

    private val voiceSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                (requireActivity() as? MainActivity)?.navigateToJelajahi(spokenText)
            }
        }
    }
    
    private val heroRunnable = object : Runnable {
        override fun run() {
            _binding?.let { b ->
                if (heroImages.isNotEmpty()) {
                    heroIndex = (heroIndex + 1) % heroImages.size
                    com.bumptech.glide.Glide.with(this@HomeFragment)
                        .load(heroImages[heroIndex])
                        .centerCrop()
                        .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(1000))
                        .into(b.ivHeroBg)
                }
            }
            heroHandler.postDelayed(this, 5000)
        }
    }

    private fun launchVoiceSearch() {
        val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_prompt))
        }
        try {
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), getString(R.string.voice_search_unavailable), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupGreeting()
        setupRecyclerViews()
        setupClickListeners()
        observeDestinations()
        viewModel.loadDestinations()
        setupAboutIndonesia()
        animateEntrance()
        heroHandler.postDelayed(heroRunnable, 5000)
        
        requestLocation()
    }

    private fun requestLocation() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(permission)
        }
    }

    private fun fetchLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setUserLocation(location.latitude, location.longitude)
                }
            }
        } catch (e: SecurityException) {
            // Permission wasn't actually granted
        }
    }


    private fun setupGreeting() {
        val userPrefs = UserPrefs(requireContext())
        val name = userPrefs.getCurrentName().ifBlank { getString(R.string.home_default_user) }
        binding.tvGreeting.text = getString(R.string.home_greeting_name, name)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreetingTime.text = when {
            hour < 11 -> getString(R.string.greeting_morning)
            hour < 15 -> getString(R.string.greeting_afternoon)
            hour < 18 -> getString(R.string.greeting_evening)
            else       -> getString(R.string.greeting_night)
        }
    }

    private fun setupRecyclerViews() {
        featuredAdapter = FeaturedDestinationAdapter { destination ->
            openDetail(destination)
        }
        binding.rvFeatured.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredAdapter
            setHasFixedSize(true)
        }

        nearbyAdapter = HomeNearbyAdapter { destination ->
            openDetail(destination)
        }
        binding.rvNearby.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearbyAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
        }
    }

    private fun observeDestinations() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            featuredAdapter.submitList(state.featured)
            nearbyAdapter.submitList(state.nearby)
            setupCategoryChips(state.categories, state.selectedCategory)
            
            // Populate hero images from featured destinations
            if (state.featured.isNotEmpty() && heroImages.isEmpty()) {
                heroImages = state.featured.take(5).map { it.imageUrl }
                // Load first image immediately
                com.bumptech.glide.Glide.with(this@HomeFragment)
                    .load(heroImages[0])
                    .centerCrop()
                    .into(binding.ivHeroBg)
            }
        }
    }

    private fun setupCategoryChips(categories: List<String>, selectedCategory: String) {
        binding.llCategories.removeAllViews()
        val density = resources.displayMetrics.density

        categories.forEach { category ->
            val selected = category == selectedCategory
            val chip = android.widget.TextView(requireContext()).apply {
                text = category
                textSize = 13f
                setTextColor(
                    resources.getColor(
                        if (selected) R.color.charcoal_primary else R.color.cream_primary,
                        null
                    )
                )
                typeface = ResourcesCompat.getFont(requireContext(), R.font.plus_jakarta_sans_semibold)
                background = resources.getDrawable(
                    if (selected) R.drawable.bg_gold_button else R.drawable.bg_chip_glass_green,
                    null
                )
                setPadding(
                    (16 * density).toInt(), (8 * density).toInt(),
                    (16 * density).toInt(), (8 * density).toInt()
                )
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = (10 * density).toInt() }

                setOnClickListener { v ->
                    HapticUtil.click(v)
                    viewModel.selectCategory(category)
                }
                bounceClick()
            }
            binding.llCategories.addView(chip)
        }
    }

    private fun setupClickListeners() {
        binding.btnVoiceSearch.setOnClickListener {
            HapticUtil.click(it)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                launchVoiceSearch()
            } else {
                voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        
        binding.cardSearch.bounceClick()
        binding.cardSearch.setOnClickListener {
            HapticUtil.click(it)
            (activity as? MainActivity)?.showFragment(1)
        }

        binding.tvSeeAllFeatured.bounceClick()
        binding.tvSeeAllFeatured.setOnClickListener {
            HapticUtil.click(it)
            (activity as? MainActivity)?.showFragment(1)
        }

        binding.tvSeeAllNearby.bounceClick()
        binding.tvSeeAllNearby.setOnClickListener {
            HapticUtil.click(it)
            (activity as? MainActivity)?.showFragment(1)
        }

        binding.btnMusic.bounceClick()
        binding.btnMusic.setOnClickListener {
            HapticUtil.click(it)
            val intent = Intent(requireContext(), MusicService::class.java)
            if (MusicService.isPlaying) {
                intent.action = MusicService.ACTION_PAUSE
                MusicService.isPlaying = false
            } else {
                intent.action = MusicService.ACTION_RESUME
                MusicService.isPlaying = true
            }
            binding.btnMusic.setPlaying(MusicService.isPlaying)
            requireContext().startService(intent)
        }
    }

    private fun setupAboutIndonesia() {
        var isExpanded = false

        binding.tvAboutReadMore.bounceClick()
        binding.tvAboutReadMore.setOnClickListener {
            HapticUtil.click(it)
            isExpanded = !isExpanded
            if (isExpanded) {
                binding.tvAboutIndonesia.maxLines = Int.MAX_VALUE
                binding.tvAboutIndonesia.ellipsize = null
                binding.tvAboutReadMore.text = getString(R.string.about_indonesia_read_less)
            } else {
                binding.tvAboutIndonesia.maxLines = 4
                binding.tvAboutIndonesia.ellipsize = android.text.TextUtils.TruncateAt.END
                binding.tvAboutReadMore.text = getString(R.string.about_indonesia_read_more)
            }
        }
    }

    private fun animateEntrance() {
        val views = listOf(
            binding.rvFeatured,
            binding.llCategories,
            binding.rvNearby,
            binding.cardAboutIndonesia
        )
        views.forEachIndexed { i, v ->
            v.alpha = 0f
            v.translationY = 40f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200L + i * 100L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun openDetail(destination: Destination) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("DESTINATION_ID", destination.id)
        }
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out_scale)
    }

    override fun onResume() {
        super.onResume()
        binding.btnMusic.setPlaying(MusicService.isPlaying)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        heroHandler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
