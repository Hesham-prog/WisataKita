package com.wisatakita.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DestinationHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: DestinationHistoryEntity)

    @Query("SELECT * FROM destination_history WHERE destinationId = :destinationId LIMIT 1")
    suspend fun get(destinationId: String): DestinationHistoryEntity?

    @Query("SELECT * FROM destination_history ORDER BY viewedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<DestinationHistoryEntity>

    @Query("DELETE FROM destination_history")
    suspend fun clear()
}
