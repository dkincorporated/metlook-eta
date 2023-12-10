package dev.dkong.metlook.eta.common.utils

import androidx.compose.ui.graphics.Color
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.ui.official.*
import dev.dkong.metlook.eta.ui.theme.*

/**
 * Utility functions for resource-related calls
 * @author David Kong
 */
class ResourceUtils {
    companion object {
        /**
         * Gets the route color based on the route ID and route type
         * @param routeId the route ID
         * @param routeType the route type
         * @return the route color
         */
        fun getRouteColour(routeType: RouteType?, routeId: Int? = null): Color {
            return when (routeType) {
                RouteType.Train -> {
                    if (routeId == null) return PtvTrain
                    when (routeId) {
                        1, 2, 7, 9 -> TrainBurnley
                        5, 8 -> TrainCliftonHill
                        4, 11 -> TrainDandenong
                        6, 16, 17 -> TrainCrosscity
                        12 -> TrainSandringham
                        3, 14, 15 -> TrainNorthern
                        else -> PtvSpecialServices
                    }
                }
                RouteType.Tram -> {
                    if (routeId == null) return PtvTram
                    when (routeId) {
                        721 -> Yt1
                        722 -> Yt109
                        724 -> Yt16
                        725 -> Yt19
                        761 -> Yt3
                        887 -> Yt57
                        897 -> Yt59
                        909 -> Yt64
                        913 -> Yt67
                        940 -> Yt70
                        947 -> Yt72
                        958 -> Yt75
                        976 -> Yt78
                        1002 -> Yt82
                        1041 -> Yt96
                        1083 -> Yt5
                        1112 -> Yt35
                        1880 -> Yt30
                        1881 -> Yt86
                        2903 -> Yt48
                        3343 -> Yt11
                        8314 -> Yt12
                        11529 -> Yt58
                        11544 -> Yt6
                        else -> PtvTram
                    }
                }
                RouteType.Bus -> PtvBus
                else -> PtvSpecialServices
            }
        }

        /**
         * Returns the foreground color for the route type and route ID
         * @param intRouteId the route ID
         * @param routeType the route type
         * @return the foreground color -- black or white
         */
        fun getRouteForegroundColour(routeType: RouteType?, intRouteId: Int?): Color {
            return when (routeType) {
                RouteType.Train -> {
                    if (intRouteId == null) return White
                    when (intRouteId) {
                        12, 4, 11, 3, 14, 15 -> Black
                        else -> White
                    }
                }
                RouteType.Tram -> {
                    if (intRouteId == null) return White
                    when (intRouteId) {
                        721, 722, 724, 761, 887, 940, 947, 958, 976, 1002, 1881, 3343 -> Black
                        else -> White
                    }
                }
                RouteType.Bus -> White
                else -> White
            }
        }

        /**
         * Returns the icon depending on the route type
         * @param routeType the route type
         * @param routeId the route ID (used for airport services)
         * @return the resource ID of the icon
         */
        fun getRouteTypeIcon(routeType: RouteType?, routeId: Int? = null): Int {
            if (routeId in arrayOf(1123, 13621)) return R.drawable.outline_local_airport_24
            return when (routeType) {
                RouteType.Train -> R.drawable.outline_train_24
                RouteType.Tram -> R.drawable.outline_tram_24
                RouteType.Bus -> R.drawable.outline_directions_bus_24
                else -> R.drawable.outline_navigation_24
            }
        }
    }
}