package dev.dkong.metlook.eta.common

/**
 * Modes of transport
 * @author David Kong
 */
enum class RouteType(routeId: Int, name: String) {
    Train(0, "Train"),
    Tram(1, "Tram"),
    Bus(2, "Bus"),
    Other(99, "Other");

    companion object {
        /**
         * Get the Route Type from its ID
         */
        fun fromId(id: Int): RouteType {
            return when (id) {
                0 -> Train
                1 -> Tram
                2 -> Bus
                else -> Other
            }
        }
    }
}