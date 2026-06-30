package com.wisatakita.app.data

data class DestinationValidationIssue(
    val destinationId: String,
    val field: String,
    val message: String
)

object DestinationValidator {
    fun validate(destinations: List<Destination>): List<DestinationValidationIssue> {
        val issues = mutableListOf<DestinationValidationIssue>()
        val duplicateIds = destinations.groupBy { it.id }.filterValues { it.size > 1 }.keys

        destinations.forEach { destination ->
            fun issue(field: String, message: String) {
                issues.add(DestinationValidationIssue(destination.id, field, message))
            }

            if (destination.id.isBlank()) issue("id", "ID wajib diisi.")
            if (destination.id in duplicateIds) issue("id", "ID duplikat.")
            if (destination.name.isBlank()) issue("name", "Nama destinasi wajib diisi.")
            if (destination.location.isBlank()) issue("location", "Lokasi wajib diisi.")
            if (destination.address.isBlank()) issue("address", "Alamat wajib diisi.")
            if (destination.description.length < 80) issue("description", "Deskripsi terlalu pendek untuk halaman detail.")
            if (destination.category.isBlank() || destination.category == "Wisata") issue("category", "Kategori harus spesifik.")
            if (destination.latitude !in -11.0..6.5) issue("latitude", "Latitude terlihat di luar area Indonesia.")
            if (destination.longitude !in 95.0..141.5) issue("longitude", "Longitude terlihat di luar area Indonesia.")
            if (!destination.imageUrl.startsWith("https://")) issue("imageUrl", "Foto utama harus memakai HTTPS.")
            if (destination.galleryImages.size < 2) issue("galleryImages", "Minimal dua foto galeri untuk tiap destinasi.")
            if (destination.rating !in 0.0..5.0) issue("rating", "Rating harus 0 sampai 5.")
            if (destination.reviewCount < 0) issue("reviewCount", "Jumlah review tidak boleh minus.")
            if (destination.ticketPrice.isBlank()) issue("ticketPrice", "Info tiket wajib diisi.")
            if (destination.openingHours.isBlank()) issue("openingHours", "Jam buka wajib diisi.")
            if (destination.transportInfo.isBlank()) issue("transportInfo", "Info transportasi wajib diisi.")
            if (destination.emergencyContact.isBlank()) issue("emergencyContact", "Kontak/info penting wajib diisi.")
        }

        return issues
    }

    fun isDemoReady(destinations: List<Destination>): Boolean = validate(destinations).isEmpty()
}
