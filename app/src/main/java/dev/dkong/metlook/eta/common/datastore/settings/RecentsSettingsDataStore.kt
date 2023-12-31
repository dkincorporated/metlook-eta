package dev.dkong.metlook.eta.common.datastore.settings

import androidx.datastore.preferences.core.intPreferencesKey

/**
 * Coordinator for recents-related settings
 */
object RecentsSettingsDataStore {
    /**
     * Coordinator for number of stops to be stored
     */
    val stopsLimit =
        object : SettingsDataStore<Int>(intPreferencesKey("recent_stops_limit")) {}

    /**
     * Coordinator for number of services to be stored
     */
    val servicesLimit =
        object : SettingsDataStore<Int>(intPreferencesKey("recent_services_limit")) {}

    /**
     * Coordinator for the time to retain services
     */
    val timeLimit =
        object : SettingsDataStore<Int>(intPreferencesKey("recent_services_time")) {}
}