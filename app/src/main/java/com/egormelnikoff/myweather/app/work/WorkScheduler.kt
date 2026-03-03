package com.egormelnikoff.myweather.app.work

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    companion object {
        private const val UPDATING_WEATHER_PERIODICALLY = "updatingWeatherPeriodically"
        private const val UPDATING_WEATHER_INTERVAL = 30L //Minutes
        private const val SENDING_NOTIFY_PERIODICALLY = "sendingNotifyPeriodically"
        private const val NOTIFY_INTERVAL = 24L //Hours
    }

    fun startPeriodicWeatherUpdating() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val scheduleWorkRequest = PeriodicWorkRequestBuilder<UpdateWorker>(
            UPDATING_WEATHER_INTERVAL,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(UPDATING_WEATHER_PERIODICALLY)
            .build()

        workManager.enqueueUniquePeriodicWork(
            UPDATING_WEATHER_PERIODICALLY,
            ExistingPeriodicWorkPolicy.KEEP,
            scheduleWorkRequest
        )
    }

    fun cancelPeriodicScheduleUpdating() {
        workManager.cancelUniqueWork(UPDATING_WEATHER_PERIODICALLY)
    }

    fun startNotifyWork() {
        val currentTime = Calendar.getInstance()
        val dueTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (before(currentTime)) {
                add(Calendar.HOUR_OF_DAY, 24)
            }
        }

        val initialDelay = dueTime.timeInMillis - currentTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<NotifyWorker>(NOTIFY_INTERVAL, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(SENDING_NOTIFY_PERIODICALLY)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SENDING_NOTIFY_PERIODICALLY,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelNotifyWork() {
        workManager.cancelUniqueWork(SENDING_NOTIFY_PERIODICALLY)
        workManager.cancelAllWorkByTag(SENDING_NOTIFY_PERIODICALLY)
    }
}