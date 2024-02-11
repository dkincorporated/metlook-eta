package dev.dkong.metlook.eta.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ventura.TrackerIntegrationSettingsScreen
import dev.dkong.metlook.eta.screens.RootScreen
import dev.dkong.metlook.eta.screens.settings.LocationFeaturesScreen
import dev.dkong.metlook.eta.screens.settings.RecentsSettingsScreen
import dev.dkong.metlook.eta.screens.settings.SettingsScreen
import dev.dkong.metlook.eta.screens.settings.SettingsScreens
import dev.dkong.metlook.eta.ui.theme.MetlookTheme

/**
 * Activity for Settings
 * @author David Kong
 */
class SettingsActivity : ComponentActivity() {
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
                    SettingsScreenHost(navController)
                }
            }
        }
    }

    /**
     * Root screen for all Settings screens
     */
    @Composable
    fun SettingsScreenHost(navHostController: NavHostController) {
        NavHost(
            navController = navHostController,
            startDestination = RootScreen.Settings.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / Constants.transitionOffsetProportion },
                    animationSpec = Constants.transitionAnimationSpec
                ) + fadeIn()
            },
            exitTransition = {
                fadeOut() + slideOutHorizontally(
                    targetOffsetX = { -it / Constants.transitionOffsetProportion },
                    animationSpec = Constants.transitionAnimationSpec
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / Constants.transitionOffsetProportion },
                    animationSpec = Constants.transitionAnimationSpec
                ) + fadeIn()
            },
            popExitTransition = {
                fadeOut() + slideOutHorizontally(
                    targetOffsetX = { it / Constants.transitionOffsetProportion },
                    animationSpec = Constants.transitionAnimationSpec
                )
            }
        ) {
            // Settings
            composable(RootScreen.Settings.route) {
                SettingsScreen(navHostController = navHostController)
            }
            composable(SettingsScreens.LocationFeatures.route) {
                LocationFeaturesScreen(navHostController = navHostController)
            }
            composable(SettingsScreens.Recents.route) {
                RecentsSettingsScreen(navHostController = navHostController)
            }
            composable(SettingsScreens.TrackerIntegration.route) {
                TrackerIntegrationSettingsScreen(navHostController = navHostController)
            }
        }
    }
}