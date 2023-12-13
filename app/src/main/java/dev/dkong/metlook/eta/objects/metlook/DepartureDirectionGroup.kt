package dev.dkong.metlook.eta.objects.metlook

import androidx.core.text.isDigitsOnly

/**
 * Record for a group of departures on the Direction level
 */
data class DepartureDirectionGroup(
    val groupingId: Int,
    val routeNumber: String?,
    val name: String
) : Comparable<DepartureDirectionGroup> {
    /**
     * Comparison function
     */
    override fun compareTo(other: DepartureDirectionGroup): Int = compareValuesBy(
        this,
        other
    ) { group ->
        if (group.routeNumber?.isDigitsOnly() == true) {
            group.routeNumber.toInt()
        } else {
            group.groupingId
        }
    }

}
