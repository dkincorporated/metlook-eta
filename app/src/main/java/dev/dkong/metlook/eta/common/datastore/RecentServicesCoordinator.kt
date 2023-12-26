package dev.dkong.metlook.eta.common.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import kotlinx.serialization.encodeToString

/**
 * Manage recent [ServiceDeparture]s
 */
object RecentServicesCoordinator : RecentsDataStore<ServiceDeparture>(
    stringPreferencesKey("recent_services"),
    intPreferencesKey("recent_services")
) {
    override fun serialise(data: List<ServiceDeparture>): String =
        Constants.jsonFormat.encodeToString(data)

    override fun deserialise(data: String): List<ServiceDeparture> =
        Constants.jsonFormat.decodeFromString(data)
}