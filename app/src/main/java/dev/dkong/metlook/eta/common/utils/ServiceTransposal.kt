package dev.dkong.metlook.eta.common.utils

import android.net.Uri
import android.util.Log
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.objects.metlook.PatternDeparture
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import dev.dkong.metlook.eta.objects.ptv.DepartureResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException

/**
 * Utilities for obtaining info about service transposal
 */
object ServiceTransposal {
    /**
     * Get the transposing service, if possible/any, for a service
     * @param service the service to obtain for which to obtain its transposing service
     */
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