package dev.dkong.metlook.eta.screens.home

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
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navHostController: NavHostController) {
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
                            Text(text = homeScreen.displayName)
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
            }

        }
    ) {
        LargeTopAppbarScaffold(
            navController = navHostController,
            title = selectedNavBarItem.displayName,
            navigationIcon = Icons.Default.Menu,
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