package com.egormelnikoff.myweather.data.repos.remote

import android.location.Location
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.datasource.remote.Api
import com.egormelnikoff.myweather.model.HourlyWeather
import com.egormelnikoff.myweather.model.Place
import com.egormelnikoff.myweather.model.PlaceWeather
import com.egormelnikoff.myweather.model.Weather

interface RemoteReposInterface {
    suspend fun getPlacesByQuery(query: String): Result<List<Place>>
    suspend fun getPlaceByLocation(location: Location): Result<Place>
    suspend fun getWeather(
        id: Long = 0,
        isCurrentLocation: Boolean,
        place: Place
    ): Result<PlaceWeather>
}

class RemoteRepos : RemoteReposInterface {
    private val api = Api()

    override suspend fun getPlacesByQuery(query: String): Result<List<Place>> {
        return when (val placesJson = api.getData(api.makeUrlNominatimSearch(query))) {
            is Result.Error -> {
                Result.Error(placesJson.exception)
            }

            is Result.Success -> {
                when (val parsedPlaces = api.parseJson<List<Place>>(placesJson)) {
                    is Result.Success -> {
                        if (parsedPlaces.data.isNotEmpty()) {
                            Result.Success(
                                parsedPlaces.data.distinctBy {
                                    Pair(
                                        it.name,
                                        it.address
                                    )
                                }
                            )
                        } else {
                            Result.Success(listOf())
                        }

                    }

                    is Result.Error -> {
                        Result.Error(parsedPlaces.exception)
                    }
                }
            }
        }
    }

    override suspend fun getPlaceByLocation(location: Location): Result<Place> {
        return when (val placeJson = api.getData(api.makeUrlNominatimReverse(location))) {
            is Result.Error -> {
                Result.Error(placeJson.exception)
            }

            is Result.Success -> {
                when (val parsedPlace = api.parseJson<Place>(placeJson)) {
                    is Result.Success -> {
                        Result.Success(parsedPlace.data)
                    }

                    is Result.Error -> {
                        Result.Error(parsedPlace.exception)
                    }
                }
            }
        }
    }

    override suspend fun getWeather(
        id: Long,
        isCurrentLocation: Boolean,
        place: Place
    ): Result<PlaceWeather> {
        return when (val weatherJson =
            api.getData(api.makeUrlOpenMeteo(place.lat, place.lon))) {
            is Result.Error -> {
                Result.Error(weatherJson.exception)
            }

            is Result.Success -> {
                when (val weather = api.parseJson<Weather>(weatherJson)) {
                    is Result.Error -> {
                        Result.Error(weather.exception)
                    }

                    is Result.Success -> {
                        Result.Success(
                            PlaceWeather(
                                id = id,
                                isCurrentLocation = isCurrentLocation,
                                place = place,
                                weather = prepareWeather(weather.data)
                            )
                        )
                    }
                }


            }
        }
    }


    private fun prepareWeather(weather: Weather): Weather {
        val currentHour = weather.current.currentTime.drop(11).dropLast(3).toInt()
        val dropLastCount = (167 - (currentHour + 24))
        val hourlyTime = weather.hourly.hourlyTime.drop(currentHour).dropLast(dropLastCount)
        val hourlyWeatherCode =
            weather.hourly.hourlyWeatherCode.drop(currentHour).dropLast(dropLastCount)
        val hourlyTemperature2m =
            weather.hourly.hourlyTemperature.drop(currentHour).dropLast(dropLastCount)

        return weather.copy(
            hourly = HourlyWeather(
                hourlyTime = hourlyTime,
                hourlyWeatherCode = hourlyWeatherCode,
                hourlyTemperature = hourlyTemperature2m
            )
        )
    }
}