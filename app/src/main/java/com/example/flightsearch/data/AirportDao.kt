package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {
    @Query(
        """
        SELECT * FROM airport 
        ORDER BY passengers DESC
    """
    )
    fun getAll(): Flow<List<Airport>>

    @Query(
        """
        SELECT * FROM airport
        WHERE name LIKE '%' || :airportName || '%'
    """
    )
    fun searchAirport(airportName: String): Flow<List<Airport>>
}