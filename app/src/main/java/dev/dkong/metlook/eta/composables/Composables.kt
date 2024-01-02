package dev.dkong.metlook.eta.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils.allSame
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import dev.dkong.metlook.eta.objects.ptv.Stop

/**
 * Spacing to place at the end of a full-height list
 */
@Composable
fun NavBarPadding() {
    Box(modifier = Modifier.navigationBarsPadding())
}

/**
 * Card for displaying a Stop
 * @see Stop
 */
@Composable
fun StopCard(stop: Stop, shape: Shape, onClick: (Stop) -> Unit, modifier: Modifier = Modifier) {
    val stopName = stop.stopName

    ListItem(
        headlineContent = {
            Text(
                text = stopName.first,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text =
                if (stopName.second != null) "on ${stopName.second.toString()}"
                else "in ${stop.stopSuburb}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            if (stopName.third == null) {
                // Route type icon
                IconMetLabel(stop.routeType.icon)
            } else {
                stopName.third?.let { stopNumber ->
                    // Stop number
                    TextMetLabel(text = stopNumber)
                }
            }
        },
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(stop)
            }
    )
}

/**
 * Card for a Departure service
 * @see ServiceDeparture
 */
@Composable
fun DepartureCard(
    departureList: List<ServiceDeparture>,
    shape: Shape,
    onClick: (List<ServiceDeparture>) -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * The first departure of the group
     */
    val departure = departureList.first()

    ListItem(
        headlineContent = {
            val serviceTitle =
                if (departureList.size == 1)
                    "${departure.scheduledDepartureTime} ${departure.serviceTitle}"
                else
                    departure.serviceTitle

            Text(
                text = serviceTitle,
                style = MaterialTheme.typography.titleLarge,
                color =
                if (departure.isCancelled) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            val serviceSubtitles = departureList
                .map { departure ->
                    if (departure.isCancelled) "Not running today"
                    else if (departure.routeType == RouteType.Train)
                        stringResource(id = departure.patternType.displayName)
                    else "To ${departure.destinationName}"
                }

            Text(
                text =
                // If all the subtitles are the same, just show the first one
                if (serviceSubtitles.allSame()) serviceSubtitles.first()
                else serviceSubtitles.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            if (departure.routeType == RouteType.Train && departure.platform != null) {
                TextMetLabel(
                    text = departure.platform, modifier = metLabelModifier.clip(
                        CircleShape
                    )
                )
            } else if (departure.route.routeNumber != null) {
                TextMetLabel(text = departure.route.routeNumber)
            }
        },
        trailingContent = {
            if (departure.isCancelled) return@ListItem

            val timeTexts = departureList.map { departure ->
                if (departure.isAtPlatform) "Now" // now arrived
                else if (departure.isArriving) "Now*" // now arriving
                else if (departure.estimatedDeparture != null
                    && departure.timeToEstimatedDeparture?.inWholeMinutes?.let { it < 1 } == true
                ) "<1"
                else if (departure.estimatedDeparture != null)
                    "${departure.timeToEstimatedDeparture?.inWholeMinutes}"
                else "${departure.timeToScheduledDeparture.inWholeMinutes}*"
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeTexts.joinToString(" • "),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // Display the 'min' indicator if at least one departure is not at platform or arriving
                if (departureList.any { d -> !d.isAtPlatform && !d.isArriving }) {
                    Text(
                        text = "min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (departure.isCancelled) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface,
            headlineColor = if (departure.isCancelled) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .clip(shape)
            // TODO: Add check for cancelled service (from flag)
            .clickable {
                onClick(departureList)
            }
    )
}

/**
 * Card to display a recent service, similar to [DepartureCard]
 * @param service the service to be displayed
 */
@Composable
fun RecentServiceCard(
    service: ServiceDeparture,
    shape: Shape,
    onClick: (ServiceDeparture) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = "${service.scheduledDepartureTime} ${service.serviceTitle}",
                style = MaterialTheme.typography.titleLarge,
                color =
                if (service.isCancelled) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = "From " + service.departureStop.stopName
                    .let { if (it.second != null) "${it.first} / ${it.second}" else it.first },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            if (service.routeType == RouteType.Train && service.platform != null) {
                TextMetLabel(
                    text = service.platform, modifier = metLabelModifier.clip(
                        CircleShape
                    )
                )
            } else if (service.route.routeNumber != null) {
                TextMetLabel(text = service.route.routeNumber)
            }
        },
        trailingContent = {
            if (service.isCancelled) return@ListItem

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    // Display min to scheduled departure for now
                    text = "${
                        service.timeToScheduledDeparture
                            .inWholeMinutes.toString()
                            .replace("-", "−")
                    }*",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "min",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (service.isCancelled) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface,
            headlineColor = if (service.isCancelled) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .clip(shape)
            .clickable {
                onClick(service)
            }
    )
}

/**
 * Full-width placeholder message
 * @param largeIcon icon to be displayed
 * @param title the title to be displayed
 * @param subtitle the subtitle to be displayed
 */
@Composable
fun PlaceholderMessage(
    @DrawableRes largeIcon: Int? = null,
    title: String,
    subtitle: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            largeIcon?.let {
                Image(
                    painterResource(id = it),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewStopCard() {
}
