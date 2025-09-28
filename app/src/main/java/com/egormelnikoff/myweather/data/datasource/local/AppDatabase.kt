package com.egormelnikoff.myweather.data.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.egormelnikoff.myweather.model.PlaceWeather

@Database(
    entities = [
        PlaceWeather::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): MyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "places_weather_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
