package dev.dkong.metlook.eta.common.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.dkong.metlook.eta.objects.ptv.Stop

/**
 * Manage recent [Stop]s
 */
object RecentStopsCoordinator : RecentsDataStore<Stop>(
    stringPreferencesKey("recent_stops"),
    intPreferencesKey("recent_stops")
)