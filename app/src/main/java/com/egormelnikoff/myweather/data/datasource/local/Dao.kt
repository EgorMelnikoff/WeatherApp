package com.egormelnikoff.myweather.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.egormelnikoff.myweather.app.entity.PlaceWeather

@Dao
interface WeatherDao {
    @Insert
    suspend fun insertPlaceWeather(placeWeather: PlaceWeather): Long

    @Query("DELETE FROM PlaceWeather WHERE id = :primaryKey")
    suspend fun deletePlaceWeatherById(primaryKey: Long)

    @Query("SELECT * FROM PlaceWeather")
    suspend fun getSavedPlacesWeather(): List<PlaceWeather>

    @Query("SELECT * FROM PlaceWeather WHERE id =:primaryKey")
    suspend fun getPlaceWeatherById (primaryKey: Long): PlaceWeather?

    @Query("SELECT * FROM PlaceWeather WHERE lat = :lat AND lon = :lon")
    suspend fun getPlaceWeatherByLocation (lat: Double, lon: Double): PlaceWeather?

    @Query("SELECT * FROM PlaceWeather WHERE isDefault = 1")
    suspend fun getDefaultPlaceWeather(): PlaceWeather?

    @Query("UPDATE PlaceWeather SET isDefault = 1 WHERE id = :primaryKey")
    suspend fun setDefaultPlace(primaryKey: Long)
    @Query("UPDATE PlaceWeather SET isDefault = 0 WHERE id != :primaryKey")
    suspend fun setNonDefaultPlace(primaryKey: Long)

    @Update
    suspend fun updatePlaceWeather(placeWeather: PlaceWeather)
}