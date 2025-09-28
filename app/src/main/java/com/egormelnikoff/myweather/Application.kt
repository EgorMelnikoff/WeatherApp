package com.egormelnikoff.myweather

import android.app.Application
import com.egormelnikoff.myweather.data.datasource.local.AppDatabase
import com.egormelnikoff.myweather.data.datasource.location.LocationHelper
import com.egormelnikoff.myweather.data.repos.WeatherRepository
import com.egormelnikoff.myweather.data.repos.datastore.WeatherDataStore
import com.egormelnikoff.myweather.data.repos.local.LocalRepos
import com.egormelnikoff.myweather.data.repos.remote.RemoteRepos
import com.google.android.gms.location.LocationServices

class WeatherApplication : Application() {
    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    val repository by lazy {
        WeatherRepository(
            localRepos = LocalRepos(AppDatabase.getDatabase(this).dao()),
            remoteRepos = RemoteRepos(),
            locationHelper = LocationHelper(locationClient, this)
        )
    }


    val dataStore by lazy {
        WeatherDataStore(this)
    }
}