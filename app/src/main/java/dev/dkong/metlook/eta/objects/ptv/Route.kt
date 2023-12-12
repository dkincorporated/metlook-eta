package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.RouteType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Object for the Route from PTV API
 * @author David Kong
 */
@Serializable
data class Route(
    @SerialName("route_name")
    val routeName: String,
    @SerialName("route_number")
    private val routeNumberString: String,
    @SerialName("route_type")
    private val routeTypeId: Int,
    @SerialName("route_id")
    val routeId: Int,
    @SerialName("route_gtfs_id")
    val routeGtfsId: String,
    @SerialName("route_service_status")
    val routeServiceStatus: RouteServiceStatus? = null
) {
    val routeType = RouteType.fromId(routeTypeId)

    val routeNumber =
        if (routeNumberString == "") null
        else routeNumberString
}

/**
 * Service Status object for Route object
 * @see Route
 */
@Serializable
data class RouteServiceStatus(
    val description: String,
    val timestamp: String
)
