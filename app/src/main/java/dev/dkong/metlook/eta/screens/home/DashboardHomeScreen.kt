package dev.dkong.metlook.eta.screens.home

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.activities.SearchActivity
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.StopCard
import dev.dkong.metlook.eta.objects.ptv.Stop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHomeScreen(navHostController: NavHostController) {
    val context = LocalContext.current

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
            StopCard(
                stop = Stop(
                    0.0,
                    "Melbourne city",
                    0,
                    emptyList(),
                    0.0,
                    0.0,
                    0,
                    1181,
                    "Southern Cross Station",
                    "Southern Cross station"
                ),
                shape = ListPosition.First.roundedShape,
                context = context
            )
            StopCard(
                stop = Stop(
                    0.0,
                    "Box Hill",
                    1,
                    emptyList(),
                    0.0,
                    0.0,
                    0,
                    2409,
                    "Box Hill Central/Whitehorse Road #59",
                    "Box Hill Central"
                ),
                shape = ListPosition.InBetween.roundedShape,
                context = context
            )
            StopCard(
                stop = Stop(
                    0.0,
                    "Clayton",
                    2,
                    emptyList(),
                    0.0,
                    0.0,
                    0,
                    33430,
                    "Monash University",
                    "Monash University"
                ),
                shape = ListPosition.Last.roundedShape,
                context = context
            )
        }
        item {
            SectionHeading(heading = "Recent services")
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