package com.wisatakita.app.data

data class Destination(
    val id: String,
    val name: String,
    val location: String,
    val address: String,
    val description: String,
    val funFacts: List<String>,
    val imageUrl: String,
    val geoUri: String,
    val galleryImages: List<String> = emptyList(),
    val ticketUrl: String = "",
    val category: String = "Wisata",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageQuery: String = name,
    val ticketPrice: String = "Menyesuaikan kebijakan lokasi",
    val openingHours: String = "Lihat info resmi lokasi",
    val rating: Double = 4.5,
    val reviewCount: Int = 0,
    val reviews: List<String> = emptyList(),
    val promoTitle: String = "",
    val promoDescription: String = "",
    val transportInfo: String = "",
    val emergencyContact: String = ""
)
