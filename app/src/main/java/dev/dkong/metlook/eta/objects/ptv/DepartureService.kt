package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.objects.metlook.PatternType

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
    val routeId = departure.routeId
    val scheduledDeparture = departure.scheduledDeparture
    val estimatedDeparture = departure.estimatedDeparture
    val platformNumber = departure.platformNumber
    val isAtPlatform = departure.atPlatform
    val flags = if (departure.flags == "") null else departure.flags

    val finalStopId = run.finalStopId
    val destinationName = run.destinationName
    val status = run.status
    private val expressStopCount = run.expressStopCount
    val vehicleDescriptor = run.vehicleDescriptor
    val vehiclePosition = run.vehiclePosition

    /**
     * Get the discrete stopping pattern description/type (only for Train)
     */
    fun patternType(): PatternType {
        if (routeType != RouteType.Train) return PatternType.NotApplicable
        if (expressStopCount == 0) {
            return PatternType.AllStops
        }
        if (expressStopCount == 1) {
            return PatternType.SkipsOneStop
        }
        when (routeId) {
            2, 9 -> {
                if (expressStopCount <= 5) {
                    return PatternType.LimitedStops
                }
            }
            1, 4, 11, 3, 6, 8 -> {
                if (expressStopCount <= 4) {
                    return PatternType.LimitedStops
                }
            }
            5, 14, 16 -> {
                if (expressStopCount <= 3) {
                    return PatternType.LimitedStops
                }
            }
            7 -> {
                if (expressStopCount <= 7) {
                    return PatternType.LimitedStops
                }
            }
            12, 13, 15, 17 -> {
                return PatternType.LimitedStops
            }
            1482 -> {
                if (expressStopCount <= 2) {
                    return PatternType.LimitedStops
                }
            }
        }
        return PatternType.SuperLimitedStops
    }
}
