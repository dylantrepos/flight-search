package com.example.flightsearch

import android.app.Application
import com.example.flightsearch.data.AppContainer
import com.example.flightsearch.data.AppDataContainer
import com.example.flightsearch.data.AppDatabase

class FlightSearchApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}