package dev.dkong.metlook.eta.objects.metlook

import dev.dkong.metlook.eta.composables.StoppingPatternComposables
import dev.dkong.metlook.eta.objects.ptv.Departure
import dev.dkong.metlook.eta.objects.ptv.Run
import dev.dkong.metlook.eta.objects.ptv.Stop

/**
 * Departure object for stopping patterns
 */
data class PatternDeparture(
    private val departure: Departure,
    private val run: Run,
    val stop: Stop,
    val stopType: StoppingPatternComposables.StopType
) : dev.dkong.metlook.eta.objects.metlook.Departure(departure, run) {
    val departureSequence = departure.departureSequence
}