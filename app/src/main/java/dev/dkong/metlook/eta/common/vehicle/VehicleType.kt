package dev.dkong.metlook.eta.common.vehicle

import androidx.compose.runtime.Composable

/**
 * Abstract class for vehicle types
 * @param name display name of the vehicle
 * @param id the fleet number of the vehicle
 */
abstract class VehicleType(
    val name: String,
    val id: String,
    val icon: (@Composable () -> Unit)? = null
)
