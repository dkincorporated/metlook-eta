package dev.dkong.metlook.eta.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.datastore.settings.RecentsSettingsDataStore
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
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

    val stopsScope = rememberCoroutineScope()
    var stopsLimit by remember { mutableIntStateOf(RecentsSettingsDataStore.recentsCountLimit) }
    val servicesScope = rememberCoroutineScope()
    var servicesLimit by remember { mutableIntStateOf(RecentsSettingsDataStore.recentsCountLimit) }
    val timeScope = rememberCoroutineScope()
    var timeLimit by remember { mutableIntStateOf(RecentsSettingsDataStore.defaultTimeLimit) }

    LaunchedEffect(Unit) {
        stopsScope.launch {
            RecentsSettingsDataStore.stopsLimit.listen(context) { stopsLimit = it }
        }
        servicesScope.launch {
            RecentsSettingsDataStore.servicesLimit.listen(context) { servicesLimit = it }
        }
        timeScope.launch {
            RecentsSettingsDataStore.timeLimit.listen(context) { timeLimit = it }
        }
    }

    @Composable
    fun LimitItem(
        title: String,
        current: Int,
        range: ClosedFloatingPointRange<Float>,
        onChange: (Int) -> Unit
    ) {
        val sliderPosition = current.toFloat()

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
            valueRange = range,
            steps = (range.endInclusive / maxOf(1f, range.start)).roundToInt(),
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
                The number of recent items can be adjusted to suit your taste. More stops? No problem. More services? You got it.
            """.trimIndent(),
                horizontalPadding = 0.dp
            )
        }
        item {
            LimitItem(
                title = "Stops and stations",
                current = stopsLimit,
                range = 0f..10f
            ) { limit ->
                scope.launch {
                    RecentsSettingsDataStore.stopsLimit.update(context, limit)
                }
            }
        }
        item {
            LimitItem(title = "Services", current = servicesLimit, range = 0f..10f) { limit ->
                scope.launch {
                    RecentsSettingsDataStore.servicesLimit.update(context, limit)
                }
            }
        }
        item {
            SettingsInfoText(
                info = """
                To help reduce clutter, services that departed a certain length of time ago can be automatically removed.
            """.trimIndent(),
                horizontalPadding = 0.dp
            )
        }
        item {
            val sliderPosition = timeLimit.toFloat()

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SectionHeading(heading = "Retention time", includePadding = false)
                Text(
                    text = "${sliderPosition.roundToInt()} hr",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Slider(
                value = sliderPosition,
                onValueChange = { v ->
                    // Update the value as it is selected
                    scope.launch {
                        RecentsSettingsDataStore.timeLimit.update(context, v.roundToInt())
                    }
                },
                valueRange = 1f..24f,
                steps = 25,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        item {
            SettingsInfoFootnote(
                info = """
                Changes to limits are reflected the next time they are loaded.
                
                Pinned items will not be removed by any limits, but are still counted in the limits.
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
