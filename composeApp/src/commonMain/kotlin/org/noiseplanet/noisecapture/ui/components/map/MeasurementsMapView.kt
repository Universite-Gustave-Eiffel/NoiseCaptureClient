package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.compass
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.util.shadow.dropShadow
import ovh.plrapps.mapcompose.ui.MapUI


private val CONTROLS_SIZE = 40.dp


@Composable
fun MeasurementsMapView(
    focusedMeasurementUuid: String? = null,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val viewModel: MeasurementsMapViewModel = koinViewModel {
        parametersOf(sizeClass, focusedMeasurementUuid)
    }
    val mapOrientation by viewModel.mapOrientationFlow.collectAsStateWithLifecycle()

    var showHelpDialog by remember { mutableStateOf(false) }


    // - Layout

    Box {
        MapUI(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
            state = viewModel.mapState,
        )

        if (viewModel.parameters.showControls) {

            var controlsModifier = modifier
            // If the view expands down to the bottom of the screen, take safe area padding into account
            if (viewModel.parameters.visibleAreaPaddingRatio.bottom == 0.0f) {
                controlsModifier = controlsModifier.windowInsetsPadding(
                    WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top,
                modifier = controlsModifier.fillMaxWidth()
                    .fillMaxHeight(fraction = 1f - viewModel.parameters.visibleAreaPaddingRatio.bottom)
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Help button (shows legend and any additional info)
                    NCButton(
                        viewModel = viewModel.helpButtonViewModel,
                        onClick = {
                            showHelpDialog = true
                        },
                        modifier = Modifier.size(CONTROLS_SIZE)
                            .mapControl()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Compass button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(CONTROLS_SIZE)
                            .mapControl()
                            .rotate(mapOrientation)
                    ) {
                        IconButton(
                            onClick = { viewModel.resetOrientation() },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.compass),
                                contentDescription = "Compass",
                                tint = Color.Unspecified,
                            )
                        }
                    }

                    // Zoom controls
                    Column(
                        modifier = Modifier.width(CONTROLS_SIZE)
                            .mapControl()
                    ) {
                        IconButton(
                            onClick = { viewModel.zoomIn() },
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Zoom in",
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        IconButton(
                            onClick = { viewModel.zoomOut() },
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Zoom out",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Recenter button
                    NCButton(
                        viewModel = viewModel.recenterButtonViewModel,
                        onClick = {
                            viewModel.recenter()
                            viewModel.autoRecenterEnabled = true
                        },
                        modifier = Modifier.size(CONTROLS_SIZE)
                            .mapControl()
                    )
                }
            }

            if (showHelpDialog) {
                MapLegendView(
                    onDismissRequest = { showHelpDialog = false }
                )
            }
        }
    }
}


@Composable
private fun Modifier.mapControl() = this
    .dropShadow(shape = RoundedCornerShape(100))
    .clip(shape = RoundedCornerShape(percent = 100))
    .background(MaterialTheme.colorScheme.surfaceContainer)
