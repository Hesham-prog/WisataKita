package com.wisatakita.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import kotlinx.coroutines.launch

data class PenjelajahUiState(
    val destinations: List<Destination> = emptyList(),
    val categories: List<String> = listOf("Semua"),
    val selectedCategory: String = "Semua",
    val searchQuery: String = "",
    val viewMode: PenjelajahFragment.ViewMode = PenjelajahFragment.ViewMode.LIST
)

class PenjelajahViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DestinationRepository(application)
    private val allDestinations = mutableListOf<Destination>()
    private val _uiState = MutableLiveData(PenjelajahUiState())
    val uiState: LiveData<PenjelajahUiState> = _uiState

    fun loadDestinations() {
        if (allDestinations.isNotEmpty()) return
        viewModelScope.launch {
            val destinations = repository.getAllDestinations()
            allDestinations.clear()
            allDestinations.addAll(destinations)
            publishState(
                category = _uiState.value?.selectedCategory ?: "Semua",
                query = _uiState.value?.searchQuery.orEmpty(),
                viewMode = _uiState.value?.viewMode ?: PenjelajahFragment.ViewMode.LIST
            )
        }
    }

    fun setCategory(category: String) {
        publishState(
            category = category,
            query = _uiState.value?.searchQuery.orEmpty(),
            viewMode = _uiState.value?.viewMode ?: PenjelajahFragment.ViewMode.LIST
        )
    }

    fun setSearchQuery(query: String) {
        publishState(
            category = _uiState.value?.selectedCategory ?: "Semua",
            query = query,
            viewMode = _uiState.value?.viewMode ?: PenjelajahFragment.ViewMode.LIST
        )
    }

    fun setViewMode(viewMode: PenjelajahFragment.ViewMode) {
        publishState(
            category = _uiState.value?.selectedCategory ?: "Semua",
            query = _uiState.value?.searchQuery.orEmpty(),
            viewMode = viewMode
        )
    }

    fun resetFilters() {
        publishState(
            category = "Semua",
            query = "",
            viewMode = _uiState.value?.viewMode ?: PenjelajahFragment.ViewMode.LIST
        )
    }

    private fun publishState(
        category: String,
        query: String,
        viewMode: PenjelajahFragment.ViewMode
    ) {
        val cleanedQuery = query.trim().lowercase()
        val filtered = allDestinations.filter { destination ->
            val categoryMatch = category == "Semua" || destination.category == category
            val searchMatch = cleanedQuery.isEmpty() ||
                destination.name.lowercase().contains(cleanedQuery) ||
                destination.location.lowercase().contains(cleanedQuery) ||
                destination.description.lowercase().contains(cleanedQuery)
            categoryMatch && searchMatch
        }

        _uiState.value = PenjelajahUiState(
            destinations = filtered,
            categories = listOf("Semua") + allDestinations.map { it.category }.distinct().sorted(),
            selectedCategory = category,
            searchQuery = query,
            viewMode = viewMode
        )
    }
}
