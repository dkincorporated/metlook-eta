package dev.dkong.metlook.eta.screens.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.composables.SectionHeading

@Composable
fun DashboardHomeScreen(navHostController: NavHostController) {
    LazyColumn {
        item {
            SectionHeading(heading = "Recent stops and stations")
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