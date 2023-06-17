package dev.dkong.metlook.eta.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Constants maintained throughout the app
 * @author David Kong
 */
class Constants {
    companion object {
        /**
         * Get the base surface colour for background-type surfaces
         * @return background surface colour
         */
        @Composable
        fun appSurfaceColour() = MaterialTheme.colorScheme.inverseOnSurface

        /**
         * Get the container colour for Material List Card
         * @return Material List Card container colour
         */
        @Composable
        fun materialListCardContainerColour() =
            MaterialTheme.colorScheme.surfaceColorAtElevation(0.5.dp)

        /**
         * Get the container colour of the top app bar when scrolled
         * @return top app bar scrolled container colour
         */
        @Composable
        fun scrolledAppbarContainerColour() =
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    }
}
