package dev.dkong.metlook.eta.objects.ptv

import kotlinx.serialization.Serializable

@Serializable
data class PatternResult(
    val disruptions: List<Disruption>,
    val departures: List<Departure>,
    val stops: Map<Int, Stop>,
    val routes: Map<Int, Route>,
    val runs: Map<String, Run>,
    val directions: Map<String, Direction>
)