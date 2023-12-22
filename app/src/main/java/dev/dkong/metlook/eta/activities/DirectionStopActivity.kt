package dev.dkong.metlook.eta.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.Constants.refreshInterval
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.CheckableChip
import dev.dkong.metlook.eta.composables.DepartureCard
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PersistentBottomSheetScaffold
import dev.dkong.metlook.eta.composables.PlaceholderMessage
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.TwoLineCenterTopAppBarText
import dev.dkong.metlook.eta.objects.metlook.DepartureDirectionGroup
import dev.dkong.metlook.eta.objects.metlook.DepartureService
import dev.dkong.metlook.eta.objects.metlook.ParcelableService
import dev.dkong.metlook.eta.objects.metlook.PatternType
import dev.dkong.metlook.eta.objects.ptv.DepartureResult
import dev.dkong.metlook.eta.objects.ptv.Direction
import dev.dkong.metlook.eta.objects.ptv.Route
import dev.dkong.metlook.eta.objects.ptv.Stop
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString

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

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    fun DirectionStopScreen(
        navHostController: NavHostController,
        stop: Stop,
        direction: Direction
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            )
        )
        var topBarHeight by remember { mutableStateOf(0.dp) }

        /**
         * "Clean" list of all departures (no filters)
         */
        val allDepartures =
            remember { mutableStateListOf<DepartureService>() }

        /**
         * Observed list of departures (for filters)
         */
        val departures =
            remember { mutableStateListOf<DepartureService>() }

        /**
         * List of distinct stopping patterns in the departure list (Train only)
         */
        val departuresPatternList = remember { mutableStateListOf<PatternType.PatternClass>() }

        /**
         * List of distinct routes in the departure list (Tram and Bus only)
         */
        val departuresRouteList = remember { mutableStateListOf<Route>() }

        // Filters
        val filters: MutableMap<Int, Boolean?> = remember {
            mutableStateMapOf(
                // Initialise filters with default values
                *PatternType.PatternClass.values()
                    .map { it.ordinal }
                    .associateWith { null }
                    .toList().toTypedArray()
            )
        }


        var loadingState by remember { mutableStateOf(true) }

        // Lifecycle states
        // TODO: Try to find a more elegant way to handle these
        var hasFirstLoaded by remember { mutableStateOf(false) }
        var isScreenActive by remember { mutableStateOf(true) }

        fun updateFilters() {
            var result = allDepartures.toList()
            // Filter by stopping pattern (train only), and only if at least one filter is applied
            if (
                direction.routeType == RouteType.Train
                && PatternType.PatternClass.values().any { filters[it.ordinal] != null }
            ) {
                result = result
                    .filter { departure ->
                        filters[departure.patternType().patternClass.ordinal] == true
                    }
            }
            if (
                listOf(RouteType.Tram, RouteType.Bus).contains(stop.routeType)
                && departuresRouteList.any { filters[it.routeId] != null }
            ) {
                result = result
                    .filter { departure ->
                        filters[departure.routeId] == true
                    }
            }
            // Update departures
            departures.clear()
            departures.addAll(result)
        }

        suspend fun updateDepartures(
            isFirstLoad: Boolean = false
        ) {
            if (isFirstLoad) loadingState = true

            // Get departures from web API
            val request = PtvApi.getApiUrl(
                Uri.Builder().apply {
                    appendPath("v3")
                    appendPath("departures")
                    appendPath("route_type")
                    appendPath(stop.routeType.id.toString())
                    appendPath("stop")
                    appendPath(stop.stopId.toString())
                    appendQueryParameter("direction_id", direction.directionId.toString())
                    appendQueryParameter("include_cancelled", true.toString())
                    appendQueryParameter("max_results", 100.toString())
                    appendQueryParameter("expand", "all")
                }
            )

            Log.d("DIRECTION", request.toString())

            request?.let {
                val response: String = Constants.httpClient.get(request).body()

                try {
                    // Parse departures
                    val decodedDepartures =
                        Constants.jsonFormat.decodeFromString<DepartureResult>(response)

                    val processedDepartures = decodedDepartures.departures
                        .asSequence()
                        .map { departure ->
                            val route = decodedDepartures.routes[departure.routeId]
                                ?: return@map null
                            val run = decodedDepartures.runs[departure.runRef]
                                ?: return@map null
                            val direction =
                                decodedDepartures.directions[departure.directionId.toString()]
                                    ?: return@map null

                            // Initiate the all-in-one departure object
                            val processedDeparture = DepartureService(
                                departure,
                                route,
                                run,
                                direction,
                                decodedDepartures.disruptions.filter { entry ->
                                    departure.disruptionIds.contains(entry.value.disruptionId)
                                }.values.toList()
                            )

                            // Filter out unwanted departures
                            if (!processedDeparture.isValid()) return@map null

                            return@map processedDeparture
                        }
                        // Remove any failed parse results
                        .filterNotNull()

                    // Add all departures
                    allDepartures.clear()
                    allDepartures.addAll(processedDepartures)

                    // Record distinct stopping patterns (Train only)
                    if (stop.routeType == RouteType.Train) {
                        departuresPatternList.clear()

                        val distinctPatterns =
                            allDepartures.map { departure -> departure.patternType().patternClass }
                                .distinct()
                                .sortedBy { patternType -> patternType.ordinal }

                        // Don't add patterns if only one distinct exists
                        if (distinctPatterns.size > 1)
                            departuresPatternList.addAll(distinctPatterns)
                    }
                    // Record distinct routes (Tram and Bus only)
                    else if (listOf(RouteType.Tram, RouteType.Bus).contains(stop.routeType)) {
                        departuresRouteList.clear()

                        val distinctRoutes =
                            allDepartures.map { departure -> departure.route }.distinct()
                                .sortedBy { route -> route.routeNumber }

                        // Don't add the routes for the filters if there is only one route
                        if (distinctRoutes.size > 1)
                            departuresRouteList.addAll(distinctRoutes)
                    }

                    // Run any filters
                    updateFilters()

                    loadingState = false
                    hasFirstLoaded = true

                    return
                } catch (e: SerializationException) {
                    // TODO: Show error for failed request
                    Log.e("DEPARTURES", e.toString())
                }
            }

            // TODO: Show error for failed request
            Log.e("DEPARTURES", "Failed to generate API URL: $request")
        }

        // This block runs while the screen is not yet destroyed
        LaunchedEffect(Unit) {
            updateDepartures()
            delay(refreshInterval)
            while (true) {
                // Refresh departures every refresh interval
                if (isScreenActive) {
                    updateDepartures()
                }

                // Wait the interval
                delay(refreshInterval)
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
                        updateDepartures(isFirstLoad = true)
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
                            TwoLineCenterTopAppBarText(
                                title = "To ${direction.directionName}",
                                subtitle = with(stop) {
                                    stopName().first +
                                            (stopName().second?.let { s -> "/$s" } ?: "")
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
                PlaceholderMessage(
                    title = "The map is coming soon",
                    subtitle = "Please excuse us while we work behind the scenes."
                )
            },
            mainContentBackgroundColour = MaterialTheme.colorScheme.tertiaryContainer,
            sheetContent = {
                // Departures content

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    // Filter chip(s)
                    // Stopping pattern filters
                    if (departuresPatternList.isNotEmpty()) {
                        item {
                            SectionHeading(heading = "Stopping pattern")
                        }
                    }
                    // Route filters
                    else if (departuresRouteList.isNotEmpty()) {
                        item {
                            SectionHeading(heading = "Line")
                        }
                    }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            if (direction.routeType == RouteType.Train) {
                                departuresPatternList.forEach { patternClass ->
                                    // Display all stopping pattern types
                                    CheckableChip(
                                        selected = filters[patternClass.ordinal] == true,
                                        name = stringResource(id = patternClass.displayName),
                                        showIcon = false,
                                        showRemoveIcon = true
                                    ) {
                                        // Toggle the status
                                        filters[patternClass.ordinal] =
                                            if (filters[patternClass.ordinal] == null) true else null
                                        updateFilters()
                                    }
                                }
                            } else if (listOf(RouteType.Tram, RouteType.Bus)
                                    .contains(stop.routeType)
                            ) {
                                // For tram and bus, filter by route
                                departuresRouteList
                                    .forEach { route ->
                                        if (route.routeNumber == null) return@forEach
                                        // Display all routes
                                        CheckableChip(
                                            selected = filters[route.routeId] == true,
                                            name = route.routeNumber,
                                            showIcon = false,
                                            showRemoveIcon = true
                                        ) {
                                            // Toggle the status
                                            filters[route.routeId] =
                                                if (filters[route.routeId] == null) true else null
                                            updateFilters()
                                        }
                                    }
                            }
                        }
                    }
                    item {
                        SectionHeading(heading = "Departures")
                    }
                    // Display the services
                    departures.forEachIndexed { index, departure ->
                        item(key = departure.runRef) {
                            DepartureCard(
                                departureList = listOf(departure),
                                shape = ListPosition.fromPosition(
                                    index,
                                    departures.size
                                ).roundedShape,
                                onClick = {
                                    val serviceIntent = Intent(context, ServiceActivity::class.java)
                                    serviceIntent.putExtra(
                                        "service",
                                        Constants.jsonFormat.encodeToString(with(departure) {
                                            ParcelableService(
                                                runRef,
                                                routeType,
                                                route,
                                                serviceTitle,
                                                destinationName,
                                                stop.stopId
                                            )
                                        })
                                    )
                                    context.startActivity(serviceIntent)
                                },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                    item {
                        NavBarPadding()
                    }
                }
            },
            sheetPeekHeight = 512.dp
        )
    }
}