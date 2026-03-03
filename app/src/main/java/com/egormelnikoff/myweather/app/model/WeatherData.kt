package com.egormelnikoff.myweather.app.model

data class WeatherData(
    val code: Int,
    val title: String,
    val dayImage: Int,
    val nightImage: Int? = null,
    val dayBackground: Int,
    val nightBackground: Int
)
