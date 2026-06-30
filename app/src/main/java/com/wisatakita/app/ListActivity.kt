package com.wisatakita.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import com.wisatakita.app.data.LocationTools
import com.wisatakita.app.databinding.ActivityListBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var adapter: DestinationAdapter
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val allDestinations = mutableListOf<Destination>()
    private var selectedCategory = ""
    private var selectedQuickFilter: QuickFilter = QuickFilter.None
    private var selectedSort: SortMode = SortMode.Recommended
    private var query = ""
    private var userLocation: Location? = null
    private var lastRenderedItems: List<DestinationUiItem> = emptyList()

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            loadCurrentLocation(sortNearest = true)
        } else {
            Toast.makeText(this, R.string.list_location_permission_required, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedCategory = allCategoriesLabel()

        adapter = DestinationAdapter { destination ->
            startActivity(Intent(this, DetailActivity::class.java).apply {
                putExtra("DESTINATION_ID", destination.id)
            })
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupSearch()
        setupStaticControls()
        loadDestinations()
    }

    private fun loadDestinations() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val result = DestinationRepository(this@ListActivity).getDestinationsWithSource()
            allDestinations.clear()
            allDestinations.addAll(result.destinations)
            binding.tvDataSource.text = getString(R.string.list_data_source_format, result.sourceLabel, result.destinations.size)
            binding.progressBar.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            setupCategoryChips()
            applyFilters()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString()?.trim().orEmpty()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupStaticControls() {
        addQuickChip(getString(R.string.filter_all), QuickFilter.None, checked = true)
        addQuickChip(getString(R.string.filter_budget), QuickFilter.Budget)
        addQuickChip(getString(R.string.filter_high_rating), QuickFilter.HighRating)
        addQuickChip(getString(R.string.filter_family), QuickFilter.Family)
        addQuickChip(getString(R.string.filter_nature), QuickFilter.Nature)

        addSortChip(getString(R.string.sort_recommended), SortMode.Recommended, checked = true)
        addSortChip(getString(R.string.sort_rating), SortMode.Rating)
        addSortChip(getString(R.string.sort_popular), SortMode.Popular)
        addSortChip(getString(R.string.sort_name), SortMode.Name)

        binding.btnNearest.bounceClick()
        binding.btnNearest.setOnClickListener { requestNearestMode() }
        binding.btnResetFilters.bounceClick()
        binding.btnResetFilters.setOnClickListener { resetFilters() }
        binding.cardFeatured.bounceClick()
        binding.cardFeatured.setOnClickListener {
            lastRenderedItems.firstOrNull()?.destination?.let { destination ->
                startActivity(Intent(this, DetailActivity::class.java).apply {
                    putExtra("DESTINATION_ID", destination.id)
                })
            }
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews()
        addCategoryChip(allCategoriesLabel(), checked = true)
        allDestinations.map { it.category }
            .distinct()
            .sorted()
            .forEach { addCategoryChip(it) }
    }

    private fun addCategoryChip(label: String, checked: Boolean = false) {
        val chip = createChip(label).apply {
            isChecked = checked
            bounceClick()
            setOnClickListener {
                selectedCategory = label
                applyFilters()
            }
        }
        binding.chipGroupCategories.addView(chip)
    }

    private fun addQuickChip(label: String, filter: QuickFilter, checked: Boolean = false) {
        val chip = createChip(label).apply {
            isChecked = checked
            bounceClick()
            setOnClickListener {
                selectedQuickFilter = filter
                applyFilters()
            }
        }
        binding.chipGroupQuick.addView(chip)
    }

    private fun addSortChip(label: String, mode: SortMode, checked: Boolean = false) {
        val chip = createChip(label).apply {
            isChecked = checked
            bounceClick()
            setOnClickListener {
                selectedSort = mode
                applyFilters()
            }
        }
        binding.chipGroupSort.addView(chip)
    }

    private fun createChip(label: String): Chip {
        return Chip(this).apply {
            text = label
            isCheckable = true
            chipStrokeWidth = 1f
            chipStrokeColor = ContextCompat.getColorStateList(this@ListActivity, R.color.colorPrimary)
            chipBackgroundColor = ContextCompat.getColorStateList(this@ListActivity, R.color.white)
            setTextColor(ContextCompat.getColor(this@ListActivity, R.color.colorPrimary))
        }
    }

    private fun requestNearestMode() {
        selectedSort = SortMode.Nearest
        ensureSortChipUnchecked()
        when {
            hasLocationPermission() -> loadCurrentLocation(sortNearest = true)
            else -> locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadCurrentLocation(sortNearest: Boolean) {
        if (!hasLocationPermission()) return
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = location
                        if (sortNearest) {
                            binding.btnNearest.text = getString(R.string.btn_nearest_active)
                        }
                        applyFilters()
                    } else {
                        Toast.makeText(this, R.string.list_location_unavailable, Toast.LENGTH_SHORT).show()
                        applyFilters()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.list_location_failed, Toast.LENGTH_SHORT).show()
                    applyFilters()
                }
        } catch (_: SecurityException) {
            Toast.makeText(this, R.string.list_location_permission_inactive, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFilters() {
        val filtered = allDestinations
            .filter { destination -> selectedCategory == allCategoriesLabel() || destination.category == selectedCategory }
            .filter { destination -> selectedQuickFilter.matches(destination) }
            .filter { destination -> destination.matchesQuery(query) }
            .map { destination -> DestinationUiItem(destination, distanceTo(destination)) }
            .sortedWith(currentComparator())

        lastRenderedItems = filtered
        adapter.updateData(filtered)
        renderFeatured(filtered.firstOrNull())
        renderSummary(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty() && allDestinations.isNotEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun currentComparator(): Comparator<DestinationUiItem> {
        return when (selectedSort) {
            SortMode.Recommended -> compareByDescending<DestinationUiItem> {
                (it.destination.rating * 1000) + (it.destination.reviewCount * 0.1)
            }
            SortMode.Rating -> compareByDescending { it.destination.rating }
            SortMode.Popular -> compareByDescending { it.destination.reviewCount }
            SortMode.Name -> compareBy { it.destination.name }
            SortMode.Nearest -> compareBy { it.distanceKm ?: Double.MAX_VALUE }
        }
    }

    private fun distanceTo(destination: Destination): Double? {
        val location = userLocation ?: return null
        return LocationTools.distanceKm(location.latitude, location.longitude, destination)
    }

    private fun renderFeatured(item: DestinationUiItem?) {
        val destination = item?.destination
        binding.cardFeatured.visibility = if (destination == null) View.GONE else View.VISIBLE
        if (destination == null) return

        Glide.with(this)
            .load(destination.imageUrl)
            .override(1000, 520)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(GlidePlaceholders.batik(this))
            .into(binding.ivFeatured)

        binding.tvFeaturedName.text = destination.name
        binding.tvFeaturedMeta.text = buildString {
            append(destination.category)
            append(" - ${"%.1f".format(destination.rating)}")
            item.distanceKm?.let { append(" - ${"%.1f".format(it)} km") }
        }
        binding.tvFeaturedLabel.text = when (selectedSort) {
            SortMode.Nearest -> getString(R.string.list_feature_nearest)
            SortMode.Rating -> getString(R.string.list_feature_top_rating)
            SortMode.Popular -> getString(R.string.list_feature_popular)
            SortMode.Name -> getString(R.string.list_feature_name)
            SortMode.Recommended -> getString(R.string.list_feature_recommended)
        }
    }

    private fun renderSummary(items: List<DestinationUiItem>) {
        val queryText = if (query.isBlank()) "" else getString(R.string.list_query_suffix, query)
        val categoryText = if (selectedCategory == allCategoriesLabel()) getString(R.string.list_all_categories) else selectedCategory
        val nearestText = if (selectedSort == SortMode.Nearest && userLocation == null) " - ${getString(R.string.list_gps_unavailable)}" else ""
        binding.tvResultSummary.text = getString(
            R.string.list_result_summary,
            items.size,
            queryText,
            categoryText,
            sortLabel(selectedSort),
            nearestText
        )
    }

    private fun resetFilters() {
        query = ""
        selectedCategory = allCategoriesLabel()
        selectedQuickFilter = QuickFilter.None
        selectedSort = SortMode.Recommended
        binding.etSearch.setText("")
        binding.btnNearest.text = getString(R.string.btn_nearest)
        checkChipByText(binding.chipGroupCategories, allCategoriesLabel())
        checkChipByText(binding.chipGroupQuick, getString(R.string.filter_all))
        checkChipByText(binding.chipGroupSort, sortLabel(SortMode.Recommended))
        applyFilters()
    }

    private fun ensureSortChipUnchecked() {
        binding.chipGroupSort.clearCheck()
        binding.btnNearest.text = getString(R.string.btn_nearest_active)
    }

    private fun checkChipByText(group: com.google.android.material.chip.ChipGroup, text: String) {
        for (index in 0 until group.childCount) {
            val chip = group.getChildAt(index) as? Chip ?: continue
            if (chip.text == text) {
                chip.isChecked = true
                return
            }
        }
    }

    private fun Destination.matchesQuery(rawQuery: String): Boolean {
        if (rawQuery.isBlank()) return true
        val parts = rawQuery.lowercase().split(" ").filter { it.isNotBlank() }
        val haystack = listOf(
            name,
            location,
            address,
            category,
            description,
            promoTitle,
            promoDescription,
            transportInfo,
            funFacts.joinToString(" ")
        ).joinToString(" ").lowercase()
        return parts.all { haystack.contains(it) }
    }

    private enum class SortMode {
        Recommended,
        Rating,
        Popular,
        Name,
        Nearest
    }

    private sealed class QuickFilter {
        open fun matches(destination: Destination): Boolean = true

        object None : QuickFilter()
        object Budget : QuickFilter() {
            override fun matches(destination: Destination): Boolean {
                val text = destination.ticketPrice.lowercase()
                return text.contains("gratis") || text.contains("free") || text.contains("rp10") || text.contains("rp15") ||
                    text.contains("rp16") || text.contains("rp20") || text.contains("rp25")
            }
        }
        object HighRating : QuickFilter() {
            override fun matches(destination: Destination): Boolean = destination.rating >= 4.7
        }
        object Family : QuickFilter() {
            override fun matches(destination: Destination): Boolean {
                val text = "${destination.category} ${destination.description} ${destination.promoDescription}".lowercase()
                return text.contains("keluarga") || text.contains("family") || text.contains("edukasi") || text.contains("education") || text.contains("taman") || text.contains("park")
            }
        }
        object Nature : QuickFilter() {
            override fun matches(destination: Destination): Boolean {
                val text = "${destination.category} ${destination.description}".lowercase()
                return listOf("pantai", "beach", "gunung", "mountain", "laut", "marine", "danau", "lake", "alam", "nature", "pegunungan", "highland", "bukit", "hill")
                    .any { text.contains(it) }
            }
        }
    }

    private fun allCategoriesLabel(): String = getString(R.string.filter_all)

    private fun sortLabel(mode: SortMode): String = when (mode) {
        SortMode.Recommended -> getString(R.string.sort_recommended)
        SortMode.Rating -> getString(R.string.sort_rating)
        SortMode.Popular -> getString(R.string.sort_popular)
        SortMode.Name -> getString(R.string.sort_name)
        SortMode.Nearest -> getString(R.string.sort_nearest)
    }
}
