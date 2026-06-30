package com.wisatakita.app.data.remote

import com.wisatakita.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class PexelsService {
    suspend fun searchPhotos(query: String, count: Int = 4): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.PEXELS_API_KEY
        if (apiKey.isBlank()) return@withContext emptyList()

        runCatching {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.pexels.com/v1/search?query=$encodedQuery&per_page=$count&orientation=landscape"
            val response = ApiHttpClient.get(url, mapOf("Authorization" to apiKey))
            val photos = JSONObject(response).getJSONArray("photos")
            (0 until photos.length()).mapNotNull { index ->
                val src = photos.getJSONObject(index).optJSONObject("src")
                src?.optString("landscape")?.takeIf { it.isNotBlank() }
                    ?: src?.optString("large")?.takeIf { it.isNotBlank() }
            }
        }.getOrElse { emptyList() }
    }
}
