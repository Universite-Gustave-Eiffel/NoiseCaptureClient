package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_loading_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI


@OptIn(KoinExperimentalAPI::class)
@Composable
fun MeasurementDetailsScreen(
    viewModel: MeasurementDetailsScreenViewModel,
    onMeasurementDeleted: () -> Unit,
) {

    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(measurementDetailsModule)
    }


    // - Properties

    val viewState by viewModel.viewState.collectAsState()
    val isLoading = viewState is MeasurementDetailsScreenViewState.Loading

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (isLoading) SheetValue.Hidden else SheetValue.PartiallyExpanded,
            skipHiddenState = isLoading == false
        ),
    )
    var containerHeight by remember {
        mutableStateOf(0.dp)
    }
    val localDensity = LocalDensity.current


    // - Layout

    // TODO: For lager screens (tablets / browsers), this layout could be improved by splitting
    //       the screen in two instead of using a bottom sheet.

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetShape = BottomSheetDefaults.ExpandedShape,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        containerColor = Color.White,
        sheetPeekHeight = containerHeight * 0.33f,
        sheetContent = {
            when (viewState) {
                is MeasurementDetailsScreenViewState.ContentReady -> {
                    val state = viewState as MeasurementDetailsScreenViewState.ContentReady
                    MeasurementDetailsChartsView(
                        state.measurement,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                else -> {}
            }
        },
        modifier = Modifier.safeContentPadding()
            .onGloballyPositioned { coordinates ->
                containerHeight = with(localDensity) {
                    coordinates.size.height.toDp()
                }
            },
    ) { contentPadding ->
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
                val state = viewState as MeasurementDetailsScreenViewState.ContentReady

                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(contentPadding)
                ) {
                    Text(
                        text = "Measurement id: ${state.measurement.uuid}",
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.deleteMeasurement()
                            onMeasurementDeleted()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete")
                    }
                }
            }

            else -> {}
        }
    }
}
