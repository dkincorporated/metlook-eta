package dev.dkong.metlook.eta.screens.settings

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.datastore.settings.RecentsSettingsDataStore
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.SettingsInfoFootnote
import dev.dkong.metlook.eta.composables.SettingsInfoText
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentLimits by remember { mutableStateOf(Pair(0, 0)) }

    LaunchedEffect(Unit) {
        RecentsSettingsDataStore.listenTransformed(context) { data: Pair<Int, Int> ->
            currentLimits = data
        }
    }

    @Composable
    fun LimitItem(title: String, limit: Int, onChange: (Int) -> Unit) {
        val sliderPosition = limit.toFloat()

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            SectionHeading(heading = title, includePadding = false)
            Text(
                text =
                if (sliderPosition == 0f) "Disabled"
                else sliderPosition.roundToInt().toString(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Slider(
            value = sliderPosition,
            onValueChange = { v ->
                // Update the value as it is selected
                onChange(v.roundToInt())
            },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier
                .fillMaxWidth()
        )
    }

    LargeTopAppbarScaffold(
        navController = navHostController,
        title = "Dashboard recents",
        horizontalPadding = 16.dp
    ) {
        item {
            SettingsInfoText(
                info = """
                The number of recent items and their ratio can be adjusted to best fit the screen dimensions.
            """.trimIndent(),
                horizontalPadding = 0.dp
            )
        }
        item {
            LimitItem(title = "Stops and stations", currentLimits.first) { limit ->
                scope.launch {
                    RecentsSettingsDataStore.updateTransformed(
                        context,
                        Pair(limit, currentLimits.second)
                    )
                }
            }
        }
        item {
            LimitItem(title = "Services", currentLimits.second) { limit ->
                scope.launch {
                    RecentsSettingsDataStore.updateTransformed(
                        context,
                        Pair(currentLimits.first, limit)
                    )
                }
            }
        }
        item {
            SettingsInfoFootnote(
                info = """
                If a lower limit is selected, the existing items that exceed the limit are not deleted right away, and are instead updated when a new entry is next added.
            """.trimIndent(),
                horizontalPadding = 0.dp
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
