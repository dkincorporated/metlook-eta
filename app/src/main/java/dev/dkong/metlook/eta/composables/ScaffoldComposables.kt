@file:OptIn(ExperimentalMaterial3Api::class)

package dev.dkong.metlook.eta.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.dkong.metlook.eta.common.Constants

// Scaffold-type Composables, used throughout the app
// Author: David Kong

/**
 * Base template for screens using a scaffold
 * @param navController the nav controller for the app
 * @param title the title to be displayed at the top
 * @param horizontalPadding the horizontal padding for the content
 * @param navigationIcon the icon to be displayed in the navigation button
 * @param onNavigationIconClick the click action for the navigation icon
 * @param content the content to be displayed (must be lazy list)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopAppbarScaffold(
    navController: NavHostController,
    title: String,
    horizontalPadding: Dp = 0.dp,
    navigationIcon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    onNavigationIconClick: () -> Unit = { navController.navigateUp() },
    navigationBar: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Constants.appSurfaceColour(),
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Constants.appSurfaceColour(),
                    scrolledContainerColor = Constants.scrolledAppbarContainerColour()
                ),
                navigationIcon = {
                    ElevatedAppBarNavigationIcon(
                        onClick = onNavigationIconClick,
                        icon = navigationIcon
                    )
                }
            )
        },
        bottomBar = navigationBar
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
        ) {
            content()
            item {
                Box(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

/**
 * Base template for screens using a scaffold
 * @param navController the nav controller for the app
 * @param title the title to be displayed at the top
 * @param horizontalPadding the horizontal padding for the content
 * @param navigationIcon the icon to be displayed in the navigation button
 * @param onNavigationIconClick the click action for the navigation icon
 * @param content the content to be displayed (in box form)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopAppbarScaffoldBox(
    navController: NavHostController,
    title: String,
    horizontalPadding: Dp = 0.dp,
    navigationIcon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    onNavigationIconClick: () -> Unit = { navController.navigateUp() },
    navigationBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Constants.appSurfaceColour(),
        contentWindowInsets = WindowInsets(bottom = 0.dp),
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Constants.appSurfaceColour(),
                    scrolledContainerColor = Constants.scrolledAppbarContainerColour()
                ),
                navigationIcon = {
                    ElevatedAppBarNavigationIcon(
                        onClick = onNavigationIconClick,
                        icon = navigationIcon
                    )
                }
            )
        },
        bottomBar = navigationBar
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth(),
            content = content
        )
    }
}

/**
 * Heading for a section
 * @param heading the text to be displayed
 * @param includePadding whether standard padding should be applied
 * @param modifier any modifier for the heading
 */
@Composable
fun SectionHeading(heading: String, includePadding: Boolean = true, modifier: Modifier = Modifier) {
    Text(
        text = heading,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = if (includePadding) modifier.padding(vertical = 8.dp, horizontal = 16.dp) else modifier
    )
}

/**
 * A rounded, elevated column to display content that stands out from the background surface
 * @param modifier any modifier for the column
 * @param content the content to be displayed in the column
 */
@Composable
fun MaterialColumn(modifier: Modifier = Modifier, content: @Composable (ColumnScope.() -> Unit)) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(Constants.largeListCornerRadius))
            .background(Constants.materialListCardContainerColour()),
        content = content
    )
}

/**
 * Implementation of a vertical divider, because [Divider]s are only horizontal
 * @param modifier additional modifiers for the divider
 * @param color override on surface variant
 * @param thickness override the thickness
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color = color)
    )
}

/**
 * A section that can be expanded or collapsed with a heading
 * @param sectionName the name of the section
 * @param initiallyExpanded whether the section should be expanded upon loading
 * @param horizontalPadding the horizontal padding for the column
 * @param content the content to be displayed in the column
 */
@Composable
fun ExpandableSection(
    sectionName: String,
    initiallyExpanded: Boolean = false,
    horizontalPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val expanded = remember { mutableStateOf(initiallyExpanded) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded.value = !expanded.value }
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeading(heading = sectionName, modifier = Modifier.weight(1f))
            IconButton(onClick = { expanded.value = !expanded.value }) {
                Image(
                    if (expanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    if (expanded.value) "Collapse $sectionName" else "Expand $sectionName",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        }
        AnimatedVisibility(
            visible = expanded.value,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = horizontalPadding), content = content)
        }
    }
}

