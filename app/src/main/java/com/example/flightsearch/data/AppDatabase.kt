package com.example.flightsearch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flightsearch.model.Airport
import com.example.flightsearch.model.Favorite

@Database(entities = [Airport::class, Favorite::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun airportDao(): AirportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of the database.
         * @param context The application context.
         * @return The singleton instance of the database.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val dbFile = context.getDatabasePath("app_database")
                    val builder = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app_database"
                    ).fallbackToDestructiveMigration()

                    if (!dbFile.exists()) {
                        builder.createFromAsset("database/flight_search.db")
                    }

                    builder.build().also {
                        INSTANCE = it
                    }
                } catch (e: Exception) {
                    throw RuntimeException("Error creating database", e)
                }
            }
        }
    }
}