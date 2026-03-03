package com.egormelnikoff.myweather.app.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.egormelnikoff.myweather.MainActivity
import com.egormelnikoff.myweather.R
import com.egormelnikoff.myweather.app.model.WeatherCodes
import com.egormelnikoff.myweather.data.repos.weather.WeatherRepos
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.math.round

@HiltWorker
class NotifyWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherCodes: WeatherCodes,
    private val weatherRepos: WeatherRepos
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        sendNotify()
        return Result.success()
    }

    private suspend fun sendNotify() {
        weatherRepos.getDefaultPlaceWeather()?.let { placeWeather ->
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

            val dailyWeather = placeWeather.weather.daily
            val currentWeatherParams =
                weatherCodes.getWeatherDataByWeatherCode(dailyWeather.dailyWeatherCode[1])
            val title =
                "${round(dailyWeather.dailyTemperatureMax[1]).toInt()}°/${round(dailyWeather.dailyTemperatureMin[1]).toInt()}° · ${currentWeatherParams!!.title}"
            val builder = NotificationCompat.Builder(applicationContext, "daily_notify")
                .setSmallIcon(R.drawable.notifications)
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
}