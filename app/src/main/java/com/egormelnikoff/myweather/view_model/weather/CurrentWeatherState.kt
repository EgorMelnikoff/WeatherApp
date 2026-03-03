package com.egormelnikoff.myweather.view_model.weather

import com.egormelnikoff.myweather.app.entity.PlaceWeather


sealed interface CurrentWeatherState {
    data object NotSettled : CurrentWeatherState
    data class Error(
        val message: String?
    ) : CurrentWeatherState

    data object Loading : CurrentWeatherState
    data class Loaded(
        val placeWeather: PlaceWeather
    ) : CurrentWeatherState
}

data class PlacesWeatherState(
    val placesWeather: List<PlaceWeather> = listOf()
)