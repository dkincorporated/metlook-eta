package dev.dkong.metlook.eta.screens.search

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.dkong.metlook.eta.composables.SectionHeading

/**
 * Main Search interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(true) }

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
                onSearch = {},
                active = isSearchActive,
                onActiveChange = { a -> isSearchActive = a },
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
                                }
                        )
                    }
                }
            }
        }
        // Search results
        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                SearchPlaceholder()
            }
        }
    }
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
            modifier = Modifier.fillMaxWidth().padding(16.dp)
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
