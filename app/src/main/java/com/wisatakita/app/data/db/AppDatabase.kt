package com.wisatakita.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        AlbumEntity::class,
        PhotoEntity::class,
        DestinationCacheEntity::class,
        FavoriteDestinationEntity::class,
        DestinationHistoryEntity::class,
        DestinationReviewEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun albumDao(): AlbumDao
    abstract fun destinationCacheDao(): DestinationCacheDao
    abstract fun favoriteDestinationDao(): FavoriteDestinationDao
    abstract fun destinationHistoryDao(): DestinationHistoryDao
    abstract fun destinationReviewDao(): DestinationReviewDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wisatakita.db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
