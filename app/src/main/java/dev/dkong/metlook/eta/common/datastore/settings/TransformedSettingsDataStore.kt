package dev.dkong.metlook.eta.common.datastore.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences

/**
 * Blueprint for settings-type data store *with transformation between data and stored data*
 * @param T the type of input data to be saved
 * @param U the type of data in which [T] will be saved
 * @param transformedKey the [Preferences.Key] for the transformed type [U]
 * @see SettingsDataStore
 */
abstract class TransformedSettingsDataStore<T, U>(
    transformedKey: Preferences.Key<U>
) : SettingsDataStore<U>(transformedKey) {
    /**
     * Serialise/transform the data from input to stored type
     */
    abstract fun serialise(data: T): U

    /**
     * Deserialise/transform the data from the stored type to output
     */
    abstract fun deserialise(data: U?): T

    /**
     * Overwrite the current value (if any)
     * @param data the new data to be stored
     */
    suspend fun update(context: Context, data: T) =
        super.update(context, serialise(data))

    /**
     * Get the current value
     */
    suspend fun get(context: Context): T? =
        deserialise(super.getOnce(context))

    /**
     * Register a listener to listen for new changes
     * @param listener the function to receive new values
     */
    suspend fun listen(context: Context, listener: (T) -> Unit) {
        super.listen(context) { t -> listener(deserialise(t)) }
    }
}