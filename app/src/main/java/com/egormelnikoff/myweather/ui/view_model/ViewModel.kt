package com.egormelnikoff.myweather.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.egormelnikoff.myweather.WeatherApplication
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.data.repos.WeatherRepository
import com.egormelnikoff.myweather.data.repos.datastore.AppSettings
import com.egormelnikoff.myweather.data.repos.datastore.WeatherDataStore
import com.egormelnikoff.myweather.model.Place
import com.egormelnikoff.myweather.model.PlaceWeather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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

sealed interface CurrentWeatherState {
    data object NotSettled : CurrentWeatherState
    data class Error(
        val message: String?
    ) : CurrentWeatherState

    data object Loading : CurrentWeatherState
    data class Loaded(
        val placeWeather: PlaceWeather
    ) : CurrentWeatherState
}

data class PlacesWeatherState(
    val currentLocationWeather: PlaceWeather?,
    val placesWeather: MutableList<PlaceWeather>
)

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val dataStore: WeatherDataStore
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as WeatherApplication
                return WeatherViewModel(
                    repository = application.repository,
                    dataStore = application.dataStore
                ) as T
            }
        }
    }

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings

    private val _stateSearch = MutableLiveData<SearchState>(SearchState.EmptyQuery)
    val stateSearch: LiveData<SearchState> get() = _stateSearch

    private val _statePlaces = MutableLiveData(PlacesWeatherState(null, mutableListOf()))
    val statePlaces: LiveData<PlacesWeatherState> get() = _statePlaces

    private val _stateWeather = MutableLiveData<CurrentWeatherState>(CurrentWeatherState.NotSettled)
    val stateWeather: LiveData<CurrentWeatherState> get() = _stateWeather

    init {
        collectSettings()
        loadPlacesWeather()
    }


    private fun collectSettings() {
        viewModelScope.launch {
            combine(
                dataStore.theme,
                dataStore.isTemperatureIsCelsius,
                dataStore.isWindIsMetersPerSecond,
                dataStore.isSurfacePressureIsMmOfMercury,
                dataStore.notifications
            ) { theme, isCelsius, isMeters, isMmOfMercury, notifications ->
                AppSettings(
                    theme = theme,
                    temperature = isCelsius,
                    wind = isMeters,
                    surfacePressure = isMmOfMercury,
                    notifications = notifications
                )
            }.collect { settings ->
                _appSettings.value = settings
            }
        }
    }

    fun searchPlaces(query: String) {
        _stateSearch.value = SearchState.Loading
        viewModelScope.launch {
            when (val places = repository.getPlacesByQuery(query)) {
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
    }

    fun setDefaultSearchState() {
        _stateSearch.value = SearchState.EmptyQuery
    }


    private fun loadPlacesWeather() {
        viewModelScope.launch {
            _statePlaces.value = PlacesWeatherState(
                currentLocationWeather = repository.getCurrentLocationPlaceWeather(),
                placesWeather = repository.getSavedPlacesWeather()
            )
        }

    }

    fun fetchPlaceWeather(place: Place) {
        _stateWeather.value = CurrentWeatherState.Loading
        viewModelScope.launch {
            val placeWeather = PlaceWeather(
                id = 0,
                place = place,
                weather = null
            )
            val id = repository.insertPlaceWeather(placeWeather)
            when (val updatedPlaceWeather = repository.getWeather(id, false, place)) {
                is Result.Error -> {
                    repository.deletePlaceWeather(id)
                    _stateWeather.value = CurrentWeatherState.Error(
                        message = updatedPlaceWeather.exception.message.toString()
                    )
                }

                is Result.Success -> {
                    repository.updatePlaceWeather(id, updatedPlaceWeather.data.weather!!)
                    loadPlacesWeather()
                    setPlaceWeather(updatedPlaceWeather.data)
                }
            }

        }
    }

    fun setPlaceWeather(placeWeather: PlaceWeather?) {
        viewModelScope.launch {
            if (placeWeather != null) {
                _stateWeather.value = CurrentWeatherState.Loaded(
                    placeWeather
                )
            } else {
                _stateWeather.value = CurrentWeatherState.NotSettled
            }
        }
    }

    fun setCLPlaceWeather() {
        viewModelScope.launch {
            setPlaceWeather(_statePlaces.value?.currentLocationWeather)
        }
    }

    fun deletePlaceWeather(primaryKey: Long) {
        viewModelScope.launch {
            repository.deletePlaceWeather(primaryKey)
            loadPlacesWeather()
        }
    }

    fun updatePlacesWeather(onRefreshed: () -> Unit) {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            repository.updateSavedPlacesWeather(formatter, viewModelScope)
            repository.updateCurrentLocationPlaceWeather(formatter)
            onRefreshed()
            loadPlacesWeather()
        }
    }


    fun setTheme(theme: String) {
        viewModelScope.launch {
            dataStore.setTheme(theme)
        }
    }

    fun setTemperature(temperature: String) {
        viewModelScope.launch {
            dataStore.setTemperature(temperature)
        }
    }

    fun setWind(wind: String) {
        viewModelScope.launch {
            dataStore.setWind(wind)
        }
    }

    fun setSurfacePressure(surfacePressure: String) {
        viewModelScope.launch {
            dataStore.setSurfacePressure(surfacePressure)
        }
    }

    fun updateDefaultPlace(id: Long) {
        viewModelScope.launch {
            repository.setDefaultPlace(id)
            loadPlacesWeather()
        }
    }

    fun setNotifications(sendNotifications: Boolean) {
        viewModelScope.launch {
            dataStore.setNotifications(sendNotifications)
        }
    }
}


