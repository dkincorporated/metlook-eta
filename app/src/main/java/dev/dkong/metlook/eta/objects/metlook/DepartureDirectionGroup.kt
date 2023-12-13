package dev.dkong.metlook.eta.objects.metlook

/**
 * Record for a group of departures on the Direction level
 */
data class DepartureDirectionGroup(
    val groupingId: Int,
    val routeNumber: String?,
    val name: String
)
