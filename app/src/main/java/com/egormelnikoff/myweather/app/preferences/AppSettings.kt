package com.egormelnikoff.myweather.app.preferences

import com.egormelnikoff.myweather.app.enums.SurfacePressure
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.enums.Theme
import com.egormelnikoff.myweather.app.enums.Wind

data class AppSettings(
    val theme: Theme = Theme.SYSTEM,
    val temperature: Temperature = Temperature.CELSIUS,
    val wind: Wind = Wind.METERS_PER_SECOND,
    val surfacePressure: SurfacePressure = SurfacePressure.MM_OF_MERCURY,
    val notifications: Boolean = false
)