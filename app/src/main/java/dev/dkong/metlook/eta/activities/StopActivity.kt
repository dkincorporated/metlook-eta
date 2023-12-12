package dev.dkong.metlook.eta.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.DepartureCard
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PlaceholderMessage
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.TextMetLabel
import dev.dkong.metlook.eta.objects.metlook.DepartureGroup
import dev.dkong.metlook.eta.objects.ptv.DepartureResult
import dev.dkong.metlook.eta.objects.metlook.DepartureService
import dev.dkong.metlook.eta.objects.ptv.Stop
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException
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
        val scope = rememberCoroutineScope()
        val scaffoldState = rememberBottomSheetScaffoldState()

        // "Clean" list of all departures (no filters)
        val allDepartures = mutableListOf<Pair<DepartureGroup, List<DepartureService>>>()
        // Observed list of departures (for filters)
        val departures =
            remember { mutableStateListOf<Pair<DepartureGroup, List<DepartureService>>>() }
        val stopName = stop.stopName()
        val filters = remember { mutableStateMapOf<String, Boolean>() }

        // Initialise the filters
        filters["next-sixty"] = true

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
                            .filter { departure ->
                                if ((departure.timeToEstimatedDeparture()?.inWholeMinutes
                                        ?: departure.timeToScheduledDeparture().inWholeMinutes) > 60
                                ) {
                                    filters["next-sixty"] != true
                                } else true
                            }
                    )
                }
            // Final step
            departures.clear()
            departures.addAll(result)
        }

        /**
         * Get (or update) list of departures
         */
        suspend fun updateDepartures() {
            departures.clear()

            // Get departures from web API
            // A max-result value must be provided or it will return departures from the start
            // of the day.
            val request = PtvApi.getApiUrl(
                "/v3/departures/route_type/${stop.routeType.id}/stop/${stop.stopId}" +
                        "?expand=all&max_results=100&include_cancelled=true&"
            )

            Log.d("DEPARTURES", "Request: $request")

            request?.let {
                val response: String = Constants.httpClient.get(request).body()

                try {
                    // Parse departure result
                    val decodedDepartures =
                        Constants.jsonFormat.decodeFromString<DepartureResult>(response)

                    // Temporarily store the processed departure objects
                    val processedDepartures = mutableListOf<DepartureService>()

                    // Process the received departures
                    decodedDepartures.departures.forEach { departure ->
                        val route = decodedDepartures.routes[departure.routeId]
                            ?: return@forEach
                        val run = decodedDepartures.runs[departure.runRef]
                            ?: return@forEach
                        val direction =
                            decodedDepartures.directions[departure.directionId]
                                ?: return@forEach

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
                        if (!processedDeparture.isValid()) return@forEach

                        // Add new departure to list
                        processedDepartures.add(processedDeparture)
                    }

                    val groupedDepartures = processedDepartures
                        .groupBy { d -> d.directionGroupingValue }
                        .toList()
                        .sortedBy { pair -> pair.first.groupingId }

                    allDepartures.clear()
                    allDepartures.addAll(groupedDepartures)
                    departures.clear()
                    departures.addAll(groupedDepartures)

                    updateFilters()

                    return
                } catch (e: SerializationException) {
                    // TODO: Show error for failed request
                    Log.e("DEPARTURES", e.toString())
                }
            }

            // TODO: Show error for failed request
            Log.e("DEPARTURES", "Failed to generate API URL: $request")
        }

        LaunchedEffect(Unit) {
            updateDepartures()
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            containerColor = Constants.appSurfaceColour(),
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            sheetPeekHeight = 384.dp,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stop number
                            stopName.third?.let { n ->
                                TextMetLabel(text = n)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stopName.first,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Secondary name
                                stopName.second?.let { s ->
                                    Text(
                                        text = "on $s",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        // TODO: Always show scrolled colour (?)
                        containerColor = Constants.appSurfaceColour(),
                        scrolledContainerColor = Constants.scrolledAppbarContainerColour()
                    ),
                    navigationIcon = {
                        ElevatedAppBarNavigationIcon(onClick = {
                            // Finish the Activity
                            (context as? Activity)?.finish()
                        })
                    }
                )
            },
            sheetContent = {
                // Departures
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    // Filter chip(s)
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            FilterChip(
                                selected = filters["next-sixty"] == true,
                                onClick = {
                                    filters["next-sixty"] =
                                        filters["next-sixty"] != true
                                    updateFilters()
                                },
                                label = { Text("Next 60 min") },
                                leadingIcon = {
                                    AnimatedVisibility(
                                        visible = filters["next-sixty"] == true,
                                        enter = expandHorizontally(),
                                        exit = shrinkHorizontally()
                                    ) {
                                        Icon(Icons.Default.Check, "Checked")
                                    }
                                })
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
                        group.second.slice(0..min(2, group.second.lastIndex))
                            .forEachIndexed { index, departure ->
                                item(key = departure.runRef) {
                                    DepartureCard(
                                        departure = departure,
                                        shape = ListPosition.fromPosition(
                                            index,
                                            min(3, group.second.size) // TODO
                                        ).roundedShape,
                                        context = context,
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                            }
                    }
                    item {
                        NavBarPadding()
                    }
                }
            }
        ) { innerPadding ->
            // Map
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                PlaceholderMessage(
                    title = "We're busy making the map",
                    subtitle = "Promise you it'll be worth the wait."
                )
            }
        }
    }
}