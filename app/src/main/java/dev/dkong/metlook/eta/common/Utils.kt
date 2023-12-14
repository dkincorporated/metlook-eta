package dev.dkong.metlook.eta.common

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
            val observer = LifecycleEventObserver { source, event ->
                eventHandler.value(event)
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }
    }
}