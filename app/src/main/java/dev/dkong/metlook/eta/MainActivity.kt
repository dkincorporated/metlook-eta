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
import dev.dkong.metlook.eta.screens.RootScreen
import dev.dkong.metlook.eta.screens.home.HomeScreen
import dev.dkong.metlook.eta.screens.settings.SettingsScreen
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
    // Transition between screens
    val offsetProportion = 10
    val animationSpec: FiniteAnimationSpec<IntOffset> = tween(300)

    NavHost(
        navController = navHostController,
        startDestination = RootScreen.Home.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / offsetProportion },
                animationSpec = animationSpec
            ) + fadeIn()
        },
        exitTransition = {
            fadeOut() + slideOutHorizontally(
                targetOffsetX = { -it / offsetProportion },
                animationSpec = animationSpec
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / offsetProportion },
                animationSpec = animationSpec
            ) + fadeIn()
        },
        popExitTransition = {
            fadeOut() + slideOutHorizontally(
                targetOffsetX = { it / offsetProportion },
                animationSpec = animationSpec
            )
        }
    ) {
        composable(RootScreen.Home.route) {
            HomeScreen(navHostController = navHostController)
        }
        composable(RootScreen.Settings.route) {
            SettingsScreen(navHostController = navHostController)
        }
    }
}