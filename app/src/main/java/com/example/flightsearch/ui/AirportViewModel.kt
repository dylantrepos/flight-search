package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.AirportDao
import com.example.flightsearch.data.AirportTimetable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AirportViewModel(private val airportDao: AirportDao) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _airportTimetable = MutableStateFlow<List<AirportTimetable>>(emptyList())
    val airportTimetable: StateFlow<List<AirportTimetable>> = _airportTimetable

    @OptIn(ExperimentalCoroutinesApi::class)
    val airports: StateFlow<List<Airport>> = _query.flatMapLatest { query ->
        if (query.isEmpty()) {
            getAllAirport().map { it }
        } else {
            airportDao.searchAirport("%$query%").map { it }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
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
                _airportTimetable.value = airportList.map { arrival ->
                    AirportTimetable(
                        departure = airport,
                        arrival = arrival
                    )
                }
            }
        }
    }

    private fun getAllAirport(): Flow<List<Airport>> = airportDao.getAll()

    private fun searchAirportName(airportName: String): Flow<List<Airport>> =
        airportDao.searchAirport(airportName)

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                AirportViewModel(application.database.airportDao())
            }
        }
    }
}

