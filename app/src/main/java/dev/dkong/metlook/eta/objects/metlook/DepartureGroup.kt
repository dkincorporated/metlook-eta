package dev.dkong.metlook.eta.objects.metlook

/**
 * Record for a group of departures
 */
data class DepartureGroup(
    val groupingId: Int,
    val routeNumber: String?,
    val name: String
)
