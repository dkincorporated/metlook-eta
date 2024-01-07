package dev.dkong.metlook.eta.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.dkong.metlook.eta.R
import dev.dkong.metlook.eta.common.Constants
import dev.dkong.metlook.eta.common.RouteType
import dev.dkong.metlook.eta.common.Utils
import dev.dkong.metlook.eta.common.Utils.finishActivity
import dev.dkong.metlook.eta.common.VehicleData
import dev.dkong.metlook.eta.common.datastore.recents.RecentServicesCoordinator
import dev.dkong.metlook.eta.common.utils.PtvApi
import dev.dkong.metlook.eta.composables.ElevatedAppBarNavigationIcon
import dev.dkong.metlook.eta.composables.IconMetLabel
import dev.dkong.metlook.eta.composables.NavBarPadding
import dev.dkong.metlook.eta.composables.PersistentBottomSheetScaffold
import dev.dkong.metlook.eta.composables.SectionHeading
import dev.dkong.metlook.eta.composables.PatternComposables
import dev.dkong.metlook.eta.composables.TextMetLabel
import dev.dkong.metlook.eta.composables.TwoLineCenterTopAppBarText
import dev.dkong.metlook.eta.objects.metlook.PatternDeparture
import dev.dkong.metlook.eta.objects.metlook.PatternType
import dev.dkong.metlook.eta.objects.metlook.ServiceDeparture
import dev.dkong.metlook.eta.objects.ptv.PatternResult
import dev.dkong.metlook.eta.ui.theme.MetlookTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

/**
 * Activity for a specific Service
 */
class ServiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receive Service info from Bundle
        val bundleService = intent?.extras?.getString("service")
        bundleService?.let { serviceString ->
            // Parse the service
            val service = Constants.jsonFormat.decodeFromString<ServiceDeparture>(serviceString)

            // Render the UI
            setContent {
                MetlookTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Constants.appSurfaceColour() // wait until new Surface
                    ) {
                        val navController = rememberNavController()
                        ServiceScreen(navController, service)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun ServiceScreen(navHostController: NavHostController, originalDeparture: ServiceDeparture) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current

        val scaffoldState = rememberBottomSheetScaffoldState()
        val patternListState = rememberLazyListState()
        var topBarHeight by remember { mutableStateOf(0.dp) }
        var collapsedPatternHeight by remember { mutableStateOf(512.dp) }

        var loadingState by remember { mutableStateOf(true) }
        val pattern = remember { mutableStateListOf<PatternDeparture>() }
        var nextStopId by remember { mutableStateOf<Int?>(null) }
        var previousStopIndex by remember { mutableStateOf<Int?>(null) }
        var nextStopIndex by remember { mutableStateOf<Int?>(null) }
        var followingStopIndex by remember { mutableStateOf<Int?>(null) }
        var patternType by remember { mutableStateOf<PatternType?>(null) }

        var originalStopIndex by remember { mutableStateOf<Int?>(null) }
        var alightingStopIndex by remember { mutableStateOf<Int?>(null) }

        // Lifecycle states
        // TODO: Try to find a more elegant way to handle these
        var hasFirstLoaded by remember { mutableStateOf(false) }
        var isScreenActive by remember { mutableStateOf(true) }

        suspend fun update(isFirstLoad: Boolean) {
            if (isFirstLoad)
                loadingState = true

            val request = PtvApi.getApiUrl(
                Uri.Builder().apply {
                    appendPath("v3")
                    appendPath("pattern")
                    appendPath("run")
                    appendPath(originalDeparture.runRef)
                    appendPath("route_type")
                    appendPath(originalDeparture.routeType.id.toString())
                    appendQueryParameter("expand", "all")
                    // Pass a stop for Tram to get estimated times, because the API is weird
                    if (originalDeparture.routeType == RouteType.Tram)
                        appendQueryParameter(
                            "stop_id",
                            originalDeparture.departureStop.stopId.toString()
                        )
                    // Only get skipped stops for Train
                    if (originalDeparture.routeType == RouteType.Train)
                        appendQueryParameter("include_skipped_stops", "true")
                }
            )

            Log.d("SERVICE", request.toString())

            if (request == null) {
                return
            }

            val response: String = Constants.httpClient.get(request).body()

            try {
                val decodedPattern =
                    Constants.jsonFormat.decodeFromString<PatternResult>(response)

                val run = decodedPattern.runs[originalDeparture.runRef]
                    ?: return

                val processedPattern = decodedPattern.departures
                    .asSequence()
                    .mapIndexed { index, departure ->
                        val stop = decodedPattern.stops[departure.stopId]
                            ?: return@mapIndexed null

                        // Initiate the all-in-one departure object
                        val processedDeparture =
                            PatternDeparture(
                                departure,
                                run,
                                stop,
                                when (index) {
                                    0 -> PatternComposables.StopType.First
                                    decodedPattern.departures.lastIndex -> PatternComposables.StopType.Last
                                    else -> PatternComposables.StopType.Stop
                                }
                            )

                        val skippedStops =
                            // Skipped stops after Flinders Street are usually due to API error
                            if (departure.stopId == 1071) null else
                                departure.skippedStops?.mapIndexed { skippedIndex, skippedStop ->
                                    PatternDeparture(
                                        departure,
                                        run,
                                        skippedStop,
                                        if (
                                            skippedIndex == departure.skippedStops.lastIndex.floorDiv(
                                                2
                                            )
                                            && departure.skippedStops.size > 1
                                        ) PatternComposables.StopType.ArrowSkipped
                                        else PatternComposables.StopType.Skipped
                                    )
                                }

                        return@mapIndexed if (skippedStops != null)
                            listOf(processedDeparture) + skippedStops
                        else listOf(processedDeparture)
                    }
                    // Remove any failed parse results
                    .filterNotNull()
                    // Flatten stops and skipped stops
                    .flatten()

                pattern.clear()
                pattern.addAll(processedPattern)

                // Get the next stop
                nextStopId = processedPattern.find { departure ->
                    (departure.timeToEstimatedDeparture()?.inWholeSeconds ?: -1) > 0
                }
                    ?.stop
                    ?.stopId

                nextStopIndex =
                    pattern.indexOfFirst { departure -> departure.stop.stopId == nextStopId }
                        .takeIf { it != -1 }

                originalStopIndex =
                    pattern.indexOfFirst { departure ->
                        departure.stop.stopId == originalDeparture.departureStop.stopId
                    }
                        .takeIf { it != -1 }

                // Find the actual next stop (not skipped stop)
                nextStopIndex?.let { nextIndex ->
                    // Find the latest stop that is actually a Stop before the next stop
                    previousStopIndex =
                        pattern.slice(0 until nextIndex)
                            .indexOfLast { departure ->
                                departure.stopType == PatternComposables.StopType.Stop
                                        || departure.stopType == PatternComposables.StopType.First
                            }
                            .takeIf { it != -1 }
                    // Find the earliest stop that is actually a Stop after the next stop
                    followingStopIndex =
                        pattern.slice((nextIndex + 1) until pattern.size)
                            .indexOfFirst { departure ->
                                departure.stopType == PatternComposables.StopType.Stop
                                        || departure.stopType == PatternComposables.StopType.Last
                            }
                            .let { if (it == -1) null else it + nextIndex + 1 }
                    // Update the pattern type
                    followingStopIndex?.let { followingIndex ->
                        patternType = Utils.patternType(
                            originalDeparture.routeType,
                            originalDeparture.route.routeId,
                            pattern.slice((followingIndex + 1) until pattern.size)
                                .count { departure ->
                                    departure.stopType.stopClass ==
                                            PatternComposables.StopType.StopClass.Skipped
                                }
                        )
                    }
                    // Get alighting stop if set
                    if (alightingStopIndex == null)
                        originalDeparture.alightingStop?.let { alightingStop ->
                            alightingStopIndex = pattern
                                .indexOfFirst { it.stop == alightingStop }
                                .takeIf { it != -1 }
                        }
                }

                hasFirstLoaded = true
                loadingState = false
            } catch (e: SerializationException) {
                // TODO: Error message
                Log.e("SERVICE", e.toString())
            }
        }

        LaunchedEffect(Unit) {
            update(isFirstLoad = true)
            delay(Constants.refreshInterval)
            while (true) {
                if (isScreenActive) update(isFirstLoad = false)
                delay(Constants.refreshInterval)
            }
        }

        // Listen for Composable lifecycle
        Utils.ComposableEventListener { event ->
            when (event) {
                // On pause, disable refresh
                Lifecycle.Event.ON_PAUSE -> isScreenActive = false
                // On resume, update departures, and enable refresh
                Lifecycle.Event.ON_RESUME -> scope.launch {
                    if (hasFirstLoaded)
                        update(isFirstLoad = true)
                    isScreenActive = true
                }

                else -> {}
            }
        }

        PersistentBottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        TwoLineCenterTopAppBarText(
                            title = originalDeparture.serviceTitle,
                            subtitle = if (arrayOf(RouteType.Tram, RouteType.Bus).contains(
                                    originalDeparture.routeType
                                )
                            ) {
                                "To ${originalDeparture.destinationName}"
                            } else if (originalDeparture.routeType == RouteType.Train) {
                                nextStopIndex?.let {
                                    val nextStopName = pattern[it].stop.stopName.first
                                    "${
                                        with(pattern[it]) {
                                            if (this.isAtPlatform) "Arrived at"
                                            else if (this.isArriving()) "Arriving at"
                                            else "Next station is"
                                        }
                                    }: $nextStopName"
                                }
                            } else {
                                null
                            }
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor =
                        if (with(scaffoldState.bottomSheetState) {
                                currentValue == SheetValue.Expanded
                                        && targetValue == SheetValue.Expanded
                            })
                            Constants.scrolledAppbarContainerColour()
                        else Constants.appSurfaceColour()
                    ),
                    navigationIcon = {
                        ElevatedAppBarNavigationIcon(onClick = {
                            // Finish the Activity
                            context.finishActivity()
                        })
                    },
                    actions = {
                        // Progress indicator
                        if (loadingState)
                            IconButton(onClick = { /* Dummy container */ }) {
                                CircularProgressIndicator(
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .requiredSize(24.dp) // from icon button
                                )
                            }
                        else {
                            // Service icon
                            originalDeparture.route.routeNumber?.let {
                                TextMetLabel(text = it)
                            }
                        }
                    },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        with(density) {
                            topBarHeight = coordinates.size.height.toDp()
                        }
                    })
            },
            topBarHeight = topBarHeight,
            mainContent = {
                // Map
            },
            sheetContent = {
                val isSheetExpanded =
                    scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
                            || scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        state = patternListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                if (isSheetExpanded) return@onGloballyPositioned
                                with(density) {
                                    collapsedPatternHeight = coordinates.size.height.toDp()
                                }
                            }
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        // Info card
                        if (!isSheetExpanded) {
                            @Composable
                            fun InfoCard(
                                title: String,
                                subtitle: String? = null,
                                @DrawableRes icon: Int? = null
                            ) {
                                ListItem(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                                    headlineContent = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    },
                                    supportingContent = {
                                        subtitle?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    },
                                    leadingContent = {
                                        icon?.let {
                                            IconMetLabel(icon = it)
                                        }
                                    }
                                )
                            }
                            item {
                                nextStopIndex?.let next@{ nextIndex ->
                                    // Show departure stop card when
                                    // - original stop is after the currently next stop.
                                    originalStopIndex?.let original@{ originalIndex ->
                                        // Don't display if the stop has passed, or it is the next stop
                                        if (originalIndex <= nextIndex) return@original
                                        with(pattern[originalIndex]) {
                                            InfoCard(
                                                title =
                                                if (isAtPlatform) "Departs now!"
                                                else
                                                    timeToEstimatedDeparture()?.let {
                                                        if (it.inWholeSeconds < 60) "Departs in <1 min"
                                                        else "Departs in ${it.inWholeMinutes} min"
                                                    }
                                                        ?: "Departs in ${timeToScheduledDeparture().inWholeMinutes}* min",
                                                subtitle = "From ${stop.fullStopName}",
                                                R.drawable.baseline_airline_stops_24
                                            )
                                        }
                                    }
                                    // Show alighting card when
                                    // - the service has passed the original stop; and
                                    // - the alighting stop is after the next stop (hasn't passed).
                                    if (
                                        compareValues(alightingStopIndex, nextStopIndex) >= 0
                                        && compareValues(nextStopIndex, originalStopIndex) > 0
                                    ) {
                                        // Display alighting stop if service is past original stop
                                        alightingStopIndex?.let { alightingIndex ->
                                            with(pattern[alightingIndex]) {
                                                InfoCard(
                                                    title =
                                                    if (nextStopIndex == alightingIndex) "Alight at the next stop"
                                                    else "${
                                                        pattern
                                                            .slice((nextIndex + if (pattern[nextIndex].isAtPlatform) 1 else 0)..alightingIndex)
                                                            .count { it.stopType.stopClass == PatternComposables.StopType.StopClass.Stop }
                                                    } stops, ${
                                                        (timeToEstimatedDeparture()
                                                            ?: timeToScheduledDeparture())
                                                            .inWholeMinutes
                                                    } min to go",
                                                    subtitle = "Alighting at ${stop.fullStopName}",
                                                    R.drawable.outline_pin_drop_24
                                                )

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Vehicle info
                        val vehicle = VehicleData.getVehicle(
                            originalDeparture.vehicleDescriptor?.id,
                            originalDeparture.routeType
                        )
                        vehicle?.let { v ->
                            if (isSheetExpanded) {
                                item {
                                    SectionHeading(heading = "Vehicle")
                                    ListItem(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surface),
                                        headlineContent = {
                                            Text(
                                                text = v.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = v.id,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        if (isSheetExpanded) {
                            item {
                                SectionHeading(heading = "Stopping pattern")
                            }
                        }
                        pattern.forEachIndexed { index, patternStop ->
                            val isStopBeforeNext = index == previousStopIndex
                            val isNextStop = index == nextStopIndex
                            val isStopAfterNext = index == followingStopIndex
                            val isLastStop = index == pattern.lastIndex

                            if (
//                                isStopBeforeNext
                                isNextStop
                                || isStopAfterNext
                                || isLastStop
                                || isSheetExpanded
                            ) {
                                if (isNextStop && !isSheetExpanded) {
                                    // Display next-stop heading
                                    item {
                                        PatternComposables.PatternHeadingCard(
                                            heading = when (index) {
                                                0 -> "Originates from"
                                                pattern.lastIndex -> "Terminates at"
                                                else -> when (patternStop.routeType) {
                                                    RouteType.Train -> "Next station is"
                                                    else -> "Next stop is"
                                                }
                                            },
                                            showIndicator = false, // index > 0,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                                if (
                                    isLastStop
                                    && originalDeparture.routeType == RouteType.Train
                                    && (nextStopIndex ?: pattern.lastIndex) < pattern.lastIndex - 1
                                    && !isSheetExpanded
                                ) {
                                    // Display pattern type heading (only if next stop is not the last or second-last stop)
                                    patternType?.let { patternType ->
                                        item {
                                            PatternComposables.PatternHeadingCard(
                                                heading = when (patternType) {
                                                    // Conditionally format the message
                                                    PatternType.LimitedStops -> "Express to"
                                                    PatternType.SuperLimitedStops -> "Ltd Express to"
                                                    PatternType.AllStops -> "Stopping all stations to"
                                                    PatternType.SkipsOneStop -> // Find the skipped stop
                                                        "Stopping all stations except ${
                                                            followingStopIndex?.let { followingStopIndex ->
                                                                pattern.slice((followingStopIndex + 1) until pattern.size)
                                                                    .find { departure -> departure.stopType == PatternComposables.StopType.Skipped }
                                                                    ?.stop?.stopName?.first
                                                            }
                                                        } to"

                                                    else -> "Service to"
                                                },
                                                showIndicator = index > 0,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                                item(key = patternStop.stop.stopId.toString() + patternStop.departureSequence) {
                                    var isDropDownShown by remember { mutableStateOf(false) }

                                    PatternComposables.StoppingPatternCard(
                                        patternStop = patternStop,
                                        stopType =
                                        if (!isSheetExpanded && patternStop.stopType == PatternComposables.StopType.Stop) {
                                            if (isNextStop) PatternComposables.StopType.ContinuesBefore
                                            else if (isStopAfterNext) PatternComposables.StopType.ContinuesAfter
                                            else patternStop.stopType
                                        } else patternStop.stopType,
                                        modifier = (if (isNextStop) Modifier
                                            .clip(
                                                RoundedCornerShape(16.dp)
                                            )
                                            .background(if (isSheetExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                            .animateItemPlacement()
                                        else Modifier.animateItemPlacement())
                                            .clickable {
                                                // Don't show options if not a stop
                                                if (
                                                    patternStop.stopType.stopClass !=
                                                    PatternComposables.StopType.StopClass.Stop
                                                ) return@clickable
                                                // Don't show options if stop is before next
                                                nextStopIndex?.let { if (index < it) return@clickable }
                                                // Open dropdown menu
                                                isDropDownShown = true
                                            },
                                        dropdown = {
                                            DropdownMenu(
                                                expanded = isDropDownShown,
                                                onDismissRequest = { isDropDownShown = false }
                                            ) {
                                                SectionHeading(
                                                    heading = patternStop.stop.stopName.first,
                                                    includePadding = false,
                                                    modifier = Modifier.padding(MenuDefaults.DropdownMenuItemContentPadding)
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Alight here") },
                                                    onClick = {
                                                        // Mark stop as the alighting stop
                                                        alightingStopIndex = index
                                                        // Update the stop
                                                        scope.launch {
                                                            RecentServicesCoordinator.add(
                                                                context,
                                                                originalDeparture.copy(alightingStop = patternStop.stop)
                                                            )
                                                            update(false)
                                                        }
                                                        // Hide the dropdown
                                                        isDropDownShown = false
                                                    },
                                                    trailingIcon = {
                                                        Image(
                                                            painter = painterResource(id = R.drawable.outline_notifications_active_24),
                                                            null,
                                                            colorFilter = ColorFilter.tint(
                                                                MaterialTheme.colorScheme.onSurface
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        },
                                        isAlighting = compareValues(index, alightingStopIndex) == 0
                                    )
                                }
                                // Check whether skipped-stop card needs to be shown
                                nextStopIndex?.let { nextIndex ->
                                    if (!isSheetExpanded && isStopBeforeNext && index != nextIndex - 1) {
                                        item(key = "E" + patternStop.stop.stopId.toString()) {
                                            PatternComposables.SkippedStopPatternCard(
                                                skippedStops = pattern.slice((index + 1) until nextIndex),
                                                isBefore = true
                                            )
                                        }
                                    }
                                    followingStopIndex?.let { followingIndex ->
                                        if (!isSheetExpanded && isNextStop && nextIndex + 1 != followingIndex) {
                                            item(key = "E" + patternStop.stop.stopId.toString()) {
                                                PatternComposables.SkippedStopPatternCard(
                                                    skippedStops = pattern.slice((nextIndex + 1) until followingIndex),
                                                    isBefore = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            NavBarPadding()
                        }
                        if (!isSheetExpanded) {
                            // Automatically sized sheet is a bit off, so add more padding to compensate
                            items(2) {
                                NavBarPadding()
                            }
                        }
                    }
                }
            },
            sheetPeekHeight = maxOf(
                256.dp,
                animateDpAsState(targetValue = collapsedPatternHeight, label = "").value
            )
        )
    }
}