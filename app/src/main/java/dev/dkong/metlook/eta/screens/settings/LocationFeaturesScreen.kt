package dev.dkong.metlook.eta.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.datastore.settings.LocationBasedFeaturesDataStore
import dev.dkong.metlook.eta.composables.HeadlineToggleableSettingsItem
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.SettingsInfoFootnote
import dev.dkong.metlook.eta.composables.SettingsInfoText
import kotlinx.coroutines.launch

/**
 * Settings screen for location-based features
 * @param navHostController the nav controller for the app
 */
@Composable
fun LocationFeaturesScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialise the variable; update the value later
    val isLocationFeaturesEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Listen for new changes
        LocationBasedFeaturesDataStore.listen(context) { value ->
            isLocationFeaturesEnabled.value = value
        }
    }

    LargeTopAppbarScaffold(navController = navHostController, title = "Location-based features") {
        item {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Box {
                    Image(
                        painterResource(id = R.drawable.custom_settings_location_background),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(16.dp)
                    )
                    Image(
                        painterResource(id = R.drawable.custom_settings_location_foreground_pin),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(16.dp)
                    )
                    Image(
                        painterResource(id = R.drawable.custom_settings_location_foreground_transport),
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        item {
            HeadlineToggleableSettingsItem(
                name = "Use location-based features",
                checked = isLocationFeaturesEnabled,
                onCheckedChange = {
                    scope.launch {
                        // Update the value
                        LocationBasedFeaturesDataStore.update(context, it)
                    }
                }
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