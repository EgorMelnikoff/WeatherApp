package com.egormelnikoff.myweather.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.egormelnikoff.myweather.R
import com.egormelnikoff.myweather.model.PlaceWeather
import com.egormelnikoff.myweather.model.WeatherCodes
import com.egormelnikoff.myweather.ui.view_model.WeatherViewModel
import kotlin.math.round


class SavedPlacesAdapter(
    private val weatherCodes: WeatherCodes,
    private val placesWeather: MutableList<PlaceWeather>,
    private val temperature: String?,
    private val onClick: (PlaceWeather) -> Unit
) : RecyclerView.Adapter<SavedPlacesAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeWeatherImage: ImageView = itemView.findViewById(R.id.weather_image)
        private val placeName: TextView = itemView.findViewById(R.id.name)
        private val placeSituation: TextView = itemView.findViewById(R.id.situation)
        private val placeTemperature2m: TextView = itemView.findViewById(R.id.temperature)

        @SuppressLint("SetTextI18n")
        fun bind(placeWeather: PlaceWeather, onClick: (PlaceWeather) -> Unit) {
            itemView.setOnClickListener {
                onClick(placeWeather)
            }
            val weatherData = weatherCodes.getWeatherDataByWeatherCode(placeWeather.weather!!.current.weatherCode)

            placeName.text = placeWeather.place.name

            placeTemperature2m.text = if (temperature == "celsius") {
                "${placeWeather.weather?.current?.temperature?.let { round(it).toInt() } ?: "-"}°"
            } else {
                "${placeWeather.weather?.current?.temperature?.let { round((it * 9 / 5) + 32).toInt() } ?: "-"}°"
            }
            placeSituation.text = weatherData?.title ?: "Error"
            if (weatherData != null) {
                if (placeWeather.weather?.current?.isDay == 1) {
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_place_list, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(placesWeather[position], onClick)
    }

    override fun getItemCount(): Int = placesWeather.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList (newPlacesWeathers: MutableList<PlaceWeather>) {
        placesWeather.clear()
        placesWeather.addAll(newPlacesWeathers)
        notifyDataSetChanged()
    }


    fun deleteItem(position: Int, viewModel: WeatherViewModel) {
        viewModel.deletePlaceWeather(placesWeather[position].id)
    }
}

class MyItemTouchHelperCallback(
    private val adapter: SavedPlacesAdapter,
    private val viewModel: WeatherViewModel,
    private val context: Context
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.deleteItem(viewHolder.adapterPosition, viewModel)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val paint = Paint().apply { color = context.getColor(R.color.error) }
            val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.delete)

            val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + icon.intrinsicHeight

            if (dX > 0) {
                val background = RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    dX,
                    itemView.bottom.toFloat()
                )
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + icon.intrinsicWidth
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                c.drawRect(background, paint)
                icon.draw(c)
            } else if (dX < 0) {
                val background = RectF(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                val iconLeft = itemView.right - icon.intrinsicWidth - iconMargin
                val iconRight = itemView.right - iconMargin
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                c.drawRect(background, paint)
                icon.draw(c)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}