package dev.dkong.metlook.eta.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.MaterialColumn
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.SettingsItem
import dev.dkong.metlook.eta.composables.SwitchSettingsItem

@Composable
fun SettingsScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(navController = navHostController, title = "Settings") {
        item {
            SectionHeading(heading = "Privacy")
        }
        item {
            MaterialColumn {
                val isLocationChecked = remember { mutableStateOf(true) }
                SwitchSettingsItem(
                    name = "Location-based features",
                    description = "Decide whether/how your location is used",
                    onClick = {
                        navHostController.navigate("settings/location")
                    },
                    selected = isLocationChecked
                )
            }
        }
        item {
            SectionHeading(heading = "Dashboard")
        }
        item {
            MaterialColumn {
                val isRecentStopsChecked = remember { mutableStateOf(true) }
                SwitchSettingsItem(
                    name = "Recent stops and stations",
                    onClick = {},
                    description = "Save 5 recent stops and stations",
                    selected = isRecentStopsChecked
                )

                val isRecentServicesChecked = remember { mutableStateOf(true) }
                SwitchSettingsItem(
                    name = "Recent services",
                    onClick = {},
                    description = "Save 5 recent services",
                    selected = isRecentServicesChecked
                )
            }
        }

    }
}