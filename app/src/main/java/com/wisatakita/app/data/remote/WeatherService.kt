package com.wisatakita.app.data.remote

import com.wisatakita.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class WeatherInfo(
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double
) {
    fun asDisplayText(): String {
        return if (java.util.Locale.getDefault().language == "en") {
            "${temperature.toInt()} C - $description\nHumidity $humidity% - Wind ${"%.1f".format(windSpeed)} m/s"
        } else {
            "${temperature.toInt()} C - $description\nKelembapan $humidity% - Angin ${"%.1f".format(windSpeed)} m/s"
        }
    }
}

class WeatherService {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherInfo? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENWEATHER_API_KEY
        if (apiKey.isBlank() || latitude == 0.0 || longitude == 0.0) return@withContext null

        runCatching {
            val lang = if (java.util.Locale.getDefault().language == "en") "en" else "id"
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric&lang=$lang"
            val root = JSONObject(ApiHttpClient.get(url))
            val main = root.getJSONObject("main")
            val weather = root.getJSONArray("weather").getJSONObject(0)
            val wind = root.optJSONObject("wind")
            WeatherInfo(
                temperature = main.optDouble("temp", 0.0),
                description = weather.optString("description", "Cuaca tidak tersedia"),
                humidity = main.optInt("humidity", 0),
                windSpeed = wind?.optDouble("speed", 0.0) ?: 0.0
            )
        }.getOrNull()
    }
}
