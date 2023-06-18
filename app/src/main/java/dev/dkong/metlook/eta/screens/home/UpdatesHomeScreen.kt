package dev.dkong.metlook.eta.screens.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.composables.SectionHeading

@Composable
fun UpdatesHomeScreen(navHostController: NavHostController) {
    LazyColumn {
        item {
            SectionHeading(heading = "Train")
        }
    }
}