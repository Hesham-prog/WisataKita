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

data class HomeUiState(
    val featured: List<Destination> = emptyList(),
    val nearby: List<Destination> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DestinationRepository(application)
    private val allDestinations = mutableListOf<Destination>()
    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState
    
    private var userLat: Double? = null
    private var userLon: Double? = null

    fun loadDestinations() {
        if (allDestinations.isNotEmpty()) return
        viewModelScope.launch {
            val destinations = repository.getAllDestinations()
            allDestinations.clear()
            allDestinations.addAll(destinations)

            _uiState.value = HomeUiState(
                featured = destinations.sortedByDescending { it.rating }.take(8),
                nearby = getSortedNearby(destinations),
                categories = listOf(allLabel()) + destinations.map { it.category }.distinct().sorted(),
                selectedCategory = allLabel()
            )
        }
    }

    fun setUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        if (allDestinations.isNotEmpty()) {
            val currentCategory = _uiState.value?.selectedCategory ?: allLabel()
            selectCategory(currentCategory) // Re-sort and update
        }
    }

    private fun getSortedNearby(sourceList: List<Destination>): List<Destination> {
        val lat = userLat
        val lon = userLon
        return if (lat != null && lon != null) {
            sourceList.sortedBy { dest ->
                haversine(lat, lon, dest.latitude, dest.longitude)
            }.take(5)
        } else {
            sourceList.shuffled().take(5)
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun selectCategory(category: String) {
        val destinations = if (category == allLabel()) {
            allDestinations
        } else {
            allDestinations.filter { it.category == category }
        }

        _uiState.value = _uiState.value?.copy(
            selectedCategory = category,
            nearby = getSortedNearby(destinations)
        )
    }

    private fun allLabel(): String =
        DestinationLocalizer.allCategoryLabel(getApplication())
}
