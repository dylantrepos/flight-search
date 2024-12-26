package com.example.flightsearch.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.FlightSearchTheme
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport
import java.util.Locale


@Composable
fun SuggestionList(
    airports: List<Airport>,
    query: String,
    onSuggestionClick: (Airport) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    if (airports.isEmpty()) {
        Text(
            text = stringResource(
                R.string.no_airport_found,
                query.uppercase(Locale.getDefault())
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
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
                        text = airport.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
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
            onSuggestionClick = {},
            query = "lia"
        )
    }
}