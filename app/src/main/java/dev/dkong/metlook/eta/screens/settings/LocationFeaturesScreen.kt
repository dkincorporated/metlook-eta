package dev.dkong.metlook.eta.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.HeadlineToggleableSettingsItem
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.SettingsInfoFootnote
import dev.dkong.metlook.eta.composables.SettingsInfoText

@Composable
fun LocationFeaturesScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(navController = navHostController, title = "Location-based features") {
        item {
            val isLocationFeaturesEnabled = remember { mutableStateOf(false) }
            HeadlineToggleableSettingsItem(
                name = "Use location-based features",
                checked = isLocationFeaturesEnabled
            )
        }
        item {
            SettingsInfoFootnote(info = "metlook can use your location information to provide location-based features that improve the user experience and add extra functionality.\nLocation data is not stored anywhere and is only transmitted to PTV to obtain transport data.")
        }
    }
}