package dev.dkong.metlook.eta.objects.ptv

/**
 * All-in-one object for a service departure from a Stop
 *
 * This combines all data objects into one that provides all the useful data.
 */
data class DepartureService(
    private val departure: Departure,
    val route: Route,
    private val run: Run,
    val direction: Direction,
    val disruptions: List<Disruption>
) {
    val departureStopId = departure.stopId
    val runRef = departure.runRef
    private val runId = departure.runId
    val routeType = run.routeType
    val scheduledDeparture = departure.scheduledDeparture
    val estimatedDeparture = departure.estimatedDeparture
    val platformNumber = departure.platformNumber
    val isAtPlatform = departure.atPlatform
    val flags = if (departure.flags == "") null else departure.flags

    val finalStopId = run.finalStopId
    val destinationName = run.destinationName
    val status = run.status
    val expressStopCount = run.expressStopCount
    // TODO: Add enum for pattern type
    val vehicleDescriptor = run.vehicleDescriptor
    val vehiclePosition = run.vehiclePosition
}
