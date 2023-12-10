package dev.dkong.metlook.eta.screens.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold

/**
 * Unified settings screen for recent stops and stations, and recent services
 * @param navHostController the nav controller for the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsSettingsScreen(navHostController: NavHostController) {
    var selectedPage by remember { mutableIntStateOf(0) }
    val pages = arrayOf("Stops", "Services")

    LargeTopAppbarScaffold(
        navController = navHostController,
        title = "Dashboard recents",
        horizontalPadding = 16.dp
    ) {
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                pages.forEachIndexed { index, page ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = pages.size
                        ),
                        onClick = {
                            selectedPage = index
                        },
                        selected = selectedPage == index,
                        icon = {}
                    ) {
                        Text(text = page, maxLines = 1)
                    }
                }
            }
        }
    }
}