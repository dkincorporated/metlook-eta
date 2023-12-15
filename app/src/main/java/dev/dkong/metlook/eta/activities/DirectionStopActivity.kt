package dev.dkong.metlook.eta.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.PersistentBottomSheetScaffold
import dev.dkong.metlook.eta.composables.TextMetLabel
import dev.dkong.metlook.eta.composables.TwoLineCenterTopAppBarText
import dev.dkong.metlook.eta.objects.ptv.Direction
import dev.dkong.metlook.eta.objects.ptv.Stop
import dev.dkong.metlook.eta.ui.theme.MetlookTheme

/**
 * Activity for departures of a direction for a stop
 * @see StopActivity
 */
class DirectionStopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive Stop and Direction info from Bundle
        val bundleStop = intent.extras?.getString("stop")
        val bundleDirection = intent.extras?.getString("direction")
        bundleStop?.let { stopString ->
            bundleDirection?.let { directionString ->
                // Parse the Stop and Direction
                val stop = Constants.jsonFormat.decodeFromString<Stop>(stopString)
                val direction = Constants.jsonFormat.decodeFromString<Direction>(directionString)

                // Render the UI
                setContent {
                    MetlookTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Constants.appSurfaceColour() // wait until new Surface
                        ) {
                            val navController = rememberNavController()
                            DirectionStopScreen(navController, stop, direction)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DirectionStopScreen(
        navHostController: NavHostController,
        stop: Stop,
        direction: Direction
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scaffoldState = rememberBottomSheetScaffoldState()
        var topBarHeight by remember { mutableStateOf(0.dp) }

        val stopName = stop.stopName()

        PersistentBottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TwoLineCenterTopAppBarText(
                                title = direction.directionName,
                                subtitle = stop.fullStopName
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor =
                        if (with(scaffoldState.bottomSheetState) {
                                currentValue == SheetValue.Expanded
                                        && targetValue == SheetValue.Expanded
                            })
                            Constants.scrolledAppbarContainerColour()
                        else Constants.appSurfaceColour()
                    ),
                    navigationIcon = {
                        ElevatedAppBarNavigationIcon(onClick = {
                            // Finish the Activity
                            (context as? Activity)?.finish()
                        })
                    },
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                topBarHeight = coordinates.size.height.toDp()
                            }
                        }
                )
            },
            topBarHeight = topBarHeight,
            mainContent = {
                // TODO: Map
            },
            sheetContent = {

            },
            sheetPeekHeight = 512.dp
        )
    }
}