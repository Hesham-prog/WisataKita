package com.wisatakita.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_destinations")
data class FavoriteDestinationEntity(
    @PrimaryKey val destinationId: String,
    val createdAt: Long
)
