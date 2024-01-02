package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.RouteType
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

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
    private val routeTypeId: Int,
    @SerialName("routes")
    val routes: List<Route>? = null,
    @SerialName("stop_latitude")
    val stopLatitude: Double,
    @SerialName("stop_longitude")
    val stopLongitude: Double,
    @SerialName("stop_sequence")
    val stopSequence: Int,
    @SerialName("stop_id")
    val stopId: Int,
    @SerialName("stop_name")
    val fullStopName: String,
    @SerialName("stop_landmark")
    val stopLandmark: String
) {
    val routeType = RouteType.fromId(routeTypeId)

    /**
     * Attempt to split the stop name for on-road stops (tram, bus)
     * @return stop road, stop on-road, stop number (if applicable)
     */
    @Contextual
    val stopName: Triple<String, String?, String?> get() {
        val stopName = when (routeType) {
            RouteType.Train -> fullStopName.replace(" Station", "")
            else -> fullStopName
        }.trim()

        // Only Tram and Bus can be split; other route types will just return the stop name as-is
        if (!arrayOf(RouteType.Tram, RouteType.Bus).contains(routeType))
            return Triple(stopName, null, null)

        // Attempt to split by slash
        val splitItems = stopName.split("/")
        // If split didn't result in 2 items, it cannot be split properly
        if (splitItems.size != 2) return Triple(stopName, null, null)
        // Attempt to extract the stop number
        return if (splitItems[1].split("#").size == 2) {
            // A stop number is available
            val splitStopNumber = splitItems[1].split("#")
            Triple(splitItems[0].trim(), splitStopNumber[0].trim(), splitStopNumber[1].trim())
        } else if (Regex("""D(\d+)-""").find(splitItems[0])?.groupValues?.size == 2) {
            // D-style stop number is available
            Triple(
                splitItems[0].trim().split("-")[1],
                splitItems[1].trim(),
                "D" + Regex("""D(\d+)-""").find(splitItems[0])?.groupValues?.get(1)
            )
        } else {
            // Stop number is not available
            Triple(splitItems[0].trim(), splitItems[1].trim(), null)
        }
    }

    /**
     * Whether one Stop is the same as another Stop or object
     * @return true if both are [Stop]s and have matching route type and stop IDs, else false
     */
    override fun equals(other: Any?): Boolean =
        (other as? Stop)?.let {
            it.routeTypeId == routeTypeId
                    && it.stopId == stopId
        } ?: false
}
