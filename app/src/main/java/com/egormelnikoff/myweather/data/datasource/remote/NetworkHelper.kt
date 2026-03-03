package com.egormelnikoff.myweather.data.datasource.remote

import android.util.Log
import com.egormelnikoff.myweather.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class NetworkHelper @Inject constructor() {
    suspend fun <T : Any> callNetwork(
        requestType: String = "Unknown",
        requestParams: String? = null,
        retries: Int = 3,
        timeoutMs: Long = 5000,
        callApi: (suspend () -> Response<T>)
    ): Result<T> = withContext(Dispatchers.IO) {
        repeat(retries) { attempt ->
            try {
                return@withContext withTimeout(timeoutMs) {
                    executeApiCall(requestType, requestParams, callApi)
                }
            } catch (e: IOException) {
                logError(
                    message = "Network error (${attempt + 1}/$retries)",
                    requestType = requestType,
                    requestParams = requestParams,
                    e = e
                )
                if (attempt == retries - 1) {
                    return@withContext Result.Error(e)
                }
                delay(1000)
            } catch (e: TimeoutCancellationException) {
                logError(
                    message = "Timeout error (${attempt + 1}/$retries)",
                    requestType = requestType,
                    requestParams = requestParams,
                    e = e
                )
                if (attempt == retries - 1) {
                    return@withContext Result.Error(e)
                }
                delay(1000)
            }
        }
        return@withContext Result.Error(Exception("Unknown error"))
    }

    private suspend fun <T : Any> executeApiCall(
        requestType: String, requestParams: String? = null, call: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = call()

            if (response.isSuccessful) {
                response.body()?.let {
                    logInfo(
                        message = "Success fetched data",
                        requestType = requestType,
                        requestParams = requestParams
                    )

                    Result.Success(it)
                } ?: run {
                    logInfo(
                        message = "Empty body",
                        requestType = requestType,
                        requestParams = requestParams
                    )
                    Result.Error(NoSuchElementException())
                }
            } else {
                logError(
                    message = "Http error: ${response.code()}, ${response.message()}",
                    requestType = requestType,
                    requestParams = requestParams
                )
                Result.Error(
                    HttpException(response)
                )
            }
        } catch (e: SerializationException) {
            logError(
                message = "Serialization error",
                requestType = requestType,
                requestParams = requestParams,
                e = e
            )
            Result.Error(e)
        }
    }

    private fun logError(
        message: String,
        requestType: String,
        requestParams: String?,
        e: Throwable? = null,
    ) {
        requestParams?.let { r ->
            Log.e(requestType, "$r\n$message", e)
        }
            ?: Log.e(requestType, message, e)
    }

    private fun logInfo(
        message: String, requestType: String, requestParams: String?
    ) {
        requestParams?.let { r -> Log.i(requestType, "$r\n$message") } ?: Log.i(
            requestType,
            message
        )
    }
}