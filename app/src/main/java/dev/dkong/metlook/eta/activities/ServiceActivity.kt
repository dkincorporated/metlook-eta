package dev.dkong.metlook.eta.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PersistentBottomSheetScaffold
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.StoppingPatternComposables
import dev.dkong.metlook.eta.composables.TwoLineCenterTopAppBarText
import dev.dkong.metlook.eta.objects.metlook.DepartureService
import dev.dkong.metlook.eta.objects.metlook.PatternDeparture
import dev.dkong.metlook.eta.objects.ptv.PatternResult
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException

/**
 * Activity for a specific Service
 */
class ServiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive Service info from Bundle
        val bundleService = intent?.extras?.getString("service")
        bundleService?.let { serviceString ->
            // Parse the service
            val service = Constants.jsonFormat.decodeFromString<DepartureService>(serviceString)

            // Render the UI
            setContent {
                MetlookTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Constants.appSurfaceColour() // wait until new Surface
                    ) {
                        val navController = rememberNavController()
                        ServiceScreen(navController, service)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ServiceScreen(navHostController: NavHostController, originalDeparture: DepartureService) {
        val context = LocalContext.current
        val density = LocalDensity.current

        val scaffoldState = rememberBottomSheetScaffoldState()
        var topBarHeight by remember { mutableStateOf(0.dp) }
        var collapsedPatternHeight by remember { mutableStateOf(512.dp) }

        var loadingState by remember { mutableStateOf(true) }
        val pattern = remember { mutableStateListOf<PatternDeparture>() }

        suspend fun update() {
            loadingState = true

            val request = PtvApi.getApiUrl(
                Uri.Builder()
                    .appendPath("v3")
                    .appendPath("pattern")
                    .appendPath("run")
                    .appendPath(originalDeparture.runRef)
                    .appendPath("route_type")
                    .appendPath(originalDeparture.routeType.id.toString())
                    .appendQueryParameter("expand", "All")
                    .appendQueryParameter("include_skipped_stops", "true")
            )

            Log.d("SERVICE", request.toString())

            if (request == null) {
                return
            }

            val response: String = Constants.httpClient.get(request).body()

            try {
                val decodedPattern =
                    Constants.jsonFormat.decodeFromString<PatternResult>(response)

                val processedPattern = decodedPattern.departures
                    .asSequence()
                    .map { departure ->
                        val run = decodedPattern.runs[departure.runRef]
                            ?: return@map null
                        val stop = decodedPattern.stops[departure.stopId]
                            ?: return@map null

                        // Initiate the all-in-one departure object
                        PatternDeparture(
                            departure,
                            run,
                            stop
                        )
                    }
                    // Remove any failed parse results
                    .filterNotNull()

                pattern.clear()
                pattern.addAll(processedPattern)

                loadingState = false
            } catch (e: SerializationException) {
                // TODO: Error message
                Log.e("SERVICE", e.toString())
            }
        }

        LaunchedEffect(Unit) {
            update()
        }

        PersistentBottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        TwoLineCenterTopAppBarText(
                            title = originalDeparture.serviceTitle,
                            subtitle = ""
                        )
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
                    actions = {
                        // Progress indicator
                        if (loadingState)
                            IconButton(onClick = { /* Dummy container */ }) {
                                CircularProgressIndicator(
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .requiredSize(24.dp) // from icon button
                                )
                            }
                    },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        with(density) {
                            topBarHeight = coordinates.size.height.toDp()
                        }
                    })
            },
            topBarHeight = topBarHeight,
            mainContent = {
                // Map
            },
            sheetContent = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
//                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    // Collapsed pattern (mini pattern)
//                    item {
//                        Column(modifier = Modifier.onGloballyPositioned { coordinates ->
//                            with(density) {
//                                collapsedPatternHeight = coordinates.size.height.toDp()
//                            }
//                        }) {
//
//                        }
//                    }
                    // Full pattern
                    pattern.forEachIndexed { index, stop ->
                        item(key = stop.stop.stopId.toString() + stop.stop.stopSequence.toString()) {
                            StoppingPatternComposables.StoppingPatternCard(
                                patternStop = stop,
                                stopType = when (index) {
                                    0 -> StoppingPatternComposables.StopType.First
                                    pattern.lastIndex -> StoppingPatternComposables.StopType.Last
                                    else -> StoppingPatternComposables.StopType.Stop
                                }
                            )
                        }
                    }
                    item {
                        NavBarPadding()
                    }
                }
            },
            sheetPeekHeight = collapsedPatternHeight
        )
    }
}