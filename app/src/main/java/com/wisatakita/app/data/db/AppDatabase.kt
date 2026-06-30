package com.wisatakita.app.data.db

import android.content.Context
import androidx.room.*

@Database(entities = [UserEntity::class, AlbumEntity::class, PhotoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wisatakita.db"
                ).allowMainThreadQueries().build().also { INSTANCE = it }
            }
        }
    }
}
