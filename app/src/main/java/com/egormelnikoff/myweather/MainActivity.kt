package com.egormelnikoff.myweather

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.egormelnikoff.myweather.data.repos.datastore.AppSettings
import com.egormelnikoff.myweather.databinding.ActivityMainBinding
import com.egormelnikoff.myweather.model.Place
import com.egormelnikoff.myweather.model.PlaceWeather
import com.egormelnikoff.myweather.model.Weather
import com.egormelnikoff.myweather.model.WeatherCodes
import com.egormelnikoff.myweather.ui.adapter.CustomItemDecoration
import com.egormelnikoff.myweather.ui.adapter.DailyAdapter
import com.egormelnikoff.myweather.ui.adapter.HourlyAdapter
import com.egormelnikoff.myweather.ui.adapter.MyItemTouchHelperCallback
import com.egormelnikoff.myweather.ui.adapter.SavedPlacesAdapter
import com.egormelnikoff.myweather.ui.fragment.MyBottomSheetFragment
import com.egormelnikoff.myweather.ui.view_model.CurrentWeatherState
import com.egormelnikoff.myweather.ui.view_model.SearchState
import com.egormelnikoff.myweather.ui.view_model.WeatherViewModel
import kotlinx.coroutines.launch
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    private lateinit var weatherCodes: WeatherCodes
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels { WeatherViewModel.Factory }

    private lateinit var requestLocationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var savedPlacesAdapter: SavedPlacesAdapter
    private var placesWeather = mutableListOf<PlaceWeather>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        weatherCodes = WeatherCodes(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //scheduleDailyWork()
        setupViews()
        setupListeners()

        viewModel.updatePlacesWeather(
            onRefreshed = {
                binding.placesListScreen.swipeRefreshLayout.isRefreshing = false
                binding.placeWeatherScreen.swipeRefreshLayout.isRefreshing = false
            }
        )
        lifecycleScope.launch {
            viewModel.appSettings.collect { settings ->
                setupAdapters(settings, weatherCodes)
                setupObservers(settings)
            }
        }
    }

    private fun setupViews() {
        binding.placesListScreen.swipeRefreshLayout.isRefreshing = true
        binding.placeWeatherScreen.swipeRefreshLayout.isRefreshing = true

        if (checkLocationPermission()) {
            binding.placesListScreen.currentLocationMessage.root.visibility = View.GONE
            binding.placesListScreen.currentLocationWeather.root.visibility = View.VISIBLE
        } else {
            binding.placesListScreen.currentLocationWeather.root.visibility = View.GONE
            binding.placesListScreen.currentLocationMessage.root.visibility = View.VISIBLE
        }

        binding.placeWeatherScreen.toolbar.isSubtitleCentered = true

        binding.placeWeatherScreen.hourlyRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.placeWeatherScreen.dailyRecyclerView.layoutManager = LinearLayoutManager(this)
        val hourlyItemDecoration = CustomItemDecoration(this, 4, 16, 16)
        val dailyItemDecoration = CustomItemDecoration(this, 0, 16, 16)

        binding.placeWeatherScreen.hourlyRecyclerView.addItemDecoration(hourlyItemDecoration)
        binding.placeWeatherScreen.dailyRecyclerView.addItemDecoration(dailyItemDecoration)

        binding.placeWeatherScreen.wind.header.text = getString(R.string.wind)
        binding.placeWeatherScreen.wind.header.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.wind,
            0,
            0,
            0
        )

        binding.placeWeatherScreen.surfacePressure.header.text = getString(R.string.pressure)
        binding.placeWeatherScreen.surfacePressure.header.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.pressure,
            0,
            0,
            0
        )

        binding.placeWeatherScreen.relativeHumidity.header.text = getString(R.string.humidity)
        binding.placeWeatherScreen.relativeHumidity.header.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.humidity,
            0,
            0,
            0
        )

        val text =
            "<a href=\"https://open-meteo.com/\">Open Meteo</a> · <a href=\"https://nominatim.org/\">Nominatim</a>\n"
        binding.placeWeatherScreen.weatherLink.movementMethod = LinkMovementMethod.getInstance()
        binding.placeWeatherScreen.weatherLink.text =
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)


        requestLocationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.entries.any { it.value }
            if (granted) {
                binding.placesListScreen.currentLocationMessage.root.visibility = View.GONE
                binding.placesListScreen.currentLocationWeather.root.visibility = View.VISIBLE
                Toast.makeText(this, "Разрешение предоставлено", Toast.LENGTH_SHORT).show()
            } else {
                binding.placesListScreen.currentLocationWeather.root.visibility = View.GONE
                binding.placesListScreen.currentLocationMessage.root.visibility = View.VISIBLE
                Toast.makeText(this, "Разрешение отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.placeWeatherScreen.weatherLink) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }

        binding.placesListScreen.toolbar.setNavigationOnClickListener {
            binding.placesListScreen.searchView.show()
        }

        binding.placesListScreen.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings_button -> {
                    openScreen(binding.settingsScreen.root)
                    true
                }

                else -> false
            }

        }

        binding.placesListScreen.searchView.editText.setOnEditorActionListener { _, _, _ ->
            val query = binding.placesListScreen.searchView.text.toString().trim()
            viewModel.searchPlaces(query)
            binding.placesListScreen.searchView.clearFocusAndHideKeyboard()
            return@setOnEditorActionListener false
        }

        binding.placesListScreen.swipeRefreshLayout.setOnRefreshListener {
            viewModel.updatePlacesWeather(
                onRefreshed = {
                    binding.placesListScreen.swipeRefreshLayout.isRefreshing = false
                }
            )
        }

        binding.placesListScreen.currentLocationMessage.root.setOnClickListener {
            requestLocationPermission()
        }

        binding.placesListScreen.currentLocationWeather.root.setOnClickListener {
            openScreen(binding.placeWeatherScreen.root)
            viewModel.setCLPlaceWeather()
        }


        binding.placeWeatherScreen.toolbar.setNavigationOnClickListener {
            closeScreen(binding.placeWeatherScreen.root)
        }

        binding.placeWeatherScreen.swipeRefreshLayout.setOnRefreshListener {
            viewModel.updatePlacesWeather(
                onRefreshed = {
                    binding.placeWeatherScreen.swipeRefreshLayout.isRefreshing = false
                }
            )
        }


        binding.settingsScreen.toolbar.setNavigationOnClickListener {
            closeScreen(binding.settingsScreen.root)
        }

        binding.settingsScreen.themePreference.onValueChangeListener = { newValue ->
            lifecycleScope.launch {
                viewModel.setTheme(newValue)
            }
        }
        binding.settingsScreen.temperaturePreference.onValueChangeListener = { newValue ->
            lifecycleScope.launch {
                viewModel.setTemperature(newValue)
            }
        }
        binding.settingsScreen.windPreference.onValueChangeListener = { newValue ->
            lifecycleScope.launch {
                viewModel.setWind(newValue)
            }
        }
        binding.settingsScreen.pressurePreference.onValueChangeListener = { newValue ->
            lifecycleScope.launch {
                viewModel.setSurfacePressure(newValue)
            }
        }
        binding.settingsScreen.notificationsPreference.onValueChangeListener = { newValue ->
            lifecycleScope.launch {
                viewModel.setNotifications(newValue)
            }
        }
    }

    private fun setupAdapters(
        appSettings: AppSettings,
        weatherCodes: WeatherCodes
    ) {
        binding.settingsScreen.themePreference.selectedValue = appSettings.theme
        binding.settingsScreen.temperaturePreference.selectedValue = appSettings.temperature
        binding.settingsScreen.windPreference.selectedValue = appSettings.wind
        binding.settingsScreen.pressurePreference.selectedValue = appSettings.surfacePressure
        binding.settingsScreen.notificationsPreference.selectedValue = appSettings.notifications

        savedPlacesAdapter = SavedPlacesAdapter(
            weatherCodes = weatherCodes,
            onClick = { placeWeather ->
                viewModel.setPlaceWeather(placeWeather)
                binding.placeWeatherScreen.toolbar.title = placeWeather.place.name
                openScreen(binding.placeWeatherScreen.root)
            },
            placesWeather = placesWeather,
            temperature = appSettings.temperature
        )

        binding.placesListScreen.savedPlaces.adapter = savedPlacesAdapter
        binding.placesListScreen.savedPlaces.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper =
            ItemTouchHelper(
                MyItemTouchHelperCallback(
                    savedPlacesAdapter,
                    viewModel,
                    this
                )
            )
        itemTouchHelper.attachToRecyclerView(binding.placesListScreen.savedPlaces)
    }

    private fun setupObservers(appSettings: AppSettings) {
        viewModel.stateSearch.observe(this) { stateSearch ->
            when (stateSearch) {
                is SearchState.EmptyQuery -> {
                    binding.placesListScreen.searchPlaces.visibility = View.GONE
                    binding.placesListScreen.searchLoadingIndicator.visibility = View.GONE
                    binding.placesListScreen.searchMessageIcon.setImageResource(R.drawable.search)
                    binding.placesListScreen.searchMessageText.text = "Введите запрос"
                    binding.placesListScreen.searchMessageIcon.visibility = View.VISIBLE
                    binding.placesListScreen.searchMessage.visibility = View.VISIBLE
                }

                is SearchState.Loading -> {
                    binding.placesListScreen.searchMessageIcon.visibility = View.GONE
                    binding.placesListScreen.searchMessage.visibility = View.GONE
                    binding.placesListScreen.searchPlaces.visibility = View.GONE
                    binding.placesListScreen.searchLoadingIndicator.visibility = View.VISIBLE
                }

                is SearchState.EmptyResult -> {
                    binding.placesListScreen.searchPlaces.visibility = View.GONE
                    binding.placesListScreen.searchLoadingIndicator.visibility = View.GONE
                    binding.placesListScreen.searchMessageIcon.visibility = View.GONE
                    binding.placesListScreen.searchMessageText.text = "Ничего не найдено"
                    binding.placesListScreen.searchMessage.visibility = View.VISIBLE
                }

                is SearchState.Error -> {
                    binding.placesListScreen.searchPlaces.visibility = View.GONE
                    binding.placesListScreen.searchLoadingIndicator.visibility = View.GONE
                    binding.placesListScreen.searchMessageIcon.visibility = View.GONE
                    binding.placesListScreen.searchMessageText.text =
                        StringBuilder("Ошибка\n${stateSearch.message}")
                    binding.placesListScreen.searchMessage.visibility = View.VISIBLE
                }

                is SearchState.Loaded -> {
                    binding.placesListScreen.searchMessage.visibility = View.GONE
                    binding.placesListScreen.searchLoadingIndicator.visibility = View.GONE

                    val formattedPlaces = stateSearch.places.map { place ->
                        if (place.name == place.address.state || place.address.state == null) {
                            place.name
                        } else {
                            "${place.name}, ${place.address.state}"
                        }
                    }

                    val adapter =
                        ArrayAdapter(this, android.R.layout.simple_list_item_1, formattedPlaces)

                    binding.placesListScreen.searchPlaces.setOnItemClickListener { _, _, position, _ ->
                        val place = stateSearch.places[position]
                        viewModel.fetchPlaceWeather(place)
                        binding.placeWeatherScreen.toolbar.title = place.name
                        openScreen(binding.placeWeatherScreen.root)

                        viewModel.setDefaultSearchState()
                        binding.placesListScreen.searchView.editText.text.clear()
                        binding.placesListScreen.searchView.hide()
                    }

                    binding.placesListScreen.searchPlaces.adapter = adapter
                    binding.placesListScreen.searchPlaces.visibility = View.VISIBLE
                }

            }
        }

        viewModel.statePlaces.observe(this) { statePlaces ->
            if (statePlaces.currentLocationWeather != null) {
                outputCurrentLocationWeather(
                    appSettings.temperature,
                    statePlaces.currentLocationWeather,
                    weatherCodes
                )
            }

            val defaultLocation = statePlaces.placesWeather.find { it.isDefault }
            binding.settingsScreen.defaultLocationPreference.selectedValue =
                defaultLocation?.place?.name ?: getString(R.string.not_selected)

            binding.settingsScreen.defaultLocationPreference.setOnClickListener {
                showBottomSheet(
                    places = statePlaces.placesWeather.map { Pair(it.id, it.place) },
                    defaultId = defaultLocation?.id
                )
            }
            if (statePlaces.placesWeather.isNotEmpty()) {
                binding.placesListScreen.messageText.visibility = View.GONE
                savedPlacesAdapter.updateList(statePlaces.placesWeather)
                binding.placesListScreen.savedPlaces.visibility = View.VISIBLE
                binding.settingsScreen.locationPreference.visibility = View.VISIBLE
            } else {
                binding.placesListScreen.savedPlaces.visibility = View.GONE
                binding.placesListScreen.messageText.text = "Нет сохраненных локаций"
                binding.placesListScreen.messageText.visibility = View.VISIBLE
                binding.settingsScreen.root.visibility = View.GONE
            }
        }

        viewModel.stateWeather.observe(this) { stateWeather ->
            when (stateWeather) {
                is CurrentWeatherState.NotSettled -> {
                    binding.placeWeatherScreen.root.visibility = View.GONE
                }

                is CurrentWeatherState.Error -> {
                    binding.placeWeatherScreen.loadingIndicator.visibility = View.GONE
                    binding.placeWeatherScreen.swipeRefreshLayout.visibility = View.GONE
                    binding.placeWeatherScreen.messageText.text =
                        StringBuilder("Ошибка\n${stateWeather.message}")
                    binding.placeWeatherScreen.message.visibility = View.VISIBLE
                }

                is CurrentWeatherState.Loading -> {
                    binding.placeWeatherScreen.message.visibility = View.GONE
                    binding.placeWeatherScreen.swipeRefreshLayout.visibility = View.GONE
                    binding.placeWeatherScreen.loadingIndicator.visibility = View.VISIBLE
                }

                is CurrentWeatherState.Loaded -> {
                    binding.placeWeatherScreen.message.visibility = View.GONE
                    binding.placeWeatherScreen.loadingIndicator.visibility = View.GONE
                    binding.placeWeatherScreen.swipeRefreshLayout.visibility = View.VISIBLE

                    outputWeather(
                        appSettings = appSettings,
                        placeWeather = stateWeather.placeWeather
                    )
                    binding.placeWeatherScreen.swipeRefreshLayout.visibility = View.VISIBLE
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun outputCurrentLocationWeather(
        temperature: String?,
        placeWeather: PlaceWeather,
        weatherCodes: WeatherCodes
    ) {
        val weatherData =
            weatherCodes.getWeatherDataByWeatherCode(placeWeather.weather!!.current.weatherCode)!!


        val placeName = placeWeather.place.name
        binding.placesListScreen.currentLocationWeather.name.text = if (placeName.isNullOrEmpty()) {
            placeWeather.place.address.state ?: getString(R.string.default_location)
        } else {
            placeWeather.place.name
        }

        if (temperature == "celsius") {
            binding.placesListScreen.currentLocationWeather.temperature.text =
                "${round(placeWeather.weather!!.current.temperature).toInt()}°"
        } else {
            binding.placesListScreen.currentLocationWeather.temperature.text =
                "${((round(placeWeather.weather!!.current.temperature).toInt()) * 9 / 5) + 32}°"
        }

        binding.placesListScreen.currentLocationWeather.situation.text = weatherData.title
        if (placeWeather.weather!!.current.isDay == 1) {
            binding.placesListScreen.currentLocationWeather.root.setBackgroundResource(weatherData.dayBackground)
            binding.placesListScreen.currentLocationWeather.weatherImage.setImageResource(
                weatherData.dayImage
            )
        } else {
            binding.placesListScreen.currentLocationWeather.root.setBackgroundResource(weatherData.nightBackground)
            binding.placesListScreen.currentLocationWeather.weatherImage.setImageResource(
                weatherData.nightImage ?: weatherData.dayImage
            )
        }

    }

    private fun outputWeather(
        placeWeather: PlaceWeather,
        appSettings: AppSettings
    ) {
        outputCurrentWeather(
            placeWeather = placeWeather,
            appSettings = appSettings
        )
        outputHourlyWeather(
            appSettings = appSettings,
            weather = placeWeather.weather!!
        )
        outputDailyWeather(
            appSettings = appSettings,
            weather = placeWeather.weather!!
        )
    }

    @SuppressLint("SetTextI18n")
    private fun outputCurrentWeather(
        appSettings: AppSettings,
        placeWeather: PlaceWeather
    ) {
        val weatherData =
            weatherCodes.getWeatherDataByWeatherCode(placeWeather.weather!!.current.weatherCode)!!

        val placeName = placeWeather.place.name
        val title = if (placeName.isNullOrEmpty()) {
            placeWeather.place.address.state
        } else {
            placeWeather.place.name
        }
        binding.placeWeatherScreen.toolbar.title = title ?: getString(R.string.current_location)
        if (placeWeather.isCurrentLocation && title != null) {
            binding.placeWeatherScreen.toolbar.subtitle = getString(R.string.current_location)
        } else {
            binding.placeWeatherScreen.toolbar.subtitle = null
        }

        if (appSettings.temperature == "celsius") {
            binding.placeWeatherScreen.currentWeatherTemperature.text =
                round(placeWeather.weather!!.current.temperature).toInt().toString()
            binding.placeWeatherScreen.currentWeatherApparentTemperature.text =
                "${weatherData.title}, ${getString(R.string.feels_like)} ${round(placeWeather.weather?.current?.apparentTemperature!!).toInt()}°"
        } else {
            binding.placeWeatherScreen.currentWeatherTemperature.text =
                (((round(placeWeather.weather!!.current.temperature).toInt()) * 9 / 5) + 32).toString()
            binding.placeWeatherScreen.currentWeatherApparentTemperature.text =
                "${weatherData.title}, ${getString(R.string.feels_like)} ${((round(placeWeather.weather!!.current.apparentTemperature).toInt()) * 9 / 5) + 32}°"

        }

        when {
            (placeWeather.weather!!.current.isDay == 1) -> {
                binding.placeWeatherScreen.root.setBackgroundResource(weatherData.dayBackground)
                binding.placeWeatherScreen.currentWeatherImage.setImageResource(weatherData.dayImage)
            }

            else -> {
                binding.placeWeatherScreen.root.setBackgroundResource(weatherData.nightBackground)
                binding.placeWeatherScreen.currentWeatherImage.setImageResource(
                    weatherData.nightImage ?: weatherData.dayImage
                )
            }
        }
        val windDirection = defineWindDirection(
            placeWeather.weather!!.current.windDirection
        )
        binding.placeWeatherScreen.wind.value.text = when (appSettings.wind) {
            "kilometers_per_hour" -> {
                "${placeWeather.weather!!.current.windSpeed} ${getString(R.string.kilometers_per_hour)}, $windDirection"
            }

            "miles_per_hour" -> {
                "${round((placeWeather.weather!!.current.windSpeed) / 1.609).toInt()} ${getString(R.string.miles_per_hour)}, $windDirection"
            }

            else -> {
                "${round((placeWeather.weather!!.current.windSpeed) / 3.6).toInt()} ${getString(R.string.meters_per_second)}, $windDirection"
            }
        }

        binding.placeWeatherScreen.surfacePressure.value.text =
            if (appSettings.surfacePressure == "hPa") {
                "${placeWeather.weather!!.current.surfacePressure} ${getString(R.string.hPa)}"
            } else {
                "${round(placeWeather.weather!!.current.surfacePressure * 3 / 4).toInt()} ${
                    getString(
                        R.string.mm_of_mercury
                    )
                }"
            }

        binding.placeWeatherScreen.relativeHumidity.value.text =
            "${placeWeather.weather!!.current.relativeHumidity} %"
    }

    private fun outputHourlyWeather(
        appSettings: AppSettings,
        weather: Weather
    ) {
        val adapter = HourlyAdapter(
            weatherCodes = weatherCodes,
            hourlyWeather = weather.hourly,
            sunrise = weather.daily.dailySunrise.first(),
            sunset = weather.daily.dailySunset.first(),
            temperature = appSettings.temperature
        )
        binding.placeWeatherScreen.hourlyRecyclerView.adapter = adapter
        binding.placeWeatherScreen.hourlyRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun outputDailyWeather(
        appSettings: AppSettings,
        weather: Weather
    ) {
        val adapter = DailyAdapter(
            weatherCodes = weatherCodes,
            dailyWeather = weather.daily,
            temperature = appSettings.temperature,
            context = this
        )
        binding.placeWeatherScreen.dailyRecyclerView.adapter = adapter
        binding.placeWeatherScreen.dailyRecyclerView.layoutManager =
            object : LinearLayoutManager(this) {
                override fun canScrollVertically(): Boolean = false
                override fun canScrollHorizontally(): Boolean = false
            }
    }

    private fun openScreen(view: View) {
        if (!view.isVisible) {
            view.visibility = View.VISIBLE
            view.doOnLayout {
                view.translationX = view.width.toFloat()
                view.animate()
                    .translationX(0f)
                    .setDuration(200)
                    .start()
            }
        }
    }

    private fun closeScreen(view: View) {
        view.animate()
            .translationX(view.width.toFloat())
            .setDuration(200)
            .withEndAction {
                view.visibility = View.GONE
                view.translationX = 0f
            }
            .start()
    }

    private fun showBottomSheet(places: List<Pair<Long, Place>>, defaultId: Long?) {
        val bottomSheetFragment = MyBottomSheetFragment(
            places = places,
            defaultId = defaultId,
            onLocationSelected = { placeId ->
                viewModel.updateDefaultPlace(placeId)
            }
        )
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun defineWindDirection(windDirection: Int): String {
        val cardinalDirections = resources.getStringArray(R.array.cardinal_directions)
        return when {
            (windDirection in (0..22)) -> cardinalDirections[0] //Север (North)
            (windDirection in (23..67)) -> cardinalDirections[1] //Северо-восток (Northeast)
            (windDirection in (68..112)) -> cardinalDirections[2] //Восток (East)
            (windDirection in (113..157)) -> cardinalDirections[3] //Юго-восток(Southeast)
            (windDirection in (158..202)) -> cardinalDirections[4] //Юг (South)
            (windDirection in (203..248)) -> cardinalDirections[5] //Юго-запад (Southwest)
            (windDirection in (249..293)) -> cardinalDirections[6] //Запад (West)
            (windDirection in (294..338)) -> cardinalDirections[7] //Северо-запад (Northwest)
            else -> cardinalDirections[0]
        }
    }

    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

//    private fun scheduleDailyWork() {
//        //val currentDate = Calendar.getInstance()
//        val dueDate = Calendar.getInstance()
//
//        dueDate.set(Calendar.HOUR_OF_DAY, 0)
//        dueDate.set(Calendar.MINUTE, 58)
//        dueDate.set(Calendar.SECOND, 0)
//
////        if (dueDate.before(currentDate)) {
////            dueDate.add(Calendar.DAY_OF_MONTH, 1)
////        }
//
//        //val delay = dueDate.timeInMillis - currentDate.timeInMillis
//        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
//            .setInitialDelay(0, TimeUnit.MILLISECONDS)
//            .build()
//
//        WorkManager.getInstance(applicationContext)
//            .enqueue(dailyWorkRequest)
//    }
}
