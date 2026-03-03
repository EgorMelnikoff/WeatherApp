package com.egormelnikoff.myweather.domain

import com.egormelnikoff.myweather.app.entity.HourlyWeather
import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.app.entity.Weather
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.repos.weather.WeatherRepos
import javax.inject.Inject

class FetchWeatherUseCase @Inject constructor(
    private val weatherRepos: WeatherRepos
) {
    suspend operator fun invoke(
        id: Long = 0,
        place: Place,
        fetchForce: Boolean = false,
        insert: Boolean = true
    ): Result<PlaceWeather> {
        val tempPlaceWeather = weatherRepos.getPlaceWeatherByLocation(place.lat, place.lon)
        if (tempPlaceWeather != null && !fetchForce) {
            return Result.Success(tempPlaceWeather)
        }

        return when (val weather = weatherRepos.fetchWeather(place)) {
            is Result.Success -> {
                val placeWeather = PlaceWeather(
                    id = id,
                    place = place,
                    weather = weather.data.normalizeWeather()
                )
                if (insert) weatherRepos.insertPlaceWeather(placeWeather)
                Result.Success(placeWeather)
            }

            is Result.Error -> weather
        }
    }

    private fun Weather.normalizeWeather(): Weather {
        val currentHour = this.current.currentTime.hour
        val dropLastCount = (167 - (currentHour + 24))
        val hourlyTime = this.hourly.hourlyTime
            .drop(currentHour)
            .dropLast(dropLastCount)
        val hourlyWeatherCode = this.hourly.hourlyWeatherCode
            .drop(currentHour)
            .dropLast(dropLastCount)
        val hourlyTemperature2m = this.hourly.hourlyTemperature
            .drop(currentHour)
            .dropLast(dropLastCount)

        return this.copy(
            hourly = HourlyWeather(
                hourlyTime = hourlyTime,
                hourlyWeatherCode = hourlyWeatherCode,
                hourlyTemperature = hourlyTemperature2m
            )
        )
    }
}