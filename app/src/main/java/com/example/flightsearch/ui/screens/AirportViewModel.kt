package com.example.flightsearch.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.SearchHistoryRepository
import com.example.flightsearch.model.Airport
import com.example.flightsearch.model.AirportTimetable
import com.example.flightsearch.model.Favorite
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    @OptIn(ExperimentalCoroutinesApi::class)
    val airports: StateFlow<List<Airport>> = _query
        .flatMapLatest { query ->
            if (query.isNotEmpty()) {
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
        if (newQuery != _query.value) {
            _airportTimetable.value = emptyList()
            _query.value = newQuery
        }
    }

    fun generateTimetable(airport: Airport) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate timetable: ${e.message}"
            }
        }
    }

    private fun addSearchHistory(searchedAirport: String) {
        viewModelScope.launch {
            try {
                searchHistoryRepository.saveSearchQuery(searchedAirport)
                _searchHistory.value =
                    searchHistoryRepository.searchHistory.firstOrNull() ?: emptySet()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add search history: ${e.message}"
            }
        }
    }

    fun removeSearchHistory(searchedAirport: Airport) {
        viewModelScope.launch {
            try {
                searchHistoryRepository.removeSearchQuery(searchedAirport.iataCode)
                _searchHistory.value =
                    searchHistoryRepository.searchHistory.firstOrNull() ?: emptySet()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove search history: ${e.message}"
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
        try {
            val favoriteFound =
                flightRepository.findFavorite(favorite.departureCode, favorite.destinationCode)
                    .firstOrNull()
            if (favoriteFound != null) {
                flightRepository.deleteFavoriteFlight(favoriteFound)
            } else {
                flightRepository.addFavoriteFlight(favorite)
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to toggle favorite: ${e.message}"
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}