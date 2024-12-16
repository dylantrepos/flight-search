package com.example.flightsearch.data

import kotlinx.coroutines.flow.Flow

class FlightRepository(
    private val airportDao: AirportDao
) {
    fun fetchAllAirports(): Flow<List<Airport>> = airportDao.fetchAllAirports()

    fun getAirportDetails(iata: String): Flow<Airport> = airportDao.getAirportDetails(iata)

    fun getAirportTimetable(airportName: String): Flow<List<Airport>> =
        airportDao.getAirportTimetable(airportName)

    fun getFavoriteFlights(): Flow<List<Favorite>> = airportDao.getFavoriteFlights()

    suspend fun addFavoriteFlight(flight: Favorite) =
        airportDao.addFavoriteFlight(flight)

    suspend fun deleteFavoriteFlight(favorite: Favorite) =
        airportDao.removeFavoriteFlight(favorite.id)

    fun findFavorite(departureCode: String, destinationCode: String) =
        airportDao.findFavorite(departureCode, destinationCode)
}