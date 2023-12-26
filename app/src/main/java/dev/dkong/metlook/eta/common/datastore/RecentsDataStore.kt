package dev.dkong.metlook.eta.common.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.Constants.dataStoreRecents
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString

/**
 * Blueprint for any Recent-type data store
 */
abstract class RecentsDataStore<T>(
    val recentsKey: Preferences.Key<String>,
    val limitKey: Preferences.Key<Int>
) {
    /**
     * Add a new recent item
     * @param newItem the new [T] to add
     * @return whether the addition was successful
     */
    suspend fun add(context: Context, newItem: T): Boolean {
        val current = getOnce(context) ?: return false
        // TODO: Integrate limit settings
        val limit = 5
        val combined =
            (listOf(newItem) + current).slice(0..(minOf(limit, current.size + 1)))
        return try {
            context.dataStoreRecents.edit { preferences ->
                preferences[recentsKey] = Constants.jsonFormat.encodeToString<List<T>>(combined)
            }
            true
        } catch (e: SerializationException) {
            Log.d("RECENT STOPS", e.toString())
            false
        }
    }

    /**
     * Get the current recent items
     * @return list of current items, or null if something went wrong
     */
    suspend fun getOnce(context: Context): List<T>? {
        val retrieved =
            context.dataStoreRecents.data
                .first()[recentsKey]
                ?: return null
        return try {
            Constants.jsonFormat.decodeFromString<List<T>>(retrieved)
        } catch (e: SerializationException) {
            Log.d("RECENT STOPS", e.toString())
            null
        }
    }

    /**
     * Listen for new recent items
     * @param listener the function that will be fired when a new value arrives
     */
    suspend fun listen(context: Context, listener: (List<T>) -> Unit) {
        context.dataStoreRecents.data
            .map { preferences ->
                preferences[recentsKey]
            }
            .collect { value ->
                value?.let {
                    try {
                        listener(
                            Constants.jsonFormat.decodeFromString<List<T>>(it)
                        )
                    } catch (e: SerializationException) {
                        Log.d("RECENT STOPS", e.toString())
                    }
                }
            }
    }
}