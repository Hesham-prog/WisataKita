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
    private var selectedCategory = ALL_CATEGORIES
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
            Toast.makeText(this, "Izin lokasi diperlukan untuk mode terdekat", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.tvDataSource.text = "${result.sourceLabel} - ${result.destinations.size} destinasi"
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
        addQuickChip("Semua", QuickFilter.None, checked = true)
        addQuickChip("Budget", QuickFilter.Budget)
        addQuickChip("Rating 4.7+", QuickFilter.HighRating)
        addQuickChip("Keluarga", QuickFilter.Family)
        addQuickChip("Alam terbuka", QuickFilter.Nature)

        addSortChip("Rekomendasi", SortMode.Recommended, checked = true)
        addSortChip("Rating", SortMode.Rating)
        addSortChip("Populer", SortMode.Popular)
        addSortChip("Nama A-Z", SortMode.Name)

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
        addCategoryChip(ALL_CATEGORIES, checked = true)
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
                            binding.btnNearest.text = "Terdekat aktif"
                        }
                        applyFilters()
                    } else {
                        Toast.makeText(this, "Lokasi belum tersedia. Nyalakan GPS lalu coba lagi.", Toast.LENGTH_SHORT).show()
                        applyFilters()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal membaca lokasi", Toast.LENGTH_SHORT).show()
                    applyFilters()
                }
        } catch (_: SecurityException) {
            Toast.makeText(this, "Izin lokasi belum aktif", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFilters() {
        val filtered = allDestinations
            .filter { destination -> selectedCategory == ALL_CATEGORIES || destination.category == selectedCategory }
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
            SortMode.Nearest -> "Destinasi terdekat"
            SortMode.Rating -> "Rating tertinggi"
            SortMode.Popular -> "Paling populer"
            SortMode.Name -> "Urutan A-Z"
            SortMode.Recommended -> "Rekomendasi terbaik"
        }
    }

    private fun renderSummary(items: List<DestinationUiItem>) {
        val queryText = if (query.isBlank()) "" else " untuk \"$query\""
        val categoryText = if (selectedCategory == ALL_CATEGORIES) "semua kategori" else selectedCategory
        val nearestText = if (selectedSort == SortMode.Nearest && userLocation == null) " - GPS belum tersedia" else ""
        binding.tvResultSummary.text = "${items.size} destinasi cocok$queryText - $categoryText - ${selectedSort.label}$nearestText"
    }

    private fun resetFilters() {
        query = ""
        selectedCategory = ALL_CATEGORIES
        selectedQuickFilter = QuickFilter.None
        selectedSort = SortMode.Recommended
        binding.etSearch.setText("")
        binding.btnNearest.text = "Terdekat"
        checkChipByText(binding.chipGroupCategories, ALL_CATEGORIES)
        checkChipByText(binding.chipGroupQuick, "Semua")
        checkChipByText(binding.chipGroupSort, SortMode.Recommended.label)
        applyFilters()
    }

    private fun ensureSortChipUnchecked() {
        binding.chipGroupSort.clearCheck()
        binding.btnNearest.text = "Terdekat aktif"
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

    private enum class SortMode(val label: String) {
        Recommended("Rekomendasi"),
        Rating("Rating"),
        Popular("Populer"),
        Name("Nama A-Z"),
        Nearest("Terdekat")
    }

    private sealed class QuickFilter {
        open fun matches(destination: Destination): Boolean = true

        object None : QuickFilter()
        object Budget : QuickFilter() {
            override fun matches(destination: Destination): Boolean {
                val text = destination.ticketPrice.lowercase()
                return text.contains("gratis") || text.contains("rp10") || text.contains("rp15") ||
                    text.contains("rp16") || text.contains("rp20") || text.contains("rp25")
            }
        }
        object HighRating : QuickFilter() {
            override fun matches(destination: Destination): Boolean = destination.rating >= 4.7
        }
        object Family : QuickFilter() {
            override fun matches(destination: Destination): Boolean {
                val text = "${destination.category} ${destination.description} ${destination.promoDescription}".lowercase()
                return text.contains("keluarga") || text.contains("edukasi") || text.contains("taman")
            }
        }
        object Nature : QuickFilter() {
            override fun matches(destination: Destination): Boolean {
                val text = "${destination.category} ${destination.description}".lowercase()
                return listOf("pantai", "gunung", "laut", "danau", "alam", "pegunungan", "bukit")
                    .any { text.contains(it) }
            }
        }
    }

    companion object {
        private const val ALL_CATEGORIES = "Semua"
    }
}
