package com.example.flightsearch.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val flightRepository: FlightRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
    override val flightRepository: FlightRepository by lazy {
        FlightRepository(AppDatabase.getDatabase(context).airportDao())
    }
}