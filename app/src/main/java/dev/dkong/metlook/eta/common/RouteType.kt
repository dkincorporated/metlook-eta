package dev.dkong.metlook.eta.common

import androidx.annotation.DrawableRes
import dev.dkong.metlook.eta.R

/**
 * Modes of transport
 * @author David Kong
 */
enum class RouteType(
    val id: Int,
    val displayName: String,
    @DrawableRes val icon: Int
) {
    Train(0, "Train", R.drawable.outline_train_24),
    Tram(1, "Tram", R.drawable.outline_tram_24),
    Bus(2, "Bus", R.drawable.outline_directions_bus_24),
    Regional(3, "Regional", R.drawable.outline_commute_24),
    Other(99, "Other", R.drawable.outline_navigation_24);

    companion object {
        /**
         * Get the Route Type from its ID
         */
        fun fromId(id: Int): RouteType {
            return when (id) {
                0 -> Train
                1 -> Tram
                2 -> Bus
                3 -> Regional
                else -> Other
            }
        }
    }
}