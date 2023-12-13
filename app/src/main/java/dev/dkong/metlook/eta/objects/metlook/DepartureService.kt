package dev.dkong.metlook.eta.objects.metlook

import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.objects.ptv.Departure
import dev.dkong.metlook.eta.objects.ptv.Direction
import dev.dkong.metlook.eta.objects.ptv.Disruption
import dev.dkong.metlook.eta.objects.ptv.Route
import dev.dkong.metlook.eta.objects.ptv.Run
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

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
    val runId = departure.runId
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
     * Whether the service is valid
     */
    fun isValid(): Boolean {
        if (routeType == RouteType.Bus && runId != -1) return false

        return true
    }

    /**
     * Whether the service is cancelled
     */
    val isCancelled = status == "cancelled"

    /**
     * Get the title of the service
     */
    val serviceTitle =
        if (routeType == RouteType.Train) {
            // Flinders Street, Flagstaff, Parliament services get displayed as Flinders St
            if (arrayOf(1071, 1068, 1155).contains(finalStopId)) "Flinders St"
            // Frankston services to Southern Cross get displayed as Flinders St
            else if (routeId == 6 && finalStopId == 1181) "Flinders St"
            else destinationName
        } else direction.directionName

    /**
     * Get the time to the scheduled departure
     * @return duration until scheduled departure
     */
    fun timeToScheduledDeparture(): Duration = scheduledDeparture - Clock.System.now()

    /**
     * Get the time to the estimated departure
     * @return duration until estimated departure
     */
    fun timeToEstimatedDeparture(): Duration? =
        if (estimatedDeparture != null) estimatedDeparture - Clock.System.now()
        else null

    /**
     * Get the delay of the service
     * @return delay duration
     */
    fun delay(): Duration? = estimatedDeparture?.minus(scheduledDeparture)

    /**
     * Get the display time of the scheduled departure
     */
    fun scheduledDepartureTime() =
        Constants.displayTimeFormatter.format(
            scheduledDeparture
                .toLocalDateTime(TimeZone.of("Australia/Melbourne"))
                .toJavaLocalDateTime()
        ).lowercase() // the lowercase() turns the am/pm indicator to lowercase

    /**
     * Value to be used as the grouping identifier for the direction group stage
     */
    val directionGroupingValue = when (routeType) {
        RouteType.Train -> {
            // Group based on sets of routes
            when (direction.directionId) {
                1 -> DepartureDirectionGroup(10, null, "To City") // City
                0 -> DepartureDirectionGroup(11, null, "Alamein line") // Alamein
                2, 13, 14 -> DepartureDirectionGroup(
                    12,
                    null,
                    "Upfield, Craigieburn, Sunbury lines"
                ) // Northern
                3, 8 -> DepartureDirectionGroup(13, null, "Belgrave and Lilydale lines") // Ringwood
                4, 10 -> DepartureDirectionGroup(
                    14,
                    null,
                    "Pakenham and Cranbourne lines"
                ) // Dandenong
                5 -> DepartureDirectionGroup(15, null, "Frankston line") // Frankston
                6 -> DepartureDirectionGroup(16, null, "Glen Waverley line") // Glen Waverley
                7, 9 -> DepartureDirectionGroup(
                    17,
                    null,
                    "Hurstbridge and Mernda lines"
                ) // Clifton Hill
                11 -> DepartureDirectionGroup(18, null, "Sandringham line") // Sandringham
                15, 16 -> DepartureDirectionGroup(
                    19,
                    null,
                    "Werribee and Williamstown lines"
                ) // Newport
                18 -> DepartureDirectionGroup(
                    20,
                    null,
                    "Racecourse line"
                ) // Flemington Racecourse / Showgrounds
                else -> DepartureDirectionGroup(0, null, "")
            }
        }

        else -> {
            // Group based on individual route

            // For buses and trams, edit the route name
            if (arrayOf(RouteType.Tram, RouteType.Bus).contains(routeType)) {
                // Alter name
                var correctedName = route.routeName
                    // Remove Smartbus name
                    .replace(" (SMARTBUS Service)", "")
                    // Remove via info
                    .split(" via ").first()

                // Replace hyphen with en dash in route path
                val hyphenSplitName = correctedName.split(" - ")
                if (hyphenSplitName.size > 1)
                    correctedName = "${hyphenSplitName[0].trim()} – ${hyphenSplitName[1].trim()}"

                DepartureDirectionGroup(
                    routeId,
                    route.routeNumber,
                    correctedName
                )
            } else {
                DepartureDirectionGroup(routeId, route.routeNumber, route.routeName)
            }
        }
    }

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
