package dev.dkong.metlook.eta.screens.settings

import dev.dkong.metlook.eta.screens.Screen

sealed class SettingsScreens(route: String, name: String) : Screen("settings/$route", name) {
    object LocationFeatures : SettingsScreens("location", "Location-based features")
    object RecentStops : SettingsScreens("recent_stops", "Recent stops and stations")
    object RecentServices : SettingsScreens("recent_services", "Recent services")
}