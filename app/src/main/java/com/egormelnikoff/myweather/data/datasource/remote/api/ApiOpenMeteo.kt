package com.egormelnikoff.myweather.data.datasource.remote.api

import com.egormelnikoff.myweather.app.entity.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiOpenMeteo {
    @GET("v1/forecast?current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,is_day&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<Weather>
}