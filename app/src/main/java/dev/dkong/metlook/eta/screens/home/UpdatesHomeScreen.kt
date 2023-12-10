package dev.dkong.metlook.eta.screens.home

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants.Companion.httpClient
import dev.dkong.metlook.eta.common.Constants.Companion.jsonFormat
import dev.dkong.metlook.eta.common.ListPosition
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.objects.ptv.Disruption
import dev.dkong.metlook.eta.objects.ptv.Disruptions
import dev.dkong.metlook.eta.objects.ptv.DisruptionsResult
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Updates page for the home screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpdatesHomeScreen(navHostController: NavHostController) {
    val context = LocalContext.current

    var selectedPage by remember { mutableIntStateOf(0) }
    val pages = arrayOf(RouteType.Train, RouteType.Tram, RouteType.Bus)

    // Get all disruptions
    var allDisruptions: Disruptions? = null
    val disruptions = remember { mutableStateMapOf<Int, List<Disruption>>() }

    fun updateView(routeType: RouteType) {
        disruptions.clear()
        allDisruptions?.let {
            val filteredDisruptions = it.filterDisruptions(routeType)
            val groupedDisruptions = filteredDisruptions
                .groupBy { d -> d.typePriority }
                .toList()
                .sortedBy { p -> p.first }
            disruptions.putAll(groupedDisruptions)
        }
    }

    LaunchedEffect(Unit) {
        // Fetch disruptions (async)
        allDisruptions = getDisruptions()
        updateView(pages[selectedPage])
    }

    LazyColumn {
        stickyHeader {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                pages.forEachIndexed { index, page ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = pages.size
                        ),
                        onClick = {
                            selectedPage = index
                            updateView(pages[index])
                        },
                        selected = selectedPage == index,
                        icon = {}
                    ) {
                        Text(text = page.name, maxLines = 1)
                    }
                }
            }
        }
        disruptions.forEach { type ->
            item {
                SectionHeading(heading = type.value[0].disruptionType)
            }
            type.value.forEachIndexed { index, disruption ->
                item {
                    DisruptionCard(
                        disruption = disruption,
                        shape = ListPosition.fromPosition(index, type.value.size).roundedShape,
                        context = context
                    )
                }
            }
        }
    }
}

/**
 * Get the latest disruptions
 * @return list of disruptions
 */
suspend fun getDisruptions(): Disruptions? {
    val request = PtvApi.getApiUrl(
        "/v3/disruptions?route_types=0&route_types=1&route_types=2&disruption_status=current&"
    )

    request?.let {
        val response: String = httpClient.get(request).body()
        return jsonFormat.decodeFromString<DisruptionsResult>(response).disruptions
    }

    return null
}

/**
 * Individual disruption card
 * @param disruption the Disruption to display
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DisruptionCard(disruption: Disruption, shape: RoundedCornerShape, context: Context) {
    var showDetail by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                text = disruption.title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                disruption.routes.forEach { route ->
                    AssistChip(
                        onClick = {},
                        label = { Text(if (route.routeType == RouteType.Train) route.routeName else route.routeNumber) }
                    )
                }
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 1.dp)
            .clip(shape)
            .clickable {
                // Open the Disruption page
                showDetail = true
            }
    )

    // Modal bottom sheet
    if (showDetail) {
        ModalBottomSheet(onDismissRequest = { showDetail = false }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = disruption.disruptionType,
                    style = MaterialTheme.typography.headlineLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    disruption.routes.forEach { route ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    if (route.routeType == RouteType.Train) route.routeName
                                    else route.routeNumber
                                )
                            },
                            leadingIcon = {
                                Image(
                                    painterResource(id = route.routeType.icon),
                                    route.routeType.displayName,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }
                        )
                    }
                }
                Text(
                    text = disruption.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                if (disruption.description != disruption.title) {
                    // Sometimes, they are the same, which would be redundant to show twice
                    Text(
                        text = disruption.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        append("Status: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(disruption.disruptionStatus)
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (disruption.url != "http://ptv.vic.gov.au/live-travel-updates/") {
                    Button(
                        onClick = {
                            // Build the Custom Tab
                            val customTabsIntent = CustomTabsIntent
                                .Builder()
                                .setUrlBarHidingEnabled(true)
                                .build()

                            // Launch the built Intent
                            customTabsIntent.launchUrl(context, Uri.parse(disruption.url))

                            // Dismiss the bottom sheet
                            showDetail = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("More info")
                    }
                }
            }
        }
    }
}

/**
 * Preview
 */
