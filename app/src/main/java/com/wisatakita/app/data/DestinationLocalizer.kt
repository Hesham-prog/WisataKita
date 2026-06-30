package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.LanguageUtil

object DestinationLocalizer {
    private data class TextOverride(
        val name: String? = null,
        val location: String? = null,
        val category: String? = null,
        val ticketPrice: String? = null,
        val description: String? = null,
        val funFacts: List<String>? = null,
        val reviews: List<String>? = null,
        val promoTitle: String? = null,
        val promoDescription: String? = null,
        val transportInfo: String? = null,
        val emergencyContact: String? = null
    )

    fun localize(context: Context, destinations: List<Destination>): List<Destination> {
        if (LanguageUtil.currentLanguage(context) != LanguageUtil.ENGLISH) return destinations
        return destinations.map { destination ->
            val override = englishOverrides[destination.id]
            destination.copy(
                name = override?.name ?: translateName(destination.name),
                location = override?.location ?: translateLocation(destination.location),
                address = translateLocation(destination.address),
                category = override?.category ?: translateCategory(destination.category),
                ticketPrice = override?.ticketPrice ?: translateTicket(destination.ticketPrice),
                description = override?.description ?: defaultDescription(destination),
                funFacts = override?.funFacts ?: defaultFunFacts(destination),
                reviews = override?.reviews ?: defaultReviews(destination),
                promoTitle = override?.promoTitle ?: translatePromoTitle(destination.promoTitle),
                promoDescription = override?.promoDescription ?: defaultPromo(destination),
                transportInfo = override?.transportInfo ?: defaultTransport(destination),
                emergencyContact = override?.emergencyContact ?: defaultEmergency(destination)
            )
        }
    }

    fun translateCategory(category: String): String = when (category) {
        "Sejarah" -> "History"
        "Kota dan Kuliner" -> "City & Culinary"
        "Kota dan Sejarah" -> "City & History"
        "Pantai" -> "Beach"
        "Budaya" -> "Culture"
        "Alam" -> "Nature"
        "Danau" -> "Lake"
        "Gunung" -> "Mountain"
        "Bukit dan Pantai" -> "Hills & Beach"
        "Laut" -> "Marine"
        "Pegunungan" -> "Highlands"
        "Keluarga" -> "Family"
        "Danau Kawah" -> "Crater Lake"
        "Sejarah dan Laut" -> "History & Marine"
        "Pantai dan Sejarah" -> "Beach & History"
        "Wisata" -> "Travel"
        else -> category
    }

    fun allCategoryLabel(context: Context): String =
        if (LanguageUtil.currentLanguage(context) == LanguageUtil.ENGLISH) "All" else "Semua"

