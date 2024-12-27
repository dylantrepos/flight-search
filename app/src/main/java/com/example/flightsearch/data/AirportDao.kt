package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flightsearch.model.Airport
import com.example.flightsearch.model.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE :iata
        """
    )
    fun getAirportDetails(iata: String): Flow<Airport>

    @Query(
        """
        SELECT * FROM airport
        ORDER BY passengers DESC
        """
    )
    fun fetchAllAirports(): Flow<List<Airport>>

    @Query(
        """
        SELECT * FROM airport
        WHERE name LIKE '%' || :airportName || '%'
        OR iata_code LIKE '%' || :airportName || '%'
        """
    )
    fun getAirportTimetable(airportName: String): Flow<List<Airport>>

    @Query(
        """
        SELECT * FROM favorite
        ORDER BY id ASC
        """
    )
    fun getFavoriteFlights(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteFlight(favorite: Favorite)

    @Query("DELETE FROM favorite WHERE id = :id")
    suspend fun removeFavoriteFlight(id: Int)

    @Query(
        """
        SELECT * FROM favorite
        WHERE departure_code = :departureCode
        AND destination_code = :destinationCode
        LIMIT 1
        """
    )
    fun findFavorite(departureCode: String, destinationCode: String): Flow<Favorite?>
}