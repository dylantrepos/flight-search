package com.example.flightsearch

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.SearchHistoryRepository
import com.example.flightsearch.ui.screens.AirportViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = flightSearchApplication()
            val airportDao = application.database.airportDao()
            val flightRepository = FlightRepository(airportDao)
            val searchHistoryRepository = SearchHistoryRepository(application)
            AirportViewModel(flightRepository, searchHistoryRepository)
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [FlightSearchApplication].
 */
fun CreationExtras.flightSearchApplication(): FlightSearchApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as FlightSearchApplication)
