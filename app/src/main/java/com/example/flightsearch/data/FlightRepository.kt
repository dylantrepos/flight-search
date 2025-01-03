package com.example.flightsearch.data

import com.example.flightsearch.model.Airport
import com.example.flightsearch.model.Favorite
import kotlinx.coroutines.flow.Flow

class FlightRepository(
    private val airportDao: AirportDao,
) {
    fun fetchAllAirports(): Flow<List<Airport>> = airportDao.fetchAllAirports()

    fun getAirportDetails(iata: String): Flow<Airport> = airportDao.getAirportDetails(iata)

    fun getAirportTimetable(airportName: String): Flow<List<Airport>> =
        airportDao.getAirportTimetable(airportName)

    fun getFavoriteFlights(): Flow<List<Favorite>> = airportDao.getFavoriteFlights()

    /**
     * Adds a favorite flight.
     * @param flight The favorite flight to add.
     */
    suspend fun addFavoriteFlight(flight: Favorite) {
        airportDao.addFavoriteFlight(flight)
    }

    /**
     * Deletes a favorite flight.
     * @param favorite The favorite flight to delete.
     */
    suspend fun deleteFavoriteFlight(favorite: Favorite) {
        airportDao.removeFavoriteFlight(favorite.id)
    }

    fun findFavorite(departureCode: String, destinationCode: String): Flow<Favorite?> =
        airportDao.findFavorite(departureCode, destinationCode)
}