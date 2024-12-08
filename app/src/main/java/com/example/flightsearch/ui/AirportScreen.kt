package com.example.flightsearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
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
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: AirportViewModel = viewModel(factory = AirportViewModel.factory)
) {
    val airports by viewModel.airports.collectAsState()
    val query by viewModel.query.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    airports: List<Airport>,
    query: String,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
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
            TextField (
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                shape = RoundedCornerShape(100),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
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
                    text = stringResource(R.string.flights_from, query.toUpperCase()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            SuggestionList(
                airports = airports,
                onSuggestionClick = {},
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        }
    }
}


@Composable
fun SuggestionList(
    airports: List<Airport>,
    onSuggestionClick: (String) -> Unit,
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
           key = { airport -> airport.id}
       ) { airport ->
           Row(
               modifier = Modifier
               .fillMaxWidth()
               .clickable(enabled = true) {
                   onSuggestionClick.invoke(airport.iataCode)
               }
           ) {
               Text(
                   text = airport.iataCode,
                   style = MaterialTheme.typography.labelMedium,
                   fontWeight = FontWeight.Bold
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

@Composable
fun FlightList(
    modifier: Modifier = Modifier,
) {

}

@Preview(showBackground = true)
@Composable
fun SuggestionListPreview() {
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

@Preview
@Composable
fun HomeScreenPreview() {
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
        onActiveChange = {}
    )
}