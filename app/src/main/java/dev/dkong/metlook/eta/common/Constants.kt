package dev.dkong.metlook.eta.common

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.serialization.json.Json

/**
 * Constants maintained throughout the app
 * @author David Kong
 */
class Constants {
    companion object {
        // Transition between screens
        val transitionOffsetProportion = 10
        val transitionAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(300)

        /**
         * Get the base surface colour for background-type surfaces
         * @return background surface colour
         */
        @Composable
        fun appSurfaceColour() = MaterialTheme.colorScheme.surfaceContainer

        /**
         * Get the container colour for Material List Card
         * @return Material List Card container colour
         */
        @Composable
        fun materialListCardContainerColour() =
            MaterialTheme.colorScheme.surfaceContainerLow

        /**
         * Get the container colour of the top app bar when scrolled
         * @return top app bar scrolled container colour
         */
        @Composable
        fun scrolledAppbarContainerColour() =
            MaterialTheme.colorScheme.surfaceContainerHighest

        // JSON
        /**
         * Format for parsing JSON using Kotlinx Serialization
         */
        val jsonFormat = Json { ignoreUnknownKeys = true }

        /**
         * Client for ktor
         */
        val httpClient = HttpClient(Android)

        // Corner radii
        val largeListCornerRadius = 16.dp
        val smallListCornerRadius = 4.dp
    }
}
