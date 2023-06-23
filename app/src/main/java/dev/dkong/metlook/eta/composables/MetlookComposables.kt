package dev.dkong.metlook.eta.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dkong.metlook.eta.common.RouteType

// Baseline Composables for metlook-specific elements
// Author: David Kong

/**
 * Baseline MetCard template
 * @param leadingContent the content at the start of the card (e.g. route icon/text)
 * @param primaryText the main large text to be displayed
 * @param secondaryText the secondary, smaller text to be displayed under the primary text
 * @param trailingContent the content at the end of the card (e.g. departure time, pinned status)
 */
@Composable
internal fun MetCardBase(
    leadingContent: @Composable (BoxScope.() -> Unit)? = null,
    primaryText: String,
    onClick: () -> Unit,
    secondaryText: String? = null,
    trailingContent: @Composable (BoxScope.() -> Unit)? = null,
    horizontalPadding: Dp = 0.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
    ) {
        leadingContent?.let {
            Box(content = it, modifier = Modifier.padding(end = horizontalPadding))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            secondaryText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailingContent?.let {
            Box(content = it)
        }
    }
}