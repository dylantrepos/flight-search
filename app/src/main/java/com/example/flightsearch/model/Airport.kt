package com.example.flightsearch.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an airport entity in the database.
 * @property id The unique identifier of the airport.
 * @property name The name of the airport.
 * @property iataCode The IATA code of the airport.
 * @property passengers The number of passengers at the airport.
 */
@Entity(tableName = "airport")
data class Airport(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "iata_code") val iataCode: String,
    @ColumnInfo(name = "passengers") val passengers: Int
)

/**
 * Represents a favorite flight entity in the database.
 * @property id The unique identifier of the favorite flight.
 * @property departureCode The departure code of the flight.
 * @property destinationCode The destination code of the flight.
 */
@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "departure_code") val departureCode: String,
    @ColumnInfo(name = "destination_code") val destinationCode: String
)

/**
 * Represents an airport timetable.
 * @property departure The departure airport.
 * @property arrival The arrival airport.
 * @property isFavorite Indicates if the timetable is marked as favorite.
 */
data class AirportTimetable(
    val departure: Airport,
    val arrival: Airport,
    val isFavorite: Boolean = false
)