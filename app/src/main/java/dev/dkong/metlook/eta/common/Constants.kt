package dev.dkong.metlook.eta.common

import android.content.Context
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Constants maintained throughout the app
 * @author David Kong
 */
object Constants {
    // DataStore
    /**
     * DataStore for Recent items
     */
    val Context.dataStoreRecents: DataStore<Preferences>
            by preferencesDataStore(name = "recents")

    /**
     * DataStore for app settings
     */
    val Context.dataStoreSettings: DataStore<Preferences>
            by preferencesDataStore(name = "settings")

    // Transition between screens
    const val transitionOffsetProportion = 10
    val transitionAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(300)

    // PTV API
    /**
     * Date Format for date-times from PTV API
     */
    val dateTimeFormat = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Get the base surface colour for background-type surfaces
     * @return background surface colour
     */
    @Composable
    fun appSurfaceColour() = MaterialTheme.colorScheme.surfaceContainer

    /**
     * Get the container colour for Material List Card
     * @return Material List Card container colour
     */
    @Composable
    fun materialListCardContainerColour() =
        MaterialTheme.colorScheme.surfaceContainerLow

    /**
     * Get the container colour of the top app bar when scrolled
     * @return top app bar scrolled container colour
     */
    @Composable
    fun scrolledAppbarContainerColour() =
        MaterialTheme.colorScheme.surfaceContainerHighest

    // JSON
    /**
     * Format for parsing JSON using Kotlinx Serialization
     */
    @OptIn(ExperimentalSerializationApi::class)
    val jsonFormat = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    /**
     * Client for ktor
     */
    val httpClient = HttpClient(Android)

    // Corner radii
    /**
     * Size for the large rounded corner radius
     */
    val largeListCornerRadius = 16.dp

    /**
     * Size for the small rounded corner radius
     */
    val smallListCornerRadius = 4.dp

    /**
     * Refresh interval for all automatically refresh screens, in milliseconds
     */
    val refreshInterval = 15000L

    // Date Time
    private fun Instant.toLocal(): LocalDateTime =
        this.toLocalDateTime(TimeZone.currentSystemDefault())

    /**
     * Convert an [Instant] to a date + time display string
     */
    fun Instant.toDateTimeString(): String =
        with(this.toLocal()) {
            "$hour:${minute.toString().padStart(2, '0')} $dayOfMonth/$monthNumber/$year"
        }

    /**
     * Convert an [Instant] to a date-only display string
     */
    fun Instant.toDateString(): String =
        with(this.toLocal()) {
            "$dayOfMonth/$monthNumber/$year"
        }

    /**
     * Convert an [Instant] to a time-only 24-hour-time display string
     */
    fun Instant.toTimeString(): String =
        with(this.toLocal()) {
            "$hour:${minute.toString().padStart(2, '0')}"
        }

    /**
     * Format for displaying time of DateTime objects
     */
    val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
}
