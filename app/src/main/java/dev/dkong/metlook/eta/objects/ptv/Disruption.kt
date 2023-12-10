package dev.dkong.metlook.eta.objects.ptv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val publishedOn: String,
    @SerialName("last_updated")
    val lastUpdated: String,
    @SerialName("from_date")
    val fromDate: String,
    @SerialName("to_date")
    val toDate: String,
    val routes: List<DisruptionRoute>,
    val stops: List<DisruptionStop>,
    val colour: String,
    @SerialName("display_on_board")
    val displayOnBoard: Boolean,
    @SerialName("display_status")
    val displayStatus: Boolean
)

/**
 * Route object for Disruption object
 */
@Serializable
data class DisruptionRoute(
    @SerialName("route_type")
    val routeType: Int,
    @SerialName("route_id")
    val routeId: Int,
    @SerialName("route_name")
    val routeName: String,
    @SerialName("route_number")
    val routeNumber: String,
    @SerialName("route_gtfs_id")
    val routeGtfsId: String,
    val direction: String? // Replace with the actual type of the direction if known
)

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