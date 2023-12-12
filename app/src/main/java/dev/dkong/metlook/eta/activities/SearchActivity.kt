package dev.dkong.metlook.eta.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.screens.search.SearchScreen
import dev.dkong.metlook.eta.ui.theme.MetlookTheme

/**
 * Activity for Search
 */
class SearchActivity : ComponentActivity() {
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
                    SearchScreen(navController)
                }
            }
        }
    }
}