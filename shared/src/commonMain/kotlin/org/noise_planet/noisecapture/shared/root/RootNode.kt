package org.noise_planet.noisecapture.shared.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.components.backstack.BackStackModel
import com.bumble.appyx.components.backstack.operation.push
import com.bumble.appyx.components.backstack.ui.parallax.BackStackParallax
import com.bumble.appyx.navigation.composable.AppyxComponent
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import com.bumble.appyx.navigation.node.ParentNode
import org.noise_planet.noisecapture.shared.child.PermissionScreen
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.ScreenData.PermissionTarget
import org.noise_planet.noisecapture.shared.ScreenData.HomeTarget
import org.koin.core.Koin
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.child.HomeScreen
import org.noise_planet.noisecapture.shared.child.MeasurementScreen
import org.noise_planet.noisecapture.shared.child.NavigationScreen

class RootNode(
    buildContext: BuildContext,
    private val backStack: BackStack<ScreenData> = BackStack(
        model = BackStackModel(
            initialTargets = listOf(HomeTarget),
            savedStateMap = buildContext.savedStateMap
        ),
        visualisation = { BackStackParallax(it) }
    ),
    val koin: Koin
) : ParentNode<ScreenData>(
    buildContext = buildContext,
    appyxComponent = backStack
) {

    override fun resolve(interactionTarget: ScreenData, buildContext: BuildContext): Node =
        when(interactionTarget) {
            is PermissionTarget -> PermissionScreen(buildContext, koin.get(),
                ::onPermissionGrantedBeforeMeasurement)
            is HomeTarget -> HomeScreen(buildContext, backStack)
            is ScreenData.MeasurementTarget -> MeasurementScreen(buildContext, backStack, koin.get())
        }

    private fun onPermissionGrantedBeforeMeasurement() {
        // Initialize audiorecord
        val audioSource : AudioSource = koin.get()
        audioSource.setup(48000, 96000)
        backStack.push(ScreenData.MeasurementTarget)
    }

    @Composable
    override fun View(modifier: Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            NavigationScreen(backStack) {
                AppyxComponent(
                    appyxComponent = backStack,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
