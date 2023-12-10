package dev.dkong.metlook.eta.common

/**
 * Modes of transport
 * @author David Kong
 */
enum class RouteType(routeId: Int, name: String) {
    Train(0, "Train"),
    Tram(1, "Tram"),
    Bus(2, "Bus")
}