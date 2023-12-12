package dev.dkong.metlook.eta.screens.home

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.activities.SearchActivity
import dev.dkong.metlook.eta.composables.SectionHeading

@Composable
fun NavigationHomeScreen(navHostController: NavHostController) {
    val context = LocalContext.current

    Box {
        LazyColumn {
            item {
                SectionHeading(heading = "Train")
            }
        }
        LargeFloatingActionButton(
            onClick = {
                context.startActivity(Intent(context, SearchActivity::class.java))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search for stops and stations",
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
            )
        }
    }
}