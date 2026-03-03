package com.egormelnikoff.myweather.app.di

import android.content.Context
import androidx.work.WorkManager
import com.egormelnikoff.myweather.app.model.WeatherCodes
import com.egormelnikoff.myweather.app.preferences.WeatherDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideWeatherDataStore(@ApplicationContext context: Context): WeatherDataStore {
        return WeatherDataStore(context)
    }

    @Provides
    @Singleton
    fun provideWeatherCodes(@ApplicationContext context: Context): WeatherCodes {
        return WeatherCodes(context)
    }

    @Provides
    @Singleton
    fun provideLanguage(): String {
        return Locale.getDefault().language
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(
                LocalDateTime::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_DATE_TIME)
                }
            ).registerTypeAdapter(
                LocalDateTime::class.java,
                JsonSerializer<LocalDateTime> { src, _, _ ->
                    JsonPrimitive(DateTimeFormatter.ISO_DATE_TIME.format(src))
                }
            ).registerTypeAdapter(
                LocalDate::class.java,
                JsonDeserializer { json, _, _ ->
                    LocalDate.parse(json.asString, DateTimeFormatter.ISO_DATE)
                }
            )
            .registerTypeAdapter(
                LocalDate::class.java,
                JsonSerializer<LocalDate> { src, _, _ ->
                    JsonPrimitive(DateTimeFormatter.ISO_DATE.format(src))
                }
            ).create()
    }
}