package dev.dkong.metlook.eta.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.Utils.finishActivity
import dev.dkong.metlook.eta.common.ventura.TrackerManager
import dev.dkong.metlook.eta.composables.BetterListItem
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold

/**
 * Main settings screen for the app
 * @param navHostController the nav controller for the app
 */
@Composable
fun SettingsScreen(navHostController: NavHostController) {
    val context = LocalContext.current

    // Define the settings pages
    /**
     * Record for a settings page
     */
    data class SettingsPage(
        val title: String,
        val subtitle: String? = null,
        @DrawableRes val icon: Int,
        val onClick: () -> Unit
    )

    val pages = arrayOf(
        SettingsPage(
            "Location-based features",
            "Decide whether and how your location is used",
            R.drawable.outline_location_on_24
        ) { navHostController.navigate(SettingsScreens.LocationFeatures.route) },
        SettingsPage(
            "Dashboard recents",
            "Save recent stops, stations and services",
            R.drawable.baseline_history_24
        ) { navHostController.navigate(SettingsScreens.Recents.route) },
        SettingsPage(
            TrackerManager.integrationSettingsName,
            TrackerManager.integrationSettingsDescription,
            R.drawable.outline_dataset_linked_24
        ) { navHostController.navigate(SettingsScreens.TrackerIntegration.route) }
    )

    LargeTopAppbarScaffold(
        navController = navHostController,
        title = "Settings",
        onNavigationIconClick = {
            // Exit the activity
            context.finishActivity()
        }
    ) {
        pages.forEachIndexed { index, page ->
            item {
                BetterListItem(
                    headlineContent = {
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    supportingContent = {
                        page.subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = {
                        Box(
                            modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Image(
                                painter = painterResource(id = page.icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                        .clip(ListPosition.fromPosition(index, pages.size).roundedShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { page.onClick() }
                )
            }
        }
    }
}