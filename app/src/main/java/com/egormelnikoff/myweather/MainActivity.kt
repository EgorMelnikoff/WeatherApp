package com.egormelnikoff.myweather

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egormelnikoff.myweather.app.entity.Place
import com.egormelnikoff.myweather.app.entity.PlaceWeather
import com.egormelnikoff.myweather.app.entity.Weather
import com.egormelnikoff.myweather.app.enums.SurfacePressure
import com.egormelnikoff.myweather.app.enums.Temperature
import com.egormelnikoff.myweather.app.enums.Wind
import com.egormelnikoff.myweather.app.model.WeatherCodes
import com.egormelnikoff.myweather.app.preferences.AppSettings
import com.egormelnikoff.myweather.databinding.ActivityMainBinding
import com.egormelnikoff.myweather.ui.adapter.CustomItemDecoration
import com.egormelnikoff.myweather.ui.adapter.DailyWeatherAdapter
import com.egormelnikoff.myweather.ui.adapter.HourlyWeatherAdapter
import com.egormelnikoff.myweather.ui.adapter.SavedPlacesAdapter
import com.egormelnikoff.myweather.ui.fragment.MyBottomSheetFragment
import com.egormelnikoff.myweather.view_model.search.SearchState
import com.egormelnikoff.myweather.view_model.search.SearchViewModel
import com.egormelnikoff.myweather.view_model.settings.SettingsViewModel
import com.egormelnikoff.myweather.view_model.weather.CurrentWeatherState
import com.egormelnikoff.myweather.view_model.weather.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appSettings: AppSettings
    private lateinit var windowInsetsController: WindowInsetsControllerCompat
    private lateinit var savedPlacesAdapter: SavedPlacesAdapter

    @Inject
    lateinit var weatherCodes: WeatherCodes

    private val searchViewModel: SearchViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setupOnBackCallBack()
        setupViews()
        collectSettings()
        setupListeners()
        setupObservers()
    }

    private fun setupOnBackCallBack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.placeWeatherScreen.root.isVisible -> {
                        closeScreen(binding.placeWeatherScreen.root)
                        windowInsetsController.isAppearanceLightStatusBars = !this@MainActivity.isDarkThemeOn()
                    }

                    binding.settingsScreen.root.isVisible -> {
                        closeScreen(binding.settingsScreen.root)
                    }

                    binding.placesListScreen.searchView.isShowing -> {
                        binding.placesListScreen.searchView.hide()
                    }

                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupViews() {
        setupPlacesAdapter()

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

        val text = "<a href=\"https://open-meteo.com/\">Open Meteo</a>" +
                " · " +
                "<a href=\"https://nominatim.org/\">Nominatim</a>"
        binding.placeWeatherScreen.weatherLink.movementMethod = LinkMovementMethod.getInstance()
        binding.placeWeatherScreen.weatherLink.text =
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
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
            searchViewModel.searchPlaces(query)
            binding.placesListScreen.searchView.clearFocusAndHideKeyboard()
            return@setOnEditorActionListener false
        }

        binding.placesListScreen.swipeRefreshLayout.setOnRefreshListener {
            weatherViewModel.updatePlacesWeather {
                binding.placesListScreen.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.placeWeatherScreen.toolbar.setNavigationOnClickListener {
            closeScreen(binding.placeWeatherScreen.root)
            windowInsetsController.isAppearanceLightStatusBars = !this@MainActivity.isDarkThemeOn()
        }

        binding.placeWeatherScreen.swipeRefreshLayout.setOnRefreshListener {
            weatherViewModel.updatePlacesWeather {
                binding.placeWeatherScreen.swipeRefreshLayout.isRefreshing = false
            }
        }


        binding.settingsScreen.toolbar.setNavigationOnClickListener {
            closeScreen(binding.settingsScreen.root)
        }

        binding.settingsScreen.temperaturePreference.onValueChangeListener = { newValue ->
            settingsViewModel.setTemperature(Temperature.valueOf(newValue))
        }
        binding.settingsScreen.windPreference.onValueChangeListener = { newValue ->
            settingsViewModel.setWind(Wind.valueOf(newValue))
        }
        binding.settingsScreen.pressurePreference.onValueChangeListener = { newValue ->
            settingsViewModel.setSurfacePressure(SurfacePressure.valueOf(newValue))
        }
        binding.settingsScreen.notificationsPreference.onValueChangeListener = { newValue ->
            settingsViewModel.setNotifications(newValue)
        }
    }

    private fun setupPlacesAdapter() {
        savedPlacesAdapter = SavedPlacesAdapter(
            weatherCodes = weatherCodes
        ) { placeWeather ->
            weatherViewModel.setPlaceWeather(placeWeather)
            binding.placeWeatherScreen.toolbar.title = placeWeather.place.name
            openScreen(binding.placeWeatherScreen.root)
            windowInsetsController.isAppearanceLightStatusBars = false
        }

        binding.placesListScreen.savedPlaces.adapter = savedPlacesAdapter
        binding.placesListScreen.savedPlaces.layoutManager = LinearLayoutManager(this)

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            val paint = Paint().apply { color = this@MainActivity.getColor(R.color.error) }
            val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.delete)!!
            private val backgroundRect = RectF()

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val deletedPlace = savedPlacesAdapter.currentList[position]
                weatherViewModel.deletePlaceWeather(deletedPlace.id)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.height

                    val iconMargin = (itemHeight - icon.intrinsicHeight) / 2
                    val iconTop = itemView.top + iconMargin
                    val iconBottom = iconTop + icon.intrinsicHeight

                    if (dX > 0) {
                        backgroundRect.set(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            itemView.left + dX,
                            itemView.bottom.toFloat()
                        )

                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    } else {
                        backgroundRect.set(
                            itemView.right + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )

                        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }

                    c.drawRect(backgroundRect, paint)
                    icon.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.placesListScreen.savedPlaces)
    }

    private fun collectSettings() {
        lifecycleScope.launch {
            settingsViewModel.appSettings.collect {
                appSettings = it
                updateSettings(it)
                savedPlacesAdapter.updateTemperatureUnit(it.temperature)
            }
        }
    }

    private fun setupObservers() {
        searchViewModel.stateSearch.observe(this) { stateSearch ->
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
                        weatherViewModel.fetchPlaceWeather(place)
                        binding.placeWeatherScreen.toolbar.title = place.name
                        openScreen(binding.placeWeatherScreen.root)

                        searchViewModel.setDefaultSearchState()
                        binding.placesListScreen.searchView.editText.text.clear()
                        binding.placesListScreen.searchView.hide()
                    }

                    binding.placesListScreen.searchPlaces.adapter = adapter
                    binding.placesListScreen.searchPlaces.visibility = View.VISIBLE
                }

            }
        }

        weatherViewModel.statePlaces.observe(this) { statePlaces ->
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
                savedPlacesAdapter.submitList(statePlaces.placesWeather)
                binding.placesListScreen.savedPlaces.visibility = View.VISIBLE
                binding.settingsScreen.locationPreference.visibility = View.VISIBLE
            } else {
                binding.placesListScreen.savedPlaces.visibility = View.GONE
                binding.placesListScreen.messageText.text = "Нет сохраненных локаций"
                binding.placesListScreen.messageText.visibility = View.VISIBLE
                binding.settingsScreen.root.visibility = View.GONE
            }
        }

        weatherViewModel.stateWeather.observe(this) { stateWeather ->
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

    private fun updateSettings(settings: AppSettings) {
        binding.settingsScreen.temperaturePreference.selectedValue = settings.temperature.name
        binding.settingsScreen.windPreference.selectedValue = settings.wind.name
        binding.settingsScreen.pressurePreference.selectedValue = settings.surfacePressure.name
        binding.settingsScreen.notificationsPreference.selectedValue = settings.notifications
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
            weather = placeWeather.weather
        )
        outputDailyWeather(
            appSettings = appSettings,
            weather = placeWeather.weather
        )
    }

    @SuppressLint("SetTextI18n")
    private fun outputCurrentWeather(
        appSettings: AppSettings,
        placeWeather: PlaceWeather
    ) {
        val weatherData = weatherCodes.getWeatherDataByWeatherCode(
            placeWeather.weather.current.weatherCode
        )

        val placeName = placeWeather.place.name
        val title = if (placeName.isNullOrEmpty()) {
            placeWeather.place.address.state
        } else {
            placeWeather.place.name
        }
        binding.placeWeatherScreen.toolbar.title = title ?: getString(R.string.current_location)
        binding.placeWeatherScreen.toolbar.subtitle = null

        when (appSettings.temperature) {
            Temperature.CELSIUS -> {
                binding.placeWeatherScreen.currentWeatherTemperature.text =
                    round(placeWeather.weather.current.temperature).toInt().toString()
                binding.placeWeatherScreen.currentWeatherApparentTemperature.text =
                    "${weatherData.title}, ${getString(R.string.feels_like)} ${round(placeWeather.weather.current.apparentTemperature).toInt()}°"

            }

            Temperature.FAHRENHEIT -> {
                binding.placeWeatherScreen.currentWeatherTemperature.text =
                    (((round(placeWeather.weather.current.temperature).toInt()) * 9 / 5) + 32).toString()
                binding.placeWeatherScreen.currentWeatherApparentTemperature.text =
                    "${weatherData.title}, ${getString(R.string.feels_like)} ${((round(placeWeather.weather.current.apparentTemperature).toInt()) * 9 / 5) + 32}°"
            }
        }

        when {
            (placeWeather.weather.current.isDay == 1) -> {
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
            placeWeather.weather.current.windDirection
        )
        binding.placeWeatherScreen.wind.value.text = when (appSettings.wind) {
            Wind.KILOMETERS_PER_HOUR -> {
                "${placeWeather.weather.current.windSpeed} ${getString(R.string.kilometers_per_hour)}, $windDirection"
            }

            Wind.MILES_PER_HOUR -> {
                "${round((placeWeather.weather.current.windSpeed) / 1.609).toInt()} ${getString(R.string.miles_per_hour)}, $windDirection"
            }

            Wind.METERS_PER_SECOND -> {
                "${round((placeWeather.weather.current.windSpeed) / 3.6).toInt()} ${getString(R.string.meters_per_second)}, $windDirection"
            }
        }

        binding.placeWeatherScreen.surfacePressure.value.text = when (appSettings.surfacePressure) {
            SurfacePressure.HPA -> "${placeWeather.weather.current.surfacePressure} ${getString(R.string.hPa)}"
            SurfacePressure.MM_OF_MERCURY -> "${round(placeWeather.weather.current.surfacePressure * 3 / 4).toInt()} ${
                getString(R.string.mm_of_mercury)
            }"
        }


        binding.placeWeatherScreen.relativeHumidity.value.text =
            "${placeWeather.weather.current.relativeHumidity} %"
    }

    private fun outputHourlyWeather(
        appSettings: AppSettings,
        weather: Weather
    ) {
        val adapter = HourlyWeatherAdapter(
            weatherCodes = weatherCodes,
            hourlyWeather = weather.hourly,
            sunrise = weather.daily.dailySunrise.first().toLocalTime(),
            sunset = weather.daily.dailySunset.first().toLocalTime(),
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
        val adapter = DailyWeatherAdapter(
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
                weatherViewModel.updateDefaultPlace(placeId)
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

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
