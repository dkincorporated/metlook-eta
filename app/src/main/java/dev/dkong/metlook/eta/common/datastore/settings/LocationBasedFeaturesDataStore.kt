package dev.dkong.metlook.eta.common.datastore.settings

import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * Coordinator for 'Location-based features'
 */
object LocationBasedFeaturesDataStore :
    SettingsDataStore<Boolean>(
        booleanPreferencesKey("location_features")
    )
