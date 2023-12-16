package dev.dkong.metlook.eta.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.dkong.metlook.eta.R

/**
 * Modes of transport
 * @author David Kong
 */
enum class RouteType(
    val id: Int,
    @StringRes val displayName: Int,
    @DrawableRes val icon: Int
) {
    Train(0, R.string.route_type_train, R.drawable.outline_train_24),
    Tram(1, R.string.route_type_tram, R.drawable.outline_tram_24),
    Bus(2, R.string.route_type_bus, R.drawable.outline_directions_bus_24),
    Regional(3, R.string.route_type_regional, R.drawable.outline_commute_24),
    Other(99, R.string.not_applicable, R.drawable.outline_navigation_24);

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