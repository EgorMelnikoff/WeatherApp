package com.egormelnikoff.myweather.data.datasource.remote

import android.location.Location
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.API_NOMINATIM_BASE
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.API_OPEN_METEO_BASE
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.NOMINATIM_QUERY
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.NOMINATIM_REVERSE_LAT
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.NOMINATIM_REVERSE_LON
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.OPEN_METEO_LAT
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.OPEN_METEO_LON
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.OPEN_METEO_PARAMS
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.nominatimReverse
import com.egormelnikoff.myweather.data.datasource.remote.ApiRoutes.nominatimSearch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

interface ApiInterface {
    fun makeUrlOpenMeteo(lat: Double?, lon: Double?): URL
    fun makeUrlNominatimReverse(location: Location): URL
    fun makeUrlNominatimSearch(name: String): URL
    suspend fun getData(url: URL): Result<String>

}

class Api : ApiInterface {
    val gson = Gson()

    override fun makeUrlOpenMeteo(lat: Double?, lon: Double?): URL {
        return (URL(API_OPEN_METEO_BASE + OPEN_METEO_LAT + lat.toString() + OPEN_METEO_LON + lon.toString() + OPEN_METEO_PARAMS))
    }

    override fun makeUrlNominatimReverse(location: Location): URL {
        return (URL(API_NOMINATIM_BASE + nominatimReverse + NOMINATIM_REVERSE_LAT + location.latitude.toString() + NOMINATIM_REVERSE_LON + location.longitude.toString()))
    }

    override fun makeUrlNominatimSearch(name: String): URL {
        return (URL(API_NOMINATIM_BASE + nominatimSearch + NOMINATIM_QUERY + name))
    }

    override suspend fun getData(url: URL): Result<String> {
        println(url)
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            return@withContext try {
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream.bufferedReader().use {
                        it.readText()
                    }
                    Result.Success(inputStream)
                } else {
                    Result.Error(
                        Exception(
                            "Error response code: ${connection.responseCode}"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.Error(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    inline fun <reified T> parseJson(jsonString: Result<String?>): Result<T> {
        return try {
            when (jsonString) {
                is Result.Success -> {
                    if (!jsonString.data.isNullOrEmpty()) {
                        val type = object : TypeToken<T>() {}.type
                        Result.Success(gson.fromJson(jsonString.data, type))
                    } else {
                        Result.Error(Exception("Empty JSON"))
                    }
                }

                is Result.Error -> {
                    Result.Error(jsonString.exception)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }
}