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
import com.egormelnikoff.myweather.app.entity.DailyWeather
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.model.WeatherCodes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.round

class DailyWeatherAdapter(
    private val weatherCodes: WeatherCodes,
    private val dailyWeather: DailyWeather,
    private val temperature: Temperature,
    private val context: Context
) : RecyclerView.Adapter<DailyWeatherAdapter.DailyWeatherViewHolder>() {

    inner class DailyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val dayDate: TextView = itemView.findViewById(R.id.day_date)
        private val dayName: TextView = itemView.findViewById(R.id.day_name)
        private val dayWeatherImage: ImageView = itemView.findViewById(R.id.day_weather_image)
        private val dayTemperature2mMax: TextView =
            itemView.findViewById(R.id.day_temperature_2m_max)
        private val dayTemperature2mMin: TextView =
            itemView.findViewById(R.id.day_temperature_2m_min)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val today = LocalDate.now()
            val weatherData =
                weatherCodes.getWeatherDataByWeatherCode(dailyWeather.dailyWeatherCode[position])

            val date = dailyWeather.dailyTime[position]
            val dateIndex = date.dayOfWeek.value.minus(1)

            dayDate.text =
                date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())).toString()
            when (date.dayOfWeek) {
                today.dayOfWeek -> {
                    dayDate.text = context.getString(R.string.today)
                }

                today.plusDays(1).dayOfWeek -> {
                    dayDate.text = context.getString(R.string.tomorrow)
                }

                else -> {}
            }

            dayName.text = date.dayOfWeek
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                .replaceFirstChar { it.uppercase() }

            if (dateIndex == 5 || dateIndex == 6) {
                val color = context.getColor(R.color.onError)
                dayDate.setTextColor(color)
            }

            when (temperature) {
                Temperature.CELSIUS -> {
                    dayTemperature2mMax.text =
                        "${round(dailyWeather.dailyTemperatureMax[position]).toInt()}°"
                    dayTemperature2mMin.text =
                        "${round(dailyWeather.dailyTemperatureMin[position]).toInt()}°"
                }

                Temperature.FAHRENHEIT -> {
                    dayTemperature2mMax.text =
                        "${((round(dailyWeather.dailyTemperatureMax[position]).toInt()) * 9 / 5) + 32}°"
                    dayTemperature2mMin.text =
                        "${((round(dailyWeather.dailyTemperatureMin[position]).toInt()) * 9 / 5) + 32}°"
                }
            }
            dayWeatherImage.setImageResource(weatherData.dayImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.day,
                parent,
                false
            )
        return DailyWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = dailyWeather.dailyTime.size
}