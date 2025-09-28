package com.egormelnikoff.myweather.data.repos.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Settings")

data class AppSettings(
    val theme: String? = null,
    val temperature: String? = null,
    val wind: String? = null,
    val surfacePressure: String? = null,
    val notifications: Boolean? = null
)

object PreferencesKeys {
    val THEME = stringPreferencesKey(name = "theme")
    val TEMPERATURE = stringPreferencesKey(name = "temperature")
    val WIND = stringPreferencesKey(name = "wind")
    val SURFACE_PRESSURE = stringPreferencesKey(name = "surface_pressure")
    val NOTIFICATIONS = booleanPreferencesKey(name = "notifications")
}

class WeatherDataStore(
    private val context: Context
) {
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun setTemperature(temperature: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE] = temperature
        }
    }

    suspend fun setWind(wind: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIND] = wind
        }
    }

    suspend fun setSurfacePressure(surfacePressure: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SURFACE_PRESSURE] = surfacePressure
        }
    }

    suspend fun setNotifications(notifications: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] = notifications
        }
    }

    val theme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME] ?: "system"
    }

    val isTemperatureIsCelsius: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TEMPERATURE] ?: "celsius"
    }

    val isWindIsMetersPerSecond: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIND] ?: "meters_per_second"
    }

    val isSurfacePressureIsMmOfMercury: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SURFACE_PRESSURE] ?: "mm_of_mercury"
    }

    val notifications: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS] ?: false
    }
}