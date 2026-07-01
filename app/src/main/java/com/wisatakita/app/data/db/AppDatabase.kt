package com.wisatakita.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        AlbumEntity::class,
        PhotoEntity::class,
        DestinationCacheEntity::class,
        FavoriteDestinationEntity::class,
        DestinationHistoryEntity::class,
        DestinationReviewEntity::class,
        JourneyStampEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun albumDao(): AlbumDao
    abstract fun destinationCacheDao(): DestinationCacheDao
    abstract fun favoriteDestinationDao(): FavoriteDestinationDao
    abstract fun destinationHistoryDao(): DestinationHistoryDao
    abstract fun destinationReviewDao(): DestinationReviewDao
    abstract fun journeyStampDao(): JourneyStampDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wisatakita.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS destination_cache (
                        destinationId TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        cachedAt INTEGER NOT NULL,
                        sourceLabel TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorite_destinations (
                        destinationId TEXT NOT NULL PRIMARY KEY,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS destination_history (
                        destinationId TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        location TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        viewedAt INTEGER NOT NULL,
                        viewCount INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS destination_reviews (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        destinationId TEXT NOT NULL,
                        rating INTEGER NOT NULL,
                        comment TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS journey_stamps (
                        destinationId TEXT NOT NULL PRIMARY KEY,
                        categoryType TEXT NOT NULL,
                        stampColor INTEGER NOT NULL,
                        unlockedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_albumId` ON `photos` (`albumId`)")
            }
        }
    }
}
