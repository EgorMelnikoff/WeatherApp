package com.egormelnikoff.myweather.data.repos.weather

import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.app.entity.Weather
import com.egormelnikoff.myweather.data.Result

interface WeatherRepos {
    suspend fun fetchWeather(
        place: Place
    ): Result<Weather>

    suspend fun deletePlaceWeather(primaryKey: Long)
    suspend fun insertPlaceWeather(placeWeather: PlaceWeather)
    suspend fun getSavedPlacesWeather(): List<PlaceWeather>
    suspend fun getPlaceWeatherById(primaryKey: Long): PlaceWeather?
    suspend fun getPlaceWeatherByLocation(lat: Double, lon: Double): PlaceWeather?
    suspend fun getDefaultPlaceWeather(): PlaceWeather?

    suspend fun setDefaultPlace(placeId: Long)
    suspend fun updateSavedPlaceWeather(placeWeather: PlaceWeather)
}