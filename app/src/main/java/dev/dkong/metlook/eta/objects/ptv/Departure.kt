package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.Constants
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Resultant object from PTV API Departures
 */
@Serializable
data class DepartureResult(
    val departures: List<Departure>,
    val stops: Map<Int, Stop>,
    val routes: Map<Int, Route>,
    val runs: Map<String, Run>,
    val directions: Map<Int, Direction>,
    val disruptions: Map<Int, Disruption>
)

/**
 * Departure object from PTV API
 */
@Serializable
data class Departure(
    @SerialName("stop_id")
    val stopId: Int,
    @SerialName("route_id")
    val routeId: Int,
    @SerialName("run_id")
    val runId: Int,
    @SerialName("run_ref")
    val runRef: String,
    @SerialName("direction_id")
    val directionId: Int,
    @SerialName("disruption_ids")
    val disruptionIds: List<Int>,
    @SerialName("scheduled_departure_utc")
    private val scheduledDepartureUtcString: String,
    @SerialName("estimated_departure_utc")
    private val estimatedDepartureUtcString: String?,
    @SerialName("at_platform")
    val atPlatform: Boolean,
    @SerialName("platform_number")
    val platformNumber: String?,
    @SerialName("flags")
    val flags: String,
    @SerialName("departure_sequence")
    val departureSequence: Int
) {
    @Contextual
    val scheduledDeparture = Instant.parse(scheduledDepartureUtcString)

    @Contextual
    val estimatedDeparture =
        if (estimatedDepartureUtcString != null) Instant.parse(estimatedDepartureUtcString)
        else null
}