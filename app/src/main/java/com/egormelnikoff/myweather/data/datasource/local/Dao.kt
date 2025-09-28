package com.egormelnikoff.myweather.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.egormelnikoff.myweather.model.PlaceWeather

@Dao
interface MyDao {
    @Insert
    suspend fun insertPlaceWeather(placeWeather: PlaceWeather): Long

    @Query("SELECT * FROM PlaceWeather WHERE isCurrentLocation = 1")
    suspend fun getCurrentLocationPlaceWeather(): PlaceWeather?

    @Query("SELECT * FROM PlaceWeather WHERE isCurrentLocation = 0")
    suspend fun getSavedPlacesWeather(): MutableList<PlaceWeather>

    @Query("SELECT * FROM PlaceWeather WHERE id =:primaryKey")
    suspend fun getPlaceWeatherById (primaryKey: Long): PlaceWeather?

    @Query("SELECT * FROM PlaceWeather WHERE isDefault = 1")
    suspend fun getDefaultPlaceWeather(): PlaceWeather?

    @Query("SELECT COUNT(*) FROM PlaceWeather")
    suspend fun getCount(): Int

    @Query("DELETE FROM PlaceWeather WHERE id = :primaryKey")
    suspend fun deletePlaceWeatherById(primaryKey: Long)

    @Update
    suspend fun updatePlaceWeather(placeWeather: PlaceWeather)
    @Query("UPDATE PlaceWeather SET isDefault = 1 WHERE id = :primaryKey")
    suspend fun setDefaultPlace(primaryKey: Long)
    @Query("UPDATE PlaceWeather SET isDefault = 0 WHERE id != :primaryKey")
    suspend fun setNonDefaultPlace(primaryKey: Long)

}