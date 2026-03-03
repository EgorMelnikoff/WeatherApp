package com.egormelnikoff.myweather.app.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.egormelnikoff.myweather.app.enums.SurfacePressure
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.enums.Theme
import com.egormelnikoff.myweather.app.enums.Wind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Settings")

class WeatherDataStore @Inject constructor(
    private val context: Context
) {
    suspend fun setTheme(theme: Theme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.ordinal
        }
    }

    suspend fun setTemperature(temperature: Temperature) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE] = temperature.ordinal
        }
    }

    suspend fun setWind(wind: Wind) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIND] = wind.ordinal
        }
    }

    suspend fun setSurfacePressure(surfacePressure: SurfacePressure) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SURFACE_PRESSURE] = surfacePressure.ordinal
        }
    }

    suspend fun setNotifications(notifications: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] = notifications
        }
    }

    val theme: Flow<Theme> = context.dataStore.data.map { preferences ->
        Theme.entries.find { it.ordinal == preferences[PreferencesKeys.THEME] } ?: Theme.SYSTEM
    }

    val temperature: Flow<Temperature> = context.dataStore.data.map { preferences ->
        Temperature.entries.find { it.ordinal == preferences[PreferencesKeys.TEMPERATURE] }
            ?: Temperature.CELSIUS
    }

    val wind: Flow<Wind> = context.dataStore.data.map { preferences ->
        Wind.entries.find { it.ordinal == preferences[PreferencesKeys.WIND] } ?: Wind.MILES_PER_HOUR
    }

    val surfacePressure: Flow<SurfacePressure> = context.dataStore.data.map { preferences ->
        SurfacePressure.entries.find { it.ordinal == preferences[PreferencesKeys.SURFACE_PRESSURE] }
            ?: SurfacePressure.MM_OF_MERCURY

    }

    val notifications: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS] ?: false
    }
}