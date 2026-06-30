package com.wisatakita.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journey_stamps")
data class JourneyStampEntity(
    @PrimaryKey val destinationId: String,
    val categoryType: String,
    val stampColor: Int,
    val unlockedAt: Long
)
