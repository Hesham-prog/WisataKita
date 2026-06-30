package com.wisatakita.app.data.remote

import com.wisatakita.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class NearbyPlace(
    val name: String,
    val address: String
)

class GeoapifyService {
    suspend fun getNearbyPlaces(latitude: Double, longitude: Double): List<NearbyPlace> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEOAPIFY_API_KEY
        if (apiKey.isBlank() || latitude == 0.0 || longitude == 0.0) return@withContext emptyList()

        runCatching {
            val categories = "tourism.sights,tourism.attraction,entertainment,catering.restaurant"
            val url = "https://api.geoapify.com/v2/places?categories=$categories&filter=circle:$longitude,$latitude,3500&limit=5&apiKey=$apiKey"
            val features = JSONObject(ApiHttpClient.get(url)).getJSONArray("features")
            (0 until features.length()).mapNotNull { index ->
                val properties = features.getJSONObject(index).optJSONObject("properties") ?: return@mapNotNull null
                val name = properties.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val address = properties.optString("address_line2")
                    .takeIf { it.isNotBlank() }
                    ?: properties.optString("formatted")
                NearbyPlace(name, address)
            }
        }.getOrElse { emptyList() }
    }
}
