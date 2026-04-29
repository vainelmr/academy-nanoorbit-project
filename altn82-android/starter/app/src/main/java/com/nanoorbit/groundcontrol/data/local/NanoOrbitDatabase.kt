package com.nanoorbit.groundcontrol.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SatelliteEntity::class, FenetreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NanoOrbitDatabase : RoomDatabase() {
    abstract fun satelliteDao(): SatelliteDao
    abstract fun fenetreDao(): FenetreDao

    companion object {
        @Volatile
        private var INSTANCE: NanoOrbitDatabase? = null

        fun getDatabase(context: Context): NanoOrbitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NanoOrbitDatabase::class.java,
                    "nanoorbit_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
