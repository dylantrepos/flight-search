package com.example.flightsearch.ui

import androidx.annotation.ColorRes
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
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FlightSearchTheme
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.AirportTimetable
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: AirportViewModel = viewModel(factory = AirportViewModel.factory)
) {
    FlightSearchTheme {
        val airports by viewModel.airports.collectAsState()
        val query by viewModel.query.collectAsState()
        val airportTimetable by viewModel.airportTimetable.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.DarkGray
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
                onQueryChange = viewModel::updateQuery,
                onSearch = {},
                onActiveChange = {},
                onSelectAirport = viewModel::generateTimetable,
                timetable = airportTimetable,
                modifier = Modifier.fillMaxSize(),
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
    onSearch: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSelectAirport: (Airport) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val layoutDirection = LocalLayoutDirection.current
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier.padding(contentPadding)
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
                        color = Color.DarkGray
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.DarkGray
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.DarkGray
                    )
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
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray
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
            if (query.isNotEmpty() && !isFocused) {
                Text(
                    text = stringResource(
                        R.string.flights_from,
                        query.uppercase(Locale.getDefault())
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (timetable.isNotEmpty()) {
                TimetableList(
                    timetable = timetable
                )
            } else {
                if (query.trim().length > 0) {
                    SuggestionList(
                        airports = airports,
                        onSuggestionClick = onSelectAirport,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }
}

@Composable
fun TimetableList(
    timetable: List<AirportTimetable>,
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
            var isStarred by remember { mutableStateOf(false) }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(R.string.departure).uppercase(Locale.getDefault()),
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            flight.departure.iataCode,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            flight.departure.airportName,
                            fontWeight = FontWeight.Light,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        stringResource(R.string.arrival).uppercase(Locale.getDefault()),
                        fontWeight = FontWeight.Light,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            flight.arrival.iataCode,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            flight.arrival.airportName,
                            fontWeight = FontWeight.Light,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (isStarred) Color.Yellow else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { isStarred = !isStarred }
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = airports,
            key = { airport -> airport.id }
        ) { airport ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = true) {
//                        onSuggestionClick.invoke(airport.iataCode)
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
            onSearch = {},
            onSelectAirport = {},
            timetable = emptyList(),
            onActiveChange = {}
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
            onSearch = {},
            onSelectAirport = {},
            timetable = emptyList(),
            onActiveChange = {}
        )
    }
}