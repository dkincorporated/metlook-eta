package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.RouteType
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Resultant object from PTV API Disruptions
 */
@Serializable
data class DisruptionsResult(
    val disruptions: Disruptions
)

/**
 * Master object for Disruptions
 */
@Serializable
data class Disruptions(
    @SerialName("metro_train")
    val metroTrain: List<Disruption>,
    @SerialName("metro_tram")
    val metroTram: List<Disruption>,
    @SerialName("metro_bus")
    val metroBus: List<Disruption>
) {
    /**
     * Get the disruptions for the route type
     * @param routeType the Route Type for which to filter
     */
    fun filterDisruptions(routeType: RouteType): List<Disruption> {
        return when (routeType) {
            RouteType.Train -> metroTrain
            RouteType.Tram -> metroTram
            RouteType.Bus -> metroBus
            else -> emptyList()
        }
    }
}

/**
 * Disruption object from PTV API
 */
@Serializable
data class Disruption(
    @SerialName("disruption_id")
    val disruptionId: Int,
    val title: String,
    val url: String,
    val description: String,
    @SerialName("disruption_status")
    val disruptionStatus: String,
    @SerialName("disruption_type")
    val disruptionType: String,
    @SerialName("published_on")
    private val publishedOnString: String,
    @SerialName("last_updated")
    private val lastUpdatedString: String,
    @SerialName("from_date")
    private val fromDateString: String,
    @SerialName("to_date")
    private val toDateString: String?,
    val routes: List<DisruptionRoute>,
    val stops: List<DisruptionStop>,
    val colour: String,
    @SerialName("display_on_board")
    val displayOnBoard: Boolean,
    @SerialName("display_status")
    val displayStatus: Boolean
) {
    /**
     * Priority of the disruption type when being shown
     */
    val typePriority = when (disruptionType.lowercase()) {
        "suspended" -> 1
        "part suspended" -> 2
        "major delays" -> 3
        "minor delays" -> 4
        "planned works" -> 5
        "diversion" -> 6
        "timetable/route changes" -> 7
        "service information" -> 8
        "other information" -> 9
        "planned closure" -> 10
        else -> 99
    }

    /**
     * Published-on [Instant] date time
     */
    @Contextual
    val publishedOn = Instant.parse(publishedOnString)

    /**
     * Last-updated [Instant] date time
     */
    @Contextual
    val lastUpdated = Instant.parse(lastUpdatedString)

    /**
     * From-date [Instant] date time
     */
    @Contextual
    val fromDate = Instant.parse(fromDateString)

    /**
     * To-date [Instant] date time
     */
    @Contextual
    val toDate = toDateString?.let { Instant.parse(it) }
}

/**
 * Route object for Disruption object
 */
@Serializable
data class DisruptionRoute(
    @SerialName("route_type")
    private val routeTypeId: Int,
    @SerialName("route_id")
    val routeId: Int,
    @SerialName("route_name")
    val routeName: String,
    @SerialName("route_number")
    val routeNumber: String,
    @SerialName("route_gtfs_id")
    val routeGtfsId: String,
    val direction: DisruptionRouteDirection?
) {
    val routeType = RouteType.fromId(routeTypeId)
}

/**
 * Direction object for Disruption Route object
 */
@Serializable
data class DisruptionRouteDirection(
    @SerialName("route_direction_id")
    val routeDirectionId: Int,
    @SerialName("direction_id")
    val directionId: Int,
    @SerialName("direction_name")
    val directionName: String,
    @SerialName("service_time")
    val serviceTime: String
)


/**
 * Stop object for Disruption object
 */
@Serializable
data class DisruptionStop(
    @SerialName("stop_id")
    val stopId: Int,
    @SerialName("stop_name")
    val stopName: String
)