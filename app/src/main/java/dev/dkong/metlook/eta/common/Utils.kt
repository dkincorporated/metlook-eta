package dev.dkong.metlook.eta.common

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.dkong.metlook.eta.objects.metlook.PatternType

/**
 * Universal utilities
 */
object Utils {
    /**
     * Event listener for Composables to observe lifecycle
     * @param onEventListener the listener for when a lifecycle event fires
     */
    @Composable
    fun ComposableEventListener(onEventListener: (event: Lifecycle.Event) -> Unit) {
        val eventHandler = rememberUpdatedState(newValue = onEventListener)
        val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)

        DisposableEffect(lifecycleOwner.value) {
            val lifecycle = lifecycleOwner.value.lifecycle
            val observer = LifecycleEventObserver { _, event ->
                eventHandler.value(event)
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }
    }

    /**
     * Check whether all the elements of a [List] are the same
     * @return true if all items in the list are the same, else false
     */
    fun <E> List<E>.allSame() = this.all { it == this.first() }

    /**
     * Finish the [Activity] surrounding the [Context]
     *
     * Does nothing if failed to find the [Activity], or there is no [Activity] to finish.
     */
    fun Context.finishActivity() = (this as? Activity)?.finish()

    // PT data
    /**
     * Get the discrete stopping pattern description/type (only for Train)
     */
    fun patternType(routeType: RouteType, routeId: Int, expressStopCount: Int): PatternType {
        if (routeType != RouteType.Train) return PatternType.NotApplicable
        if (expressStopCount == 0) {
            return PatternType.AllStops
        }
        if (expressStopCount == 1) {
            return PatternType.SkipsOneStop
        }
        when (routeId) {
            2, 9 -> {
                if (expressStopCount <= 5) {
                    return PatternType.LimitedStops
                }
            }

            1, 4, 11, 3, 6, 8 -> {
                if (expressStopCount <= 4) {
                    return PatternType.LimitedStops
                }
            }

            5, 14, 16 -> {
                if (expressStopCount <= 3) {
                    return PatternType.LimitedStops
                }
            }

            7 -> {
                if (expressStopCount <= 7) {
                    return PatternType.LimitedStops
                }
            }

            12, 13, 15, 17 -> {
                return PatternType.LimitedStops
            }

            1482 -> {
                if (expressStopCount <= 2) {
                    return PatternType.LimitedStops
                }
            }
        }
        return PatternType.SuperLimitedStops
    }
}