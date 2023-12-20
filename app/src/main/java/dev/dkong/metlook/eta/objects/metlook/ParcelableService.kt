package dev.dkong.metlook.eta.objects.metlook

import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.objects.ptv.Route
import kotlinx.serialization.Serializable

/**
 * Carrier object for a [DepartureService], with only the essential data
 */
@Serializable
data class ParcelableService(
    val runRef: String,
    val routeType: RouteType,
    val route: Route,
    val name: String,
    val destination: String
)