package com.egormelnikoff.myweather.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egormelnikoff.myweather.R
import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.model.WeatherCodes
import kotlin.math.round

class SavedPlacesAdapter(
    private val weatherCodes: WeatherCodes,
    private val onClick: (PlaceWeather) -> Unit
) : ListAdapter<PlaceWeather, SavedPlacesAdapter.SavedPlacesViewHolder>(DiffCallback) {
    private var currentTemperature: Temperature = Temperature.CELSIUS

    object DiffCallback : DiffUtil.ItemCallback<PlaceWeather>() {
        override fun areItemsTheSame(oldItem: PlaceWeather, newItem: PlaceWeather) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PlaceWeather, newItem: PlaceWeather) =
            oldItem == newItem
    }

    inner class SavedPlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeWeatherImage: ImageView = itemView.findViewById(R.id.weather_image)
        private val placeName: TextView = itemView.findViewById(R.id.name)
        private val placeSituation: TextView = itemView.findViewById(R.id.situation)
        private val placeTemperature2m: TextView = itemView.findViewById(R.id.temperature)

        @SuppressLint("SetTextI18n")
        fun bind(placeWeather: PlaceWeather, onClick: (PlaceWeather) -> Unit) {
            itemView.setOnClickListener {
                onClick(placeWeather)
            }
            val weatherData = weatherCodes.getWeatherDataByWeatherCode(
                placeWeather.weather.current.weatherCode
            )

            placeName.text = placeWeather.place.name

            placeTemperature2m.text = when (currentTemperature) {
                Temperature.CELSIUS -> "${placeWeather.weather.current.temperature.let { round(it).toInt() }}°"
                Temperature.FAHRENHEIT -> "${placeWeather.weather.current.temperature.let { round((it * 9 / 5) + 32).toInt() }}°"
            }

            placeSituation.text = weatherData.title

            if (placeWeather.weather.current.isDay == 1) {
                itemView.setBackgroundResource(weatherData.dayBackground)
                placeWeatherImage.setImageResource(weatherData.dayImage)
            } else {
                itemView.setBackgroundResource(weatherData.nightBackground)
                placeWeatherImage.setImageResource(
                    weatherData.nightImage ?: weatherData.dayImage
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPlacesViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_place_list,
                parent,
                false
            )
        return SavedPlacesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedPlacesViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    fun updateTemperatureUnit(newUnit: Temperature) {
        if (currentTemperature != newUnit) {
            currentTemperature = newUnit
            notifyDataSetChanged()
        }
    }
}