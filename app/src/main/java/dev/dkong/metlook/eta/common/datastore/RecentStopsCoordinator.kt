package dev.dkong.metlook.eta.common.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.objects.ptv.Stop
import kotlinx.serialization.encodeToString

/**
 * Manage recent [Stop]s
 */
object RecentStopsCoordinator : RecentsDataStore<Stop>(
    stringPreferencesKey("recent_stops"),
    intPreferencesKey("recent_stops")
) {
    override fun serialise(data: List<Stop>): String =
        Constants.jsonFormat.encodeToString(data)

    override fun deserialise(data: String): List<Stop> =
        Constants.jsonFormat.decodeFromString(data)
}