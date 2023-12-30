package dev.dkong.metlook.eta.common.datastore.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.dkong.metlook.eta.common.Constants.dataStoreSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Blueprint for any single-valued data store
 *
 * Data will be stored in [dev.dkong.metlook.eta.common.Constants.dataStoreSettings].
 * @param K type of data being stored
 */
abstract class SettingsDataStore<K>(
    val key: Preferences.Key<K>,
) {
    /**
     * Overwrite the current value
     * @param data new value
     */
    suspend fun update(context: Context, data: K) =
        context.dataStoreSettings.edit { preferences ->
            preferences[key] = data
        }

    /**
     * Get the current value once
     * @return null if a value has never been set
     */
    suspend fun getOnce(context: Context): K? =
        context.dataStoreSettings
            .data
            .first()[key]

    /**
     * Register a listener to listen for new values
     * @param listener the function to receive the new values
     */
    suspend fun listen(context: Context, listener: (K) -> Unit) =
        context.dataStoreSettings.data
            .map { preferences ->
                preferences[key]
            }
            .collect { value ->
                value?.let { listener(it) }
            }
}