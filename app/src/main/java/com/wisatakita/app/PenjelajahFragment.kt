package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisatakita.app.databinding.FragmentPenjelajahBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PenjelajahFragment : Fragment() {

    private var _binding: FragmentPenjelajahBinding? = null
    private val binding get() = _binding!!

    private var currentViewMode = ViewMode.LIST
    private var searchJob: Job? = null
    private var renderedCategories = emptyList<String>()
    private var renderedCategory = ""

    private lateinit var destinationAdapter: MultiModeDestinationAdapter
    private lateinit var viewModel: PenjelajahViewModel

    enum class ViewMode { LIST, GRID, CARD }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPenjelajahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PenjelajahViewModel::class.java]
        setupAdapter()
        setupViewModeToggle()
        setupSearch()
        setupSortButton()
        setupResetFilters()
        observeDestinations()
        viewModel.loadDestinations()
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            HapticUtil.click(it)
            val options = arrayOf(
                getString(R.string.sort_option_az),
                getString(R.string.sort_option_za),
                getString(R.string.sort_option_nearest),
                getString(R.string.sort_option_farthest),
                getString(R.string.sort_option_highest_rating),
                getString(R.string.sort_option_lowest_rating),
                getString(R.string.sort_option_most_reviews),
                getString(R.string.sort_option_least_reviews)
            )
            
            val currentSort = viewModel.uiState.value?.sortOption ?: SortOption.AZ
            val checkedItem = currentSort.ordinal
            
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.sort_dialog_title))
                .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                    val sortOption = SortOption.values()[which]
                    
                    if (sortOption == SortOption.NEAREST || sortOption == SortOption.FARTHEST) {
                        fetchLocationAndSort(sortOption)
                    } else {
                        viewModel.setSortOption(sortOption)
                    }
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun fetchLocationAndSort(sortOption: SortOption) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocation = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocation.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.setUserLocation(location.latitude, location.longitude)
                }
                viewModel.setSortOption(sortOption)
            }
        } else {
            android.widget.Toast.makeText(requireContext(), getString(R.string.location_permission_required), android.widget.Toast.LENGTH_SHORT).show()
            viewModel.setSortOption(sortOption) // will fallback to AZ if location is null
        }
    }

    private fun setupAdapter() {
        destinationAdapter = MultiModeDestinationAdapter(
            currentViewMode,
            onItemClick = { destination ->
                val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("DESTINATION_ID", destination.id)
                }
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out_scale)
            },
            onCardLongPress = { destination ->
                if (currentViewMode == ViewMode.CARD) {
                    val intent = Intent(requireContext(), ReviewActivity::class.java).apply {
                        putExtra("DESTINATION_ID", destination.id)
                    }
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out_scale)
                }
            }
        )
        applyLayoutManager()
        binding.rvDestinations.adapter = destinationAdapter
    }

    private fun applyLayoutManager() {
        binding.rvDestinations.layoutManager = when (currentViewMode) {
            ViewMode.GRID -> GridLayoutManager(requireContext(), 2)
            else          -> LinearLayoutManager(requireContext())
        }
    }

    private fun setupViewModeToggle() {
        binding.viewModeToggle.onModeSelected = { index ->
            val mode = when (index) {
                1 -> ViewMode.GRID
                2 -> ViewMode.CARD
                else -> ViewMode.LIST
            }
            viewModel.setViewMode(mode)
        }
    }

    private fun switchViewMode(mode: ViewMode) {
        currentViewMode = mode
        destinationAdapter.setViewMode(mode)
        applyLayoutManager()
        binding.rvDestinations.alpha = 0f
        binding.rvDestinations.animate().alpha(1f).setDuration(200)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    viewModel.setSearchQuery(query)
                }
            }
        })
    }

    private fun setupResetFilters() {
        binding.btnResetFilters.bounceClick()
        binding.btnResetFilters.setOnClickListener {
            HapticUtil.click(it)
            binding.etSearch.setText("")
            viewModel.resetFilters()
        }
    }
    
    fun setSearchQueryFromVoice(query: String) {
        binding.etSearch.setText(query)
        viewModel.setSearchQuery(query)
    }

    private fun observeDestinations() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.viewMode != currentViewMode) {
                switchViewMode(state.viewMode)
            }
            val selectedIndex = when (state.viewMode) {
                ViewMode.LIST -> 0
                ViewMode.GRID -> 1
                ViewMode.CARD -> 2
            }
            binding.viewModeToggle.setSelectedIndex(selectedIndex)
            destinationAdapter.submitList(state.destinations)

            if (state.categories != renderedCategories || state.selectedCategory != renderedCategory) {
                renderedCategories = state.categories
                renderedCategory = state.selectedCategory
                buildFilterChips(state.categories, state.selectedCategory)
            }

            val isEmpty = state.destinations.isEmpty()
            binding.rvDestinations.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun buildFilterChips(categories: List<String>, activeCategory: String) {
        binding.llFilterChips.removeAllViews()
        val density = resources.displayMetrics.density

        categories.forEach { category ->
            val chip = android.widget.TextView(requireContext()).apply {
                text = category
                textSize = 12f
                setTextColor(
                    if (category == activeCategory) requireContext().getColor(R.color.charcoal_primary)
                    else requireContext().getColor(R.color.cream_primary)
                )
                background = requireContext().getDrawable(
                    if (category == activeCategory) R.drawable.bg_gold_button
                    else R.drawable.bg_chip_glass_green
                )
                setPadding(
                    (14 * density).toInt(), (7 * density).toInt(),
                    (14 * density).toInt(), (7 * density).toInt()
                )
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = (8 * density).toInt() }

                setOnClickListener { v ->
                    HapticUtil.click(v)
                    viewModel.setCategory(category)
                }
                bounceClick()
            }
            binding.llFilterChips.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
