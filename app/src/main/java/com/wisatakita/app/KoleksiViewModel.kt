package com.wisatakita.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wisatakita.app.data.Album
import com.wisatakita.app.data.Destination
import com.wisatakita.app.data.TravelLocalRepository
import com.wisatakita.app.data.db.JourneyStampEntity
import kotlinx.coroutines.launch

class KoleksiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TravelLocalRepository(application)

    private val _favorites = MutableLiveData<List<Destination>>(emptyList())
    val favorites: LiveData<List<Destination>> = _favorites

    private val _stamps = MutableLiveData<List<JourneyStampEntity>>(emptyList())
    val stamps: LiveData<List<JourneyStampEntity>> = _stamps

    private val _albums = MutableLiveData<List<Album>>(emptyList())
    val albums: LiveData<List<Album>> = _albums

    fun refresh() {
        viewModelScope.launch {
            _favorites.value = repository.getFavoriteDestinations()
            _stamps.value = repository.getJourneyStamps()
            _albums.value = repository.getAlbums()
        }
    }
}
