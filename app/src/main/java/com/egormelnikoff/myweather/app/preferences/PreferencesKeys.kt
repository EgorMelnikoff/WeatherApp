package com.egormelnikoff.myweather.app.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKeys {
    val THEME = intPreferencesKey(name = "theme")
    val TEMPERATURE = intPreferencesKey(name = "temperature")
    val WIND = intPreferencesKey(name = "wind")
    val SURFACE_PRESSURE = intPreferencesKey(name = "surface_pressure")
    val NOTIFICATIONS = booleanPreferencesKey(name = "notifications")
}