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
import kotlinx.coroutines.flow.first
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
    val airports: StateFlow<List<Airport>> = _query.flatMapLatest { query ->
        if (query.isEmpty()) {
            getAllAirport().map { it }
        } else {
            flightRepository.getAirportTimetable("%$query%").map { it }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    val favorites: StateFlow<List<AirportTimetable>> =
        flightRepository.getFavoriteFlight().map { favorites ->
            mapFavoritesToTimetables(favorites)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    fun updateQuery(newQuery: String) {
        if (_airportTimetable.value.isNotEmpty()) {
            _airportTimetable.value = emptyList()
        }
        _query.value = newQuery
    }

    fun generateTimetable(airport: Airport) {
        viewModelScope.launch {
            getAllAirport().collect { airportList ->
                _airportTimetable.value = airportList.mapNotNull { arrival ->
                    if (arrival.iataCode != airport.iataCode) {
                        val isFavorite = flightRepository.findFavorite(
                            departureCode = airport.iataCode,
                            destinationCode = arrival.iataCode
                        ).firstOrNull()
                        AirportTimetable(
                            departure = airport,
                            arrival = arrival,
                            isFavorite = isFavorite !== null
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    private fun getAllAirport(): Flow<List<Airport>> = flightRepository.getAllAirport()

    private suspend fun mapFavoritesToTimetables(favorites: List<Favorite>): List<AirportTimetable> {
        val timetables = mutableListOf<AirportTimetable>()
        for (favorite in favorites) {
            val departureAirport =
                flightRepository.getAirportDetails(favorite.departureCode).first()
            val arrivalAirport =
                flightRepository.getAirportDetails(favorite.destinationCode).first()
            timetables.add(
                AirportTimetable(
                    departure = departureAirport,
                    arrival = arrivalAirport,
                    isFavorite = true
                )
            )
        }
        return timetables
    }

    suspend fun toggleFavorite(favorite: Favorite) {
        val favoriteFound =
            flightRepository.findFavorite(favorite.departureCode, favorite.destinationCode).first()

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
