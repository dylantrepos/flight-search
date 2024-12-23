package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchHistoryRepository(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")
    private val dataStore = context.dataStore

    val searchHistory: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SEARCH_HISTORY_KEY] ?: emptySet()
        }

    suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEY] ?: emptySet()
            preferences[SEARCH_HISTORY_KEY] = currentHistory + query

        }
    }

    suspend fun removeSearchQuery(query: String) {
        dataStore.edit { preferences ->
            val currentHistory = preferences[SEARCH_HISTORY_KEY] ?: emptySet()
            preferences[SEARCH_HISTORY_KEY] = currentHistory - query
        }
    }

    companion object {
        private val SEARCH_HISTORY_KEY = stringSetPreferencesKey("search_history")
    }
}