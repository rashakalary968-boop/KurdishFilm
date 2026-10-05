package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MovieEntity::class, AccessPassEntity::class, CloudConfigEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CinemaDatabase : RoomDatabase() {
    abstract fun cinemaDao(): CinemaDao

    companion object {
        @Volatile
        private var INSTANCE: CinemaDatabase? = null

        fun getInstance(context: Context): CinemaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CinemaDatabase::class.java,
                    "aether_cinema_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
