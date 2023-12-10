package dev.dkong.metlook.eta.screens.home

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.common.Constants.Companion.httpClient
import dev.dkong.metlook.eta.common.Constants.Companion.jsonFormat
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.objects.ptv.Disruption
import dev.dkong.metlook.eta.objects.ptv.Disruptions
import dev.dkong.metlook.eta.objects.ptv.DisruptionsResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch

/**
 * Updates page for the home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesHomeScreen(navHostController: NavHostController) {
    val scope = rememberCoroutineScope()

    var selectedPage by remember { mutableIntStateOf(0) }
    val pages = arrayOf(RouteType.Train, RouteType.Tram, RouteType.Bus)

    // Get all disruptions
    var allDisruptions: Disruptions? = null
    val disruptions = remember { mutableStateListOf<Disruption>() }

    LaunchedEffect(Unit) {
        allDisruptions = getDisruptions()

        disruptions.clear()
        val updatedDisruptions = allDisruptions?.filterDisruptions(pages[selectedPage])
        updatedDisruptions?.let {
            disruptions.addAll(updatedDisruptions)
        }
    }

    LazyColumn {
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
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

                            disruptions.clear()
                            allDisruptions?.let {
                                disruptions.addAll(it.filterDisruptions(pages[index]))
                            }
                        },
                        selected = selectedPage == index,
                        icon = {}
                    ) {
                        Text(text = page.name, maxLines = 1)
                    }
                }
            }
        }
        item {
            SectionHeading(heading = pages[selectedPage].name)
        }

        disruptions.forEach { disruption ->
            item {
                DisruptionCard(disruption = disruption)
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
@Composable
fun DisruptionCard(disruption: Disruption) {
    ListItem(
        headlineContent = {
            Text(text = disruption.title)
        },
        supportingContent = {
            Text(text = disruption.disruptionType)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Preview
@Composable
fun PreviewDisruptionCard() {
    UpdatesHomeScreen(navHostController = rememberNavController())
}