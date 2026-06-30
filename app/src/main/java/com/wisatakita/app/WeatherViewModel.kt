package com.wisatakita.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wisatakita.app.data.remote.WeatherInfo
import com.wisatakita.app.data.remote.WeatherService
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    enum class Mood { SUNNY, RAIN, NEUTRAL }

    data class WeatherState(
        val loading: Boolean = true,
        val info: WeatherInfo? = null,
        val mood: Mood = Mood.NEUTRAL
    )

    private val _weatherState = MutableLiveData(WeatherState())
    val weatherState: LiveData<WeatherState> = _weatherState

    fun load(latitude: Double, longitude: Double) {
        _weatherState.value = WeatherState(loading = true)
        viewModelScope.launch {
            val weather = WeatherService().getCurrentWeather(latitude, longitude)
            _weatherState.value = WeatherState(
                loading = false,
                info = weather,
                mood = weather.toMood()
            )
        }
    }

    private fun WeatherInfo?.toMood(): Mood {
        val text = this?.description?.lowercase().orEmpty()
        return when {
            text.contains("hujan") || text.contains("rain") || text.contains("gerimis") -> Mood.RAIN
            text.contains("cerah") || text.contains("clear") || text.contains("sun") -> Mood.SUNNY
            else -> Mood.NEUTRAL
        }
    }
}
