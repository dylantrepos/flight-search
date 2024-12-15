package com.example.flightsearch.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FlightSearchTheme
import com.example.flightsearch.AppViewModelProvider
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.AirportTimetable
import com.example.flightsearch.data.Favorite
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
        val airportTimetable by viewModel.airportTimetable.collectAsState()
        val favorites by viewModel.favorites.collectAsState()

        Scaffold(
            topBar = {
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
        ) { innerPadding ->
            HomeScreen(
                airports = airports,
                query = query,
                favorites = favorites,
                onQueryChange = viewModel::updateQuery,
                onSelectAirport = viewModel::generateTimetable,
                timetable = airportTimetable,
                modifier = Modifier.fillMaxSize(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    airports: List<Airport>,
    query: String,
    modifier: Modifier = Modifier,
    timetable: List<AirportTimetable>,
    favorites: List<AirportTimetable>,
    toggleFavorite: (Favorite) -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectAirport: (Airport) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val layoutDirection = LocalLayoutDirection.current
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .padding(contentPadding)
            .clickable { focusManager.clearFocus() }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .clickable {
                                    onQueryChange("")
                                }
                        )
                    }
                },
                shape = RoundedCornerShape(100),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                })
            )
            if (query.isNotEmpty() && !isFocused && timetable.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.flights_from,
                        query.uppercase(Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isFocused || query.isNotEmpty() && timetable.isEmpty()) {
                if (query.isNotEmpty()) {
                    SuggestionList(
                        airports = airports,
                        onSuggestionClick = {
                            onSelectAirport(it)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    TimetableList(
                        timetable = if (query.isEmpty()) favorites else timetable,
                        onSave = toggleFavorite,
                    )
                }
            } else {
                TimetableList(
                    timetable = if (query.isEmpty()) favorites else timetable,
                    onSave = toggleFavorite,
                )
            }
        }
    }
}

@Composable
fun TimetableList(
    timetable: List<AirportTimetable>,
    onSave: (Favorite) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = timetable,
            key = { flight -> "${flight.departure.iataCode}-${flight.arrival.iataCode}" }
        ) { flight ->
            var isStarred by remember { mutableStateOf(flight.isFavorite) }
            val animatedTint by animateColorAsState(
                targetValue = if (isStarred) Color(0xFFFFD700) else Color.LightGray,
                animationSpec = tween(durationMillis = 1000) // Adjust duration as needed
            )

            val scale by animateFloatAsState(
                targetValue = if (isStarred) 1.1f else 1f,
                animationSpec = tween(250, easing = EaseInOut)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        isStarred = !isStarred
                        onSave(
                            Favorite(
                                departureCode = flight.departure.iataCode,
                                destinationCode = flight.arrival.iataCode
                            )
                        )
                    }
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.departure).uppercase(Locale.getDefault()),
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            flight.departure.iataCode,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            flight.departure.airportName,
                            fontWeight = FontWeight.Light,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.arrival).uppercase(Locale.getDefault()),
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            flight.arrival.iataCode,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            flight.arrival.airportName,
                            fontWeight = FontWeight.Light,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = animatedTint,
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                )
            }
        }
    }
}


@Composable
fun SuggestionList(
    airports: List<Airport>,
    onSuggestionClick: (Airport) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(
            items = airports,
            key = { airport -> airport.id }
        ) { airport ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clickable(enabled = true) {
                        onSuggestionClick.invoke(airport)
                    }
            ) {
                Text(
                    text = airport.iataCode,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = airport.airportName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimetableListPreview() {
    FlightSearchTheme {
        TimetableList(
            timetable = List(3) { index ->
                AirportTimetable(
                    departure = Airport(
                        0,
                        "Paris airport",
                        "CDG$index",
                        123123
                    ),
                    arrival = Airport(
                        1,
                        "Los Angeles airport",
                        "LAX$index",
                        123123
                    )
                )
            },
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimetableListDarkPreview() {
    FlightSearchTheme(darkTheme = true) {
        TimetableList(
            timetable = List(3) { index ->
                AirportTimetable(
                    departure = Airport(
                        0,
                        "Paris airport",
                        "CDG$index",
                        123123
                    ),
                    arrival = Airport(
                        1,
                        "Los Angeles airport",
                        "LAX$index",
                        123123
                    )
                )
            },
            onSave = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SuggestionListPreview() {
    FlightSearchTheme {
        SuggestionList(
            airports = List(3) { index ->
                Airport(
                    index,
                    "lorem ipsum airport",
                    "LIA",
                    123123
                )
            },
            onSuggestionClick = {}
        )
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