    private val englishOverrides = mapOf(
        "borobudur" to TextOverride(
            name = "Borobudur Temple",
            location = "Magelang, Central Java",
            category = "History",
            ticketPrice = "From Rp50,000",
            description = "Borobudur is the world's largest Buddhist temple and one of Indonesia's most important heritage icons. Its stupas, relief panels, and hill-framed setting make it a landmark for culture, architecture, and sunrise photography.",
            funFacts = listOf("Borobudur has thousands of relief panels telling Buddhist stories.", "The temple was built with interlocking volcanic stone.", "UNESCO recognizes Borobudur as a World Heritage Site."),
            reviews = listOf("The sunrise view and relief details are beautiful.", "A great place for historical and educational travel."),
            promoTitle = "Sunrise Heritage Package",
            promoDescription = "Visit early in the morning for softer light and the best photo experience.",
            transportInfo = "About 1.5 hours from Yogyakarta by car.",
            emergencyContact = "Site information: borobudurpark.com"
        ),
        "prambanan" to TextOverride(
            name = "Prambanan Temple",
            location = "Sleman, Yogyakarta",
            category = "History",
            ticketPrice = "From Rp50,000",
            description = "Prambanan is Indonesia's largest Hindu temple complex. Its soaring towers and detailed Ramayana reliefs make it one of Yogyakarta's most iconic architectural landmarks.",
            funFacts = listOf("The main temples are dedicated to the Trimurti.", "The complex often hosts Ramayana Ballet performances.", "Its reliefs tell scenes from the Ramayana epic."),
            reviews = listOf("The complex is spacious and pleasant for an afternoon walk.", "The Ramayana performance is highly recommended."),
            promoTitle = "Ramayana Night",
            promoDescription = "Check the evening performance schedule for a memorable cultural visit.",
            transportInfo = "Close to Adisutjipto Airport and the main Yogyakarta-Solo route.",
            emergencyContact = "Site information: prambananpark.com"
        ),
        "malioboro" to TextOverride(category = "City & Culinary", ticketPrice = "Free", description = "Malioboro is Yogyakarta's classic city corridor, known for street food, shopping, heritage buildings, and a lively evening atmosphere.", promoTitle = "Evening Street Walk", promoDescription = "Visit after sunset for food stalls, lights, and a stronger local atmosphere."),
        "kuta" to TextOverride(name = "Kuta Beach Bali", category = "Beach", ticketPrice = "Free", description = "Kuta Beach is one of Bali's most famous beaches, known for long sandy shores, beginner-friendly surf, and sunset views.", promoTitle = "Sunset Surf Session", promoDescription = "Come in the late afternoon for surf lessons and sunset photos."),
        "uluwatu" to TextOverride(name = "Uluwatu Temple", category = "Culture", ticketPrice = "From Rp30,000", description = "Uluwatu Temple sits dramatically on a limestone cliff above the Indian Ocean. It is best known for sunset views, Balinese architecture, and the Kecak dance performance.", promoTitle = "Kecak Sunset", promoDescription = "Book early for the cliffside Kecak performance at sunset."),
        "ubud_monkey_forest" to TextOverride(category = "Nature", ticketPrice = "From Rp80,000", description = "Ubud Monkey Forest is a sacred forest sanctuary with ancient temples, dense tropical greenery, and long-tailed macaques.", promoTitle = "Sacred Forest Walk", promoDescription = "Follow the marked route and keep belongings secure around the monkeys."),
        "toba" to TextOverride(name = "Lake Toba", location = "North Sumatra", category = "Lake", ticketPrice = "From Rp10,000", description = "Lake Toba is a vast volcanic lake with Samosir Island at its center, offering highland scenery and Batak cultural experiences.", promoTitle = "Samosir Escape", promoDescription = "Pair the lake visit with Batak villages and lakeside homestays."),
        "bukittinggi" to TextOverride(name = "Jam Gadang Bukittinggi", location = "Bukittinggi, West Sumatra", category = "City & History", ticketPrice = "Free", description = "Jam Gadang is Bukittinggi's historic clock tower and a central landmark surrounded by markets, food spots, and Minangkabau culture.", promoTitle = "Heritage City Walk", promoDescription = "Explore the clock tower area, local snacks, and nearby canyon viewpoints."),
        "bromo" to TextOverride(name = "Mount Bromo", location = "Probolinggo, East Java", category = "Mountain", ticketPrice = "From Rp29,000", description = "Mount Bromo is an active volcano famous for its sea of sand, smoking crater, and sunrise panorama over East Java's volcanic landscape.", promoTitle = "Sunrise Jeep Route", promoDescription = "Start before dawn for the classic viewpoint and crater route."),
        "ijen" to TextOverride(name = "Ijen Crater", location = "Banyuwangi, East Java", category = "Mountain", ticketPrice = "From Rp20,000", description = "Ijen Crater is known for its turquoise acidic lake, sulfur mining landscape, and rare blue fire phenomenon on guided night hikes.", promoTitle = "Blue Fire Trek", promoDescription = "Use a licensed guide and proper mask for the night route."),
        "baluran" to TextOverride(name = "Baluran National Park", location = "Situbondo, East Java", category = "Nature", ticketPrice = "From Rp16,500", description = "Baluran National Park is often called the 'Little Africa of Java' for its savanna, wildlife, mangroves, and coastal scenery.", promoTitle = "Savanna Safari", promoDescription = "Visit Bekol Savanna in the morning for better wildlife viewing."),
        "komodo" to TextOverride(name = "Komodo Island", category = "Nature", ticketPrice = "Depends on area package", description = "Komodo Island is part of Komodo National Park, home to the world's largest living lizard and dramatic dry island scenery.", promoTitle = "Ranger-Guided Trek", promoDescription = "Enter with official rangers and follow all safety instructions."),
        "padar" to TextOverride(name = "Padar Island", category = "Hills & Beach", ticketPrice = "Included in Komodo National Park packages", description = "Padar Island is famous for its short ridge hike and panoramic view of curved bays with different beach colors.", promoTitle = "Viewpoint Hike", promoDescription = "Hike early for cooler weather and cleaner light."),
        "rajaampat" to TextOverride(name = "Raja Ampat", location = "Southwest Papua", category = "Marine", ticketPrice = "Depends on travel package", description = "Raja Ampat is a world-class marine destination known for karst islands, coral reefs, clear water, and exceptional biodiversity.", promoTitle = "Island Hopping", promoDescription = "Choose a local boat route for viewpoints, snorkeling, and village stops."),
        "derawan" to TextOverride(name = "Derawan Islands", location = "Berau, East Kalimantan", category = "Marine", ticketPrice = "Depends on travel package", description = "The Derawan Islands offer turquoise water, reef life, turtle encounters, and access to nearby islands such as Kakaban and Maratua.", promoTitle = "Turtle Lagoon", promoDescription = "Plan a multi-island route for the best marine experience."),
        "bunaken" to TextOverride(name = "Bunaken Marine Park", location = "Manado, North Sulawesi", category = "Marine", ticketPrice = "From Rp5,000", description = "Bunaken Marine Park is known for wall dives, coral gardens, and rich marine life close to Manado.", promoTitle = "Reef Day Trip", promoDescription = "Book snorkeling or diving with a certified local operator."),
        "wakatobi" to TextOverride(category = "Marine", ticketPrice = "Depends on travel package", description = "Wakatobi is a remote marine paradise in Southeast Sulawesi with pristine reefs, quiet islands, and excellent diving conditions.", promoTitle = "Remote Reef Escape", promoDescription = "Stay several days to make the inter-island travel worth it."),
        "dieng" to TextOverride(name = "Dieng Plateau", location = "Wonosobo, Central Java", category = "Highlands", ticketPrice = "From Rp15,000", description = "Dieng Plateau combines cool highland air, volcanic lakes, ancient temples, and sunrise viewpoints above the clouds.", promoTitle = "Golden Sunrise", promoDescription = "Arrive early for Sikunir sunrise and continue to the crater and lake circuit."),
        "karimunjawa" to TextOverride(category = "Marine", ticketPrice = "Depends on ferry and tour package", description = "Karimunjawa is a quiet island cluster north of Java with clear water, beaches, snorkeling spots, and relaxed village life.", promoTitle = "Island Snorkeling", promoDescription = "Check ferry schedules before planning the trip."),
        "ancol" to TextOverride(name = "Ancol Dreamland", location = "North Jakarta", category = "Family", ticketPrice = "From Rp30,000", description = "Ancol Dreamland is Jakarta's seaside recreation area with beaches, parks, attractions, and family entertainment.", promoTitle = "Family Day Pass", promoDescription = "Combine beach time with theme park or aquarium visits."),
        "kota_tua" to TextOverride(name = "Jakarta Old Town", location = "West Jakarta", category = "History", ticketPrice = "Free public area", description = "Jakarta Old Town preserves colonial-era buildings, museums, public squares, street performers, and historic city atmosphere.", promoTitle = "Museum Walk", promoDescription = "Start at Fatahillah Square and continue through the surrounding museums."),
        "tmii" to TextOverride(name = "Beautiful Indonesia Miniature Park", location = "East Jakarta", category = "Culture", ticketPrice = "From Rp25,000", description = "TMII presents Indonesian culture through regional pavilions, museums, gardens, and family-friendly attractions.", promoTitle = "Culture Circuit", promoDescription = "Use the park transport to cover more pavilions comfortably."),
        "tangkuban_perahu" to TextOverride(name = "Tangkuban Perahu", location = "West Bandung, West Java", category = "Mountain", ticketPrice = "From Rp20,000", description = "Tangkuban Perahu is a volcanic crater destination near Bandung with accessible viewpoints, cool mountain air, and local legends.", promoTitle = "Crater View Stop", promoDescription = "Bring a jacket and check volcanic activity updates before visiting."),
        "kawah_putih" to TextOverride(name = "Kawah Putih", location = "Ciwidey, West Java", category = "Crater Lake", ticketPrice = "From Rp28,000", description = "Kawah Putih is a pale volcanic lake surrounded by misty highland forest, famous for its surreal color and photo spots.", promoTitle = "Misty Lake Photo", promoDescription = "Visit on a clear morning for softer light and fewer crowds."),
        "pangandaran" to TextOverride(name = "Pangandaran Beach", location = "Pangandaran, West Java", category = "Beach", ticketPrice = "From Rp10,000", description = "Pangandaran Beach offers swimming areas, seafood, sunrise and sunset points, and access to a nearby nature reserve.", promoTitle = "Coastal Weekend", promoDescription = "Pair the beach with Green Canyon or the nature reserve."),
        "tana_toraja" to TextOverride(name = "Tana Toraja", location = "South Sulawesi", category = "Culture", ticketPrice = "Depends on attraction", description = "Tana Toraja is known for dramatic highland landscapes, traditional Tongkonan houses, and deeply rooted ceremonial culture.", promoTitle = "Highland Culture Route", promoDescription = "Travel with a local guide to understand cultural etiquette."),
        "lombok_mandalika" to TextOverride(name = "Mandalika Lombok", location = "Central Lombok, West Nusa Tenggara", category = "Beach", ticketPrice = "Free public area", description = "Mandalika combines beaches, coastal hills, resorts, and motorsport energy along Lombok's southern shoreline.", promoTitle = "South Coast Drive", promoDescription = "Explore nearby beaches such as Kuta Mandalika and Tanjung Aan."),
        "rinjani" to TextOverride(name = "Mount Rinjani", location = "Lombok, West Nusa Tenggara", category = "Mountain", ticketPrice = "Depends on trekking package", description = "Mount Rinjani is one of Indonesia's most impressive volcano treks, with crater rims, Segara Anak Lake, and challenging routes.", promoTitle = "Guided Summit Trek", promoDescription = "Use official trekking operators and prepare for changing mountain weather."),
        "belitung" to TextOverride(name = "Tanjung Tinggi Beach", location = "Belitung", category = "Beach", ticketPrice = "Free", description = "Tanjung Tinggi Beach is known for giant granite boulders, calm turquoise water, and soft white sand.", promoTitle = "Granite Beach Walk", promoDescription = "Visit during low tide for easier walking between the rocks."),
        "labuan_cermin" to TextOverride(name = "Labuan Cermin Lake", location = "Berau, East Kalimantan", category = "Lake", ticketPrice = "From Rp10,000", description = "Labuan Cermin is a clear two-layer lake where freshwater and saltwater create a mirror-like blue pool.", promoTitle = "Crystal Lake Swim", promoDescription = "Use a local boat and keep the water clean by avoiding soap or sunscreen residue."),
        "banda_neira" to TextOverride(name = "Banda Neira", location = "Central Maluku", category = "History & Marine", ticketPrice = "Depends on transport and guide", description = "Banda Neira blends spice trade history, colonial forts, volcano views, and beautiful snorkeling spots.", promoTitle = "Spice Island Walk", promoDescription = "Combine heritage sites with a short boat trip for snorkeling."),
        "morotai" to TextOverride(name = "Morotai Island", location = "North Maluku", category = "Beach & History", ticketPrice = "Depends on travel package", description = "Morotai Island offers quiet beaches, World War II history, clear water, and island-hopping routes.", promoTitle = "History and Island Hop", promoDescription = "Visit museum sites before heading to the small islands."),
        "sentani" to TextOverride(name = "Lake Sentani", location = "Jayapura, Papua", category = "Lake", ticketPrice = "Depends on attraction", description = "Lake Sentani is a large lake near Jayapura surrounded by hills, islands, and Papuan cultural villages.", promoTitle = "Lake Village Visit", promoDescription = "Take a local boat to explore lakeside villages and viewpoints."),
        "lorentz" to TextOverride(name = "Lorentz National Park", location = "Papua", category = "Nature", ticketPrice = "Requires permits and special packages", description = "Lorentz National Park is a vast UNESCO-listed wilderness spanning ecosystems from tropical coastline to snow-capped mountains.", promoTitle = "Conservation Expedition", promoDescription = "Requires serious permit planning, logistics, and local guides."),
        "gili_trawangan" to TextOverride(name = "Gili Trawangan", location = "North Lombok, West Nusa Tenggara", category = "Beach", ticketPrice = "Depends on transport and activities", description = "Gili Trawangan is a small car-free island known for beaches, cycling, snorkeling, diving, and lively sunset spots.", promoTitle = "Car-Free Island Stay", promoDescription = "Rent a bike and plan a sunset stop on the west side."),
        "lawang_sewu" to TextOverride(name = "Lawang Sewu", location = "Semarang, Central Java", category = "History", ticketPrice = "From Rp20,000", description = "Lawang Sewu is a landmark heritage building in Semarang, known for its many doors, colonial architecture, and railway history.", promoTitle = "Heritage Night Tour", promoDescription = "Check tour schedules for a guided historical walk.")
    )

