package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_loading_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapView
import org.noiseplanet.noisecapture.ui.navigation.router.DetailsRouter


@OptIn(KoinExperimentalAPI::class)
@Composable
fun MeasurementDetailsScreen(
    viewModel: MeasurementDetailsScreenViewModel,
    router: DetailsRouter,
) {

    // - DI

    rememberKoinModules {
        listOf(measurementDetailsModule)
    }


    // - Properties

    val logger: Logger = koinInject()
    val localDensity = LocalDensity.current
    var containerHeight by remember { mutableStateOf(0.dp) }
    val sheetPeekHeight by derivedStateOf { containerHeight * 0.4f }

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading: Boolean = viewState is MeasurementDetailsScreenViewState.Loading

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (isLoading) SheetValue.Hidden else SheetValue.PartiallyExpanded,
            skipHiddenState = !isLoading
        ),
    )

    // Subscribe to sheet state updates to get the current bottom sheet offset
    val currentBottomSheetOffset: Dp = try {
        with(localDensity) {
            sheetState.bottomSheetState.requireOffset().toDp()
        }
    } catch (e: IllegalStateException) {
        logger.debug("Offset is not available yet: $e")
        Dp.Infinity
    }

    // Only allow dismissing the sheet by swiping when content is fully scrolled at the top
    val sheetContentScrollState = rememberScrollState()
    val enableSheetSwipe = !(currentBottomSheetOffset == 0.dp && sheetContentScrollState.value > 0)


    // - Layout

    // TODO: For lager screens (tablets / browsers), this layout could be improved by splitting
    //       the screen in two instead of using a bottom sheet.

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
            .onGloballyPositioned { coordinates ->
                containerHeight = with(localDensity) {
                    coordinates.size.height.toDp()
                }
            }
    ) {
        BottomSheetScaffold(
            scaffoldState = sheetState,
            sheetSwipeEnabled = enableSheetSwipe,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            containerColor = MaterialTheme.colorScheme.surface,
            sheetPeekHeight = sheetPeekHeight,
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle()
            },
            sheetContent = {
                Crossfade(viewState) { viewState ->
                    when (viewState) {
                        is MeasurementDetailsScreenViewState.ContentReady -> {
                            MeasurementDetailsChartsView(
                                viewState.measurement.uuid,
                                modifier = Modifier.padding(horizontal = 16.dp)
                                    .verticalScroll(sheetContentScrollState)
                            )
                        }

                        else -> {}
                    }
                }
            },
            // Leave some extra space at the top when sheet is expended so the user knows it can
            // still be dismissed to access the content behind.
            modifier = Modifier.padding(top = 32.dp)
        ) { contentPadding ->
            Crossfade(viewState) { viewState ->
                when (viewState) {
                    is MeasurementDetailsScreenViewState.Loading -> {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(Res.string.measurement_details_loading_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is MeasurementDetailsScreenViewState.ContentReady -> {
                        MeasurementsMapView(
                            // Compensate the top padding of the expended sheet by shifting the map
                            // view up by the same amount.
                            modifier = Modifier.offset(y = -(32.dp)),
                            focusedMeasurementUuid = viewModel.measurementId,
                        )
                    }

                    is MeasurementDetailsScreenViewState.NoMeasurement -> {
                        // If measurement becomes null, it means it was deleted
                        router.onMeasurementDeleted()
                    }
                }
            }
        }

        val footerGradientHeight = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding() + 32.dp

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(footerGradientHeight)
                .background(
                    brush = Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0f),
                        1f to MaterialTheme.colorScheme.surfaceContainer,
                    )
                )
        )
    }
}
