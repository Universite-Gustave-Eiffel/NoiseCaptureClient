package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.ScreenData
import kotlin.math.log10

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource) : Node(buildContext) {
    private val noiseLevel : State<Double> =  mutableStateOf(0.0)

    @Composable
    override fun View(modifier: Modifier) {

        lifecycleScope.launch {
            audioSource.samples.collect {
                    samples -> noiseLevel to 10*log10(samples.map { it*it }.average())
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text("${noiseLevel.value} dBFS")
            }
        }
    }

}

