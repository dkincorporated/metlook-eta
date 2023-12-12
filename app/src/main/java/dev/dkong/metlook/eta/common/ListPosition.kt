package dev.dkong.metlook.eta.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import dev.dkong.metlook.eta.common.Constants.largeListCornerRadius
import dev.dkong.metlook.eta.common.Constants.smallListCornerRadius

/**
 * Record class for specifying the position of an item in a list (mainly for rounding corners)
 */
enum class ListPosition(val roundedShape: RoundedCornerShape) {
    First(
        RoundedCornerShape(
            largeListCornerRadius,
            largeListCornerRadius,
            smallListCornerRadius,
            smallListCornerRadius
        )
    ),
    InBetween(RoundedCornerShape(smallListCornerRadius)),
    Last(
        RoundedCornerShape(
            smallListCornerRadius,
            smallListCornerRadius,
            largeListCornerRadius,
            largeListCornerRadius
        )
    ),
    FirstAndLast(RoundedCornerShape(largeListCornerRadius));

    companion object {
        /**
         * Get the position based on the index
         * @param position the index of the item in the list
         * @param count the total number of items in the list
         */
        fun fromPosition(position: Int, count: Int): ListPosition {
            if (count == 1) return FirstAndLast
            if (position == 0) return First
            if (position == count - 1) return Last
            return InBetween
        }
    }
}