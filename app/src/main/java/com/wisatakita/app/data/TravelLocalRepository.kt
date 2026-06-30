package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.data.db.AppDatabase
import com.wisatakita.app.data.db.DestinationHistoryEntity
import com.wisatakita.app.data.db.DestinationReviewEntity
import com.wisatakita.app.data.db.FavoriteDestinationEntity
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
    private val db = AppDatabase.getInstance(context)
    private val favorites = db.favoriteDestinationDao()
    private val history = db.destinationHistoryDao()
    private val reviews = db.destinationReviewDao()

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
    }

    suspend fun getRecentViews(limit: Int = 20): List<RecentDestinationView> = withContext(Dispatchers.IO) {
        history.getRecent(limit).map { it.toRecentView() }
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
}
