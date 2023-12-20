package dev.dkong.metlook.eta.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.dkong.metlook.eta.R

/**
 * Composables for the stopping pattern
 */
object StoppingPatternComposables {
    /**
     * Thickness of the pattern indicator
     */
    private val indicatorWidth = 6.dp

    /**
     * Type of stop within the service pattern
     */
    enum class StopType {
        /**
         * Stop that the service calls at
         */
        Stop,

        /**
         * First stop of the service
         */
        First,

        /**
         * Last stop of the service
         */
        Last,

        /**
         * Stop that the service runs express past
         */
        Skipped,

        /**
         * Same as [Skipped], but an arrow is to be displayed
         */
        ArrowSkipped,

        /**
         * Stop is not within the service range
         */
        OutOfRange,

        /**
         * First displayed stop, with hidden stops **before**
         */
        ContinuesBefore,

        /**
         * Last displayed stop, with hidden stops **after**
         */
        ContinuesAfter
    }

    /**
     * Indicator tick beside each stop
     * @param stopType the type of stop to be displayed by the indicator
     * @param indicatorColour the colour of the indicator
     */
    @Composable
    fun StopIndicator(
        stopType: StopType,
        indicatorColour: Color = MaterialTheme.colorScheme.primary
    ) {
        /**
         * Base layout of the indicator tick mark
         */
        @Composable
        fun BaseComponent(colour: Color, height: Dp) =
            Box(
                modifier = Modifier
                    .requiredHeight(height)
                    .requiredWidth(indicatorWidth)
                    .background(colour)
            )

        /**
         * Square indicator tick mark
         */
        @Composable
        fun SquareComponent(colour: Color) = BaseComponent(colour = colour, height = indicatorWidth)

        /**
         * Space-filling component of the indicator (above and below)
         */
        @Composable
        fun SpacerComponent(colour: Color, modifier: Modifier) =
            Box(
                modifier = modifier
                    .requiredWidth(indicatorWidth)
                    .background(colour)
            )

        /**
         * Express arrow indicator
         */
        @Composable
        fun ExpressArrow(colour: Color, modifier: Modifier = Modifier) =
            Image(
                painter = painterResource(id = R.drawable.custom_express_arrow),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colour),
                modifier = modifier
                    .size(width = 24.dp, height = 16.dp)
            )

        /**
         * Colour of a visible component
         */
        val activeColour = indicatorColour

        /**
         * Colour of an invisible component
         */
        val inactiveColour = Color.Transparent

        /**
         * Spacing between segments of the continuing indicator
         */
        val continuesSpacing = 2.dp

        /**
         * Height of the larger component of the continuing indicator
         */
        val continuesSmallHeight = 4.dp

        /**
         * Height of the smaller component of the continuing indicator
         */
        val continuesLargeHeight = 8.dp

        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.requiredWidth(24.dp)
            ) {
                // Top spacing
                if (stopType == StopType.ContinuesBefore) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(continuesSpacing),
                        modifier = Modifier
                            .padding(vertical = continuesSpacing)
                            .weight(1f),
                    ) {
                        SpacerComponent(colour = activeColour, modifier = Modifier.weight(1f))
                        BaseComponent(colour = activeColour, height = continuesLargeHeight)
                        BaseComponent(colour = activeColour, height = continuesSmallHeight)
                    }
                } else {
                    SpacerComponent(
                        colour = when (stopType) {
                            StopType.Stop, StopType.Last, StopType.Skipped, StopType.ArrowSkipped,
                            StopType.ContinuesAfter -> activeColour

                            else -> inactiveColour
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Main horizontal stop bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                ) {
                    // Left part
                    SquareComponent(
                        colour = when (stopType) {
                            StopType.First, StopType.Last -> activeColour
                            else -> inactiveColour
                        }
                    )
                    // Centre part
                    SquareComponent(
                        colour = activeColour
                    )
                    // Right part
                    SquareComponent(
                        colour = when (stopType) {
                            StopType.Stop, StopType.First, StopType.Last, StopType.ContinuesBefore,
                            StopType.ContinuesAfter -> activeColour

                            else -> inactiveColour
                        }
                    )
                }
                // Bottom spacing
                if (stopType == StopType.ContinuesAfter) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(continuesSpacing),
                        modifier = Modifier
                            .padding(vertical = continuesSpacing)
                            .weight(1f),
                    ) {
                        BaseComponent(colour = activeColour, height = continuesSmallHeight)
                        BaseComponent(colour = activeColour, height = continuesLargeHeight)
                        SpacerComponent(colour = activeColour, modifier = Modifier.weight(1f))
                    }
                } else {
                    SpacerComponent(
                        colour = when (stopType) {
                            StopType.Stop, StopType.First, StopType.Skipped, StopType.ArrowSkipped, StopType.ContinuesBefore -> activeColour
                            else -> inactiveColour
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Skipped-stop arrow
            if (stopType == StopType.ArrowSkipped) {
                Box(
                    modifier = Modifier
                        .requiredHeight(21.5.dp)
                        .align(Alignment.Center)
                ) {
                    ExpressArrow(
                        colour = indicatorColour,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    ExpressArrow(
                        colour = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    /**
     * Custom implementation of [ListItem] that works better with [IntrinsicSize] in a [Row]
     *
     * Params are the same as the original [ListItem].
     *
     * @see ListItem
     */
    @Composable
    fun PatternListItem(
        patternIndicator: (@Composable () -> Unit)? = null,
        leadingContent: (@Composable () -> Unit)? = null,
        headlineContent: @Composable () -> Unit,
        supportingContent: (@Composable () -> Unit)? = null,
        trailingContent: (@Composable () -> Unit)? = null
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                patternIndicator?.let { it() }
                leadingContent?.let { it() }
                Column {
                    headlineContent()
                    supportingContent?.let { it() }
                }
            }
            trailingContent?.let { it() }
        }
    }

    /**
     * Card for displaying a stop as part of the stopping pattern
     *
     * Work in progress!
     */
    @Composable
    fun StoppingPatternCard(
        stopType: StopType
    ) {
        // Stop card
        PatternListItem(
            patternIndicator = {
                StopIndicator(stopType = stopType)
            },
            headlineContent = {
                Text(
                    text = "Richmond",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
//                    Text(
//                        text = "Belgrave, Lilydale",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
            },
            leadingContent = {
//                    TextMetLabel(
//                        text = "9", modifier = metLabelModifier.clip(
//                            CircleShape
//                        )
//                    )
            },
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.wrapContentHeight()
                ) {
                    Text(
                        text = "3",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Display the 'min' indicator if at least one departure is not at platform or arriving
                    Text(
                        text = "min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

/**
 * Preview for Stopping Pattern card
 */
@Preview
@Composable
fun PreviewStoppingPattern() {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 8.dp, end = 16.dp)
    ) {
        StoppingPatternComposables.StoppingPatternCard(StoppingPatternComposables.StopType.ContinuesBefore)
//        repeat(3) {
        StoppingPatternComposables.StoppingPatternCard(StoppingPatternComposables.StopType.Skipped)
        StoppingPatternComposables.StoppingPatternCard(StoppingPatternComposables.StopType.ArrowSkipped)
        StoppingPatternComposables.StoppingPatternCard(StoppingPatternComposables.StopType.Skipped)
//        }
        StoppingPatternComposables.StoppingPatternCard(StoppingPatternComposables.StopType.ContinuesAfter)
    }
}
