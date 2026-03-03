package com.egormelnikoff.myweather.view_model.search

import com.egormelnikoff.myweather.app.entity.Place

sealed interface SearchState {
    data object EmptyQuery : SearchState
    data class Error(
        val message: String
    ) : SearchState

    data object Loading : SearchState
    data class Loaded(
        val places: List<Place>
    ) : SearchState

    data object EmptyResult : SearchState
}