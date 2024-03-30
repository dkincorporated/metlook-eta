package dev.dkong.metlook.eta.common.utils

import android.content.Context
import androidx.annotation.StringRes
import dev.dkong.metlook.eta.R
import kotlin.math.abs
import kotlin.time.Duration

/**
 * Utility class for displaying a duration
 *
 * Note: **Not serialisable!** Don't store this in a serialisable class.
 * @param duration the duration to be displayed
 * @param textFormat transformation applied to the display value
 */
class ScaledDuration internal constructor(
    private val duration: Duration,
    private val textFormat: ((String) -> String)? = null
) {
    /**
     * Get the scaled-duration object with a definite [Duration]
     * @param duration the [Duration] to be scaled
     */
    constructor(duration: Duration) : this(duration, null)

    /**
     * Get the scaled-duration object with a nullable and fallback [Duration]
     * @param nullableDuration the [Duration] that may be null, and will fallback to [duration] if null
     * @param duration the [Duration] that will be falled back on if [nullableDuration] is null
     */
    constructor(nullableDuration: Duration?, duration: Duration) : this(
        nullableDuration ?: duration,
        if (nullableDuration == null) { s -> "$s*" } else null
    )

    companion object {
        /**
         * Scale every [DurationUnit] to the lowest common unit
         */
        fun List<ScaledDuration>.scaleToLowestCommonUnit(): List<DurationUnit> {
            this.lowestCommonUnit()?.let { unit ->
                return this.map { unit.toDurationUnit(it.scaleDuration()) }
            }
            return emptyList()
        }

        /**
         * Get the lowest common time unit
         *
         * Example: Minute, Second, Second, Hour => Second
         */
        fun List<ScaledDuration>.lowestCommonUnit(): DurationUnit? =
            this.minByOrNull { it.scaleDuration() }?.scaleDuration()
    }

    /**
     * Duration unit: defines a duration and its quantity
     * @param duration the original [Duration] value
     * @param displayUnit the display unit
     * @param ordinal how precise/"small" the unit is, for comparison purposes
     * @param textFormat transformation applied to the display value
     */
    sealed class DurationUnit(
        private val duration: Duration,
        @StringRes val displayUnit: Int,
        private val ordinal: Int,
        private val textFormat: ((String) -> String)? = null
    ) : Comparable<DurationUnit> {
        class Second(duration: Duration, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_sec, 1, textFormat) {
            override fun toUnit(duration: Duration): Long = duration.inWholeSeconds
            override fun toDurationUnit(durationUnit: DurationUnit): DurationUnit =
                Second(durationUnit.duration, durationUnit.textFormat)
        }

        class Minute(duration: Duration, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_min, 2, textFormat) {
            override fun toUnit(duration: Duration): Long = duration.inWholeMinutes
            override fun toDurationUnit(durationUnit: DurationUnit): DurationUnit =
                Minute(durationUnit.duration, durationUnit.textFormat)
        }

        class Hour(duration: Duration, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_hr, 3, textFormat) {
            override fun toUnit(duration: Duration): Long = duration.inWholeHours
            override fun toDurationUnit(durationUnit: DurationUnit): DurationUnit =
                Hour(durationUnit.duration, durationUnit.textFormat)
        }

        class Day(duration: Duration, textFormat: ((String) -> String)? = null) :
            DurationUnit(duration, R.string.duration_day, 4, textFormat) {
            override fun toUnit(duration: Duration): Long = duration.inWholeDays
            override fun toDurationUnit(durationUnit: DurationUnit): DurationUnit =
                Day(durationUnit.duration, durationUnit.textFormat)
        }

        /**
         * Convert a duration to this unit
         */
        abstract fun toUnit(duration: Duration): Long

        /**
         * Convert a [DurationUnit] to this [DurationUnit]
         */
        abstract fun toDurationUnit(durationUnit: DurationUnit): DurationUnit

        /**
         * Get the string-formatted duration value
         */
        fun value() = toUnit(this.duration)
            // If the rounded value is 0, make it 1
            .let {
                if (it == 0L) {
                    if (this.duration.isNegative()) "-1" else "1"
                } else it.toString()
            }
            // Replace negative sign
            .let {
                if (this.duration.isNegative()) it.replace("-", "−")
                else it
            }
            // Apply the text format (if any)
            .let {
                textFormat?.let { f -> f(it) } ?: it
            }

        /**
         * Compare two [DurationUnit]s based on their ordinal
         */
        override fun compareTo(other: DurationUnit): Int =
            ordinal.compareTo(other.ordinal)
    }

    /**
     * Get the time difference in the most-appropriate duration unit
     */
    fun scaleDuration(): DurationUnit {
        return when (abs(duration.inWholeSeconds)) {
//            in 0..59 -> DurationUnit.Second(duration, textFormat)
            in 0..3599 -> DurationUnit.Minute(duration, textFormat)
            in 3600..86399 -> DurationUnit.Hour(duration, textFormat)
            else -> DurationUnit.Day(duration, textFormat)
        }
    }
}
