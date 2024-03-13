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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils
import dev.dkong.metlook.eta.common.Utils.finishActivity
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.CheckableChip
import dev.dkong.metlook.eta.composables.DepartureCard
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PersistentBottomSheetScaffold
import dev.dkong.metlook.eta.composables.PlaceholderMessage
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.SettingsInfoFootnote
import dev.dkong.metlook.eta.composables.TextMetLabel
import dev.dkong.metlook.eta.composables.TwoLineCenterTopAppBarText
import dev.dkong.metlook.eta.objects.metlook.DepartureDirectionGroup
import dev.dkong.metlook.eta.objects.ptv.DepartureResult
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import dev.dkong.metlook.eta.objects.ptv.Stop
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlin.math.min

class StopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive Stop info from Bundle
        val bundleStop = intent.extras?.getString("stop")
        bundleStop?.let { stopString ->
            // Parse the Stop
            val stop = Constants.jsonFormat.decodeFromString<Stop>(stopString)

            // Render the UI
            setContent {
                MetlookTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Constants.appSurfaceColour() // wait until new Surface
                    ) {
                        val navController = rememberNavController()
                        StopScreen(navController, stop)
                    }
                }
            }
        }
    }

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
        ExperimentalLayoutApi::class
    )
    @Composable
    fun StopScreen(navHostController: NavHostController, stop: Stop) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            )
        )

        // "Clean" list of all departures (no filters)
        val allDepartures =
            remember { mutableStateListOf<Pair<DepartureDirectionGroup, List<Pair<Int, List<ServiceDeparture>>>>>() }
        // Observed list of departures (for filters)
        val departures =
            remember { mutableStateListOf<Pair<DepartureDirectionGroup, List<Pair<Int, List<ServiceDeparture>>>>>() }
        val stopName = stop.stopName
        val filters = remember {
            mutableStateMapOf(
                // Initialise filters with default values
                "next-sixty" to true
            )
        }
        var loadingState by remember { mutableStateOf(true) }

        // Lifecycle states
        // TODO: Try to find a more elegant way to handle these
        var hasFirstLoaded by remember { mutableStateOf(false) }
        var isScreenActive by remember { mutableStateOf(true) }

        var appBarHeight by remember { mutableStateOf(0.dp) }

        /**
         * Trigger upon a filter update to update the data
         */
        fun updateFilters() {
            var result = allDepartures.toList()
            // Next 60 min
            result = result
                .map { entry ->
                    Pair(
                        entry.first,
                        entry.second
                            .map { destinationGroup ->
                                Pair(
                                    destinationGroup.first,
                                    destinationGroup.second
                                        .filter { departure ->
                                            if ((departure.timeToEstimatedDeparture()?.inWholeMinutes
                                                    ?: departure.timeToScheduledDeparture().inWholeMinutes) > 60
                                            ) {
                                                filters["next-sixty"] != true
                                            } else true
                                        }
                                )
                            }
                            // Filter empty destination groups
                            .filter { destinationGroup -> destinationGroup.second.isNotEmpty() }
                    )
                }
                // Filter empty route groups
                .filter { entry -> entry.second.isNotEmpty() }
            // Remove any groups that have no children (req. if another filter is added)
//            result = result
//                .map { entry ->
//                    Pair(
//                        entry.first,
//                        entry.second
//                            .filter { destinationGroup -> destinationGroup.second.isNotEmpty() }
//                    )
//                }
//                .filter { entry ->
//                    entry.second.isNotEmpty()
//                }
            // Final step
            departures.clear()
            departures.addAll(result)
        }

        /**
         * Get (or update) list of departures
         */
        suspend fun updateDepartures(
            isFirstLoad: Boolean = false
        ) {
            // Don't show the progress indicator on subsequent updates
            if (isFirstLoad)
                loadingState = true

            // Get departures from web API
            // A max-result value must be provided or it will return departures from the start
            // of the day.
            val request = PtvApi.getApiUrl(
                Uri.Builder().apply {
                    appendPath("v3")
                    appendPath("departures")
                    appendPath("route_type")
                    appendPath(stop.routeType.id.toString())
                    appendPath("stop")
                    appendPath(stop.stopId.toString())
                    appendQueryParameter("expand", "all")
                    appendQueryParameter("max_results", 100.toString())
                }
            )

            request?.let {
                val response: String = Constants.httpClient.get(request).body()
                try {
                    // Parse departure result
                    val decodedDepartures =
                        Constants.jsonFormat.decodeFromString<DepartureResult>(response)

                    // Store the processed departures
                    val groupedDepartures = decodedDepartures
                        .toDepartureSequence(stop)
                        // Group departures
                        .groupBy { d -> d.directionGroupingValue }
                        .toList()
                        .sortedBy { pair -> pair.first }
                        .map { entry ->
                            Pair(
                                entry.first,
                                entry.second
                                    .groupBy { service ->
                                        service.finalStopId
                                    }
                                    .toList()
                                    // Sort by earliest departure
                                    .sortedBy { destinationGroup ->
                                        with(destinationGroup.second[0]) {
                                            timeToEstimatedDeparture()?.inWholeSeconds
                                                ?: timeToScheduledDeparture().inWholeSeconds
                                        }
                                    }
                            )
                        }
                        .toList()

                    // Add all departures
                    allDepartures.clear()
                    allDepartures.addAll(groupedDepartures)

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

        val refreshInterval = 15000L

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

        // Use a standard center-aligned top app bar scaffold until map is ready

        val scrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Constants.appSurfaceColour(),
            contentWindowInsets = WindowInsets(bottom = 0.dp),
            topBar = {
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        TwoLineCenterTopAppBarText(
                            title = stopName.first,
                            subtitle = stopName.second?.let { s -> "on $s" })

                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Constants.appSurfaceColour(),
                        scrolledContainerColor = Constants.scrolledAppbarContainerColour()
                    ),
                    navigationIcon = {
                        ElevatedAppBarNavigationIcon(onClick = {
                            // Finish the Activity
                            context.finishActivity()
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
                        else {
                            // Stop number
                            stopName.third?.let { n ->
                                TextMetLabel(text = n)
                            }
                        }
                    },
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            with(density) {
                                appBarHeight = coordinates.size.height.toDp()
                            }
                        }
                )
            }
        ) { padding ->
            // Departures
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
            ) {
                // Filter chip(s)
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        CheckableChip(selected = filters["next-sixty"] == true, "Next 60 min") {
                            filters["next-sixty"] =
                                filters["next-sixty"] != true
                            updateFilters()
                        }
                    }
                }
                departures.forEach { group ->
                    // Display group heading
                    item(key = group.first.groupingId) {
                        SectionHeading(
                            heading = group.first.name,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    // Display the services
                    // TODO: Decide what to do with extra services
                    val maxNumberOfGroups = 4
                    group.second.slice(
                        0 until min(
                            maxNumberOfGroups,
                            group.second.size
                        )
                    )
                        .forEachIndexed { index, departure ->
                            val listedDepartures =
                                departure.second.slice(
                                    0 until min(
                                        2,
                                        departure.second.size
                                    )
                                )

                            item(key = listedDepartures.first().runRef) {
                                DepartureCard(
                                    departureList = listedDepartures,
                                    shape = ListPosition.fromPosition(
                                        index,
                                        min(
                                            maxNumberOfGroups,
                                            group.second.size
                                        ) // TODO
                                    ).roundedShape,
                                    onClick = { departures ->
                                        val firstDeparture = departures.first()
//                                            if (departures.size == 1) {
//                                                // Launch Service directly
//                                                // TODO
//                                            } else {
                                        // Launch Direction Departures
                                        val directionDeparturesIntent = Intent(
                                            context,
                                            DirectionStopActivity::class.java
                                        )
                                        directionDeparturesIntent.putExtra(
                                            "stop",
                                            Constants.jsonFormat.encodeToString(stop)
                                        )
                                        directionDeparturesIntent.putExtra(
                                            "direction",
                                            Constants.jsonFormat.encodeToString(
                                                firstDeparture.direction
                                            )
                                        )
                                        context.startActivity(directionDeparturesIntent)
//                                            }
                                    },
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }

                        }
                }
                if (departures.isNotEmpty()) {
                    item(key = "footnote") {
                        SettingsInfoFootnote(
                            info = StringBuilder().apply {
                                append(stringResource(id = R.string.departures_asterisk_departure_times))
                                if (stop.routeType == RouteType.Train)
                                    append("\n" + stringResource(id = R.string.departures_now_asterisk))
                            }.toString()
                        )
                    }
                }
                item {
                    NavBarPadding()
                }
            }
        }
    }
}