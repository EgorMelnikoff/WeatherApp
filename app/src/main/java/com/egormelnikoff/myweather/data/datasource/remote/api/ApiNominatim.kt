package com.egormelnikoff.myweather.data.datasource.remote.api

import com.egormelnikoff.myweather.app.entity.Place
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiNominatim {
    @GET("search?&format=json&limit=10&addressdetails=1&featureType=settlement&layer=address")
    suspend fun nominatimSearch(
        @Query("q") q: String,
        @Query("accept-language") language: String
    ): Response<List<Place>>

    @GET("reverse?format=json&zoom=5")
    suspend fun nominatimReverse(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("accept-language") language: String
    ): Response<Place>
}