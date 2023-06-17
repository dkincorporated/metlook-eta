package dev.dkong.metlook.eta.screens.home

import androidx.annotation.DrawableRes
import dev.dkong.metlook.eta.R

/**
 * Screen objects for the home screen
 * @param route the navigation route path
 * @param displayName the user-facing name of the screen
 * @param icon the icon of the screen when unselected
 * @param selectedIcon the icon of the screen when selected
 */
enum class HomeScreenItem(
    val route: String,
    val displayName: String,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int
) {
    Dashboard(
        "home/dashboard",
        "Dashboard",
        R.drawable.outline_space_dashboard_24,
        R.drawable.baseline_space_dashboard_24
    ),
    Navigation(
        "home/navigation",
        "Go",
        R.drawable.outline_navigation_24,
        R.drawable.baseline_navigation_24
    ),
    Updates(
        "home/updates",
        "Updates",
        R.drawable.outline_message_24,
        R.drawable.baseline_message_24
    )
}
