package com.wisatakita.app.data

import android.net.Uri

data class DestinationMapMarker(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val category: String
)

object MapDestinationHelper {
    fun markers(destinations: List<Destination>): List<DestinationMapMarker> {
        return destinations.filter(LocationTools::hasValidCoordinates).map {
            DestinationMapMarker(
                id = it.id,
                title = it.name,
                latitude = it.latitude,
                longitude = it.longitude,
                category = it.category
            )
        }
    }

    fun mapUri(destination: Destination): Uri {
        val query = Uri.encode(destination.name)
        return Uri.parse("geo:${destination.latitude},${destination.longitude}?q=${destination.latitude},${destination.longitude}($query)")
    }

    fun directionsUri(destination: Destination, fromLatitude: Double? = null, fromLongitude: Double? = null): Uri {
        val destinationParam = "${destination.latitude},${destination.longitude}"
        val originParam = if (fromLatitude != null && fromLongitude != null) {
            "&origin=$fromLatitude,$fromLongitude"
        } else {
            ""
        }
        return Uri.parse("https://www.google.com/maps/dir/?api=1$originParam&destination=$destinationParam&travelmode=driving")
    }
}
