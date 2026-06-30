package com.wisatakita.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DestinationReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: DestinationReviewEntity): Long

    @Query("UPDATE destination_reviews SET rating = :rating, comment = :comment, updatedAt = :updatedAt WHERE id = :reviewId")
    suspend fun update(reviewId: Long, rating: Int, comment: String, updatedAt: Long)

    @Query("DELETE FROM destination_reviews WHERE id = :reviewId")
    suspend fun delete(reviewId: Long)

    @Query("SELECT * FROM destination_reviews WHERE destinationId = :destinationId ORDER BY updatedAt DESC")
    suspend fun getForDestination(destinationId: String): List<DestinationReviewEntity>

    @Query("SELECT * FROM destination_reviews ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<DestinationReviewEntity>
}
