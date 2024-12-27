package com.example.flightsearch.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val flightRepository: FlightRepository
}

/**
 * Implementation of [AppContainer] that provides dependencies.
 * @param context The application context.
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Lazily initialized instance of [FlightRepository].
     */
    override val flightRepository: FlightRepository by lazy {
        try {
            FlightRepository(AppDatabase.getDatabase(context).airportDao())
        } catch (e: Exception) {
            throw RuntimeException("Error initializing FlightRepository", e)
        }
    }
}