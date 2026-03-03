package com.egormelnikoff.myweather.app.di

import com.egormelnikoff.myweather.data.repos.location.LocationRepos
import com.egormelnikoff.myweather.data.repos.location.impl.LocationReposImpl
import com.egormelnikoff.myweather.data.repos.weather.WeatherRepos
import com.egormelnikoff.myweather.data.repos.weather.impl.WeatherReposImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {
    @Binds
    @Singleton
    abstract fun bindLocationRepos(locationReposImpl: LocationReposImpl): LocationRepos

    @Binds
    @Singleton
    abstract fun bindWeatherRepos(weatherReposImpl: WeatherReposImpl): WeatherRepos
}
