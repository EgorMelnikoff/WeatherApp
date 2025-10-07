package com.egormelnikoff.myweather.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.egormelnikoff.myweather.R
import com.egormelnikoff.myweather.model.HourlyWeather
import com.egormelnikoff.myweather.model.WeatherCodes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.round

class HourlyAdapter(
    private val weatherCodes: WeatherCodes,
    private val hourlyWeather: HourlyWeather,
    private val sunrise: String,
    private val sunset: String,
    private val temperature: String?
) : RecyclerView.Adapter<HourlyAdapter.HourlyWeatherViewHolder>() {

    inner class HourlyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val formatter = DateTimeFormatter.ISO_DATE_TIME

        private val hourTime: TextView = itemView.findViewById(R.id.hour_time)
        private val hourWeatherImage: ImageView = itemView.findViewById(R.id.hour_weather_image)
        private val hourTemperature2m: TextView = itemView.findViewById(R.id.hour_temperature_2m)

        @SuppressLint("SetTextI18n")
        fun bind(
            position: Int
        ) {
            val weatherData = weatherCodes.getWeatherDataByWeatherCode(hourlyWeather.hourlyWeatherCode[position])!!

            hourTime.text = hourlyWeather.hourlyTime[position].drop(11)

            hourTemperature2m.text = if (temperature == "celsius") {
                "${round(hourlyWeather.hourlyTemperature[position]).toInt()}°"
            } else {
                "${((round(hourlyWeather.hourlyTemperature[position]).toInt()) * 9/5) + 32}°"
            }
            val currentTime = LocalDateTime.parse(hourlyWeather.hourlyTime[position]).toLocalTime()
            val sunriseL = LocalDateTime.parse(sunrise, formatter).toLocalTime()
            val sunsetL = LocalDateTime.parse(sunset, formatter).toLocalTime()
            if (currentTime.isAfter(sunsetL) || currentTime.isBefore(sunriseL)) {
                hourWeatherImage.setImageResource(weatherData.nightImage ?: weatherData.dayImage)
            } else {
                hourWeatherImage.setImageResource(weatherData.dayImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hour, parent, false)
        return HourlyWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = hourlyWeather.hourlyTime.size
}