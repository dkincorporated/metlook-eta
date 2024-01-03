package dev.dkong.metlook.eta.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.utils.ResourceUtils

/**
 * The base modifier for the size of the MetLabel
 *
 * Add on top of this for specific implementations of MetLabel.
 */
val metLabelModifier = Modifier
    .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)

/**
 * Base MetLabel module
 * @param modifier the modifier for the box
 * @param backgroundColor the colour of the label's background
 * @param boxContent the content to be housed in the label
 */
@Composable
internal fun MetLabel(
    modifier: Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    boxContent: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor),
        content = boxContent
    )
}

/**
 * Module for an icon-based MetLabel
 * @param icon the icon to display
 * @param backgroundColor the background colour of the label (default is primary)
 * @param foregroundColor the foreground color of the label (default is onPrimary)
 * @param modifier a modifier for the base label (default is circle)
 */
@Composable
fun IconMetLabel(
    @DrawableRes icon: Int,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    foregroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = metLabelModifier.clip(CircleShape)
) {
    MetLabel(
        modifier = modifier,
        backgroundColor = backgroundColor
    ) {
        Image(
            painter = painterResource(
                id = icon
            ),
            contentDescription = null,
            colorFilter = ColorFilter.tint(foregroundColor),
            modifier = Modifier
                .size(36.dp)
                .padding(4.dp)
        )
    }
}

/**
 * Module for a text-based MetLabel
 * @param text the text to be displayed in the label
 * @param backgroundColor the background colour of the label (default is primary)
 * @param foregroundColor the foreground color of the label (default is onPrimary)
 * @param modifier a modifier for the base label (default is slightly rounded box)
 */
@Composable
fun TextMetLabel(
    text: String, // TODO: use stringResource for prod
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    foregroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = metLabelModifier.clip(RoundedCornerShape(4.dp))
) {
    MetLabel(
        modifier = modifier,
        backgroundColor = backgroundColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = foregroundColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 2.dp)
        )
    }
}

@Preview
@Composable
fun PreviewMetLabel() {
    TextMetLabel(text = "8")
}

/**
 * Generic icon for route type only
 * @param routeType the [RouteType] of the icon
 */
@Composable
fun RouteTypeIcon(
    routeType: RouteType?
) {
    IconMetLabel(
        icon = ResourceUtils.getRouteTypeIcon(routeType),
        backgroundColor = ResourceUtils.getRouteColour(routeType),
        foregroundColor = Color.White
    )
}

/**
 * Train departure implementation of MetLabel (with platform no.)
 * @param text the platform no.
 * @param routeId the Route ID of the service
 * @param flags the flags of the service (if any)
 */
@Composable
fun PlatformDepartureLabel(
    text: String? = null,
    routeId: Int? = null,
    flags: String? = null
) {
    // Get the colours of the label/icon
    val routeBackgroundColor = ResourceUtils.getRouteColour(RouteType.Train, routeId)
    val routeForegroundColor = ResourceUtils.getRouteForegroundColour(RouteType.Train, routeId)
    // Check whether the platform no. should be overriden
    if (flags?.contains("RRB") == true) {
        // Rail-replacement bus service
        IconMetLabel(
            icon = R.drawable.baseline_alt_route_24,
            backgroundColor = routeBackgroundColor,
            foregroundColor = routeForegroundColor
        )
        return
    }
    // Normal label -- proceed
    text?.let { displayText ->
        // Display a text-based MetLabel with a circle mask
        TextMetLabel(
            text = displayText,
            backgroundColor = routeBackgroundColor,
            foregroundColor = routeForegroundColor,
            modifier = metLabelModifier.clip(CircleShape)
        )
    }
}

/**
 * Road transport departure implementation of MetLabel (with route number)
 * @param text the route no.
 * @param routeType the [RouteType] of the departure
 * @param routeId the Route ID of the departure (used for background colouring)
 */
@Composable
fun RouteNumberDepartureLabel(
    text: String?,
    routeType: RouteType?,
    routeId: Int? = null
) {
    text?.let { displayText ->
        TextMetLabel(
            text = displayText,
            backgroundColor = ResourceUtils.getRouteColour(routeType, routeId),
            foregroundColor = ResourceUtils.getRouteForegroundColour(routeType, routeId)
        )
    }
}

