package dev.dkong.metlook.eta.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.SectionHeading
import kotlin.math.roundToInt

/**
 * Enumeration of the types of Recent items
 * @param qtyPreferenceKey preference key for number of recent items to be saved
 */
enum class RecentsType(val qtyPreferenceKey: String) {
    Stop("recent_stop_count"),
    Service("recent_service_count")
}

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
        item {
            var sliderPosition by remember { mutableFloatStateOf(0f) }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Max number of items",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text =
                        if (sliderPosition == 0f) "Disabled"
                        else sliderPosition.roundToInt().toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Slider(
                value = sliderPosition,
                onValueChange = { v -> sliderPosition = v },
                valueRange = 0f..10f,
                onValueChangeFinished = {
                    // TODO: Update preferences
                },
                steps = 9,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Preview
 */
@Preview
@Composable
fun PreviewRecentsSettings() {
    RecentsSettingsScreen(navHostController = rememberNavController())
}
