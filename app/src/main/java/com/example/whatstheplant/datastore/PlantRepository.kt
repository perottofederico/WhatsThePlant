package com.example.whatstheplant.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private const val SCANNED_PLANT_COUNT_KEY = "scanned_plant_count"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plant_preferences")

class PlantRepository(context: Context) {
    private val dataStore = context.dataStore

    // Save the scanned count
    suspend fun saveScannedCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(SCANNED_PLANT_COUNT_KEY)] = count
        }
    }

    // Get the scanned count
    val scannedCountFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[intPreferencesKey(SCANNED_PLANT_COUNT_KEY)] ?: 0
        }
}