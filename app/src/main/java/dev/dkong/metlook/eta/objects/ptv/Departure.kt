package dev.dkong.metlook.eta.objects.ptv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val scheduledDepartureUtc: String,
    @SerialName("estimated_departure_utc")
    val estimatedDepartureUtc: String,
    @SerialName("at_platform")
    val atPlatform: Boolean,
    @SerialName("platform_number")
    val platformNumber: String,
    @SerialName("flags")
    val flags: String,
    @SerialName("departure_sequence")
    val departureSequence: Int
)