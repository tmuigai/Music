package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackEntity::class,
        MixtapeEntity::class,
        ListeningHistoryEntity::class,
        RecordingEntity::class,
        ObsessionProfileEntity::class,
        RadioPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ReplayDatabase : RoomDatabase() {
    abstract fun replayDao(): ReplayDao

    companion object {
        @Volatile
        private var INSTANCE: ReplayDatabase? = null

        fun getDatabase(context: Context): ReplayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReplayDatabase::class.java,
                    "replay99_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
