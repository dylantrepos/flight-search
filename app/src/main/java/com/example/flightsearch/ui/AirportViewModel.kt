package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.AirportTimetable
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AirportViewModel(private val flightRepository: FlightRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _airportTimetable = MutableStateFlow<List<AirportTimetable>>(emptyList())
    val airportTimetable: StateFlow<List<AirportTimetable>> = _airportTimetable

    @OptIn(ExperimentalCoroutinesApi::class)
    val airports: StateFlow<List<Airport>> = _query
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                fetchAllAirports()
            } else {
                flightRepository.getAirportTimetable("%$query%")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<AirportTimetable>> = flightRepository.getFavoriteFlights()
        .map { favorites -> mapFavoritesToTimetables(favorites) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList())

    fun updateQuery(newQuery: String) {
        _airportTimetable.value = emptyList()
        _query.value = newQuery
    }

    fun generateTimetable(airport: Airport) {
        viewModelScope.launch {
            val airportList = fetchAllAirports().firstOrNull() ?: emptyList()
            _airportTimetable.value = airportList.mapNotNull { arrival ->
                if (arrival.iataCode != airport.iataCode) {
                    val isFavorite = flightRepository.findFavorite(
                        departureCode = airport.iataCode,
                        destinationCode = arrival.iataCode
                    ).firstOrNull()
                    AirportTimetable(
                        departure = airport,
                        arrival = arrival,
                        isFavorite = isFavorite != null
                    )
                } else {
                    null
                }
            }

        }
    }

    private fun fetchAllAirports(): Flow<List<Airport>> = flightRepository.fetchAllAirports()

    private suspend fun mapFavoritesToTimetables(favorites: List<Favorite>): List<AirportTimetable> {
        return favorites.mapNotNull { favorite ->
            val departureAirport =
                flightRepository.getAirportDetails(favorite.departureCode).firstOrNull()
            val arrivalAirport =
                flightRepository.getAirportDetails(favorite.destinationCode).firstOrNull()
            if (departureAirport != null && arrivalAirport != null) {
                AirportTimetable(
                    departure = departureAirport,
                    arrival = arrivalAirport,
                    isFavorite = true
                )
            } else {
                null
            }
        }
    }

    suspend fun toggleFavorite(favorite: Favorite) {
        val favoriteFound =
            flightRepository.findFavorite(favorite.departureCode, favorite.destinationCode)
                .firstOrNull()
        if (favoriteFound != null) {
            flightRepository.deleteFavoriteFlight(favoriteFound)
        } else {
            flightRepository.addFavoriteFlight(favorite)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}