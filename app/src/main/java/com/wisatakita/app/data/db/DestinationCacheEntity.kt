package com.wisatakita.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destination_cache")
data class DestinationCacheEntity(
    @PrimaryKey val destinationId: String,
    val name: String,
    val payloadJson: String,
    val cachedAt: Long,
    val sourceLabel: String
)
