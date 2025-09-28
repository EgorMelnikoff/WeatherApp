package com.egormelnikoff.myweather.model

import android.content.Context

import com.egormelnikoff.myweather.R

data class WeatherData(
    val code: Int,
    val title: String,
    val dayImage: Int,
    val nightImage: Int? = null,
    val dayBackground: Int,
    val nightBackground: Int
)

class WeatherParams (context: Context) {
    private val clearBackground = R.drawable.clear_background
    private val clearNightBackground = R.drawable.clear_background_night
    private val overcastBackground = R.drawable.overcast_backgroud
    private val overcastNightBackground = R.drawable.overcast_background_night
    private val snowBackground = R.drawable.snow_background
    private val snowNightBackground = R.drawable.snow_background_night

    private val clearSky = WeatherData(
        code = 0,
        title = context
.getString(R.string.clear_sky),
        dayImage = R.drawable.clear,
        nightImage = R.drawable.clear_night,
        dayBackground = clearBackground,
        nightBackground = clearNightBackground
    )

    private val mostlyClear = WeatherData(
        code = 1,
        title = context
.getString(R.string.mostly_clear_sky),
        dayImage = R.drawable.mostly_clear,
        nightImage = R.drawable.mostly_clear_night,
        dayBackground = clearBackground,
        nightBackground = clearNightBackground
    )

    private val partlyCloudy = WeatherData(
        code = 2,
        title = context
.getString(R.string.partly_cloudy),
        dayImage = R.drawable.partly_cloudy,
        nightImage = R.drawable.partly_cloudy_night,
        dayBackground = clearBackground,
        nightBackground = clearNightBackground
    )
    private val overcast = WeatherData(
        code = 3,
        title = context
.getString(R.string.overcast),
        dayImage = R.drawable.overcast,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val fog = WeatherData(
        code = 45,
        title = context
.getString(R.string.fog),
        dayImage = R.drawable.fog,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val icyFog = WeatherData(
        code = 48,
        title = context
.getString(R.string.fog),
        dayImage = R.drawable.icy_fog,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val lightDrizzle = WeatherData(
        code = 51,
        title = context
.getString(R.string.light_drizzle),
        dayImage = R.drawable.drizzle,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val drizzle = WeatherData(
        code = 53,
        title = context
.getString(R.string.drizzle),
        dayImage = R.drawable.drizzle,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val heavyDrizzle = WeatherData(
        code = 55,
        title = context
.getString(R.string.heavy_drizzle),
        dayImage = R.drawable.drizzle,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val lightFreezingDrizzle = WeatherData(
        code = 56,
        title = context
.getString(R.string.freezing_drizzle),
        dayImage = R.drawable.freezing_drizzle,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val freezingDrizzle = WeatherData(
        code = 57,
        title = context
.getString(R.string.freezing_drizzle),
        dayImage = R.drawable.freezing_drizzle,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val lightRain = WeatherData(
        code = 61,
        title = context
.getString(R.string.light_rain),
        dayImage = R.drawable.light_rain,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val rain = WeatherData(
        code = 63,
        title = context
.getString(R.string.rain),
        dayImage = R.drawable.rain,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val heavyRain = WeatherData(
        code = 65,
        title = context
.getString(R.string.heavy_rain),
        dayImage = R.drawable.heavy_rain,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val lightFreezingRain = WeatherData(
        code = 66,
        title = context
.getString(R.string.freezing_rain),
        dayImage = R.drawable.freezing_rain,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val freezingRain = WeatherData(
        code = 67,
        title = context
.getString(R.string.freezing_rain),
        dayImage = R.drawable.freezing_rain,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val lightSnow = WeatherData(
        code = 71,
        title = context
.getString(R.string.light_snow),
        dayImage = R.drawable.light_snow,
        dayBackground = snowBackground,
        nightBackground = snowNightBackground
    )
    private val snow = WeatherData(
        code = 73,
        title = context
.getString(R.string.snow),
        dayImage = R.drawable.snow,
        dayBackground = snowBackground,
        nightBackground = snowNightBackground
    )
    private val heavySnow = WeatherData(
        code = 75,
        title = context
.getString(R.string.heavy_snow),
        dayImage = R.drawable.heavy_snow,
        dayBackground = snowBackground,
        nightBackground = snowNightBackground
    )
    private val snowGrains = WeatherData(
        code = 77,
        title = context
.getString(R.string.snow_grains),
        dayImage = R.drawable.snow,
        dayBackground = snowBackground,
        nightBackground = snowNightBackground
    )
    private val lightRainShower = WeatherData(
        code = 80,
        title = context
.getString(R.string.light_rain_shower),
        dayImage = R.drawable.rain_shower,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val rainShower = WeatherData(
        code = 81,
        title = context
.getString(R.string.rain_shower),
        dayImage = R.drawable.rain_shower,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val heavyRainShower = WeatherData(
        code = 82,
        title = context
.getString(R.string.heavy_rain_shower),
        dayImage = R.drawable.rain_shower,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val snowShower = WeatherData(
        code = 85,
        title = context
.getString(R.string.snow_shower),
        dayImage = R.drawable.snow_shower,
        dayBackground = snowBackground,
        nightBackground = snowNightBackground
    )
    private val heavySnowShower = WeatherData(
        code = 86,
        title = context
.getString(R.string.heavy_snow_shower),
        dayImage = R.drawable.snow_shower,
        dayBackground = snowBackground,
        nightBackground = snowNightBackground
    )
    private val thunderstorm = WeatherData(
        code = 95,
        title = context
.getString(R.string.thunderstorm),
        dayImage = R.drawable.thunderstorm,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val hail = WeatherData(
        code = 96,
        title = context
.getString(R.string.hail),
        dayImage = R.drawable.thunderstorm_with_hail,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    private val heavyHail = WeatherData(
        code = 99,
        title = context
.getString(R.string.heavy_hail),
        dayImage = R.drawable.thunderstorm_with_hail,
        dayBackground = overcastBackground,
        nightBackground = overcastNightBackground
    )
    
    val weathersMap = mapOf(
        Pair(clearSky.code, clearSky),
        Pair(mostlyClear.code, mostlyClear),
        Pair(partlyCloudy.code, partlyCloudy),
        Pair(overcast.code, overcast),
        Pair(fog.code, fog),
        Pair(icyFog.code, icyFog),
        Pair(lightDrizzle.code, lightDrizzle),
        Pair(drizzle.code, drizzle),
        Pair(heavyDrizzle.code, heavyDrizzle),
        Pair(lightFreezingDrizzle.code, lightFreezingDrizzle),
        Pair(freezingDrizzle.code, freezingDrizzle),
        Pair(lightRain.code, lightRain),
        Pair(rain.code, rain),
        Pair(heavyRain.code, heavyRain),
        Pair(lightFreezingRain.code, lightFreezingRain),
        Pair(freezingRain.code, freezingRain),
        Pair(lightSnow.code, lightSnow),
        Pair(snow.code, snow),
        Pair(heavySnow.code, heavySnow),
        Pair(snowGrains.code, snowGrains),
        Pair(lightRainShower.code, lightRainShower),
        Pair(rainShower.code, rainShower),
        Pair(heavyRainShower.code, heavyRainShower),
        Pair(snowShower.code, snowShower),
        Pair(heavySnowShower.code, heavySnowShower),
        Pair(thunderstorm.code, thunderstorm),
        Pair(hail.code, hail),
        Pair(heavyHail.code, heavyHail)
    )
}