package com.example.flightsearch.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flightsearch.R
import com.example.flightsearch.model.Airport


@Composable
fun SearchHistoryList(
    searchHistoryList: List<Airport>,
    onSearchHistory: (Airport) -> Unit,
    onClearSearchHistory: (Airport) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.recently_searched),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = searchHistoryList,
            key = { airport -> airport.id }
        ) { airport ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clickable(enabled = true) {
                        onSearchHistory.invoke(airport)
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
                    fontWeight = FontWeight.Light,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = .5f),
                    modifier = Modifier
                        .width(15.dp)
                        .height(15.dp)
                        .clickable {
                            onClearSearchHistory(airport)
                        }
                )
            }
        }
    }

}
