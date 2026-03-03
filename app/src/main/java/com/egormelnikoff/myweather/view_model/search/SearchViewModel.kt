package com.egormelnikoff.myweather.view_model.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.repos.location.LocationRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationRepos: LocationRepos
) : ViewModel() {
    private val _stateSearch = MutableLiveData<SearchState>(SearchState.EmptyQuery)
    val stateSearch: LiveData<SearchState> = _stateSearch

    var searchJob: Job? = null

    fun searchPlaces(query: String) {
        _stateSearch.value = SearchState.Loading
        val newSearchJob = viewModelScope.launch {
            searchJob?.cancelAndJoin()
            when (val places = locationRepos.getPlacesByQuery(query)) {
                is Result.Error -> {
                    _stateSearch.value = SearchState.Error(
                        message = places.exception.message.toString()
                    )
                }

                is Result.Success -> {
                    if (places.data.isNotEmpty()) {
                        _stateSearch.value = SearchState.Loaded(
                            places = places.data
                        )
                    } else {
                        _stateSearch.value = SearchState.EmptyResult
                    }
                }
            }
        }
        searchJob = newSearchJob
    }

    fun cancelLoading() {
        searchJob?.cancel()
        _stateSearch.value = SearchState.EmptyQuery
    }


    fun setDefaultSearchState() {
        _stateSearch.value = SearchState.EmptyQuery
    }
}