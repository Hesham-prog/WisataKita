package com.wisatakita.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destination_history")
data class DestinationHistoryEntity(
    @PrimaryKey val destinationId: String,
    val name: String,
    val location: String,
    val imageUrl: String,
    val viewedAt: Long,
    val viewCount: Int
)
