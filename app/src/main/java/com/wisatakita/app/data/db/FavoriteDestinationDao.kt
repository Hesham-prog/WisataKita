package com.wisatakita.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDestinationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteDestinationEntity)

    @Query("DELETE FROM favorite_destinations WHERE destinationId = :destinationId")
    suspend fun remove(destinationId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_destinations WHERE destinationId = :destinationId)")
    suspend fun isFavorite(destinationId: String): Boolean

    @Query("SELECT destinationId FROM favorite_destinations ORDER BY createdAt DESC")
    suspend fun getFavoriteIds(): List<String>
}
