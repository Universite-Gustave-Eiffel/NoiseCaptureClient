package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import kotlin.math.round

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource) : Node(buildContext) {
    private val spectrumChannel: SpectrumChannel = SpectrumChannel().also { spectrumChannel ->
        spectrumChannel.loadConfiguration(
            when (audioSource.getSampleRate()) {
                48000 -> get48000HZ()
                else -> get44100HZ()
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        lifecycleScope.launch {
            audioSource.setup()
            audioSource.samples.collect { samples ->
                val spl = spectrumChannel.processSamplesWeightA(samples.samples)
                noiseLevel = spl
                println("Got $spl dBA")
            }
        }.invokeOnCompletion {
            println("On completion $it")
            audioSource.release()
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text("${round(noiseLevel * 100)/100} dB(A)")
            }
        }
    }

}

