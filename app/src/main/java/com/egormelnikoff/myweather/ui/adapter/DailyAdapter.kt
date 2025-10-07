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
import com.egormelnikoff.myweather.model.DailyWeather
import com.egormelnikoff.myweather.model.WeatherCodes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.round

class DailyAdapter(
    private val weatherCodes: WeatherCodes,
    private val dailyWeather: DailyWeather,
    private val temperature: String?,
    private val context: Context
) : RecyclerView.Adapter<DailyAdapter.DailyWeatherViewHolder>() {

    inner class DailyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        private val dayDate: TextView = itemView.findViewById(R.id.day_date)
        private val dayName: TextView = itemView.findViewById(R.id.day_name)
        private val dayWeatherImage: ImageView = itemView.findViewById(R.id.day_weather_image)
        private val dayTemperature2mMax: TextView = itemView.findViewById(R.id.day_temperature_2m_max)
        private val dayTemperature2mMin: TextView = itemView.findViewById(R.id.day_temperature_2m_min)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val weatherData = weatherCodes.getWeatherDataByWeatherCode(dailyWeather.dailyWeatherCode[position])!!

            val date = LocalDate.parse(dailyWeather.dailyTime[position], formatter)
            val dateIndex = date.dayOfWeek.value.minus(1)

            dayDate.text = date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())).toString().takeIf {position != 0} ?: context.getString(
                R.string.today
            )
            dayName.text = context.resources.getStringArray(R.array.days_of_week)[dateIndex]

            if (dateIndex == 5 || dateIndex == 6) {
                val color = context.getColor(R.color.onError)
                dayDate.setTextColor(color)
            }

            if (temperature == "celsius") {
                dayTemperature2mMax.text =  "${round(dailyWeather.dailyTemperatureMax[position]).toInt()}°"
                dayTemperature2mMin.text = "${round(dailyWeather.dailyTemperatureMin[position]).toInt()}°"
            } else {
                dayTemperature2mMax.text = "${((round(dailyWeather.dailyTemperatureMax[position]).toInt()) * 9/5) + 32}°"
                dayTemperature2mMin.text = "${((round(dailyWeather.dailyTemperatureMin[position]).toInt()) * 9/5) + 32}°"
            }
            dayWeatherImage.setImageResource(weatherData.dayImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyWeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day, parent, false)
        return DailyWeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyWeatherViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = dailyWeather.dailyTime.size
}