package com.egormelnikoff.myweather.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "PlaceWeather"
)
data class PlaceWeather(
    @PrimaryKey(
        autoGenerate = true
    )
    val id: Long,
    val isCurrentLocation: Boolean = false,
    val isDefault: Boolean = false,
    @Embedded
    var place: Place,
    @Embedded
    var weather: Weather?
)

data class Place(
    @SerializedName("name")
    val name: String?,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @Embedded
    @SerializedName("address")
    val address: Address
)

data class Address(
    @SerializedName("state")
    val state: String?
)

data class Weather(
    @SerializedName("timezone_abbreviation")
    val timezone: String,
    @Embedded
    @SerializedName("current")
    val current: CurrentWeather,
    @Embedded
    @SerializedName("hourly")
    val hourly: HourlyWeather,
    @Embedded
    @SerializedName("daily")
    val daily: DailyWeather
)

data class CurrentWeather(
    @SerializedName("time")
    val currentTime: String,
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: Int,
    @SerializedName("precipitation")
    val precipitation: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("surface_pressure")
    val surfacePressure: Double,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double,
    @SerializedName("wind_direction_10m")
    val windDirection: Int,
    @SerializedName("is_day")
    val isDay: Int
)

data class HourlyWeather(
    @SerializedName("time")
    var hourlyTime: List<String>,
    @SerializedName("temperature_2m")
    var hourlyTemperature: List<Double>,
    @SerializedName("weather_code")
    var hourlyWeatherCode: List<Int>
)

data class DailyWeather(
    @SerializedName("time")
    val dailyTime: List<String>,
    @SerializedName("temperature_2m_max")
    val dailyTemperatureMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val dailyTemperatureMin: List<Double>,
    @SerializedName("weather_code")
    val dailyWeatherCode: List<Int>,
    @SerializedName("sunrise")
    val dailySunrise: List<String>,
    @SerializedName("sunset")
    val dailySunset: List<String>
)