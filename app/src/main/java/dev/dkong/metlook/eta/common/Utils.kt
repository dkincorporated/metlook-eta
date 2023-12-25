package dev.dkong.metlook.eta.common

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
}