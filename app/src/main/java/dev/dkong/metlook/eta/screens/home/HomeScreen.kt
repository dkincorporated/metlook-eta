package dev.dkong.metlook.eta.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.composables.LargeTopAppbarScaffoldBox
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.screens.RootScreen

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
                Spacer(modifier = Modifier.statusBarsPadding())
                Text(
                    text = "metlook",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(vertical = 16.dp)
                )
                SectionHeading(heading = "Dashboard")
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
                                contentDescription = homeScreen.displayName,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                // Other Nav Drawer items
                SectionHeading(heading = "More")
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
                                contentDescription = item.name,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }

        }
    ) {
        val homeNavHostController = rememberNavController()

        LargeTopAppbarScaffoldBox(
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
                                if (selectedNavBarItem.route != screen.route) {
                                    homeNavHostController.navigate(screen.route) {
                                        // Do not add to backstack -- in other words, remove all backstack when navigating to the new route
                                        popUpTo(0)
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                selectedNavBarItem = screen
                            },
                            icon = {
                                Image(
                                    painter = painterResource(id = if (isSelected) screen.selectedIcon else screen.icon),
                                    contentDescription = screen.displayName,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
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
            NavHost(
                navController = homeNavHostController,
                startDestination = HomeScreenItem.Dashboard.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it / Constants.transitionOffsetProportion },
                        animationSpec = Constants.transitionAnimationSpec
                    ) + fadeIn()
                },
                exitTransition = {
                    fadeOut() + slideOutVertically(
                        targetOffsetY = { -it / Constants.transitionOffsetProportion },
                        animationSpec = Constants.transitionAnimationSpec
                    )
                },
                popEnterTransition = {
                    slideInVertically(
                        initialOffsetY = { -it / Constants.transitionOffsetProportion },
                        animationSpec = Constants.transitionAnimationSpec
                    ) + fadeIn()
                },
                popExitTransition = {
                    fadeOut() + slideOutVertically(
                        targetOffsetY = { it / Constants.transitionOffsetProportion },
                        animationSpec = Constants.transitionAnimationSpec
                    )
                }
            ) {
                composable(HomeScreenItem.Dashboard.route) {
                    DashboardHomeScreen(navHostController = navHostController)
                }
                composable(HomeScreenItem.Navigation.route) {
                    NavigationHomeScreen(navHostController = navHostController)
                }
                composable(HomeScreenItem.Updates.route) {
                    UpdatesHomeScreen(navHostController = navHostController)
                }
            }
        }
    }
}