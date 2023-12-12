package dev.dkong.metlook.eta.objects.ptv

import dev.dkong.metlook.eta.common.Constants
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.time.LocalDateTime


/**
 * Run object from PTV API
 */
@Serializable
data class Run(
    @SerialName("run_id") val runId: Int,
    @SerialName("run_ref") val runRef: String,
    @SerialName("route_id") val routeId: Int,
    @SerialName("route_type") val routeType: Int,
    @SerialName("final_stop_id") val finalStopId: Int,
    @SerialName("destination_name") val destinationName: String,
    val status: String,
    @SerialName("direction_id") val directionId: Int,
    @SerialName("run_sequence") val runSequence: Int,
    @SerialName("express_stop_count") val expressStopCount: Int,
    @SerialName("vehicle_position") val vehiclePosition: VehiclePosition,
    @SerialName("vehicle_descriptor") val vehicleDescriptor: VehicleDescriptor,
    val geopath: List<Long>
)

/**
 * Vehicle Position object from PTV API Run object
 */
@Serializable
data class VehiclePosition(
    val latitude: Double,
    val longitude: Double,
    val easting: Double,
    val northing: Double,
    val direction: String,
    val bearing: Double,
    val supplier: String,
    @SerialName("datetime_utc") private val datetimeUtcString: String,
    @SerialName("expiry_time") private val expiryTimeString: String
) {
    @Contextual
    val dateTime = LocalDateTime.parse(datetimeUtcString, Constants.dateTimeFormat)
    @Contextual
    val expiryTime = LocalDateTime.parse(expiryTimeString, Constants.dateTimeFormat)
}

/**
 * Vehicle Description object from PTV API Run object
 */
@Serializable
data class VehicleDescriptor(
    val operator: String,
    val id: String,
    @SerialName("low_floor")
    val lowFloor: Boolean?,
    @SerialName("air_conditioned")
    val airConditioned: Boolean?,
    val description: String,
    val supplier: String,
    val length: String
)
