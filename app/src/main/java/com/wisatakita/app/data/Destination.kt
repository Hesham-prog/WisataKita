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
    val ticketUrl: String = ""
)
