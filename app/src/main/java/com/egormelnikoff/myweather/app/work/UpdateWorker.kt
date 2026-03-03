package com.egormelnikoff.myweather.app.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.egormelnikoff.myweather.domain.UpdateWeatherUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateWeatherUseCase: UpdateWeatherUseCase
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        Log.i("UpdateWorker", "Start update")
        when (val result = updateWeatherUseCase()) {
            is com.egormelnikoff.myweather.data.Result.Error -> {
                Log.e("UpdateWorker", "Error update", result.exception)
                return@coroutineScope Result.retry()
            }

            is com.egormelnikoff.myweather.data.Result.Success -> {
                Log.i("UpdateWorker", "Success update")
                return@coroutineScope Result.success()
            }
        }
    }
}