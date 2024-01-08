package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
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
    val directions: Map<String, Direction>,
    val disruptions: Map<Int, Disruption>
) {
    /**
     * Convert to a sequence of [ServiceDeparture]s
     * @param stop the stop for the departures
     * @return processed sequence of [ServiceDeparture]s
     */
    fun toDepartureSequence(stop: Stop): Sequence<ServiceDeparture> =
        departures
            .asSequence()
            .map { departure ->
                // Entries with data errors will be ignored

                val route = routes[departure.routeId]
                    ?: return@map null
                val run = runs[departure.runRef]
                    ?: return@map null
                val direction =
                    directions[departure.directionId.toString()]
                        ?: return@map null

                // Initiate the all-in-one departure object
                val processedDeparture = ServiceDeparture(
                    departure,
                    route,
                    run,
                    direction,
                    disruptions.filter { entry ->
                        departure.disruptionIds.contains(entry.value.disruptionId)
                    }.values.toList(),
                    stop
                )

                // Filter out unwanted departures
                if (!processedDeparture.isValid) return@map null

                return@map processedDeparture
            }
            .filterNotNull() // remove invalid entries
}

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
    private val platformNumber: String?,
    @SerialName("flags")
    val flags: String,
    @SerialName("departure_sequence")
    val departureSequence: Int,
    @SerialName("skipped_stops")
    val skippedStops: List<Stop>? = null
) {
    val platform = platformNumber.let { if (it == "") null else it }

    @Contextual
    val scheduledDeparture = Instant.parse(scheduledDepartureUtcString)

    @Contextual
    val estimatedDeparture =
        if (estimatedDepartureUtcString != null) Instant.parse(estimatedDepartureUtcString)
        else null
}