package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.util.shadow.dropShadow
import ovh.plrapps.mapcompose.ui.MapUI


@Composable
fun MeasurementsMapView(modifier: Modifier = Modifier) {

    // - Properties

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val viewModel: MeasurementsMapViewModel = koinViewModel {
        parametersOf(sizeClass)
    }
    val mapOrientation by viewModel.mapOrientationFlow.collectAsStateWithLifecycle()


    // - Layout

    MapUI(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        state = viewModel.mapState,
    )

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier.fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            // TODO: Improve compass button styling
            NCButton(
                viewModel = viewModel.compassButtonViewModel,
                onClick = {
                    viewModel.resetOrientation()
                },
                modifier = Modifier.size(48.dp)
                    .rotate(mapOrientation)
            )

            Column(
                modifier = Modifier.width(48.dp)
                    .dropShadow(shape = RoundedCornerShape(percent = 100), isPressed = false)
                    .clip(shape = RoundedCornerShape(percent = 100))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
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

            NCButton(
                viewModel = viewModel.recenterButtonViewModel,
                onClick = {
                    viewModel.recenter()
                    viewModel.autoRecenterEnabled = true
                },
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
