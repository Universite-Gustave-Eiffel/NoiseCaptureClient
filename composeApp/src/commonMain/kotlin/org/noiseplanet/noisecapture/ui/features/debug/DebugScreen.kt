package org.noiseplanet.noisecapture.ui.features.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DebugScreen(
//    viewModel: DebugScreenViewModel,
) {
    // - DI

    rememberKoinModules {
        listOf(debugModule)
    }


    // - Properties

    val microphoneProvider: MicrophoneProvider = koinInject()
    val microphones by microphoneProvider.availableInputs.collectAsStateWithLifecycle()


    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(microphones) { mic ->
                Column {
                    Text(text = "ID: ${mic.id}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Name: ${mic.name}")
                    Text(text = "Type: ${mic.type}")
                }
            }
        }
    }
}
