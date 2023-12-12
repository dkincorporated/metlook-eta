package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.RouteType
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Direction object from PTV API
 */
@Serializable
data class Direction(
    @SerialName("direction_id") val directionId: Int,
    @SerialName("direction_name") val directionName: String,
    @SerialName("route_id") val routeId: Int,
    @SerialName("route_type") private val routeTypeId: Int
) {
    @Contextual
    val routeType = RouteType.fromId(routeTypeId)
}
