package com.wisatakita.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DestinationRepository(private val context: Context) {

    suspend fun getDestinations(): List<Destination> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open("destinations.json")
                .bufferedReader().use { it.readText() }
            parseDestinations(json)
        } catch (e: Exception) {
            DestinationData.list // Fallback ke data lokal jika terjadi error
        }
    }

    private fun parseDestinations(json: String): List<Destination> {
        val root = JSONObject(json)
        val arr = root.getJSONArray("data")
        val result = mutableListOf<Destination>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val funFactsArr = obj.getJSONArray("funFacts")
            val galleryArr = obj.getJSONArray("galleryImages")
            result.add(
                Destination(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    location = obj.getString("location"),
                    address = obj.getString("address"),
                    description = obj.getString("description"),
                    imageUrl = obj.getString("imageUrl"),
                    geoUri = obj.getString("geoUri"),
                    ticketUrl = obj.getString("ticketUrl"),
                    funFacts = (0 until funFactsArr.length()).map { funFactsArr.getString(it) },
                    galleryImages = (0 until galleryArr.length()).map { galleryArr.getString(it) }
                )
            )
        }
        return result
    }
}
