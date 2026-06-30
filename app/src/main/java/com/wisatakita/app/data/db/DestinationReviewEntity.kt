package com.wisatakita.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destination_reviews")
data class DestinationReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destinationId: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long,
    val updatedAt: Long
)
