package dev.dkong.metlook.eta.screens.search

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.StopCard
import dev.dkong.metlook.eta.objects.ptv.DisruptionsResult
import dev.dkong.metlook.eta.objects.ptv.Route
import dev.dkong.metlook.eta.objects.ptv.SearchResult
import dev.dkong.metlook.eta.objects.ptv.Stop
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch

/**
 * Main Search interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(true) }

    // Search results: for Stops and Routes
    val searchResultsStop = remember { mutableStateListOf<Pair<RouteType, List<Stop>>>() }
    val searchResultsRoute = remember { mutableStateListOf<Route>() }

    /**
     * Run the search, and update the results lists
     */
    fun updateSearch() {
        // API does not respond to queries less than 3 in length
        if (searchQuery.length < 3) return

        scope.launch {
            val results = getSearchResults(searchQuery)

            // TODO: Handle the Route results

            searchResultsStop.clear()
            val groupedStops = results?.stops
                ?.groupBy { s -> s.routeType }
                ?.toList()
                ?.sortedBy { s -> s.first.id }

            groupedStops?.let { l ->
                searchResultsStop.addAll(l)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LazyColumn(
        modifier = Modifier.statusBarsPadding()
    ) {
        item {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { s -> searchQuery = s },
                onSearch = { _ -> updateSearch() },
                active = isSearchActive,
                onActiveChange = { a ->
                    isSearchActive = a
                    if (!a) updateSearch()
                },
                placeholder = { Text("Search stops and stations") },
                leadingIcon = {
                    IconButton(onClick = {
                        // Finish the Search activity
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Go back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery != "",
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = {
                            // Clear the search field
                            searchQuery = ""
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
        // Search results
        item {
            AnimatedVisibility(
                visible = searchResultsStop.isEmpty() && searchResultsRoute.isEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SearchPlaceholder()
                }
            }
        }
        searchResultsStop.forEach { routeType ->
            item {
                SectionHeading(heading = routeType.first.displayName)
            }
            routeType.second.forEachIndexed { index, stop ->
                item {
                    StopCard(
                        stop = stop,
                        shape = ListPosition.fromPosition(index, routeType.second.size).roundedShape
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
 * Run a search for a query
 * @param query the search query
 * @return the search result (consisting of stops and routes)
 */
suspend fun getSearchResults(query: String): SearchResult? {
    val request = PtvApi.getApiUrl(
        "/v3/search/${query.replace(" ", "%20")}?"
    )

    request?.let {
        val response: String = Constants.httpClient.get(request).body()
        return Constants.jsonFormat.decodeFromString<SearchResult>(response)
    }

    return null
}

/**
 * Placeholder graphic for no search query
 */
@Composable
fun SearchPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painterResource(id = R.drawable.baseline_travel_explore_24),
                contentDescription = "Explore",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Oh, the places you'll go!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Search for something to get started.",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Preview
 */
@Preview
@Composable
fun PreviewSearch() {
    SearchPlaceholder()
}
