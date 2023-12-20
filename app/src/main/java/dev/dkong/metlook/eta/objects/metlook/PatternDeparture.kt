package dev.dkong.metlook.eta.objects.metlook

import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.composables.StoppingPatternComposables
import dev.dkong.metlook.eta.objects.ptv.Departure
import dev.dkong.metlook.eta.objects.ptv.Run
import dev.dkong.metlook.eta.objects.ptv.Stop
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

/**
 * Departure object for stopping patterns
 */
data class PatternDeparture(
    private val departure: Departure,
    private val run: Run,
    val stop: Stop,
    val stopType: StoppingPatternComposables.StopType
) {
    val routeType = run.routeType
    val isAtPlatform = departure.atPlatform
    val platform = departure.platform

    val scheduledDeparture = departure.scheduledDeparture
    val estimatedDeparture = departure.estimatedDeparture

    /**
     * Get the time to the scheduled departure
     * @return duration until scheduled departure
     */
    fun timeToScheduledDeparture(): Duration = departure.scheduledDeparture - Clock.System.now()

    /**
     * Get the time to the estimated departure
     * @return duration until estimated departure
     */
    fun timeToEstimatedDeparture(): Duration? =
        if (estimatedDeparture != null) estimatedDeparture - Clock.System.now()
        else null


    /**
     * Get the delay of the service
     * @return delay duration
     */
    fun delay(): Duration? = estimatedDeparture?.minus(scheduledDeparture)

    /**
     * Whether the service is arriving
     *
     * Only for trains; other modes will always return false.
     */
    fun isArriving(): Boolean = routeType == RouteType.Train &&
            (delay()?.inWholeSeconds?.rem(60L) ?: 0) != 0L

    /**
     * Get the display time of the scheduled departure
     */
    fun scheduledDepartureTime() =
        Constants.displayTimeFormatter.format(
            scheduledDeparture
                .toLocalDateTime(TimeZone.of("Australia/Melbourne"))
                .toJavaLocalDateTime()
        ).lowercase() // the lowercase() turns the am/pm indicator to lowercase
}