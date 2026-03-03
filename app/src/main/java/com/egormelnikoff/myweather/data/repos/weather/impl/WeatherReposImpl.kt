package com.egormelnikoff.myweather.data.repos.weather.impl

import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.app.entity.Weather
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.datasource.local.WeatherDao
import com.egormelnikoff.myweather.data.datasource.remote.NetworkHelper
import com.egormelnikoff.myweather.data.datasource.remote.api.ApiOpenMeteo
import com.egormelnikoff.myweather.data.repos.weather.WeatherRepos
import javax.inject.Inject

class WeatherReposImpl @Inject constructor(
    private val networkHelper: NetworkHelper,
    private val apiOpenMeteo: ApiOpenMeteo,
    private val weatherDao: WeatherDao
) : WeatherRepos {
    override suspend fun fetchWeather(
        place: Place
    ): Result<Weather> {
        return networkHelper.callNetwork(
            requestType = "Weather",
            requestParams = "Place: $place"
        ) {
            apiOpenMeteo.getWeather(
                latitude = place.lat,
                longitude = place.lon
            )
        }
    }

    override suspend fun insertPlaceWeather(placeWeather: PlaceWeather) {
        weatherDao.insertPlaceWeather(placeWeather)
    }

    override suspend fun getPlaceWeatherByLocation(lat: Double, lon: Double): PlaceWeather? {
        return weatherDao.getPlaceWeatherByLocation(lat, lon)
    }

    override suspend fun deletePlaceWeather(primaryKey: Long) {
        weatherDao.deletePlaceWeatherById(primaryKey)
    }

    override suspend fun getSavedPlacesWeather(): List<PlaceWeather> {
        return weatherDao.getSavedPlacesWeather()
    }

    override suspend fun getPlaceWeatherById(primaryKey: Long): PlaceWeather? {
        return weatherDao.getPlaceWeatherById(primaryKey)
    }

    override suspend fun getDefaultPlaceWeather(): PlaceWeather? {
        return weatherDao.getDefaultPlaceWeather()
    }

    override suspend fun setDefaultPlace(placeId: Long) {
        weatherDao.setDefaultPlace(placeId)
        weatherDao.setNonDefaultPlace(placeId)
    }

    override suspend fun updateSavedPlaceWeather(placeWeather: PlaceWeather) {
        weatherDao.updatePlaceWeather(placeWeather)
    }
}