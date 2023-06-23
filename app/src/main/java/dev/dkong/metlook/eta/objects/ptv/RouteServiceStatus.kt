package dev.dkong.metlook.eta.objects.ptv

import kotlinx.serialization.Serializable

/**
 * Object for the Route Service Status from PTV API
 * @see Route
 * @author David Kong
 */
@Serializable
data class RouteServiceStatus(
    val description: String,
    val timestamp: String
)
