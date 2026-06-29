package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.add
import noisecapture.composeapp.generated.resources.compass
import noisecapture.composeapp.generated.resources.location_disabled
import noisecapture.composeapp.generated.resources.my_location
import noisecapture.composeapp.generated.resources.question_mark
import noisecapture.composeapp.generated.resources.remove
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.ContainerColors
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.theme.accentBlue
import org.noiseplanet.noisecapture.util.ncDropShadow
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets
import ovh.plrapps.mapcompose.ui.MapUI


private val CONTROLS_SIZE = 40.dp


@Composable
fun MapView(
    focusedMeasurementUuid: String? = null,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val viewModel: MapViewModel = koinViewModel {
        parametersOf(sizeClass, focusedMeasurementUuid)
    }
    val mapOrientation by viewModel.mapOrientationFlow.collectAsStateWithLifecycle()

    val isLocationAvailable by viewModel.isLocationAvailable.collectAsStateWithLifecycle()
    val autoRecenterEnabled by viewModel.autoRecenterEnabled.collectAsStateWithLifecycle()

    var showHelpDialog by remember { mutableStateOf(false) }


    // - Layout

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
        MapUI(
            state = viewModel.mapState,
        )

        if (viewModel.parameters.showControls) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top,
                modifier = modifier.fillMaxSize()
                    .padding(16.dp)
                    .paddingBottomWithInsets(5.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (viewModel.parameters.showLocationAccuracy) {
                        MapLocationAccuracyView()
                    }

                    // Help button (shows legend and any additional info)
                    NCButton(
                        content = ButtonContent(icon = Res.drawable.question_mark),
                        colors = ContainerColors(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.size(CONTROLS_SIZE)
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
                                painter = painterResource(Res.drawable.add),
                                contentDescription = "Zoom in",
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        IconButton(
                            onClick = { viewModel.zoomOut() },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.remove),
                                contentDescription = "Zoom out",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Recenter button
                    val tint = if (isLocationAvailable) {
                        if (viewModel.parameters.followUserLocation && autoRecenterEnabled) {
                            MaterialTheme.colorScheme.accentBlue
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    } else {
                        MaterialTheme.colorScheme.error
                    }

                    NCButton(
                        content = ButtonContent(
                            icon = if (!viewModel.parameters.followUserLocation || isLocationAvailable) {
                                Res.drawable.my_location
                            } else {
                                Res.drawable.location_disabled
                            },
                        ),
                        onClick = {
                            viewModel.recenter()
                        },
                        colors = ContainerColors(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            contentColor = tint,
                            shadowColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier.size(CONTROLS_SIZE)
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
    .ncDropShadow(shape = CircleShape)
    .background(MaterialTheme.colorScheme.surfaceContainer, shape = CircleShape)
