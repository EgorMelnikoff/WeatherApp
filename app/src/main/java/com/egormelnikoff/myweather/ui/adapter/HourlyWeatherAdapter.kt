package com.egormelnikoff.myweather.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.egormelnikoff.myweather.R
import com.egormelnikoff.myweather.app.entity.HourlyWeather
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.model.WeatherCodes
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.round

class HourlyWeatherAdapter(
    private val weatherCodes: WeatherCodes,
    private val hourlyWeather: HourlyWeather,
    private val sunrise: LocalTime,
    private val sunset: LocalTime,
    private val temperature: Temperature
) : RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherViewHolder>() {

    inner class HourlyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hourTime: TextView = itemView.findViewById(R.id.hour_time)
        private val hourWeatherImage: ImageView = itemView.findViewById(R.id.hour_weather_image)
        private val hourTemperature2m: TextView = itemView.findViewById(R.id.hour_temperature_2m)

        @SuppressLint("SetTextI18n")
        fun bind(
            position: Int
        ) {
            val weatherData =
                weatherCodes.getWeatherDataByWeatherCode(hourlyWeather.hourlyWeatherCode[position])

            hourTime.text = hourlyWeather.hourlyTime[position].toLocalTime().format(
                DateTimeFormatter.ofPattern("HH:mm")
            )

            hourTemperature2m.text = when (temperature) {
                Temperature.CELSIUS -> "${round(hourlyWeather.hourlyTemperature[position]).toInt()}°"
                Temperature.FAHRENHEIT -> "${((round(hourlyWeather.hourlyTemperature[position]).toInt()) * 9 / 5) + 32}°"
            }


            val currentTime = hourlyWeather.hourlyTime[position].toLocalTime()
            if (currentTime.isAfter(sunset) || currentTime.isBefore(sunrise)) {
                hourWeatherImage.setImageResource(weatherData.nightImage ?: weatherData.dayImage)
            } else {
                hourWeatherImage.setImageResource(weatherData.dayImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.hour,
                parent,
                false
            )
        return HourlyWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = hourlyWeather.hourlyTime.size
}