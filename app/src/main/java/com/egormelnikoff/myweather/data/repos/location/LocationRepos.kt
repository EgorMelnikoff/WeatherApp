package com.egormelnikoff.myweather.data.repos.location

import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.data.Result

interface LocationRepos {
    suspend fun getPlacesByQuery(query: String): Result<List<Place>>
}