/**
 * Icon to be set as the navigation icon in a [TopAppBar] that is at the same elevation as an elevated/collapsed [TopAppBar]
 * @param onClick the on-click action
 * @param icon the [ImageVector] of the icon to be in the button
 * @param iconContentDescription accessible content description for the icon in the button
 */
@Composable
fun ElevatedAppBarNavigationIcon(
    onClick: () -> Unit,
    icon: ImageVector = Icons.AutoMirrored.Default.ArrowBack,
    iconContentDescription: String = "Go back"
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(Constants.scrolledAppbarContainerColour())
    ) {
        Icon(icon, contentDescription = iconContentDescription)
    }
}

/**
 * Scaffold for a persistent bottom sheet layout
 * @param scaffoldState the [BottomSheetScaffoldState] for the bottom sheet
 * @param topBar the top app bar for the scaffold; [CenterAlignedTopAppBar] is recommend
 * @param topBarHeight the height of the top bar *after composition* -- use [Modifier.onGloballyPositioned] in a `remember`, and pass the value
 * @param mainContent the content to be placed underneath the bottom sheet
 * @param sheetContent the content of the persistent bottom sheet
 * @param sheetPeekHeight the initialisation height of the bottom sheet
 */
@Composable
fun PersistentBottomSheetScaffold(
    scaffoldState: BottomSheetScaffoldState,
    topBar: @Composable (() -> Unit),
    topBarHeight: Dp,
    mainContent: @Composable (BoxScope.() -> Unit),
    mainContentBackgroundColour: Color = MaterialTheme.colorScheme.surface,
    sheetContent: @Composable (ColumnScope.() -> Unit),
    sheetContainerColour: Color = MaterialTheme.colorScheme.surfaceContainer,
    sheetPeekHeight: Dp
) {
    // Aesthetic values for bottom sheet
    val isSheetExpanded = with(scaffoldState.bottomSheetState) {
        ((currentValue == SheetValue.Expanded
                || targetValue == SheetValue.Expanded)
                && targetValue != SheetValue.PartiallyExpanded)
    }
    val bottomSheetCornerRadius = if (isSheetExpanded) 0.dp else 28.dp

    Scaffold(
        topBar = topBar
    ) { padding ->
        // Below is a stupid solution to a stupid problem of not being able to easily have
        // the persistent bottom sheet expand to just under the top app bar.
        // Hopefully, future API changes make it less stupid.

        // All main content goes in this Box
        Box(
            content = mainContent,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(mainContentBackgroundColour)
        )

        // Bottom Sheet, padded so the sheet expands to just under the app bar
        // Note: All main content (behind the bottom sheet) must go in the above Box
        Box(modifier = Modifier.padding(top = topBarHeight)) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContainerColor = sheetContainerColour,
                sheetPeekHeight = sheetPeekHeight,
                sheetShape = RoundedCornerShape(
                    topStart = animateDpAsState(
                        targetValue = bottomSheetCornerRadius,
                        label = ""
                    ).value,
                    topEnd = animateDpAsState(
                        targetValue = bottomSheetCornerRadius,
                        label = ""
                    ).value,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                // Sheet content goes in this column
                sheetContent = sheetContent,
                modifier = Modifier
                    .padding(padding)
            ) { innerPadding ->
                // Bottom Sheet Scaffold content is not used; use parent Scaffold for content
            }
        }
    }
}

/**
 * Layout for a two-line center-aligned app bar
 * @param title the primary text
 * @param subtitle the secondary text (if `null`, it will not be shown)
 */
@Composable
fun TwoLineCenterTopAppBarText(
    title: String,
    subtitle: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        subtitle?.let { s ->
            Text(
                text = s,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

/**
 * Chip with a checked indicator
 * @param selected whether the chip is selected
 * @param onClick the on-click action
 */
@Composable
fun CheckableChip(
    selected: Boolean,
    name: String,
    showIcon: Boolean = true,
    showRemoveIcon: Boolean = false,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(name) },
        leadingIcon = {
            if (showIcon)
                AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Icon(Icons.Default.Check, "Checked")
                }
        },
        trailingIcon = {
            if (showRemoveIcon)
                AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Icon(Icons.Default.Close, "Remove")
                }
        }
    )
}
