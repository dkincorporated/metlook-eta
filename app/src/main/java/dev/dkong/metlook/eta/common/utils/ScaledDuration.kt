package dev.dkong.metlook.eta.common.utils

import androidx.annotation.StringRes
import dev.dkong.metlook.eta.R
import kotlin.time.Duration

/**
 * Utility class for displaying a duration
 *
 * Note: **Not serialisable!** Don't store this in a serialisable class.
 * @param duration the duration to be displayed
 * @param textFormat transformation applied to the display value
 */
private class ScaledDuration(
    private val duration: Duration,
    private val textFormat: ((String) -> String)? = null
) {
    companion object {
        /**
         * Get the scaled-duration object with a definite [Duration]
         * @param duration the [Duration] to be scaled
         */
        fun getScaledDuration(duration: Duration) = ScaledDuration(duration)

        /**
         * Get the scaled-duration object with a nullable and fallback [Duration]
         * @param nullableDuration the [Duration] that may be null, and will fallback to [duration] if null
         * @param duration the [Duration] that will be falled back on if [nullableDuration] is null
         */
        fun getScaledDuration(nullableDuration: Duration?, duration: Duration) = ScaledDuration(
            nullableDuration ?: duration,
            if (nullableDuration == null) { s -> "$s*" } else null
        )
    }

    /**
     * Duration unit: defines a duration and its quantity
     * @param durationValue the duration scalar value
     * @param displayUnit the display unit
     * @param textFormat transformation applied to the display value
     */
    sealed class DurationUnit(
        private val durationValue: Long,
        @StringRes val displayUnit: Int,
        private val textFormat: ((String) -> String)? = null
    ) {
        class Second(duration: Long, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_sec, textFormat)

        class Minute(duration: Long, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_min, textFormat)

        class Hour(duration: Long, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_hr, textFormat)

        class Day(duration: Long, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_day, textFormat)

        /**
         * Get the string-formatted duration value
         */
        fun getValue() = durationValue
            .let {
                if (it == 0L) "<1" else it.toString()
            }
            // Replace negative sign
            .let {
                if (durationValue < 0f) it.replace("-", "−")
                else it
            }
            // Add the time suffix
            .let {
                textFormat?.let { f -> f(it) } ?: it
            }
    }

    /**
     * Get the time difference in the most-appropriate duration unit
     */
    fun getScaledDuration(): DurationUnit {
        return when (duration.inWholeSeconds) {
            in 0..59 -> DurationUnit.Second(duration.inWholeSeconds, textFormat)
            in 60..3599 -> DurationUnit.Minute(duration.inWholeMinutes, textFormat)
            in 3600..86399 -> DurationUnit.Hour(duration.inWholeHours, textFormat)
            else -> DurationUnit.Day(duration.inWholeDays, textFormat)
        }
    }
}
