package dev.dkong.metlook.eta.screens.settings

import dev.dkong.metlook.eta.common.ventura.TrackerManager
import dev.dkong.metlook.eta.screens.Screen

sealed class SettingsScreens(route: String, name: String) : Screen("settings/$route", name) {
    object LocationFeatures : SettingsScreens("location", "Location-based features")
    object Recents : SettingsScreens("recents", "Dashboard recents")
    object TrackerIntegration : SettingsScreens("tracker_integration", TrackerManager.integrationSettingsName)
}