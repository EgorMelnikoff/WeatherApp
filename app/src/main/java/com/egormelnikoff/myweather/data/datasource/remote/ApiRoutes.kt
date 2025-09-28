package com.egormelnikoff.myweather.data.datasource.remote

import android.annotation.SuppressLint
import java.util.Locale

object ApiRoutes {
    @SuppressLint("ConstantLocale")
    private val language = Locale.getDefault().language

    const val API_NOMINATIM_BASE = "https://nominatim.openstreetmap.org/"

    val nominatimSearch = "search?&format=json&limit=10&addressdetails=1&featureType=settlement&layer=address&accept-language=$language"
    const val NOMINATIM_QUERY = "&q="

    val nominatimReverse = "reverse?format=json&zoom=5&accept-language=$language"
    const val NOMINATIM_REVERSE_LAT = "&lat="
    const val NOMINATIM_REVERSE_LON = "&lon="


    const val API_OPEN_METEO_BASE = "https://api.open-meteo.com/v1/forecast?"
    const val OPEN_METEO_LAT = "latitude="
    const val OPEN_METEO_LON = "&longitude="
    const val OPEN_METEO_PARAMS =
        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,is_day&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto"
}