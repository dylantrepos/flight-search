package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.AirportTimetable
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.SearchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AirportViewModel(
    private val flightRepository: FlightRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _airportTimetable = MutableStateFlow<List<AirportTimetable>>(emptyList())
    val airportTimetable: StateFlow<List<AirportTimetable>> = _airportTimetable

    private val _searchHistory = MutableStateFlow<Set<String>>(emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    val airports: StateFlow<List<Airport>> = _query
        .flatMapLatest { query ->
            if (query.isNotEmpty()) {
                fetchAllAirports()
                flightRepository.getAirportTimetable("%$query%")
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchHistoryAirports: StateFlow<List<Airport>> = _searchHistory
        .flatMapLatest { history ->
            if (history.isEmpty()) {
                flowOf(emptyList())
            } else {
                val airportFlows = history.map { iataCode ->
                    flightRepository.getAirportDetails(iataCode)
                }
                combine(airportFlows) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<AirportTimetable>> = flightRepository.getFavoriteFlights()
        .map { favorites -> mapFavoritesToTimetables(favorites) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), emptyList())

    init {
        viewModelScope.launch {
            _searchHistory.value = searchHistoryRepository.searchHistory.firstOrNull() ?: emptySet()
        }
    }

    fun updateQuery(newQuery: String) {
        _airportTimetable.value = emptyList()
        _query.value = newQuery
    }

    fun generateTimetable(airport: Airport) {
        _query.value = airport.iataCode
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
            addSearchHistory(airport.iataCode)
        }
    }

    private fun addSearchHistory(searchedAirport: String) {
        viewModelScope.launch {
            searchHistoryRepository.saveSearchQuery(searchedAirport)
            _searchHistory.value = searchHistoryRepository.searchHistory.firstOrNull() ?: emptySet()
        }
    }

    fun searchFromHistory(airport: Airport) {
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
            addSearchHistory(airport.iataCode)
        }
    }

    fun removeSearchRepository(searchedAirport: Airport) {
        viewModelScope.launch {
            searchHistoryRepository.removeSearchQuery(searchedAirport.iataCode)
            _searchHistory.value = searchHistoryRepository.searchHistory.firstOrNull() ?: emptySet()
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