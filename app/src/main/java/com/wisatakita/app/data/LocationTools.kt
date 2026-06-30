package com.wisatakita.app.data

import android.location.Location

data class DestinationDistance(
    val destination: Destination,
    val distanceKm: Double,
    val estimatedDriveMinutes: Int
)

object LocationTools {
    fun hasValidCoordinates(destination: Destination): Boolean {
        return destination.latitude != 0.0 &&
            destination.longitude != 0.0 &&
            destination.latitude in -90.0..90.0 &&
            destination.longitude in -180.0..180.0
    }

    fun distanceKm(fromLatitude: Double, fromLongitude: Double, destination: Destination): Double? {
        if (!hasValidCoordinates(destination)) return null
        val results = FloatArray(1)
        Location.distanceBetween(
            fromLatitude,
            fromLongitude,
            destination.latitude,
            destination.longitude,
            results
        )
        return results[0] / 1000.0
    }

    fun sortByNearest(
        destinations: List<Destination>,
        fromLatitude: Double,
        fromLongitude: Double
    ): List<DestinationDistance> {
        return destinations.mapNotNull { destination ->
            val distance = distanceKm(fromLatitude, fromLongitude, destination) ?: return@mapNotNull null
            DestinationDistance(
                destination = destination,
                distanceKm = distance,
                estimatedDriveMinutes = estimateDriveMinutes(distance)
            )
        }.sortedBy { it.distanceKm }
    }

    fun estimateDriveMinutes(distanceKm: Double, averageSpeedKmh: Double = 35.0): Int {
        if (distanceKm <= 0.0 || averageSpeedKmh <= 0.0) return 0
        return ((distanceKm / averageSpeedKmh) * 60).toInt().coerceAtLeast(1)
    }
}
