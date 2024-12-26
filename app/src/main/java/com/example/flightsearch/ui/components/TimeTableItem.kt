package com.example.flightsearch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.FlightSearchTheme
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import java.util.Locale

@Composable
fun TimeTableItem(
    departure: Airport,
    arrival: Airport,
    isFavorite: Boolean = false,
    onClickFavorite: (Favorite) -> Unit
) {
    var isStarred by remember { mutableStateOf(isFavorite) }
    val animatedTint by animateColorAsState(
        targetValue = if (isStarred) Color(0xFFFFD700) else MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = tween(durationMillis = 500) // Adjust duration as needed
    )

    val scale by animateFloatAsState(
        targetValue = if (isStarred) 1.1f else 1f,
        animationSpec = tween(1000, easing = EaseInOut)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable {
                isStarred = !isStarred
                onClickFavorite(
                    Favorite(
                        departureCode = departure.iataCode,
                        destinationCode = arrival.iataCode
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
                    departure.iataCode,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    departure.name,
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
                    arrival.iataCode,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    arrival.name,
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


@Preview(showBackground = true)
@Composable
fun TimetableItemPreview() {
    FlightSearchTheme {
        TimeTableItem(
            departure = Airport(
                0,
                "Paris airport",
                "CDG",
                123123
            ),
            arrival = Airport(
                1,
                "Los Angeles airport",
                "LAX",
                123123
            ),
            onClickFavorite = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimetableItemDarkPreview() {
    FlightSearchTheme(darkTheme = true) {
        TimeTableItem(
            departure = Airport(
                0,
                "Paris airport",
                "CDG",
                123123
            ),
            arrival = Airport(
                1,
                "Los Angeles airport",
                "LAX",
                123123
            ),
            onClickFavorite = {},
        )
    }
}
