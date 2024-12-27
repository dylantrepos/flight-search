package com.example.flightsearch.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirplanemodeActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FlightSearchTheme
import com.example.flightsearch.AppViewModelProvider
import com.example.flightsearch.R
import com.example.flightsearch.model.Airport
import com.example.flightsearch.model.AirportTimetable
import com.example.flightsearch.model.Favorite
import com.example.flightsearch.ui.components.SearchBarItem
import com.example.flightsearch.ui.components.SearchHistoryList
import com.example.flightsearch.ui.components.SuggestionList
import com.example.flightsearch.ui.components.TimeTableItem
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: AirportViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    FlightSearchTheme {
        val airports by viewModel.airports.collectAsState()
        val query by viewModel.query.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val airportTimetable by viewModel.airportTimetable.collectAsState()
        val favorites by viewModel.favorites.collectAsState()
        val searchHistory by viewModel.searchHistoryAirports.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            HomeScreen(
                airports = airports,
                query = query,
                searchQuery = searchQuery,
                favorites = favorites,
                onQueryChange = viewModel::updateQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSelectAirport = viewModel::generateTimetable,
                onClearSearchHistory = viewModel::removeSearchRepository,
                timetable = airportTimetable,
                modifier = Modifier.fillMaxSize(),
                searchHistory = searchHistory,
                toggleFavorite = { favorite ->
                    viewModel.viewModelScope.launch {
                        viewModel.toggleFavorite(favorite)
                    }
                },
                contentPadding = innerPadding,
            )
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun HomeScreen(
    airports: List<Airport>,
    searchQuery: String,
    query: String,
    modifier: Modifier = Modifier,
    timetable: List<AirportTimetable>,
    favorites: List<AirportTimetable>,
    toggleFavorite: (Favorite) -> Unit,
    searchHistory: List<Airport>,
    onClearSearchHistory: (Airport) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectAirport: (Airport) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }


    var startAnimation by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val topOffset by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else -screenHeight,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 10000 // Increased duration to slow down the animation
                screenHeight at 0 using EaseInOut
                -70.dp at 4000 using EaseInOut // Adjusted timing to match the new duration
                -70.dp at 6000 using EaseInOut // Adjusted timing to match the new duration
                -screenHeight at 10000 using EaseInOut
            },
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(Unit) {
        scope.launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 10000 // Increased duration to slow down the animation
                        0f at 0 using EaseInOut
                        360f at 4000 using EaseInOut // Adjusted timing to match the new duration
                        360f at 6000 using EaseInOut // Adjusted timing to match the new duration
                        0f at 10000 using EaseInOut
                    },
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Box(
        modifier = modifier
            .padding(contentPadding)
            .clickable(
                onClick = { focusManager.clearFocus() },
                interactionSource = interactionSource,
                indication = null
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            SearchBarItem(
                query = query,
                onSearchQueryChange = onSearchQueryChange,
                onFocusChange = { isFocused = it },
                focusManager = focusManager,
            )

            val showFlightsFrom = query.isNotEmpty() && !isFocused && timetable.isNotEmpty()
            val showSearchHistory = isFocused && query.isEmpty() && searchHistory.isNotEmpty()
            val showSuggestions = isFocused && query.isNotEmpty()
            val showEmptyState = query.isEmpty() && timetable.isEmpty() && favorites.isEmpty()

            if (showFlightsFrom) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AirplanemodeActive,
                        contentDescription = null,
                        modifier = Modifier
                            .width(15.dp)
                            .height(15.dp)
                            .graphicsLayer(rotationZ = 45f)
                    )
                    Text(
                        text = stringResource(
                            R.string.flights_from,
                            query.uppercase(Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showSearchHistory) {
                SearchHistoryList(
                    searchHistoryList = searchHistory,
                    onSearchHistory = {
                        focusManager.clearFocus()
                        onQueryChange(it.iataCode)
                        onSelectAirport(it)
                    },
                    onClearSearchHistory = onClearSearchHistory
                )
            } else if (isFocused || query.isNotEmpty()) {
                if (showSuggestions) {
                    SuggestionList(
                        airports = airports,
                        query = query,
                        onSuggestionClick = {
                            focusManager.clearFocus()
                            onQueryChange(it.iataCode)
                            onSelectAirport(it)
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    TimetableList(
                        timetable = if (query.isEmpty()) favorites else timetable,
                        isFavorite = query.isEmpty(),
                        onSave = toggleFavorite
                    )
                }
            } else if (showEmptyState) {
                DisplayEmptyState(
                    updateStartAnimation = { startAnimation = it },
                    topOffset = topOffset,
                    rotation = rotation
                )
            } else {
                TimetableList(
                    timetable = if (query.isEmpty()) favorites else timetable,
                    isFavorite = query.isEmpty(),
                    onSave = toggleFavorite
                )
            }

        }
    }
}


@Composable
fun DisplayEmptyState(
    updateStartAnimation: (Boolean) -> Unit,
    topOffset: Dp,
    rotation: Animatable<Float, AnimationVector1D>
) {
    LaunchedEffect(Unit) {
        updateStartAnimation(true)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = topOffset)
                .zIndex(-1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.AirplanemodeActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .graphicsLayer(rotationY = if (topOffset > (-70).dp) rotation.value else 0f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(0.dp))
                Text(
                    text = "Where are you going ?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.start_search_airport),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
fun TimetableList(
    timetable: List<AirportTimetable>,
    isFavorite: Boolean = false,
    onSave: (Favorite) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    if (isFavorite && timetable.isNotEmpty()) {
        Text(
            text = "My flights",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = timetable,
            key = { flight -> "${flight.departure.iataCode}-${flight.arrival.iataCode}" }
        ) { flight ->
            TimeTableItem(
                departure = flight.departure,
                arrival = flight.arrival,
                isFavorite = flight.isFavorite,
                onClickFavorite = onSave
            )
        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    FlightSearchTheme {
        HomeScreen(
            airports = List(3) { index ->
                Airport(
                    index,
                    "lorem ipsum airport",
                    "LIA",
                    123123
                )
            },
            query = "",
            onQueryChange = {},
            onSelectAirport = {},
            favorites = listOf(),
            toggleFavorite = {},
            timetable = emptyList(),
            searchHistory = emptyList(),
            onClearSearchHistory = {},
            searchQuery = "",
            onSearchQueryChange = { },
        )
    }
}

@Preview()
@Composable
fun HomeScreenDarkPreview() {
    FlightSearchTheme(darkTheme = true) {
        HomeScreen(
            airports = List(3) { index ->
                Airport(
                    index,
                    "lorem ipsum airport",
                    "LIA",
                    123123
                )
            },
            query = "",
            onQueryChange = {},
            onSelectAirport = {},
            favorites = listOf(),
            toggleFavorite = {},
            timetable = emptyList(),
            searchHistory = emptyList(),
            onClearSearchHistory = {},
            searchQuery = "",
            onSearchQueryChange = { },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun TopBarPreview() {
    FlightSearchTheme(darkTheme = true) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}