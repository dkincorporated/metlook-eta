package dev.dkong.metlook.eta.screens

/**
 * Set of screens at the root level (controlled by MainActivity)
 * @param route the navigation route path
 * @param name the display name of the screen
 */
sealed class RootScreen (route: String, name: String) : Screen(route, name) {
    object Home : RootScreen("home", "Home")

    object Settings : RootScreen("settings", "Settings")
}