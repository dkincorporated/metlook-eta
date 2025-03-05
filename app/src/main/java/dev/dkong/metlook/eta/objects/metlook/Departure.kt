package dev.dkong.metlook.eta.objects.metlook

import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.objects.ptv.Departure
import dev.dkong.metlook.eta.objects.ptv.Run
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Structure for kinds of Departures
 */
@Serializable
abstract class Departure(
    private val originalDeparture: Departure,
    private val originalRun: Run,
) {
    val routeType = originalRun.routeType
    val interchange = originalRun.interchange
    val isAtPlatform = originalDeparture.atPlatform
    val platform = originalDeparture.platform
    val scheduledDepartureIso = originalDeparture.scheduledDepartureUtcString
    val estimatedDepartureIso = originalDeparture.estimatedDepartureUtcString
    private val scheduledDeparture = originalDeparture.scheduledDeparture
    private val estimatedDeparture = originalDeparture.estimatedDeparture

    /**
     * Get the time to the scheduled departure
     * @return duration until scheduled departure
     */
    fun timeToScheduledDeparture(): Duration = originalDeparture.scheduledDeparture - Clock.System.now()

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
        Constants.timeFormatter.format(
            scheduledDeparture
                .toLocalDateTime(TimeZone.of("Australia/Melbourne"))
                .toJavaLocalDateTime()
        ).lowercase() // the lowercase() turns the am/pm indicator to lowercase

    /**
     * Get the display time of the estimated departure
     */
    fun estimatedDepartureTime() =
        estimatedDeparture?.let {
            Constants.timeFormatter.format(
                estimatedDeparture
                    .toLocalDateTime(TimeZone.of("Australia/Melbourne"))
                    .toJavaLocalDateTime()
            ).lowercase() // the lowercase() turns the am/pm indicator to lowercase
        }
}