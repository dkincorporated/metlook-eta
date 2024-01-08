package dev.dkong.metlook.eta.objects.metlook

import android.content.Context
import android.content.Intent
import dev.dkong.metlook.eta.activities.ServiceActivity
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils
import dev.dkong.metlook.eta.common.datastore.recents.RecentServicesCoordinator
import dev.dkong.metlook.eta.objects.ptv.Departure
import dev.dkong.metlook.eta.objects.ptv.Direction
import dev.dkong.metlook.eta.objects.ptv.Disruption
import dev.dkong.metlook.eta.objects.ptv.Route
import dev.dkong.metlook.eta.objects.ptv.Run
import dev.dkong.metlook.eta.objects.ptv.Stop
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.time.Duration

/**
 * All-in-one object for a service departure from a Stop
 *
 * This combines all data objects into one that provides all the useful data.
 */
@Serializable
data class ServiceDeparture(
    private val departure: Departure,
    val route: Route,
    private val run: Run,
    val direction: Direction,
    val disruptions: List<Disruption>,
    val departureStop: Stop,
    var alightingStop: Stop? = null
) : dev.dkong.metlook.eta.objects.metlook.Departure(departure, run) {
    val runRef = departure.runRef
    val runId = departure.runId
    val routeId = departure.routeId
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
    @Contextual
    val isValid: Boolean = !(routeType == RouteType.Bus && runId != -1)

    /**
     * Whether the service is cancelled
     */
    @Contextual
    val isCancelled = status == "cancelled"

    /**
     * Get the title of the service
     */
    @Contextual
    val serviceTitle =
        if (routeType == RouteType.Train) {
            // Flinders Street, Flagstaff, Parliament services get displayed as Flinders St
            if (arrayOf(1071, 1068, 1155).contains(finalStopId)) "Flinders St"
            // Frankston services to Southern Cross get displayed as Flinders St
            else if (routeId == 6 && finalStopId == 1181) "Flinders St"
            else destinationName
        } else direction.directionName ?: ""

    /**
     * Value to be used as the grouping identifier for the direction group stage
     */
    @Contextual
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

            // For buses, trams and regional services, edit the route name
            if (arrayOf(RouteType.Tram, RouteType.Bus, RouteType.Regional).contains(routeType)) {
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
    @Contextual
    val patternType: PatternType = Utils.patternType(routeType, routeId, expressStopCount)

    /**
     * Launch [ServiceActivity] for this departure
     * @param context the launching context
     */
    suspend fun launchServiceActivity(context: Context) {
        // Launch the Service screen
        val serviceIntent = Intent(context, ServiceActivity::class.java)
        serviceIntent.putExtra(
            "service",
            Constants.jsonFormat.encodeToString(this)
        )
        context.startActivity(serviceIntent)
        // Save the recent service
        RecentServicesCoordinator.add(context, this)
    }

    /**
     * Whether one Service is the same as another Service or object
     * @return true if both are [ServiceDeparture]s and have matching route type and run ref, else false
     */
    override fun equals(other: Any?): Boolean {
        return (other as? ServiceDeparture)?.let {
            it.routeType.id == routeType.id
                    && it.runRef == runRef
        } ?: false
    }
}
