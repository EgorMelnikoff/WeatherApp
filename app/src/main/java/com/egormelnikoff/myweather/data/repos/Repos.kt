package com.egormelnikoff.myweather.data.repos

import android.location.Location
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.datasource.location.LocationHelper
import com.egormelnikoff.myweather.data.repos.local.LocalReposInterface
import com.egormelnikoff.myweather.data.repos.remote.RemoteReposInterface
import com.egormelnikoff.myweather.model.Place
import com.egormelnikoff.myweather.model.PlaceWeather
import com.egormelnikoff.myweather.model.Weather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface WeatherRepositoryInterface {
    suspend fun insertPlaceWeather(placeWeather: PlaceWeather): Long
    suspend fun deletePlaceWeather(primaryKey: Long)

    suspend fun getCountRows(): Int
    suspend fun getCurrentLocationPlaceWeather(): PlaceWeather?
    suspend fun getDefaultPlaceWeather(): PlaceWeather?
    suspend fun getSavedPlacesWeather(): MutableList<PlaceWeather>

    suspend fun updatePlaceWeather(placeId: Long, newWeather: Weather)
    suspend fun setDefaultPlace(placeId: Long)
    suspend fun updateSavedPlacesWeather(formatter: DateTimeFormatter, coroutineScope: CoroutineScope)
    suspend fun updateCurrentLocationPlaceWeather(formatter: DateTimeFormatter)


    suspend fun getPlacesByQuery(query: String): Result<List<Place>>
    suspend fun getPlaceByLocation(location: Location): Result<Place>
    suspend fun getWeather(id: Long, isCurrentLocation: Boolean, place: Place): Result<PlaceWeather>
}

class WeatherRepository(
    private val remoteRepos: RemoteReposInterface,
    private val localRepos: LocalReposInterface,
    private val locationHelper: LocationHelper
) : WeatherRepositoryInterface {

    override suspend fun insertPlaceWeather(placeWeather: PlaceWeather): Long {
        return localRepos.insertPlaceWeather(placeWeather)
    }

    override suspend fun updateSavedPlacesWeather(formatter: DateTimeFormatter, coroutineScope: CoroutineScope) {
        val deferredUpdates = localRepos.getSavedPlacesWeather().map { placeWeather ->
            coroutineScope.async {
                if (shouldUpdate(formatter, placeWeather.weather)) {
                    when (val updatedWeather = remoteRepos.getWeather(
                        placeWeather.id,
                        placeWeather.isCurrentLocation,
                        placeWeather.place
                    )) {
                        is Result.Error -> {

                        }

                        is Result.Success -> {
                            localRepos.updateSavedPlaceWeather(
                                placeWeather.id,
                                updatedWeather.data.weather!!
                            )
                        }
                    }
                }
            }
        }
        deferredUpdates.awaitAll()
    }

    override suspend fun updateCurrentLocationPlaceWeather(formatter: DateTimeFormatter) {
        val currentLocationPlaceWeather = localRepos.getCurrentLocationPlaceWeather()
        if (shouldUpdate(formatter, currentLocationPlaceWeather?.weather)) {
            getAndUpdateCLWeather()
        } else {
            return
        }

    }

    private suspend fun getAndUpdateCLWeather() {
        val currentPlace = locationHelper.getCurrentLocationPlace()
        if (currentPlace != null) {
            when (val placeWeather = remoteRepos.getWeather(0, true, currentPlace)) {
                is Result.Error -> {

                }

                is Result.Success -> {
                    localRepos.updateCurrentLocationPlaceWeather(placeWeather.data)
                }
            }
        }
    }

    override suspend fun getCountRows(): Int {
        return localRepos.getCountRows()
    }

    override suspend fun getCurrentLocationPlaceWeather(): PlaceWeather? {
        return localRepos.getCurrentLocationPlaceWeather()
    }

    override suspend fun getSavedPlacesWeather(): MutableList<PlaceWeather> {
        return localRepos.getSavedPlacesWeather()
    }

    override suspend fun getDefaultPlaceWeather(): PlaceWeather? {
        return localRepos.getDefaultPlaceWeather()
    }

    override suspend fun deletePlaceWeather(primaryKey: Long) {
        localRepos.deletePlaceWeather(primaryKey)
    }


    override suspend fun updatePlaceWeather(placeId: Long, newWeather: Weather) {
        return localRepos.updateSavedPlaceWeather(placeId, newWeather)
    }

    override suspend fun setDefaultPlace(placeId: Long) {
        localRepos.setDefaultPlace(placeId)
    }

    override suspend fun getPlacesByQuery(query: String): Result<List<Place>> {
        return remoteRepos.getPlacesByQuery(query)
    }

    override suspend fun getPlaceByLocation(location: Location): Result<Place> {
        return remoteRepos.getPlaceByLocation(location)
    }

    override suspend fun getWeather(
        id: Long,
        isCurrentLocation: Boolean,
        place: Place
    ): Result<PlaceWeather> {
        return remoteRepos.getWeather(id, isCurrentLocation, place)
    }

    private fun shouldUpdate(formatter: DateTimeFormatter, weather: Weather?): Boolean {
        return if (weather != null) {
            val zoneOffset = ZoneOffset.of(weather.timezone.drop(3))
            val lastTimeUpdate = LocalDateTime.parse(
                weather.current.currentTime,
                formatter
            )
            val lastTimeUpdateMs = lastTimeUpdate.toInstant(zoneOffset).toEpochMilli()
            (System.currentTimeMillis() - lastTimeUpdateMs) / 60000 > 15
        } else {
            true
        }
    }
}