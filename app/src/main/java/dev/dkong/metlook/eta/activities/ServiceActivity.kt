package dev.dkong.metlook.eta.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PersistentBottomSheetScaffold
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.StoppingPatternComposables
import dev.dkong.metlook.eta.composables.TextMetLabel
import dev.dkong.metlook.eta.composables.TwoLineCenterTopAppBarText
import dev.dkong.metlook.eta.objects.metlook.DepartureService
import dev.dkong.metlook.eta.objects.metlook.ParcelableService
import dev.dkong.metlook.eta.objects.metlook.PatternDeparture
import dev.dkong.metlook.eta.objects.ptv.PatternResult
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            val service = Constants.jsonFormat.decodeFromString<ParcelableService>(serviceString)

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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun ServiceScreen(navHostController: NavHostController, originalDeparture: ParcelableService) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current

        val patternListState = rememberLazyListState()
        var topBarHeight by remember { mutableStateOf(0.dp) }
        var collapsedPatternHeight by remember { mutableStateOf(512.dp) }

        var loadingState by remember { mutableStateOf(true) }
        val pattern = remember { mutableStateListOf<PatternDeparture>() }
        var nextStopId by remember { mutableStateOf<Int?>(null) }
        var nextStopIndex by remember { mutableStateOf<Int?>(null) }

        suspend fun scrollToNextStop() {
            patternListState.animateScrollToItem(index =
            pattern.indexOfFirst { departure -> departure.stop.stopId == nextStopId }
            )
        }

        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded,
                confirmValueChange = { value ->
                    when (value) {
                        SheetValue.Expanded -> {
                            // Scroll to the next stop on expand
                            scope.launch {
                                scrollToNextStop()
                            }
                        }

                        else -> {}
                    }
                    true
                }
            )
        )

        // Lifecycle states
        // TODO: Try to find a more elegant way to handle these
        var hasFirstLoaded by remember { mutableStateOf(false) }
        var isScreenActive by remember { mutableStateOf(true) }

        suspend fun update(isFirstLoad: Boolean) {
            if (isFirstLoad)
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

                val run = decodedPattern.runs[originalDeparture.runRef]
                    ?: return

                val processedPattern = decodedPattern.departures
                    .asSequence()
                    .mapIndexed { index, departure ->
                        val stop = decodedPattern.stops[departure.stopId]
                            ?: return@mapIndexed null

                        // Initiate the all-in-one departure object
                        val processedDeparture =
                            PatternDeparture(
                                departure,
                                run,
                                stop,
                                when (index) {
                                    0 -> StoppingPatternComposables.StopType.First
                                    decodedPattern.departures.lastIndex -> StoppingPatternComposables.StopType.Last
                                    else -> StoppingPatternComposables.StopType.Stop
                                }
                            )

                        val skippedStops =
                            // Skipped stops after Flinders Street are usually due to API error
                            if (departure.stopId == 1071) null else
                                departure.skippedStops?.mapIndexed { skippedIndex, skippedStop ->
                                    PatternDeparture(
                                        departure,
                                        run,
                                        skippedStop,
                                        if (
                                            skippedIndex == departure.skippedStops.lastIndex.floorDiv(
                                                2
                                            )
                                            && departure.skippedStops.size > 1
                                        ) StoppingPatternComposables.StopType.ArrowSkipped
                                        else StoppingPatternComposables.StopType.Skipped
                                    )
                                }

                        return@mapIndexed if (skippedStops != null)
                            listOf(processedDeparture) + skippedStops
                        else listOf(processedDeparture)
                    }
                    // Remove any failed parse results
                    .filterNotNull()
                    // Flatten stops and skipped stops
                    .flatten()

                pattern.clear()
                pattern.addAll(processedPattern)

                // Get the next stop
                nextStopId = processedPattern.find { departure ->
                    (departure.timeToEstimatedDeparture()?.inWholeSeconds ?: -1) > 0
                }
                    ?.stop
                    ?.stopId

                nextStopIndex =
                    pattern.indexOfFirst { departure -> departure.stop.stopId == nextStopId }
                        .takeIf { it != -1 }

                if (isFirstLoad)
                // Scroll only on first load
                    scope.launch {
                        scrollToNextStop()
                    }

                hasFirstLoaded = true
                loadingState = false
            } catch (e: SerializationException) {
                // TODO: Error message
                Log.e("SERVICE", e.toString())
            }
        }

        LaunchedEffect(Unit) {
            update(isFirstLoad = true)
            delay(Constants.refreshInterval)
            while (true) {
                if (isScreenActive) update(isFirstLoad = false)
                delay(Constants.refreshInterval)
            }
        }

        // Listen for Composable lifecycle
        Utils.ComposableEventListener { event ->
            when (event) {
                // On pause, disable refresh
                Lifecycle.Event.ON_PAUSE -> isScreenActive = false
                // On resume, update departures, and enable refresh
                Lifecycle.Event.ON_RESUME -> scope.launch {
                    if (hasFirstLoaded)
                        update(isFirstLoad = true)
                    isScreenActive = true
                }

                else -> {}
            }
        }

        PersistentBottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            originalDeparture.route.routeNumber?.let {
                                TextMetLabel(text = it)
                            }
                            TwoLineCenterTopAppBarText(
                                title = originalDeparture.name,
                                subtitle = if (arrayOf(RouteType.Tram, RouteType.Bus).contains(
                                        originalDeparture.routeType
                                    )
                                ) {
                                    "To ${originalDeparture.destination}"
                                } else {
                                    "${originalDeparture.route.routeName} line"
                                }
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
                    state = patternListState,
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
                    val isSheetExpanded =
                        scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded

                    pattern.forEachIndexed { index, stop ->
                        val isStopBeforeNext = index == nextStopIndex?.minus(1)
                        val isNextStop = index == nextStopIndex
                        val isStopAfterNext = index == nextStopIndex?.plus(1)
                        val isLastStop = index == pattern.lastIndex

                        if (
                            isStopBeforeNext
                            || isNextStop
                            || isStopAfterNext
                            || isLastStop
                            || isSheetExpanded
                        )
                            item(key = stop.stop.stopId.toString() + stop.stop.stopSequence.toString()) {
                                StoppingPatternComposables.StoppingPatternCard(
                                    patternStop = stop,
                                    stopType =
                                    if (!isSheetExpanded && stop.stopType == StoppingPatternComposables.StopType.Stop) {
                                        if (isStopBeforeNext) StoppingPatternComposables.StopType.ContinuesBefore
                                        else if (isStopAfterNext) StoppingPatternComposables.StopType.ContinuesAfter
                                        else stop.stopType
                                    } else stop.stopType,
                                    modifier = if (isNextStop) Modifier
                                        .clip(
                                            RoundedCornerShape(16.dp)
                                        )
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .animateItemPlacement()
                                    else Modifier.animateItemPlacement()
                                )
                            }
                    }
                    item {
                        NavBarPadding()
                    }
                }
            },
            sheetPeekHeight = 384.dp
        )
    }
}