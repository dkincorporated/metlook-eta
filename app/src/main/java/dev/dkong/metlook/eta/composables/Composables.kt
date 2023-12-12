package dev.dkong.metlook.eta.composables

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.activities.StopActivity
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.objects.ptv.Stop
import kotlinx.serialization.encodeToString

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
fun StopCard(stop: Stop, shape: Shape, context: Context, modifier: Modifier = Modifier) {
    val stopName = stop.stopName()

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
                // Open the Stop
                // TODO: Pass Stop information to new Activity
                val stopIntent = Intent(context, StopActivity::class.java)
                stopIntent.putExtra("stop", Constants.jsonFormat.encodeToString(stop))
                context.startActivity(stopIntent)
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
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewStopCard() {
    val stops = Constants.jsonFormat.decodeFromString<List<Stop>>(sampleStops)

    if (stops.isNotEmpty()) {
        StopCard(
            stops[1],
            ListPosition.FirstAndLast.roundedShape,
            LocalContext.current
        )
    }
}

val sampleStops = """
[
    {
    
          "stop_distance": 0.0,
    
          "stop_suburb": "Box Hill",
    
          "route_type": 2,
    
          "routes": [
    
            {
    
              "route_name": "Mitcham - Box Hill via Brentford Square & Forest Hill & Blackburn",
    
              "route_number": "765",
    
              "route_type": 2,
    
              "route_id": 964,
    
              "route_gtfs_id": "4-765",
    
              "route_service_status": {
    
                "description": "Good Service",
    
                "timestamp": "2023-12-11T00:05:20.9437569+00:00"
    
              }
    
            }
    
          ],
    
          "stop_latitude": -37.8245049,
    
          "stop_longitude": 145.127319,
    
          "stop_sequence": 0,
    
          "stop_id": 12586,
    
          "stop_name": "Albion Rd/William St ",
    
          "stop_landmark": ""
    
        },
    
    {
    
      "stop_distance": 0.0,
    
      "stop_suburb": "Box Hill",
    
      "route_type": 1,
    
      "routes": [
    
        {
    
          "route_name": "Box Hill - Port Melbourne",
    
          "route_number": "109",
    
          "route_type": 1,
    
          "route_id": 722,
    
          "route_gtfs_id": "3-109",
    
          "route_service_status": {
    
            "description": "Good Service",
    
            "timestamp": "2023-12-11T00:05:20.9437569+00:00"
    
          }
    
        }
    
      ],
    
      "stop_latitude": -37.81788,
    
      "stop_longitude": 145.122345,
    
      "stop_sequence": 0,
    
      "stop_id": 2409,
    
      "stop_name": "Box Hill Central/Whitehorse Rd #58 ",
    
      "stop_landmark": "Box Hill Central"
    
    },
    
    {
    
    "stop_distance": 0.0,
    
    "stop_suburb": "Box Hill",
    
    "route_type": 0,
    
    "routes": [
    
    {
    
      "route_name": "Belgrave",
    
      "route_number": "",
    
      "route_type": 0,
    
      "route_id": 2,
    
      "route_gtfs_id": "2-BEL",
    
      "route_service_status": {
    
        "description": "Good Service",
    
        "timestamp": "2023-12-11T00:05:20.9437569+00:00"
    
      }
    
    },
    
    {
    
      "route_name": "Lilydale",
    
      "route_number": "",
    
      "route_type": 0,
    
      "route_id": 9,
    
      "route_gtfs_id": "2-LIL",
    
      "route_service_status": {
    
        "description": "Good Service",
    
        "timestamp": "2023-12-11T00:05:20.9437569+00:00"
    
      }
    
    }
    
    ],
    
    "stop_latitude": -37.8192177,
    
    "stop_longitude": 145.121429,
    
    "stop_sequence": 0,
    
    "stop_id": 1026,
    
    "stop_name": "Box Hill Station",
    
    "stop_landmark": ""
    
    }

]
""".trimIndent()
