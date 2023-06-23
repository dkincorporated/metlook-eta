package dev.dkong.metlook.eta.screens.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.tracker.TrackerManager
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.MaterialColumn
import dev.dkong.metlook.eta.composables.SettingsItem

/**
 * Main settings screen for the app
 * @param navHostController the nav controller for the app
 */
@Composable
fun SettingsScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(navController = navHostController, title = "Settings") {
        item {
            MaterialColumn(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                SettingsItem(
                    name = "Location-based features",
                    description = "Decide whether/how your location is used",
                    onClick = {
                        navHostController.navigate(SettingsScreens.LocationFeatures.route)
                    },
                    iconHasCircle = true,
                    icon = R.drawable.outline_location_on_24
                )
                SettingsItem(
                    name = "Dashboard recents",
                    description = "Save recent stops, stations and services",
                    onClick = {
                        navHostController.navigate(SettingsScreens.Recents.route)
                    },
                    iconHasCircle = true,
                    icon = R.drawable.baseline_history_24
                )
                SettingsItem(
                    name = TrackerManager.integrationSettingsName,
                    description = TrackerManager.integrationSettingsDescription,
                    onClick = {
                        navHostController.navigate(SettingsScreens.TrackerIntegration.route)
                    },
                    iconHasCircle = true,
                    icon = R.drawable.outline_dataset_linked_24
                )
            }
        }
    }
}