package dev.dkong.metlook.eta.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold

/**
 * Unified settings screen for recent stops and stations, and recent services
 * @param navHostController the nav controller for the app
 */
@Composable
fun RecentsSettingsScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(
        navController = navHostController,
        title = "Dashboard recents"
    ) {

    }
}