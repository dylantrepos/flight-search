package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

class FlightRepository(
    private val airportDao: AirportDao
) {
    fun getAllAirport(): Flow<List<Airport>> = airportDao.getAllAirport()

    fun getAirportDetails(iapa: String): Flow<Airport> = airportDao.getAirportDetails(iapa)

    fun getAirportTimetable(airportName: String): Flow<List<Airport>> =
        airportDao.getAirportTimetable(airportName)

    fun getFavoriteFlight(): Flow<List<Favorite>> = airportDao.getFavoriteFlight()

    suspend fun addFavoriteFlight(flight: Favorite) =
        airportDao.addFavoriteFlight(flight)

    suspend fun deleteFavoriteFlight(favorite: Favorite) =
        airportDao.removeFavoriteFlight(favorite.id)

    fun findFavorite(departureCode: String, destinationCode: String) =
        airportDao.findFavorite(departureCode, destinationCode)
}

