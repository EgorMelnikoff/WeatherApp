package com.egormelnikoff.myweather.view_model.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egormelnikoff.myweather.app.enums.SurfacePressure
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.enums.Theme
import com.egormelnikoff.myweather.app.enums.Wind
import com.egormelnikoff.myweather.app.preferences.AppSettings
import com.egormelnikoff.myweather.app.preferences.WeatherDataStore
import com.egormelnikoff.myweather.app.work.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val weatherDataStore: WeatherDataStore,
    private val workScheduler: WorkScheduler
) : ViewModel() {
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings

    init {
        collectSettings()
    }

    private fun collectSettings() {
        viewModelScope.launch {
            combine(
                weatherDataStore.theme,
                weatherDataStore.temperature,
                weatherDataStore.wind,
                weatherDataStore.surfacePressure,
                weatherDataStore.notifications
            ) { theme, temperature, wind, surfacePressure, notifications ->
                AppSettings(
                    theme = theme,
                    temperature = temperature,
                    wind = wind,
                    surfacePressure = surfacePressure,
                    notifications = notifications
                )
            }.collect { settings ->
                _appSettings.value = settings
            }
        }
    }


    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            weatherDataStore.setTheme(theme)
        }
    }

    fun setTemperature(temperature: Temperature) {
        viewModelScope.launch {
            weatherDataStore.setTemperature(temperature)
        }
    }

    fun setWind(wind: Wind) {
        viewModelScope.launch {
            weatherDataStore.setWind(wind)
        }
    }

    fun setSurfacePressure(surfacePressure: SurfacePressure) {
        viewModelScope.launch {
            weatherDataStore.setSurfacePressure(surfacePressure)
        }
    }

    fun setNotifications(sendNotifications: Boolean) {
        viewModelScope.launch {
            if (sendNotifications) workScheduler.startNotifyWork()
            else workScheduler.cancelNotifyWork()
            weatherDataStore.setNotifications(sendNotifications)
        }
    }
}
