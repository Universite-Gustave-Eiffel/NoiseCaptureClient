package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView

/**
 * Home screen layout.
 */
@Composable
fun HomeScreen(
    onOpenSoundLevelMeterButtonClick: () -> Unit,
    viewModel: HomeScreenViewModel,
) {
    // - Lifecycle

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    viewModel.setupAudioSource()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.soundLevelMeterViewModel.stopListening()
                }

                Lifecycle.Event.ON_RESUME -> {
                    viewModel.soundLevelMeterViewModel.startListening()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            viewModel.releaseAudioSource()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Views

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            SoundLevelMeterHeaderView(onOpenSoundLevelMeterButtonClick, viewModel)
        }
    }
}


@Composable
private fun SoundLevelMeterHeaderView(
    onOpenSoundLevelMeterButtonClick: () -> Unit,
    viewModel: HomeScreenViewModel,
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        SoundLevelMeterView(viewModel.soundLevelMeterViewModel)

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            Text(
                text = stringResource(viewModel.hintText),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.75f)
            )

            // TODO: Make a button design system for the app with custom colors / spacings / drop shadows
            FilledTonalButton(
                onClick = onOpenSoundLevelMeterButtonClick,
                elevation = ButtonDefaults.filledTonalButtonElevation(
                    defaultElevation = 4.dp
                ),
                colors = ButtonDefaults.filledTonalButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth()
            ) {
                val title = stringResource(viewModel.openSoundLevelMeterButtonTitle)
                Icon(Icons.Filled.Mic, contentDescription = title, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title)
            }
        }
    }
}
