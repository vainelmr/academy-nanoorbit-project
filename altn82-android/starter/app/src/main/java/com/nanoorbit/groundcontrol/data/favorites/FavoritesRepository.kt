package com.nanoorbit.groundcontrol.data.favorites

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nano_orbit_favorites"
)

private val KEY_FAVORITES = stringSetPreferencesKey("satellite_ids")

class FavoritesRepository(context: Context) {
    private val dataStore = context.applicationContext.favoritesDataStore

    val favoriteSatelliteIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY_FAVORITES] ?: emptySet()
    }

    suspend fun toggleFavorite(satelliteId: String) {
        dataStore.edit { prefs ->
            val current = (prefs[KEY_FAVORITES] ?: emptySet()).toMutableSet()
            if (!current.add(satelliteId)) current.remove(satelliteId)
            prefs[KEY_FAVORITES] = current
        }
    }
}
