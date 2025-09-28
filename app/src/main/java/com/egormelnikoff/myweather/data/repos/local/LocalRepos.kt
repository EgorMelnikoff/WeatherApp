package com.egormelnikoff.myweather.data.repos.local

import com.egormelnikoff.myweather.data.datasource.local.MyDao
import com.egormelnikoff.myweather.model.PlaceWeather
import com.egormelnikoff.myweather.model.Weather

interface LocalReposInterface {
    suspend fun insertPlaceWeather(placeWeather: PlaceWeather): Long

    suspend fun getCountRows(): Int
    suspend fun getCurrentLocationPlaceWeather(): PlaceWeather?
    suspend fun getDefaultPlaceWeather(): PlaceWeather?
    suspend fun getSavedPlacesWeather(): MutableList<PlaceWeather>

    suspend fun updateCurrentLocationPlaceWeather(placeWeather: PlaceWeather): Long
    suspend fun updateSavedPlaceWeather(placeId: Long, newWeather: Weather)
    suspend fun setDefaultPlace(id: Long)

    suspend fun deletePlaceWeather(primaryKey: Long)
}

class LocalRepos(
    private val weatherDao: MyDao,
) : LocalReposInterface {
    override suspend fun insertPlaceWeather(placeWeather: PlaceWeather): Long {
        return weatherDao.insertPlaceWeather(placeWeather)
    }

    override suspend fun updateCurrentLocationPlaceWeather(placeWeather: PlaceWeather): Long {
        val currentLocationPlaceWeather = weatherDao.getCurrentLocationPlaceWeather()
        if (currentLocationPlaceWeather != null) {
            val updatedPlaceWeather = currentLocationPlaceWeather.copy(place = placeWeather.place, weather = placeWeather.weather)
            weatherDao.updatePlaceWeather(updatedPlaceWeather)
            return currentLocationPlaceWeather.id
        } else {
            return weatherDao.insertPlaceWeather(placeWeather)
        }
    }

    override suspend fun updateSavedPlaceWeather(placeId: Long, newWeather: Weather) {
        val currentWeather = weatherDao.getPlaceWeatherById(placeId)

        currentWeather?.let {
            val updatedWeather = it.copy(
                weather = newWeather
            )

            weatherDao.updatePlaceWeather(updatedWeather)
        }
    }


    override suspend fun getCountRows(): Int {
        return weatherDao.getCount()
    }

    override suspend fun getCurrentLocationPlaceWeather(): PlaceWeather? {
        return weatherDao.getCurrentLocationPlaceWeather()
    }

    override suspend fun getSavedPlacesWeather(): MutableList<PlaceWeather> {
        return weatherDao.getSavedPlacesWeather()
    }

    override suspend fun getDefaultPlaceWeather(): PlaceWeather? {
        return weatherDao.getDefaultPlaceWeather()
    }

    override suspend fun deletePlaceWeather(primaryKey: Long) {
        weatherDao.deletePlaceWeatherById(primaryKey)
    }



    override suspend fun setDefaultPlace(id: Long) {
        weatherDao.setDefaultPlace(id)
        weatherDao.setNonDefaultPlace(id)
    }
}