@Preview
@Composable
fun PreviewDisruptionCard() {
    val parsedDisruptions =
        jsonFormat.decodeFromString<DisruptionsResult>(sampleDisruptions).disruptions
    DisruptionCard(
        disruption = parsedDisruptions.metroTrain[0],
        shape = ListPosition.FirstAndLast.roundedShape,
        context = LocalContext.current
    )
}

val sampleDisruptions = """
{

  "disruptions": {

    "general": [],

    "metro_train": [
      {

        "disruption_id": 302274,

        "title": "Belgrave Line: partial car park opening on Saturday 9 December 2023",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/belgrave-line-partial-car-park-opening-on-saturday-9-december-2023",

        "description": "As part of Victoria’s Car Parks for Commuters Program, we have built a new multi-level car park at Belgrave Station.From Saturday 9 December, the following is open on the ground level of the car park:",

        "disruption_status": "Current",

        "disruption_type": "Planned Works",

        "published_on": "2023-12-09T08:38:07Z",

        "last_updated": "2023-12-09T08:52:32Z",

        "from_date": "2023-12-09T08:31:00Z",

        "to_date": null,

        "routes": [

          {

            "route_type": 0,

            "route_id": 2,

            "route_name": "Belgrave",

            "route_number": "",

            "route_gtfs_id": "2-BEL",

            "direction": null

          }

        ],

        "stops": [],

        "colour": "#ffd500",

        "display_on_board": true,

        "display_status": false

      },

      {

        "disruption_id": 301818,

        "title": "Hurstbridge and Mernda lines: No City Loop trains from 9pm Friday 15 December to last service Sunday 17 December 2023",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/hurstbridge-and-mernda-lines-no-city-loop-trains-from-9pm-friday-15-december-to-last-service-sunday-17-december-2023",

        "description": "Hurstbridge and Mernda line trains will run direct to and from Flinders Street, not via the City Loop from 9pm Friday 15 December to last service Sunday 17 December, due to works.",

        "disruption_status": "Current",

        "disruption_type": "Planned Works",

        "published_on": "2023-12-04T01:06:20Z",

        "last_updated": "2023-12-04T01:06:21Z",

        "from_date": "2023-12-15T10:00:00Z",

        "to_date": "2023-12-17T16:00:00Z",

        "routes": [

          {

            "route_type": 0,

            "route_id": 8,

            "route_name": "Hurstbridge",

            "route_number": "",

            "route_gtfs_id": "2-HBG",

            "direction": null

          },

          {

            "route_type": 0,

            "route_id": 5,

            "route_name": "Mernda",

            "route_number": "",

            "route_gtfs_id": "2-MER",

            "direction": null

          }

        ],

        "stops": [

          {

            "stop_id": 1100,

            "stop_name": "Hurstbridge "

          },

          {

            "stop_id": 1204,

            "stop_name": "Wattle Glen "

          },

          {

            "stop_id": 1054,

            "stop_name": "Diamond Creek "

          },

          {

            "stop_id": 1062,

            "stop_name": "Eltham "

          },

          {

            "stop_id": 1130,

            "stop_name": "Montmorency "

          },

          {

            "stop_id": 1084,

            "stop_name": "Greensborough "

          },

          {

            "stop_id": 1203,

            "stop_name": "Watsonia "

          },

          {

            "stop_id": 1117,

            "stop_name": "Macleod "

          },

          {

            "stop_id": 1168,

            "stop_name": "Rosanna "

          },

          {

            "stop_id": 1093,

            "stop_name": "Heidelberg "

          },

          {

            "stop_id": 1056,

            "stop_name": "Eaglemont "

          },

          {

            "stop_id": 1101,

            "stop_name": "Ivanhoe "

          },

          {

            "stop_id": 1050,

            "stop_name": "Darebin "

          },

          {

            "stop_id": 1004,

            "stop_name": "Alphington "

          },

          {

            "stop_id": 1065,

            "stop_name": "Fairfield "

          },

          {

            "stop_id": 1053,

            "stop_name": "Dennis "

          },

          {

            "stop_id": 1209,

            "stop_name": "Westgarth "

          },

          {

            "stop_id": 1041,

            "stop_name": "Clifton Hill "

          },

          {

            "stop_id": 1201,

            "stop_name": "Victoria Park "

          },

          {

            "stop_id": 1043,

            "stop_name": "Collingwood "

          },

          {

            "stop_id": 1145,

            "stop_name": "North Richmond "

          },

          {

            "stop_id": 1207,

            "stop_name": "West Richmond "

          },

          {

            "stop_id": 1104,

            "stop_name": "Jolimont-MCG "

          },

          {

            "stop_id": 1071,

            "stop_name": "Flinders Street "

          },

          {

            "stop_id": 1155,

            "stop_name": "Parliament "

          },

          {

            "stop_id": 1120,

            "stop_name": "Melbourne Central "

          },

          {

            "stop_id": 1068,

            "stop_name": "Flagstaff "

          },

          {

            "stop_id": 1181,

            "stop_name": "Southern Cross "

          },

          {

            "stop_id": 1228,

            "stop_name": "Mernda "

          },

          {

            "stop_id": 1227,

            "stop_name": "Hawkstowe "

          },

          {

            "stop_id": 1226,

            "stop_name": "Middle Gorge "

          },

          {

            "stop_id": 1224,

            "stop_name": "South Morang "

          },

          {

            "stop_id": 1063,

            "stop_name": "Epping "

          },

          {

            "stop_id": 1112,

            "stop_name": "Lalor "

          },

          {

            "stop_id": 1192,

            "stop_name": "Thomastown "

          },

          {

            "stop_id": 1109,

            "stop_name": "Keon Park "

          },

          {

            "stop_id": 1171,

            "stop_name": "Ruthven "

          },

          {

            "stop_id": 1161,

            "stop_name": "Reservoir "

          },

          {

            "stop_id": 1160,

            "stop_name": "Regent "

          },

          {

            "stop_id": 1159,

            "stop_name": "Preston "

          },

          {

            "stop_id": 1019,

            "stop_name": "Bell "

          },

          {

            "stop_id": 1193,

            "stop_name": "Thornbury "

          },

          {

            "stop_id": 1047,

            "stop_name": "Croxton "

          },

          {

            "stop_id": 1147,

            "stop_name": "Northcote "

          },

          {

            "stop_id": 1125,

            "stop_name": "Merri "

          },

          {

            "stop_id": 1170,

            "stop_name": "Rushall "

          }

        ],

        "colour": "#ffd500",

        "display_on_board": false,

        "display_status": false

      },

      {

        "disruption_id": 301008,

        "title": "Belgrave and Lilydale lines: Buses replace trains on select sections from late January 2024",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/belgrave-and-lilydale-lines-buses-replace-trains-on-select-sections-from-late-january-2024",

        "description": "From late January 2024, buses will replace trains on sections of the Belgrave and Lilydale lines, as works ramp up on the Bedford Road, Ringwood level crossing removal project.",

        "disruption_status": "Current",

        "disruption_type": "Planned Works",

        "published_on": "2023-11-23T05:15:44Z",

        "last_updated": "2023-11-23T05:15:46Z",

        "from_date": "2023-11-29T16:00:00Z",

        "to_date": null,

        "routes": [

          {

            "route_type": 0,

            "route_id": 2,

            "route_name": "Belgrave",

            "route_number": "",

            "route_gtfs_id": "2-BEL",

            "direction": null

          },

          {

            "route_type": 0,

            "route_id": 9,

            "route_name": "Lilydale",

            "route_number": "",

            "route_gtfs_id": "2-LIL",

            "direction": null

          }

        ],

        "stops": [],

        "colour": "#ffd500",

        "display_on_board": false,

        "display_status": false

      },

      {

        "disruption_id": 300704,

        "title": "Merlynston Station: Car Park opening from Friday 17 November 2023",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/merlynston-station-car-park-opening-from-friday-17-november-2023",

        "description": "As part of Victorias Car Parks for Commuters program, we’ve upgraded the car park at Merlynston Station.",

        "disruption_status": "Current",

        "disruption_type": "Service Information",

        "published_on": "2023-11-17T20:20:08Z",

        "last_updated": "2023-11-17T20:24:55Z",

        "from_date": "2023-11-17T20:17:00Z",

        "to_date": null,

        "routes": [

          {

            "route_type": 0,

            "route_id": 15,

            "route_name": "Upfield",

            "route_number": "",

            "route_gtfs_id": "2-UFD",

            "direction": null

          }

        ],

        "stops": [

          {

            "stop_id": 1124,

            "stop_name": "Merlynston "

          }

        ],

        "colour": "#ffd500",

        "display_on_board": false,

        "display_status": false

      }
    ],

    "metro_tram": [
      {

        "disruption_id": 302096,

        "title": "CBD trams: Service changes due to rally from 11:30am to 5pm on Sunday 10 December 2023",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/cbd-trams-service-changes-due-to-rally-from-1130am-to-5pm-on-sunday-10-december-2023",

        "description": "Multiple CBD tram routes will be affected by service changes on Sunday 10 December, due to a rally in the City.",

        "disruption_status": "Current",

        "disruption_type": "Service Information",

        "published_on": "2023-12-07T03:46:53Z",

        "last_updated": "2023-12-10T03:30:22Z",

        "from_date": "2023-12-10T00:30:00Z",

        "to_date": "2023-12-10T06:00:00Z",

        "routes": [

          {

            "route_type": 1,

            "route_id": 721,

            "route_name": "East Coburg - South Melbourne Beach",

            "route_number": "1",

            "route_gtfs_id": "3-001",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1083,

            "route_name": "Melbourne University - Malvern",

            "route_number": "5",

            "route_gtfs_id": "3-005",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 11544,

            "route_name": "Moreland - Glen Iris",

            "route_number": "6",

            "route_gtfs_id": "3-006",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 724,

            "route_name": "Melbourne University - Kew via St Kilda Beach",

            "route_number": "16",

            "route_gtfs_id": "3-016",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 909,

            "route_name": "Melbourne University - East Brighton",

            "route_number": "64",

            "route_gtfs_id": "3-064",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 913,

            "route_name": "Melbourne University - Carnegie",

            "route_number": "67",

            "route_gtfs_id": "3-067",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 947,

            "route_name": "Melbourne University - Camberwell",

            "route_number": "72",

            "route_gtfs_id": "3-072",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 940,

            "route_name": "Waterfront City Docklands -  Wattle Park",

            "route_number": "70",

            "route_gtfs_id": "3-070",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 958,

            "route_name": "Vermont South - Central Pier Docklands",

            "route_number": "75",

            "route_gtfs_id": "3-075",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 3343,

            "route_name": "West Preston - Victoria Harbour Docklands",

            "route_number": "11",

            "route_gtfs_id": "3-011",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 2903,

            "route_name": "North Balwyn - Victoria Harbour Docklands",

            "route_number": "48",

            "route_gtfs_id": "3-048",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 8314,

            "route_name": "Victoria Gardens - St Kilda",

            "route_number": "12",

            "route_gtfs_id": "3-012",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1881,

            "route_name": "Bundoora RMIT - Waterfront City Docklands",

            "route_number": "86",

            "route_gtfs_id": "3-086",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1041,

            "route_name": "East Brunswick - St Kilda Beach",

            "route_number": "96",

            "route_gtfs_id": "3-096",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 722,

            "route_name": "Box Hill - Port Melbourne",

            "route_number": "109",

            "route_gtfs_id": "3-109",

            "direction": null

          }

        ],

        "stops": [],

        "colour": "#ffd500",

        "display_on_board": true,

        "display_status": true

      },

      {

        "disruption_id": 301835,

        "title": "Multiple tram routes affected on New Year's Eve: Service changes from 4pm Sunday 31 December 2023 to 3am Monday 1 January 2024",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/multiple-tram-routes-affected-on-new-years-eve-service-changes-from-4pm-sunday-31-december-2023-to-3am-monday-1-january-2024",

        "description": "Route 1, 3a, 5, 6, 11, 12, 16, 19, 30, 35, 48, 57, 58, 59, 64, 67, 70, 72, 75, 86, 96, and 109 trams will experience service changes from 4pm Sunday 31 December 2023 to 3am Monday 1 January 2024, due to New Year's Eve city celebrations and road closures.",

        "disruption_status": "Current",

        "disruption_type": "Service Information",

        "published_on": "2023-12-04T02:56:49Z",

        "last_updated": "2023-12-06T00:20:50Z",

        "from_date": "2023-12-31T05:00:00Z",

        "to_date": "2023-12-31T15:59:00Z",

        "routes": [

          {

            "route_type": 1,

            "route_id": 721,

            "route_name": "East Coburg - South Melbourne Beach",

            "route_number": "1",

            "route_gtfs_id": "3-001",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1083,

            "route_name": "Melbourne University - Malvern",

            "route_number": "5",

            "route_gtfs_id": "3-005",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 11544,

            "route_name": "Moreland - Glen Iris",

            "route_number": "6",

            "route_gtfs_id": "3-006",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 724,

            "route_name": "Melbourne University - Kew via St Kilda Beach",

            "route_number": "16",

            "route_gtfs_id": "3-016",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 909,

            "route_name": "Melbourne University - East Brighton",

            "route_number": "64",

            "route_gtfs_id": "3-064",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 913,

            "route_name": "Melbourne University - Carnegie",

            "route_number": "67",

            "route_gtfs_id": "3-067",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 947,

            "route_name": "Melbourne University - Camberwell",

            "route_number": "72",

            "route_gtfs_id": "3-072",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 722,

            "route_name": "Box Hill - Port Melbourne",

            "route_number": "109",

            "route_gtfs_id": "3-109",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1881,

            "route_name": "Bundoora RMIT - Waterfront City Docklands",

            "route_number": "86",

            "route_gtfs_id": "3-086",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 3343,

            "route_name": "West Preston - Victoria Harbour Docklands",

            "route_number": "11",

            "route_gtfs_id": "3-011",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 2903,

            "route_name": "North Balwyn - Victoria Harbour Docklands",

            "route_number": "48",

            "route_gtfs_id": "3-048",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 8314,

            "route_name": "Victoria Gardens - St Kilda",

            "route_number": "12",

            "route_gtfs_id": "3-012",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 940,

            "route_name": "Waterfront City Docklands -  Wattle Park",

            "route_number": "70",

            "route_gtfs_id": "3-070",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 958,

            "route_name": "Vermont South - Central Pier Docklands",

            "route_number": "75",

            "route_gtfs_id": "3-075",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 725,

            "route_name": "North Coburg - Flinders Street Station & City",

            "route_number": "19",

            "route_gtfs_id": "3-019",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 887,

            "route_name": "West Maribyrnong - Flinders Street Station & City",

            "route_number": "57",

            "route_gtfs_id": "3-057",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 897,

            "route_name": "Airport West - Flinders Street Station & City",

            "route_number": "59",

            "route_gtfs_id": "3-059",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1880,

            "route_name": "St Vincents Plaza - Central Pier Docklands via La Trobe St",

            "route_number": "30",

            "route_gtfs_id": "3-030",

            "direction": null

          },

          {

            "route_type": 1,

            "route_id": 1041,

            "route_name": "East Brunswick - St Kilda Beach",

            "route_number": "96",

            "route_gtfs_id": "3-096",

            "direction": null

          }

        ],

        "stops": [],

        "colour": "#ffd500",

        "display_on_board": false,

        "display_status": false

      },

      {

        "disruption_id": 299571,

        "title": "Route 11: Buses replace trams after 8pm on select nights from Sunday 3 December to Tuesday 5 December and Sunday 10 December to Tuesday 12 December 2023",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/route-11-buses-replace-trams-after-9pm-each-night-from-sunday-3-december-to-tuesday-5-december-and-from-sunday-10-december-to-tuesday-12-december-2023",

        "description": "Buses replace Route 11 trams between Stop 27 Westbourne Grove and West Preston after 8pm each night from Sunday 3 December to Tuesday 5 December and from Sunday 10 December to Tuesday 12 December 2023, due to overhead line works along St Georges Road.",

        "disruption_status": "Current",

        "disruption_type": "Planned Closure",

        "published_on": "2023-11-03T20:15:44Z",

        "last_updated": "2023-12-05T00:31:47Z",

        "from_date": "2023-12-03T10:00:00Z",

        "to_date": "2023-12-12T16:00:00Z",

        "routes": [

          {

            "route_type": 1,

            "route_id": 3343,

            "route_name": "West Preston - Victoria Harbour Docklands",

            "route_number": "11",

            "route_gtfs_id": "3-011",

            "direction": null

          }

        ],

        "stops": [

          {

            "stop_id": 2890,

            "stop_name": "D18-Bourke St/Collins St "

          },

          {

            "stop_id": 2490,

            "stop_name": "D17-Merchant St/Collins St "

          },

          {

            "stop_id": 2696,

            "stop_name": "D16-Harbour Esp/Collins St "

          },

          {

            "stop_id": 2489,

            "stop_name": "D15-Batman's Hill/Collins St "

          },

          {

            "stop_id": 2479,

            "stop_name": "D14-Southern Cross Station/Collins St "

          },

          {

            "stop_id": 2496,

            "stop_name": "Spencer St/Collins St #1 "

          },

          {

            "stop_id": 2494,

            "stop_name": "William St/Collins St #3 "

          },

          {

            "stop_id": 2492,

            "stop_name": "Elizabeth St/Collins St #5 "

          },

          {

            "stop_id": 2491,

            "stop_name": "Melbourne Town Hall/Collins St #6 "

          },

          {

            "stop_id": 2174,

            "stop_name": "Exhibition St/Collins St #7 "

          },

          {

            "stop_id": 2488,

            "stop_name": "Spring St/Collins St #8 "

          },

          {

            "stop_id": 2487,

            "stop_name": "Parliament Railway Station/Macarthur St #10 "

          },

          {

            "stop_id": 2485,

            "stop_name": "Albert St/Gisborne St #11 "

          },

          {

            "stop_id": 2868,

            "stop_name": "Spencer St/La Trobe St #119 "

          },

          {

            "stop_id": 2867,

            "stop_name": "King St/La Trobe St #2 "

          },

          {

            "stop_id": 2866,

            "stop_name": "William St/La Trobe St #3 "

          },

          {

            "stop_id": 2865,

            "stop_name": "Queen St/La Trobe St #4 "

          },

          {

            "stop_id": 2864,

            "stop_name": "Elizabeth St/La Trobe St #5 "

          },

          {

            "stop_id": 2863,

            "stop_name": "Swanston St/La Trobe St #6 "

          },

          {

            "stop_id": 2862,

            "stop_name": "Russell St/La Trobe St #7 "

          },

          {

            "stop_id": 2861,

            "stop_name": "Exhibition St/La Trobe St #8 "

          },

          {

            "stop_id": 2860,

            "stop_name": "Victoria St/La Trobe St #9 "

          },

          {

            "stop_id": 2858,

            "stop_name": "Nicholson St/Victoria Pde #10 "

          },

          {

            "stop_id": 2484,

            "stop_name": "St Vincents Plaza/Victoria Pde #12 "

          },

          {

            "stop_id": 2551,

            "stop_name": "Gertrude St/Brunswick St #13 "

          },

          {

            "stop_id": 2550,

            "stop_name": "Hanover St/Brunswick St #14 "

          },

          {

            "stop_id": 2548,

            "stop_name": "Bell St/Brunswick St #15 "

          },

          {

            "stop_id": 2546,

            "stop_name": "Johnston St/Brunswick St #16 "

          },

          {

            "stop_id": 2545,

            "stop_name": "Leicester St/Brunswick St #17 "

          },

          {

            "stop_id": 2544,

            "stop_name": "Alexandra Pde/Brunswick St #18 "

          },

          {

            "stop_id": 2543,

            "stop_name": "Newry St/Brunswick St #19 "

          },

          {

            "stop_id": 2541,

            "stop_name": "Fitzroy Bowls Club/Brunswick St #20 "

          },

          {

            "stop_id": 2539,

            "stop_name": "Alfred Cres/St Georges Rd #21 "

          },

          {

            "stop_id": 2538,

            "stop_name": "Scotchmer St/St Georges Rd #22 "

          },

          {

            "stop_id": 2536,

            "stop_name": "Park St/St Georges Rd #23 "

          },

          {

            "stop_id": 2535,

            "stop_name": "Holden St/St Georges Rd #24 "

          },

          {

            "stop_id": 2534,

            "stop_name": "Miller St/St Georges Rd #25 "

          },

          {

            "stop_id": 2533,

            "stop_name": "Clarke St/St Georges Rd #26 "

          },

          {

            "stop_id": 2532,

            "stop_name": "Westbourne Gr/St Georges Rd #27 "

          },

          {

            "stop_id": 2531,

            "stop_name": "Sumner Ave/St Georges Rd #28 "

          },

          {

            "stop_id": 2530,

            "stop_name": "Arthurton Rd/St Georges Rd #29 "

          },

          {

            "stop_id": 2529,

            "stop_name": "Gladstone Ave/St Georges Rd #30 "

          },

          {

            "stop_id": 2528,

            "stop_name": "Bird Ave/St Georges Rd #31 "

          },

          {

            "stop_id": 2526,

            "stop_name": "Normanby Ave/St Georges Rd #32 "

          },

          {

            "stop_id": 2525,

            "stop_name": "Hutton St/St Georges Rd #33 "

          },

          {

            "stop_id": 2524,

            "stop_name": "Miller St/St Georges Rd #34 "

          },

          {

            "stop_id": 2523,

            "stop_name": "Bracken Ave/Miller St #36 "

          },

          {

            "stop_id": 2521,

            "stop_name": "Miller St/Gilbert Rd #37 "

          },

          {

            "stop_id": 2520,

            "stop_name": "Oakover Rd/Gilbert Rd #38 "

          },

          {

            "stop_id": 2008,

            "stop_name": "Bell St/Plenty Rd #45 "

          },

          {

            "stop_id": 2519,

            "stop_name": "Latona Ave/Gilbert Rd #39 "

          },

          {

            "stop_id": 2518,

            "stop_name": "Bell St/Gilbert Rd #40 "

          },

          {

            "stop_id": 2517,

            "stop_name": "Bruce St/Gilbert Rd #41 "

          },

          {

            "stop_id": 2516,

            "stop_name": "Cramer St/Gilbert Rd #42 "

          },

          {

            "stop_id": 2515,

            "stop_name": "Murray Rd/Gilbert Rd #43 "

          },

          {

            "stop_id": 2514,

            "stop_name": "Cooper St/Gilbert Rd #44 "

          },

          {

            "stop_id": 2512,

            "stop_name": "Jacka St/Gilbert Rd #45 "

          },

          {

            "stop_id": 2513,

            "stop_name": "McNamara St/Gilbert Rd #46 "

          },

          {

            "stop_id": 2511,

            "stop_name": "West Preston/Gilbert Rd #47 "

          }

        ],

        "colour": "#ffd500",

        "display_on_board": false,

        "display_status": false

      }
    ],

    "metro_bus": [
      {

        "disruption_id": 302249,

        "title": "Routes 232, 235 and 237: Temporary bus stop closures from 8pm to last service on Wednesday 13 December 2023",

        "url": "http://ptv.vic.gov.au/live-travel-updates/article/routes-232-235-and-237-temporary-bus-stop-closures-from-8pm-to-last-service-on-wednesday-13-december-2023",

        "description": "Bus Routes 232, 235 and 237 (outbound) will not service selected bus stops from 8pm to last service on Wednesday 13 December 2023, due to a works.",

        "disruption_status": "Current",

        "disruption_type": "Planned Closure",

        "published_on": "2023-12-09T00:31:52Z",

        "last_updated": "2023-12-09T00:31:54Z",

        "from_date": "2023-12-13T09:00:00Z",

        "to_date": "2023-12-13T16:00:00Z",

        "routes": [

          {

            "route_type": 2,

            "route_id": 8122,

            "route_name": "Altona North - City (Queen Victoria Market)",

            "route_number": "232",

            "route_gtfs_id": "4-232",

            "direction": null

          },

          {

            "route_type": 2,

            "route_id": 15783,

            "route_name": "City (Southern Cross Station) - Fishermans Bend via Lorimer Street",

            "route_number": "237",

            "route_gtfs_id": "4-237",

            "direction": null

          }

        ],

        "stops": [

          {

            "stop_id": 33402,

            "stop_name": "Collins Square/Collins St "

          },

          {

            "stop_id": 17805,

            "stop_name": "Southern Cross Station/Collins St "

          }

        ],

        "colour": "#ffd500",

        "display_on_board": false,

        "display_status": false

      }
    ],

    "regional_train": [],

    "regional_coach": [],

    "regional_bus": [],

    "school_bus": [],

    "telebus": [],

    "night_bus": [],

    "ferry": [],

    "interstate_train": [],

    "skybus": [],

    "taxi": []

  },

  "status": {

    "version": "3.0",

    "health": 1

  }

}
""".trimIndent()