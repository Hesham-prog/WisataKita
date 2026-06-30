package com.wisatakita.app.data

import org.json.JSONArray
import org.json.JSONObject

object DestinationJsonMapper {
    fun parseRoot(json: String): List<Destination> {
        val root = JSONObject(json)
        return parseArray(root.getJSONArray("data"))
    }

    fun parseArray(arr: JSONArray): List<Destination> {
        val result = mutableListOf<Destination>()
        for (i in 0 until arr.length()) {
            result.add(parseObject(arr.getJSONObject(i)))
        }
        return result
    }

    fun parseObject(obj: JSONObject): Destination {
        val funFactsArr = obj.optJSONArray("funFacts") ?: JSONArray()
        val galleryArr = obj.optJSONArray("galleryImages") ?: JSONArray()
        val reviewsArr = obj.optJSONArray("reviews") ?: JSONArray()
        return Destination(
            id = obj.getString("id"),
            name = obj.getString("name"),
            location = obj.getString("location"),
            address = obj.getString("address"),
            description = obj.getString("description"),
            imageUrl = obj.getString("imageUrl"),
            geoUri = obj.getString("geoUri"),
            ticketUrl = obj.optString("ticketUrl", ""),
            funFacts = funFactsArr.toStringList(),
            galleryImages = galleryArr.toStringList(),
            category = obj.optString("category", "Wisata"),
            latitude = obj.optDouble("latitude", 0.0),
            longitude = obj.optDouble("longitude", 0.0),
            imageQuery = obj.optString("imageQuery", obj.getString("name")),
            ticketPrice = obj.optString("ticketPrice", "Menyesuaikan kebijakan lokasi"),
            openingHours = obj.optString("openingHours", "Lihat info resmi lokasi"),
            rating = obj.optDouble("rating", 4.5),
            reviewCount = obj.optInt("reviewCount", 0),
            reviews = reviewsArr.toStringList(),
            promoTitle = obj.optString("promoTitle", ""),
            promoDescription = obj.optString("promoDescription", ""),
            transportInfo = obj.optString("transportInfo", ""),
            emergencyContact = obj.optString("emergencyContact", "")
        )
    }

    fun toJson(destination: Destination): String = toJsonObject(destination).toString()

    fun toRootJson(destinations: List<Destination>): String {
        val arr = JSONArray()
        destinations.forEach { arr.put(toJsonObject(it)) }
        return JSONObject().put("data", arr).toString()
    }

    private fun toJsonObject(destination: Destination): JSONObject {
        return JSONObject()
            .put("id", destination.id)
            .put("name", destination.name)
            .put("location", destination.location)
            .put("address", destination.address)
            .put("description", destination.description)
            .put("funFacts", destination.funFacts.toJsonArray())
            .put("imageUrl", destination.imageUrl)
            .put("geoUri", destination.geoUri)
            .put("galleryImages", destination.galleryImages.toJsonArray())
            .put("ticketUrl", destination.ticketUrl)
            .put("category", destination.category)
            .put("latitude", destination.latitude)
            .put("longitude", destination.longitude)
            .put("imageQuery", destination.imageQuery)
            .put("ticketPrice", destination.ticketPrice)
            .put("openingHours", destination.openingHours)
            .put("rating", destination.rating)
            .put("reviewCount", destination.reviewCount)
            .put("reviews", destination.reviews.toJsonArray())
            .put("promoTitle", destination.promoTitle)
            .put("promoDescription", destination.promoDescription)
            .put("transportInfo", destination.transportInfo)
            .put("emergencyContact", destination.emergencyContact)
    }

    private fun JSONArray.toStringList(): List<String> {
        return (0 until length()).map { getString(it) }
    }

    private fun List<String>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        forEach { arr.put(it) }
        return arr
    }
}
