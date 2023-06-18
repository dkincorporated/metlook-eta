package dev.dkong.metlook.eta.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold

@Composable
fun RecentStopsSettingsScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(
        navController = navHostController,
        title = "Recent stops and stations"
    ) {

    }
}

@Composable
fun RecentServicesSettingsScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(
        navController = navHostController,
        title = "Recent services"
    ) {

    }
}