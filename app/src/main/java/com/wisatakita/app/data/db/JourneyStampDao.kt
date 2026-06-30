package com.wisatakita.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JourneyStampDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stamp: JourneyStampEntity)

    @Query("SELECT * FROM journey_stamps ORDER BY unlockedAt DESC")
    suspend fun getAll(): List<JourneyStampEntity>

    @Query("SELECT COUNT(*) FROM journey_stamps")
    suspend fun count(): Int
}
