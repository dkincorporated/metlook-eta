package dev.dkong.metlook.eta.objects.metlook

/**
 * Record for a group of departures on the Destination / stopping pattern level
 */
data class DepartureDestinationGroup(
    val finalStopId: Int,
    val title: String
)
