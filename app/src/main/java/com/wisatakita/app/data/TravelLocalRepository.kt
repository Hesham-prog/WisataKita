package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.data.db.AlbumEntity
import com.wisatakita.app.data.db.AppDatabase
import com.wisatakita.app.data.db.DestinationHistoryEntity
import com.wisatakita.app.data.db.DestinationReviewEntity
import com.wisatakita.app.data.db.FavoriteDestinationEntity
import com.wisatakita.app.data.db.JourneyStampEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserDestinationReview(
    val id: Long,
    val destinationId: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class RecentDestinationView(
    val destinationId: String,
    val name: String,
    val location: String,
    val imageUrl: String,
    val viewedAt: Long,
    val viewCount: Int
)

class TravelLocalRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(context)
    private val albums = db.albumDao()
    private val favorites = db.favoriteDestinationDao()
    private val history = db.destinationHistoryDao()
    private val reviews = db.destinationReviewDao()
    private val stamps = db.journeyStampDao()

    suspend fun setFavorite(destinationId: String, favorite: Boolean) = withContext(Dispatchers.IO) {
        if (favorite) {
            favorites.add(FavoriteDestinationEntity(destinationId, System.currentTimeMillis()))
        } else {
            favorites.remove(destinationId)
        }
    }

    suspend fun toggleFavorite(destinationId: String): Boolean = withContext(Dispatchers.IO) {
        val next = !favorites.isFavorite(destinationId)
        if (next) {
            favorites.add(FavoriteDestinationEntity(destinationId, System.currentTimeMillis()))
        } else {
            favorites.remove(destinationId)
        }
        next
    }

    suspend fun isFavorite(destinationId: String): Boolean = withContext(Dispatchers.IO) {
        favorites.isFavorite(destinationId)
    }

    suspend fun getFavoriteIds(): List<String> = withContext(Dispatchers.IO) {
        favorites.getFavoriteIds()
    }

    suspend fun recordDestinationView(destination: Destination) = withContext(Dispatchers.IO) {
        val existing = history.get(destination.id)
        history.upsert(
            DestinationHistoryEntity(
                destinationId = destination.id,
                name = destination.name,
                location = destination.location,
                imageUrl = destination.imageUrl,
                viewedAt = System.currentTimeMillis(),
                viewCount = (existing?.viewCount ?: 0) + 1
            )
        )
        stamps.insert(
            JourneyStampEntity(
                destinationId = destination.id,
                categoryType = destination.category,
                stampColor = stampColorFor(destination.category),
                unlockedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getRecentViews(limit: Int = 20): List<RecentDestinationView> = withContext(Dispatchers.IO) {
        history.getRecent(limit).map { it.toRecentView() }
    }

    suspend fun getFavoriteDestinations(): List<Destination> = withContext(Dispatchers.IO) {
        val ids = favorites.getFavoriteIds()
        val byId = DestinationRepository(appContext).getAllDestinations().associateBy { it.id }
        ids.mapNotNull { byId[it] }
    }

    suspend fun getJourneyStamps(): List<JourneyStampEntity> = withContext(Dispatchers.IO) {
        stamps.getAll()
    }

    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        albums.getAllAlbums().map { album: AlbumEntity ->
            val photos = albums.getPhotos(album.id).map { it.uri }.toMutableList()
            Album(album.id, album.name, album.createdAt, photos)
        }
    }

    suspend fun getReviewCount(): Int = withContext(Dispatchers.IO) {
        reviews.getRecent(Int.MAX_VALUE).size
    }

    suspend fun getVisitedCount(): Int = withContext(Dispatchers.IO) {
        history.getRecent(Int.MAX_VALUE).size
    }

    suspend fun getCategoryDistribution(): Map<String, Int> = withContext(Dispatchers.IO) {
        val destinations = DestinationRepository(appContext).getAllDestinations().associateBy { it.id }
        history.getRecent(Int.MAX_VALUE)
            .mapNotNull { destinations[it.destinationId]?.category }
            .groupingBy { it }
            .eachCount()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        history.clear()
    }

    suspend fun addReview(destinationId: String, rating: Int, comment: String): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        reviews.insert(
            DestinationReviewEntity(
                destinationId = destinationId,
                rating = rating.coerceIn(1, 5),
                comment = comment.trim(),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateReview(reviewId: Long, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        reviews.update(
            reviewId = reviewId,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun deleteReview(reviewId: Long) = withContext(Dispatchers.IO) {
        reviews.delete(reviewId)
    }

    suspend fun getReviews(destinationId: String): List<UserDestinationReview> = withContext(Dispatchers.IO) {
        reviews.getForDestination(destinationId).map { it.toUserReview() }
    }

    suspend fun getAverageUserRating(destinationId: String): Double? = withContext(Dispatchers.IO) {
        val items = reviews.getForDestination(destinationId)
        if (items.isEmpty()) null else items.map { it.rating }.average()
    }

    private fun DestinationHistoryEntity.toRecentView(): RecentDestinationView {
        return RecentDestinationView(destinationId, name, location, imageUrl, viewedAt, viewCount)
    }

    private fun DestinationReviewEntity.toUserReview(): UserDestinationReview {
        return UserDestinationReview(id, destinationId, rating, comment, createdAt, updatedAt)
    }

    private fun stampColorFor(category: String): Int {
        return when {
            category.contains("Pantai", ignoreCase = true) -> 0xFF20B8C7.toInt()
            category.contains("Gunung", ignoreCase = true) -> 0xFF4F8F35.toInt()
            category.contains("Candi", ignoreCase = true) || category.contains("Sejarah", ignoreCase = true) -> 0xFFE3A33A.toInt()
            category.contains("Danau", ignoreCase = true) -> 0xFF4DCAD6.toInt()
            else -> 0xFFF2D8B3.toInt()
        }
    }
}