    private fun translateName(name: String): String = name

    private fun translateLocation(text: String): String = text
        .replace("Jawa Tengah", "Central Java")
        .replace("Jawa Timur", "East Java")
        .replace("Jawa Barat", "West Java")
        .replace("Sumatera Utara", "North Sumatra")
        .replace("Sumatera Barat", "West Sumatra")
        .replace("Kalimantan Timur", "East Kalimantan")
        .replace("Sulawesi Utara", "North Sulawesi")
        .replace("Sulawesi Tenggara", "Southeast Sulawesi")
        .replace("Sulawesi Selatan", "South Sulawesi")
        .replace("Jakarta Utara", "North Jakarta")
        .replace("Jakarta Barat", "West Jakarta")
        .replace("Jakarta Timur", "East Jakarta")
        .replace("Maluku Tengah", "Central Maluku")
        .replace("Maluku Utara", "North Maluku")
        .replace("Papua Barat Daya", "Southwest Papua")

    private fun translateTicket(text: String): String = text
        .replace("Mulai", "From")
        .replace("Gratis area publik", "Free public area")
        .replace("Gratis", "Free")
        .replace("Menyesuaikan", "Depends on")
        .replace("Perlu izin dan paket khusus", "Requires permits and special packages")

    private fun translatePromoTitle(title: String): String =
        title.ifBlank { "Travel Highlight" }

    private fun defaultDescription(destination: Destination): String =
        "${destination.name} is a recommended Indonesian destination in ${translateLocation(destination.location)}. It is known for its ${translateCategory(destination.category).lowercase()} appeal, scenic setting, and memorable travel experience."

    private fun defaultFunFacts(destination: Destination): List<String> = listOf(
        "${destination.name} is one of the standout destinations in ${translateLocation(destination.location)}.",
        "The destination is popular for ${translateCategory(destination.category).lowercase()} travel.",
        "Plan your visit around local weather, access, and official visitor guidance."
    )

    private fun defaultReviews(destination: Destination): List<String> = listOf(
        "A memorable place with a strong local character.",
        "Worth visiting with enough time to explore the area properly."
    )

    private fun defaultPromo(destination: Destination): String =
        "Plan ahead for a smoother visit and better photo opportunities."

    private fun defaultTransport(destination: Destination): String =
        "Check local transport options and travel time before visiting."

    private fun defaultEmergency(destination: Destination): String =
        "Follow official site information and local staff instructions."
}
