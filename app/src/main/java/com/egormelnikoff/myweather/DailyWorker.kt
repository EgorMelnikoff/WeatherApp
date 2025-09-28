package com.egormelnikoff.myweather

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.egormelnikoff.myweather.model.PlaceWeather
import com.egormelnikoff.myweather.model.WeatherParams
import kotlin.math.round

class DailyWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val weatherApplication = (applicationContext as WeatherApplication)

    override suspend fun doWork(): Result {
        weatherApplication.dataStore.notifications.collect { notifications ->
            if (notifications) {
                sendNotify()
            }
        }
        return Result.success()
    }

    private suspend fun sendNotify() {
        val placeWeather = getDefaultPlaceWeather()
        if (placeWeather != null) {
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, Intent(applicationContext, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

            val channel = NotificationChannel(
                "daily_notify",
                "Ежедневные уведомления",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)

            val dailyWeather = placeWeather.weather!!.daily
            val currentWeatherParams =
                WeatherParams(applicationContext).weathersMap[dailyWeather.dailyWeatherCode[1]]
            val title = "${round(dailyWeather.dailyTemperatureMax[1]).toInt()}°/${round(dailyWeather.dailyTemperatureMin[1]).toInt()}° · ${currentWeatherParams!!.title}"
            val builder = NotificationCompat.Builder(applicationContext, "daily_notify")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setContentTitle(placeWeather.place.name)
                .setContentText(title)
                .setStyle(
                    NotificationCompat.InboxStyle()
                        .setBigContentTitle("${placeWeather.place.name}, завтра")
                        .addLine(title)

                )
                .setDefaults(Notification.DEFAULT_SOUND and Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(101, builder.build())
        }
    }

    private suspend fun getDefaultPlaceWeather(): PlaceWeather? {
        val defaultPlaceWeather = weatherApplication.repository.getDefaultPlaceWeather()
        if (defaultPlaceWeather != null) {
            val updatedPlaceWeather =
                weatherApplication.repository.getWeather(id = 0, isCurrentLocation = defaultPlaceWeather.isCurrentLocation, place = defaultPlaceWeather.place)
            if (updatedPlaceWeather is com.egormelnikoff.myweather.data.Result.Success) {
                return updatedPlaceWeather.data
            }
        }
        return null
    }
}