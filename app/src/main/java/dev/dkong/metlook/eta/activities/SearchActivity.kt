package dev.dkong.metlook.eta.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils.finishActivity
import dev.dkong.metlook.eta.common.datastore.recents.RecentStopsCoordinator
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.BetterListItem
import dev.dkong.metlook.eta.composables.CheckableChip
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PlaceholderMessage
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.StopCard
import dev.dkong.metlook.eta.objects.ptv.SearchResult
import dev.dkong.metlook.eta.objects.ptv.Stop
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString

/**
 * Activity for Search
 */
class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetlookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Constants.appSurfaceColour() // wait until new Surface
                ) {
                    val navController = rememberNavController()
                    SearchScreen(navController)
                }
            }
        }
    }

    /**
     * Main Search interface
     */
    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
        ExperimentalLayoutApi::class
    )
    @Composable
    fun SearchScreen(navHostController: NavHostController) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }

        var searchQuery by rememberSaveable { mutableStateOf("") }
        var isSearchActive by rememberSaveable { mutableStateOf(true) }

        // Search results: for Stops and Routes
        /**
         * Original stop search results
         */
        val searchResultsStop =
            remember { mutableStateListOf<Pair<RouteType, List<Stop>>>() }

        /**
         * Filtered (if any) and observed stop search results
         */
        val searchResultsStopFiltered =
            remember { mutableStateListOf<Pair<RouteType, List<Stop>>>() }
//        val searchResultsRoute = remember { mutableStateListOf<Route>() }

        /**
         * Filter options
         */
        val filters = remember { mutableStateMapOf<String, Boolean?>() }

        val placeholderTitle = stringArrayResource(id = R.array.fun_msg_search).random()

        /**
         * Run the filters on the search results (if any)
         */
        fun updateFilters() {
            searchResultsStopFiltered.clear()
            if (RouteType.values().any { routeType -> filters[routeType.toString()] != null }) {
                val result = searchResultsStop.toList()
                    // Only run the filter if at least one filter is selected
                    .filter { routeTypeGroup ->
                        filters[routeTypeGroup.first.toString()] == true
                    }
                // Update the observed list with the filtered results
                searchResultsStopFiltered.addAll(result)
            } else {
                searchResultsStopFiltered.addAll(searchResultsStop)
            }
        }

        /**
         * Run the search, and update the results lists
         */
        fun updateSearch() {
            // API does not respond to queries less than 3 in length
            if (searchQuery.length < 3) return

            scope.launch {
                val request = PtvApi.getApiUrl(Uri.Builder().apply {
                    appendPath("v3")
                    appendPath("search")
                    appendPath(searchQuery.trim())
                    // Search query must not have whitespaces before or after,
                    // else the API will crash.
                })

                request?.let {
                    val response: String = Constants.httpClient.get(request).body()

                    try {
                        val results = Constants.jsonFormat.decodeFromString<SearchResult>(response)

                        // TODO: Handle the Route results

                        searchResultsStop.clear()
                        val groupedStops = results.stops
                            .groupBy { s -> s.routeType }
                            .toList()
                            .sortedBy { s -> s.first.id }

                        groupedStops.let { l ->
                            searchResultsStop.addAll(l)
                            updateFilters()
                        }
                    } catch (e: SerializationException) {
                        // TODO: Handle error
                    }
                }

                // TODO: Handle error
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        LazyColumn(
            modifier = Modifier.statusBarsPadding()
        ) {
            stickyHeader {
                DockedSearchBar(
                    query = searchQuery,
                    onQueryChange = { s -> searchQuery = s },
                    onSearch = { _ ->
                        isSearchActive = false
                        updateSearch()
                    },
                    active = isSearchActive,
                    onActiveChange = { a ->
                        isSearchActive = a
                        if (!a) updateSearch()
                    },
                    placeholder = { Text("Search stops and stations") },
                    leadingIcon = {
                        IconButton(onClick = {
                            // Finish the Search activity
                            context.finishActivity()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                "Go back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery != "",
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            IconButton(onClick = {
                                // Clear the search field
                                searchQuery = ""
                                // Re-focus the search field
                                focusRequester.requestFocus()
                            }) {
                                Icon(Icons.Default.Clear, "Clear query")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .focusRequester(focusRequester)
                ) {
                    Column {
                        // Search suggestions
                        val suggestions = arrayOf(
                            "Flinders Street",
                            "Southern Cross",
                            "Melbourne Central",
                        )
                        SectionHeading(heading = "Popular stations")
                        suggestions.forEach { suggestion ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                ),
                                /*
                                // Auto-fill button disabled until cursor can be moved
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            // Put query in search bar
                                            searchQuery = suggestion
                                        }
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.baseline_north_west_24),
                                            contentDescription = "Fill suggestion",
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                },
                                 */
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        // Put query in search bar
                                        searchQuery = suggestion
                                        isSearchActive = false
                                        updateSearch()
                                    }
                            )
                        }
                    }
                }
            }
            // Filter options
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    searchResultsStop.map { it.first }.forEach { routeType ->
                        if (routeType == RouteType.Other) return@forEach
                        // Display all route types
                        CheckableChip(
                            selected = filters[routeType.toString()] == true,
                            name = stringResource(id = routeType.displayName),
                            showIcon = false,
                            showRemoveIcon = true
                        ) {
                            // Toggle the status
                            filters[routeType.toString()] =
                                if (filters[routeType.toString()] == null) true else null
                            updateFilters()
                        }
                    }
                }
            }
            // Search results
//            if (searchResultsStopFiltered.isEmpty() /* && searchResultsRoute.isEmpty() */) {
//                item {
//                    PlaceholderMessage(
//                        largeIcon = R.drawable.baseline_travel_explore_24,
//                        title = placeholderTitle,
//                        subtitle = "Search for something to get started."
//                    )
//                }
//            }
            searchResultsStopFiltered.forEach { routeType ->
                item(key = routeType.first.id) {
                    SectionHeading(
                        heading = stringResource(id = routeType.first.displayName),
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                routeType.second.forEachIndexed { index, stop ->
                    item(key = "${stop.routeType.id}-${stop.stopId}") {
                        StopCard(
                            stop = stop,
                            shape = ListPosition.fromPosition(
                                index,
                                routeType.second.size
                            ).roundedShape,
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

    /**
     * Preview
     */
    @Preview
    @Composable
    fun PreviewSearch() {
    }
}