package dev.dkong.metlook.eta.common.utils

import android.net.Uri
import android.util.Log
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.objects.metlook.Departure
import dev.dkong.metlook.eta.objects.metlook.PatternDeparture
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import dev.dkong.metlook.eta.objects.ptv.DepartureResult
import dev.dkong.metlook.eta.objects.ptv.PatternResult
import dev.dkong.metlook.eta.objects.ptv.Run
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException

/**
 * Utilities for obtaining info about service transposal
 */
object ServiceTransposal {
    /**
     * Two-way (preceding and continuing) service transposal record
     * @param continuing the continuing service
     * @param preceding the preceding service
     */
    data class TwoWayTransposal(
        val continuing: ServiceDeparture?,
        val preceding: ServiceDeparture?
    )

    /**
     * Get the preceding and continuing services for a service
     * @param run the [Run] for which to obtain transposals
     * @param precedingStopId the stop ID for the service origin of the preceding service
     * @param continuingStopId the stop ID for the service origin of the continuing service
     */
    suspend fun getTransposals(run: Run, precedingStopId: Int? = null, continuingStopId: Int? = null): TwoWayTransposal? {
        val precedingInterchange = run.interchange?.distributor
        val continuingInterchange = run.interchange?.feeder

        if (precedingInterchange == null && continuingInterchange == null) return null

        return TwoWayTransposal(
            continuing = continuingInterchange?.let {
                getServiceDeparture(
                    run.routeType,
                    it.runRef,
                    continuingStopId ?: it.stopId
                )
            },
            preceding = precedingInterchange?.let {
                getServiceDeparture(
                    run.routeType,
                    it.runRef,
                    precedingStopId ?: it.stopId
                )
            }
        )
    }

    /**
     * Get the [ServiceDeparture] object for a service
     * @param routeType the [RouteType] of the service
     * @param runRef the Run Ref of the service
     * @param stopId the Stop at which to obtain departure info
     */
    private suspend fun getServiceDeparture(
        routeType: RouteType,
        runRef: String,
        stopId: Int? = null
    ): ServiceDeparture? {
        val request = PtvApi.getApiUrl(
            Uri.Builder().apply {
                appendPath("v3")
                appendPath("pattern")
                appendPath("run")
                appendPath(runRef)
                appendPath("route_type")
                appendPath(routeType.id.toString())
                appendQueryParameter("expand", "all")
                // Only get skipped stops for Train
                if (routeType == RouteType.Train)
                    appendQueryParameter("include_skipped_stops", "true")
            }
        ) ?: return null

        // Run web request
        val response: String = Constants.httpClient.get(request).body()

        return try {
            // Decode the pattern
            val decodedPattern =
                Constants.jsonFormat.decodeFromString<PatternResult>(response)

            ServiceDeparture(
                // The origin stop should always be findable, but in case it isn't, the first one shouldn't be too far off
                departure = decodedPattern.departures.find { d -> d.stopId == stopId }
                    ?: decodedPattern.departures.first(),
                // Other values should also be findable, but return null in case it fails
                route = decodedPattern.routes[decodedPattern.departures.first().routeId]
                    ?: return null,
                run = decodedPattern.runs[runRef] ?: return null,
                direction = decodedPattern.directions[decodedPattern.departures.first().directionId.toString()]
                    ?: return null,
                disruptions = decodedPattern.disruptions,
                // The `departures` comment also applies to `departureStop`
                departureStop = decodedPattern.stops[stopId]
                    ?: decodedPattern.stops[decodedPattern.departures.first().stopId]
            )
        } catch (e: SerializationException) {
            // Pass
            Log.d("TRANSPOSAL", e.toString())
            null
        }
    }

    /**
     * Get the transposing service, if possible/any, for a service
     * @param departure the service to obtain for which to obtain its transposing service
     */
    @Deprecated("Since Interchange API, use `getTransposals` instead.")
    suspend fun getTransposedService(departure: PatternDeparture): ServiceDeparture? {
        if (
        // Transposing service is only available for Trains
            departure.routeType != RouteType.Train
            // No transposing service if no vehicle ID
            || (departure.vehicleDescriptor?.id ?: "") == ""
        ) return null

        // Get all candidate departures
        val departures = getCandidateDepartures(departure)

        // Store the target vehicle to match against
        val targetVehicle = departure.vehicleDescriptor

        // Try to find a matching vehicle
        return departures?.find { d ->
            d.vehicleDescriptor?.id == targetVehicle?.id
        }
    }

    /**
     * Get all departures from a stop
     */
    private suspend fun getCandidateDepartures(departure: PatternDeparture): List<ServiceDeparture>? {
        val request = PtvApi.getApiUrl(
            Uri.Builder().apply {
                appendPath("v3")
                appendPath("departures")
                appendPath("route_type")
                appendPath(departure.routeType.id.toString())
                appendPath("stop")
                appendPath(departure.stop.stopId.toString())
                departure.platform?.let {
                    if (it != "") appendQueryParameter("platform_numbers", it)
                }
                appendQueryParameter("date_utc", departure.scheduledDepartureIso)
                appendQueryParameter("expand", "all")
                appendQueryParameter("max_results", 20.toString())
            }
        ) ?: return null

        // Run web request
        val response: String = Constants.httpClient.get(request).body()

        return try {
            // Decode the departures
            val decodedDepartures =
                Constants.jsonFormat.decodeFromString<DepartureResult>(response)

            // Return the list of departure candidates
            decodedDepartures.toDepartureSequence(departure.stop).toList()
        } catch (e: SerializationException) {
            // Pass
            null
        }
    }
}