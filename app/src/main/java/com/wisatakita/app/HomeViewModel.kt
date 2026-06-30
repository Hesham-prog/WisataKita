package com.wisatakita.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.DestinationRepository
import kotlinx.coroutines.launch

data class HomeUiState(
    val featured: List<Destination> = emptyList(),
    val nearby: List<Destination> = emptyList(),
    val categories: List<String> = listOf("Semua"),
    val selectedCategory: String = "Semua"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DestinationRepository(application)
    private val allDestinations = mutableListOf<Destination>()
    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    fun loadDestinations() {
        if (allDestinations.isNotEmpty()) return
        viewModelScope.launch {
            val destinations = repository.getAllDestinations()
            allDestinations.clear()
            allDestinations.addAll(destinations)

            _uiState.value = HomeUiState(
                featured = destinations.sortedByDescending { it.rating }.take(8),
                nearby = destinations.shuffled().take(5),
                categories = listOf("Semua") + destinations.map { it.category }.distinct().sorted()
            )
        }
    }

    fun selectCategory(category: String) {
        val destinations = if (category == "Semua") {
            allDestinations
        } else {
            allDestinations.filter { it.category == category }
        }

        _uiState.value = _uiState.value?.copy(
            selectedCategory = category,
            nearby = destinations.take(6)
        )
    }
}
