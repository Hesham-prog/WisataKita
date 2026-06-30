package com.wisatakita.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wisatakita.app.data.TravelLocalRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    data class ProfileStats(
        val visitedCount: Int = 0,
        val reviewCount: Int = 0,
        val categoryCounts: Map<String, Int> = emptyMap()
    )

    private val repository = TravelLocalRepository(application)
    private val _stats = MutableLiveData(ProfileStats())
    val stats: LiveData<ProfileStats> = _stats

    fun refresh() {
        viewModelScope.launch {
            _stats.value = ProfileStats(
                visitedCount = repository.getVisitedCount(),
                reviewCount = repository.getReviewCount(),
                categoryCounts = repository.getCategoryDistribution()
            )
        }
    }
}
