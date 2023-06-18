package dev.dkong.metlook.eta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.Constants.Companion.transitionAnimationSpec
import dev.dkong.metlook.eta.common.Constants.Companion.transitionOffsetProportion
import dev.dkong.metlook.eta.screens.RootScreen
import dev.dkong.metlook.eta.screens.home.HomeScreen
import dev.dkong.metlook.eta.screens.settings.LocationFeaturesScreen
import dev.dkong.metlook.eta.screens.settings.RecentServicesSettingsScreen
import dev.dkong.metlook.eta.screens.settings.RecentStopsSettingsScreen
import dev.dkong.metlook.eta.screens.settings.SettingsScreen
import dev.dkong.metlook.eta.screens.settings.SettingsScreens
import dev.dkong.metlook.eta.ui.theme.MetlookTheme

/**
 * Main activity for the app
 * @author David Kong
 */
class MainActivity : ComponentActivity() {
    /**
     * Initialise the activity
     * @param savedInstanceState the [Bundle] of saved instance
     */
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
                    MainScreen(navController)
                }
            }
        }
    }
}

/**
 * Root screen for all screens in the Compose activity
 */
@Composable
fun MainScreen(navHostController: NavHostController) {

    NavHost(
        navController = navHostController,
        startDestination = RootScreen.Home.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / transitionOffsetProportion },
                animationSpec = transitionAnimationSpec
            ) + fadeIn()
        },
        exitTransition = {
            fadeOut() + slideOutHorizontally(
                targetOffsetX = { -it / transitionOffsetProportion },
                animationSpec = transitionAnimationSpec
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / transitionOffsetProportion },
                animationSpec = transitionAnimationSpec
            ) + fadeIn()
        },
        popExitTransition = {
            fadeOut() + slideOutHorizontally(
                targetOffsetX = { it / transitionOffsetProportion },
                animationSpec = transitionAnimationSpec
            )
        }
    ) {
        composable(RootScreen.Home.route) {
            HomeScreen(navHostController = navHostController)
        }
        // Settings
        composable(RootScreen.Settings.route) {
            SettingsScreen(navHostController = navHostController)
        }
        composable(SettingsScreens.LocationFeatures.route) {
            LocationFeaturesScreen(navHostController = navHostController)
        }
        composable(SettingsScreens.RecentStops.route) {
            RecentStopsSettingsScreen(navHostController = navHostController)
        }
        composable(SettingsScreens.RecentServices.route) {
            RecentServicesSettingsScreen(navHostController = navHostController)
        }
    }
}