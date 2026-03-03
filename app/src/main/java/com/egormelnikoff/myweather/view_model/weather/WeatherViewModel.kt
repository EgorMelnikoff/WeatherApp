package com.egormelnikoff.myweather.view_model.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.repos.weather.WeatherRepos
import com.egormelnikoff.myweather.domain.FetchWeatherUseCase
import com.egormelnikoff.myweather.domain.UpdateWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepos: WeatherRepos,
    private val fetchWeatherUseCase: FetchWeatherUseCase,
    private val updateWeatherUseCase: UpdateWeatherUseCase
) : ViewModel() {
    private val _statePlaces = MutableLiveData(PlacesWeatherState())
    val statePlaces: LiveData<PlacesWeatherState> = _statePlaces

    private val _stateWeather = MutableLiveData<CurrentWeatherState>(CurrentWeatherState.NotSettled)
    val stateWeather: LiveData<CurrentWeatherState> = _stateWeather

    init {
        loadPlacesWeather()
    }

    private fun loadPlacesWeather() {
        viewModelScope.launch {
            _statePlaces.value = PlacesWeatherState(
                placesWeather = weatherRepos.getSavedPlacesWeather()
            )
        }
    }

    fun fetchPlaceWeather(place: Place) {
        viewModelScope.launch {
            _stateWeather.value = CurrentWeatherState.Loading
            when (val result = fetchWeatherUseCase(place = place)) {
                is Result.Error -> {
                    _stateWeather.value = CurrentWeatherState.Error(
                        message = result.exception.message.toString()
                    )
                }

                is Result.Success -> {
                    loadPlacesWeather()
                    setPlaceWeather(result.data)
                }
            }

        }
    }

    fun setPlaceWeather(placeWeather: PlaceWeather?) {
        viewModelScope.launch {
            placeWeather?.let {
                _stateWeather.value = CurrentWeatherState.Loaded(
                    placeWeather
                )
                return@launch
            }

            _stateWeather.value = CurrentWeatherState.NotSettled

        }
    }

    fun deletePlaceWeather(primaryKey: Long) {
        viewModelScope.launch {
            weatherRepos.deletePlaceWeather(primaryKey)
            loadPlacesWeather()
        }
    }

    fun updatePlacesWeather(onRefreshed: () -> Unit) {
        viewModelScope.launch {
            updateWeatherUseCase()
            onRefreshed()
            loadPlacesWeather()
        }
    }

    fun updateDefaultPlace(id: Long) {
        viewModelScope.launch {
            weatherRepos.setDefaultPlace(id)
            loadPlacesWeather()
        }
    }
}


