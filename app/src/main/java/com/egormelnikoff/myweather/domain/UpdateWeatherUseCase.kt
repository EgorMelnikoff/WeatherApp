package com.egormelnikoff.myweather.domain

import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.app.entity.Weather
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.repos.weather.WeatherRepos
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import java.time.ZoneOffset
import javax.inject.Inject

class UpdateWeatherUseCase @Inject constructor(
    private val fetchWeatherUseCase: FetchWeatherUseCase,
    private val weatherRepos: WeatherRepos
) {
    companion object {
        private const val UPDATE_THRESHOLD_MIN = 15
    }
    suspend operator fun invoke(): Result<List<PlaceWeather>> = supervisorScope {
        val deferredPlacesWeather = weatherRepos.getSavedPlacesWeather()
            .filter { it.weather.shouldWeatherUpdate() }
            .map { placeWeather ->
                async {
                    fetchWeatherUseCase(
                        id = placeWeather.id,
                        place = placeWeather.place,
                        fetchForce = true,
                        insert = false
                    ).let {
                        when (it) {
                            is Result.Error -> throw it.exception
                            is Result.Success -> it.data
                        }
                    }
                }
            }
        try {
            deferredPlacesWeather.awaitAll().forEach {
                weatherRepos.updateSavedPlaceWeather(it)
            }
            return@supervisorScope Result.Success(weatherRepos.getSavedPlacesWeather())
        } catch (e: Exception) {
            return@supervisorScope Result.Error(e)
        }
    }

    private fun Weather?.shouldWeatherUpdate(): Boolean {
        return this?.let {
            val zoneOffset = ZoneOffset.of(this.timezone.drop(3))

            val lastTimeUpdateMs = this.current.currentTime
                .toInstant(zoneOffset)
                .toEpochMilli()
            (System.currentTimeMillis() - lastTimeUpdateMs) / 60000 > UPDATE_THRESHOLD_MIN
        } ?: true
    }
}