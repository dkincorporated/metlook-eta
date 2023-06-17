package dev.dkong.metlook.eta.screens

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

/**
 * Set of screens at the root level (controlled by MainActivity)
 * @param route the navigation route path
 * @param name the display name of the screen
 */
sealed class RootScreen (val route: String, val name: String) {
    object Home : RootScreen("home", "Home")

    object Settings : RootScreen("settings", "Settings")
}