package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.BuildConfig
import com.wisatakita.app.data.remote.ApiHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class DestinationLoadResult(
    val destinations: List<Destination>,
    val sourceLabel: String
)

class DestinationRepository(private val context: Context) {

    suspend fun getDestinations(): List<Destination> = getDestinationsWithSource().destinations

    suspend fun getDestinationsWithSource(): DestinationLoadResult = withContext(Dispatchers.IO) {
        val local = loadAssetDestinations()
        val online = runCatching {
            val json = ApiHttpClient.get(BuildConfig.DESTINATION_API_URL)
            parseDestinations(json)
        }.getOrNull()

        if (!online.isNullOrEmpty() && isCompleteWebServiceData(online, local)) {
            return@withContext DestinationLoadResult(online, "Data dari Web Service")
        }

        if (local.isNotEmpty()) {
            val label = if (online.isNullOrEmpty()) {
                "Mode offline: data lokal"
            } else {
                "Data lokal terbaru: Web Service belum sinkron"
            }
            return@withContext DestinationLoadResult(local, label)
        }

        DestinationLoadResult(DestinationData.list, "Mode offline: data bawaan")
    }

    suspend fun getDestinationById(id: String): Destination? {
        return getDestinations().find { it.id == id }
    }

    private fun loadAssetDestinations(): List<Destination> {
        try {
            val json = context.assets.open("destinations.json")
                .bufferedReader().use { it.readText() }
            return parseDestinations(json)
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun isCompleteWebServiceData(online: List<Destination>, local: List<Destination>): Boolean {
        val hasEnoughItems = local.isEmpty() || online.size >= local.size
        val hasCoordinates = online.all { it.latitude != 0.0 && it.longitude != 0.0 }
        val hasRichMetadata = online.all { it.reviewCount > 0 && it.category != "Wisata" }
        return hasEnoughItems && hasCoordinates && hasRichMetadata
    }

    private fun parseDestinations(json: String): List<Destination> {
        val root = JSONObject(json)
        val arr = root.getJSONArray("data")
        val result = mutableListOf<Destination>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val funFactsArr = obj.getJSONArray("funFacts")
            val galleryArr = obj.optJSONArray("galleryImages") ?: JSONArray()
            val reviewsArr = obj.optJSONArray("reviews") ?: JSONArray()
            result.add(
                Destination(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    location = obj.getString("location"),
                    address = obj.getString("address"),
                    description = obj.getString("description"),
                    imageUrl = obj.getString("imageUrl"),
                    geoUri = obj.getString("geoUri"),
                    ticketUrl = obj.optString("ticketUrl", ""),
                    funFacts = (0 until funFactsArr.length()).map { funFactsArr.getString(it) },
                    galleryImages = (0 until galleryArr.length()).map { galleryArr.getString(it) },
                    category = obj.optString("category", "Wisata"),
                    latitude = obj.optDouble("latitude", 0.0),
                    longitude = obj.optDouble("longitude", 0.0),
                    imageQuery = obj.optString("imageQuery", obj.getString("name")),
                    ticketPrice = obj.optString("ticketPrice", "Menyesuaikan kebijakan lokasi"),
                    openingHours = obj.optString("openingHours", "Lihat info resmi lokasi"),
                    rating = obj.optDouble("rating", 4.5),
                    reviewCount = obj.optInt("reviewCount", 0),
                    reviews = (0 until reviewsArr.length()).map { reviewsArr.getString(it) },
                    promoTitle = obj.optString("promoTitle", ""),
                    promoDescription = obj.optString("promoDescription", ""),
                    transportInfo = obj.optString("transportInfo", ""),
                    emergencyContact = obj.optString("emergencyContact", "")
                )
            )
        }
        return result
    }
}
