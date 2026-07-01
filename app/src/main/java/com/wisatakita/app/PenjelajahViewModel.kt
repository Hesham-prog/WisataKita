package com.wisatakita.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationLocalizer
import com.wisatakita.app.data.DestinationRepository
import kotlinx.coroutines.launch

import kotlin.math.*

enum class SortOption { AZ, ZA, NEAREST, FARTHEST, HIGHEST_RATED, LOWEST_RATED, MOST_RATINGS, LEAST_RATINGS }

data class PenjelajahUiState(
    val destinations: List<Destination> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val viewMode: PenjelajahFragment.ViewMode = PenjelajahFragment.ViewMode.LIST,
    val sortOption: SortOption = SortOption.AZ
)

class PenjelajahViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DestinationRepository(application)
    private val allDestinations = mutableListOf<Destination>()
    private val _uiState = MutableLiveData(PenjelajahUiState())
    val uiState: LiveData<PenjelajahUiState> = _uiState
    private var userLat: Double? = null
    private var userLon: Double? = null

    fun loadDestinations() {
        if (allDestinations.isNotEmpty()) return
        viewModelScope.launch {
            val destinations = repository.getAllDestinations()
            allDestinations.clear()
            allDestinations.addAll(destinations)
            publishState(
                category = _uiState.value?.selectedCategory?.ifBlank { allLabel() } ?: allLabel(),
                query = _uiState.value?.searchQuery.orEmpty(),
                viewMode = _uiState.value?.viewMode ?: PenjelajahFragment.ViewMode.LIST,
                sortOption = _uiState.value?.sortOption ?: SortOption.AZ
            )
        }
    }

    fun setUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        val current = _uiState.value ?: return
        publishState(current.selectedCategory, current.searchQuery, current.viewMode, current.sortOption)
    }

    fun setCategory(category: String) {
        val current = _uiState.value ?: return
        publishState(category, current.searchQuery, current.viewMode, current.sortOption)
    }

    fun setSearchQuery(query: String) {
        val current = _uiState.value ?: return
        publishState(current.selectedCategory, query, current.viewMode, current.sortOption)
    }

    fun setViewMode(viewMode: PenjelajahFragment.ViewMode) {
        val current = _uiState.value ?: return
        publishState(current.selectedCategory, current.searchQuery, viewMode, current.sortOption)
    }
    
    fun setSortOption(sortOption: SortOption) {
        val current = _uiState.value ?: return
        publishState(current.selectedCategory, current.searchQuery, current.viewMode, sortOption)
    }

    fun resetFilters() {
        val current = _uiState.value ?: return
        publishState(allLabel(), "", current.viewMode, SortOption.AZ)
    }

    private fun publishState(
        category: String,
        query: String,
        viewMode: PenjelajahFragment.ViewMode,
        sortOption: SortOption
    ) {
        val cleanedQuery = query.trim().lowercase()
        val filtered = allDestinations.filter { destination ->
            val categoryMatch = category == allLabel() || destination.category == category
            val searchMatch = cleanedQuery.isEmpty() ||
                destination.name.lowercase().contains(cleanedQuery) ||
                destination.location.lowercase().contains(cleanedQuery) ||
                destination.description.lowercase().contains(cleanedQuery) ||
                destination.category.lowercase().contains(cleanedQuery) ||
                destination.funFacts.any { it.lowercase().contains(cleanedQuery) }
            categoryMatch && searchMatch
        }

        val sorted = when (sortOption) {
            SortOption.AZ -> filtered.sortedBy { it.name }
            SortOption.ZA -> filtered.sortedByDescending { it.name }
            SortOption.HIGHEST_RATED -> filtered.sortedByDescending { it.rating }
            SortOption.LOWEST_RATED -> filtered.sortedBy { it.rating }
            SortOption.MOST_RATINGS -> filtered.sortedByDescending { it.reviewCount }
            SortOption.LEAST_RATINGS -> filtered.sortedBy { it.reviewCount }
            SortOption.NEAREST, SortOption.FARTHEST -> {
                if (userLat != null && userLon != null) {
                    val distSorted = filtered.sortedBy { haversine(userLat!!, userLon!!, it.latitude, it.longitude) }
                    if (sortOption == SortOption.NEAREST) distSorted else distSorted.reversed()
                } else {
                    filtered.sortedBy { it.name }
                }
            }
        }

        _uiState.value = PenjelajahUiState(
            destinations = sorted,
            categories = listOf(allLabel()) + allDestinations.map { it.category }.distinct().sorted(),
            selectedCategory = category,
            searchQuery = query,
            viewMode = viewMode,
            sortOption = sortOption
        )
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun allLabel(): String =
        DestinationLocalizer.allCategoryLabel(getApplication())
}
