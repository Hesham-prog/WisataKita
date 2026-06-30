package com.wisatakita.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import com.wisatakita.app.databinding.FragmentPenjelajahBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PenjelajahFragment : Fragment() {

    private var _binding: FragmentPenjelajahBinding? = null
    private val binding get() = _binding!!

    private var allDestinations = listOf<Destination>()
    private var currentViewMode = ViewMode.LIST
    private var activeCategory = "Semua"
    private var searchQuery = ""
    private var searchJob: Job? = null

    private lateinit var destinationAdapter: MultiModeDestinationAdapter

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

        setupAdapter()
        setupViewModeToggle()
        setupSearch()
        loadDestinations()
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
        binding.btnViewList.setOnClickListener {
            HapticUtil.click(it)
            switchViewMode(ViewMode.LIST)
            updateToggleHighlight(0)
        }
        binding.btnViewGrid.setOnClickListener {
            HapticUtil.click(it)
            switchViewMode(ViewMode.GRID)
            updateToggleHighlight(1)
        }
        binding.btnViewCard.setOnClickListener {
            HapticUtil.click(it)
            switchViewMode(ViewMode.CARD)
            updateToggleHighlight(2)
        }
    }

    private fun updateToggleHighlight(selectedIndex: Int) {
        val btns = listOf(binding.btnViewList, binding.btnViewGrid, binding.btnViewCard)
        btns.forEachIndexed { i, btn ->
            val isSelected = i == selectedIndex
            btn.alpha = if (isSelected) 1f else 0.5f
            btn.setColorFilter(
                if (isSelected) requireContext().getColor(R.color.gold_primary)
                else requireContext().getColor(R.color.cream_primary)
            )
        }
    }

    private fun switchViewMode(mode: ViewMode) {
        currentViewMode = mode
        destinationAdapter.setViewMode(mode)
        applyLayoutManager()
        // Fade-swap animation
        binding.rvDestinations.alpha = 0f
        binding.rvDestinations.animate().alpha(1f).setDuration(200)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString() ?: ""
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // debounce
                    applyFilters()
                }
            }
        })
    }

    private fun loadDestinations() {
        lifecycleScope.launch {
            allDestinations = DestinationRepository(requireContext()).getAllDestinations()

            // Build category filter chips
            val categories = listOf("Semua") + allDestinations.map { it.category }.distinct().sorted()
            buildFilterChips(categories)

            applyFilters()
        }
    }

    private fun buildFilterChips(categories: List<String>) {
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
                    activeCategory = category
                    buildFilterChips(categories)
                    applyFilters()
                }
            }
            binding.llFilterChips.addView(chip)
        }
    }

    private fun applyFilters() {
        val q = searchQuery.trim().lowercase()
        val filtered = allDestinations.filter { dest ->
            val matchCategory = activeCategory == "Semua" || dest.category == activeCategory
            val matchSearch = q.isEmpty() || dest.name.lowercase().contains(q)
                || dest.location.lowercase().contains(q)
                || dest.description.lowercase().contains(q)
            matchCategory && matchSearch
        }

        destinationAdapter.submitList(filtered)

        val isEmpty = filtered.isEmpty()
        binding.rvDestinations.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE

        binding.btnResetFilters.setOnClickListener {
            HapticUtil.click(it)
            activeCategory = "Semua"
            searchQuery = ""
            binding.etSearch.setText("")
            buildFilterChips(listOf("Semua") + allDestinations.map { it.category }.distinct().sorted())
            applyFilters()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
