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

    /**
     * Retrieves the details of an airport by its IATA code.
     * @param iata The IATA code of the airport.
     * @return A Flow emitting the airport details.
     */
    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE :iata
        """
    )
    fun getAirportDetails(iata: String): Flow<Airport>

    /**
     * Fetches all airports ordered by the number of passengers.
     * @return A Flow emitting the list of airports.
     */
    @Query(
        """
        SELECT * FROM airport
        ORDER BY passengers DESC
        """
    )
    fun fetchAllAirports(): Flow<List<Airport>>

    /**
     * Retrieves the timetable of an airport by its name or IATA code.
     * @param airportName The name or IATA code of the airport.
     * @return A Flow emitting the list of airports matching the query.
     */
    @Query(
        """
        SELECT * FROM airport
        WHERE name LIKE '%' || :airportName || '%'
        OR iata_code LIKE '%' || :airportName || '%'
        """
    )
    fun getAirportTimetable(airportName: String): Flow<List<Airport>>

    /**
     * Retrieves all favorite flights ordered by their ID.
     * @return A Flow emitting the list of favorite flights.
     */
    @Query(
        """
        SELECT * FROM favorite
        ORDER BY id ASC
        """
    )
    fun getFavoriteFlights(): Flow<List<Favorite>>

    /**
     * Adds a favorite flight to the database.
     * @param favorite The favorite flight to add.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteFlight(favorite: Favorite)

    /**
     * Removes a favorite flight from the database by its ID.
     * @param id The ID of the favorite flight to remove.
     */
    @Query("DELETE FROM favorite WHERE id = :id")
    suspend fun removeFavoriteFlight(id: Int)

    /**
     * Finds a favorite flight by its departure and destination codes.
     * @param departureCode The departure code of the flight.
     * @param destinationCode The destination code of the flight.
     * @return A Flow emitting the favorite flight if found.
     */
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