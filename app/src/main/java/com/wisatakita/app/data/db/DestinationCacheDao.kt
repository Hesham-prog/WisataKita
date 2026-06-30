package com.wisatakita.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DestinationCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(destinations: List<DestinationCacheEntity>)

    @Query("SELECT * FROM destination_cache ORDER BY name ASC")
    suspend fun getAll(): List<DestinationCacheEntity>

    @Query("DELETE FROM destination_cache")
    suspend fun clear()
}
