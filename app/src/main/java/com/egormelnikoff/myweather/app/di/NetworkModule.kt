package com.egormelnikoff.myweather.app.di

import com.egormelnikoff.myweather.data.datasource.remote.api.ApiNominatim
import com.egormelnikoff.myweather.data.datasource.remote.api.ApiOpenMeteo
import com.egormelnikoff.myweather.data.datasource.remote.api.ApiRoutes.API_NOMINATIM_BASE
import com.egormelnikoff.myweather.data.datasource.remote.api.ApiRoutes.API_OPEN_METEO_BASE
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


@Module
@InstallIn(SingletonComponent::class)
object OkHttpClient {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NominatimApi {
    @Provides
    fun provideApiNominatim(okHttpClient: OkHttpClient): ApiNominatim {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_NOMINATIM_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiNominatim::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object OpenMeteoApi {
    @Provides
    fun provideApiOpenMeteo(okHttpClient: OkHttpClient, gson: Gson): ApiOpenMeteo {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_OPEN_METEO_BASE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return retrofit.create(ApiOpenMeteo::class.java)
    }
}