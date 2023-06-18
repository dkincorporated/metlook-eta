package dev.dkong.metlook.eta.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.composables.HeadlineToggleableSettingsItem
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.SettingsInfoFootnote
import dev.dkong.metlook.eta.composables.SettingsInfoText

@Composable
fun LocationFeaturesScreen(navHostController: NavHostController) {
    LargeTopAppbarScaffold(navController = navHostController, title = "Location-based features") {
        item {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Image(
                    painterResource(id = R.drawable.custom_settings_location),
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item {
            val isLocationFeaturesEnabled = remember { mutableStateOf(false) }
            HeadlineToggleableSettingsItem(
                name = "Use location-based features",
                checked = isLocationFeaturesEnabled
            )
        }
        item {
            SettingsInfoText(info = "metlook can use your location information to provide location-based features that improve the user experience and add extra functionality.")
        }
        item {
            SettingsInfoFootnote(info = "Location data is not stored anywhere and is only transmitted to PTV to obtain transport data.")
        }
    }
}