package dev.dkong.metlook.eta.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.SectionHeading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesHomeScreen(navHostController: NavHostController) {
    var selectedPage by remember { mutableIntStateOf(0) }
    val pages = arrayOf("Train", "Tram", "Bus")

    LazyColumn {
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                pages.forEachIndexed { index, page ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.shape(position = index, count = pages.size),
                        onClick = {
                            selectedPage = index
                        },
                        selected = selectedPage == index,
                        icon = {}
                    ) {
                        Text(text = page, maxLines = 1)
                    }
                }
            }
        }
        item {
            SectionHeading(heading = pages.get(selectedPage))
        }
    }
}