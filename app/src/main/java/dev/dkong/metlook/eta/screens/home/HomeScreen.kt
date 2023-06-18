package dev.dkong.metlook.eta.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.dkong.metlook.eta.R

@Composable
fun HomeScreen(navHostController: NavHostController) {
    /**
     * Carrier record for Nav Drawer items other than the Nav Bar items
     */
    data class NavDrawerItem(val name: String, val route: String, @DrawableRes val icon: Int)

    val scope = rememberCoroutineScope()

    var selectedNavBarItem by remember { mutableStateOf(HomeScreenItem.Dashboard) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                HomeScreenItem.values().forEach { homeScreen ->
                    val isSelected = selectedNavBarItem == homeScreen

                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = homeScreen.displayName,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            selectedNavBarItem = homeScreen
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Image(
                                painter = painterResource(id = if (isSelected) homeScreen.selectedIcon else homeScreen.icon),
                                contentDescription = homeScreen.displayName
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                // Other Nav Drawer items
                val navDrawerItems = listOf(
                    NavDrawerItem("Settings", "settings", R.drawable.fancy_outlined_settings)
                )
                navDrawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = {
                            Text(text = item.name, style = MaterialTheme.typography.labelLarge)
                        },
                        selected = false,
                        onClick = {
                            navHostController.navigate(item.route)
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Image(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.name
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }

        }
    ) {
        LargeTopAppbarScaffold(
            navController = navHostController,
            title = selectedNavBarItem.displayName,
            navigationIcon = Icons.Default.Menu,
            onNavigationIconClick = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigationBar = {
                NavigationBar {
                    HomeScreenItem.values().forEach { screen ->
                        val isSelected = selectedNavBarItem == screen

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedNavBarItem = screen

                            },
                            icon = {
                                Image(
                                    painter = painterResource(id = if (isSelected) screen.selectedIcon else screen.icon),
                                    contentDescription = screen.displayName
                                )
                            },
                            label = {
                                Text(
                                    text = screen.displayName
                                )
                            }
                        )
                    }
                }
            }
        ) {
            item {
                Text(
                    text = "Welcome to metlook!",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}