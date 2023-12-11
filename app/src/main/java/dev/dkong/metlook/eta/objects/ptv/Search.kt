package dev.dkong.metlook.eta.objects.ptv

import kotlinx.serialization.Serializable

/**
 * Resultant object from PTV API Search results
 */
@Serializable
data class SearchResult(
    val stops: List<Stop>,
    val routes: List<Route>
)

