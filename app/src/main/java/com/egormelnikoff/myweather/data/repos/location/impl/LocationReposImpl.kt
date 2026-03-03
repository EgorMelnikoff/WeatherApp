package com.egormelnikoff.myweather.data.repos.location.impl

import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.datasource.remote.NetworkHelper
import com.egormelnikoff.myweather.data.datasource.remote.api.ApiNominatim
import com.egormelnikoff.myweather.data.repos.location.LocationRepos
import javax.inject.Inject

class LocationReposImpl @Inject constructor(
    private val networkHelper: NetworkHelper,
    private val apiNominatim: ApiNominatim,
    private val language: String
) : LocationRepos {
    override suspend fun getPlacesByQuery(query: String): Result<List<Place>> {
        val places = networkHelper.callNetwork(
            requestType = "Placew",
            requestParams = "Query: $query"
        ) {
            apiNominatim.nominatimSearch(query, language)
        }

        return when (places) {
            is Result.Success -> {
                Result.Success(
                    places.data.distinctBy {
                        Pair(it.name, it.address)
                    }
                )
            }

            is Result.Error -> {
                places
            }
        }
    }
}