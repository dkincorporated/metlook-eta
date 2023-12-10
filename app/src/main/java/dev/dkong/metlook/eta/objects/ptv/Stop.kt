package dev.dkong.metlook.eta.objects.ptv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Stop object from PTV API
 */
@Serializable
data class Stop(
    @SerialName("stop_distance")
    val stopDistance: Double,
    @SerialName("stop_suburb")
    val stopSuburb: String,
    @SerialName("route_type")
    val routeType: Int,
    @SerialName("routes")
    val routes: List<Route>,
    @SerialName("stop_latitude")
    val stopLatitude: Double,
    @SerialName("stop_longitude")
    val stopLongitude: Double,
    @SerialName("stop_sequence")
    val stopSequence: Int,
    @SerialName("stop_id")
    val stopId: Int,
    @SerialName("stop_name")
    val stopName: String,
    @SerialName("stop_landmark")
    val stopLandmark: String
)
