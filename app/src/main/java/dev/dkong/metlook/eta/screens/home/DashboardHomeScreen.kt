package dev.dkong.metlook.eta.screens.home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.activities.SearchActivity
import dev.dkong.metlook.eta.activities.ServiceActivity
import dev.dkong.metlook.eta.activities.StopActivity
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.datastore.RecentServicesCoordinator
import dev.dkong.metlook.eta.common.datastore.RecentStopsCoordinator
import dev.dkong.metlook.eta.composables.DepartureCard
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.RecentServiceCard
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.SettingsInfoFootnote
import dev.dkong.metlook.eta.composables.StopCard
import dev.dkong.metlook.eta.objects.metlook.ParcelableService
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import dev.dkong.metlook.eta.objects.ptv.Stop
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardHomeScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val recentStops = remember { mutableStateListOf<Stop>() }
    val recentServices = remember { mutableStateListOf<ServiceDeparture>() }

    LaunchedEffect(Unit) {
        // Set listeners for the recent items
        scope.launch {
            RecentStopsCoordinator
                .listen(context) {
                    recentStops.clear()
                    recentStops.addAll(it)
                }
        }
        scope.launch {
            RecentServicesCoordinator
                .listen(context) {
                    recentServices.clear()
                    recentServices.addAll(it)
                }
        }
    }

    LazyColumn {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(SearchBarDefaults.dockedShape)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
                    .clickable {
                        context.startActivity(Intent(context, SearchActivity::class.java))
                    }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Search stops and stations",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            SectionHeading(heading = "Recent stops and stations")
        }
        recentStops.forEachIndexed { index, stop ->
            item(key = stop.routeType.id.toString() + stop.stopId.toString()) {
                StopCard(
                    stop = stop,
                    shape = ListPosition.fromPosition(index, recentStops.size).roundedShape,
                    onClick = {
                        // Open the Stop
                        val stopIntent = Intent(context, StopActivity::class.java)
                        stopIntent.putExtra(
                            "stop",
                            Constants.jsonFormat.encodeToString(it)
                        )
                        context.startActivity(stopIntent)
                        // Record the recent stop
                        scope.launch {
                            RecentStopsCoordinator.add(context, it)
                        }
                    },
//                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
        item {
            SectionHeading(heading = "Recent services")
        }
        recentServices.forEachIndexed { index, service ->
            item(key = service.runRef) {
                RecentServiceCard(
                    service = service,
                    shape = ListPosition.fromPosition(index, recentServices.size).roundedShape,
                    onClick = {
                        // Launch the Service screen
                        val serviceIntent = Intent(context, ServiceActivity::class.java)
                        serviceIntent.putExtra(
                            "service",
                            Constants.jsonFormat.encodeToString(with(service) {
                                ParcelableService(
                                    runRef,
                                    routeType,
                                    route,
                                    serviceTitle,
                                    destinationName,
                                    service.departureStop.stopId
                                )
                            })
                        )
                        context.startActivity(serviceIntent)
                        // Save the recent service
                        scope.launch {
                            RecentServicesCoordinator.add(context, service)
                        }
                    },
//                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
        item {
            SettingsInfoFootnote(info = "Recent services show the time until their scheduled departure. Make sure to check the actual estimated departure time by opening the service.")
        }
        item {
            NavBarPadding()
        }
    }
}

@Preview
@Composable
fun PreviewDashboardHomeScreen() {
    DashboardHomeScreen(navHostController = rememberNavController())
}

/*
    val searchInteraction = remember { MutableInteractionSource() }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { s ->
                searchQuery = s
            },
            onSearch = { s ->

            },
            active = isSearchActive,
            onActiveChange = { a ->
                isSearchActive = a
            },
            interactionSource = searchInteraction,
            placeholder = { Text("Search stops and stations") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = -1f }
        ) {
            SectionHeading(heading = "Train")
            ListItem(headlineContent = { Text("Box Hill") })
        }
    }
 */