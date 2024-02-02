package org.noise_planet.noisecapture.starter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.bumble.appyx.navigation.integration.NodeActivity
import com.bumble.appyx.navigation.integration.NodeHost
import com.bumble.appyx.navigation.platform.AndroidLifecycle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.noise_planet.noisecapture.AndroidAudioSource
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.root.RootNode
import org.noise_planet.noisecapture.shared.initKoin
import org.noise_planet.noisecapture.shared.ui.theme.AppyxStarterKitTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainActivity : NodeActivity() {
    override fun onDestroy() {
        super.onDestroy()
        stopKoin()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val koinApplication = initKoin(
            additionalModules = listOf(
                module {
                    single<Context> { applicationContext }
                    single<Activity> { this@MainActivity }
                    single<AudioSource> { AndroidAudioSource() }
                }
            )
        )
        setContent {
            AppyxStarterKitTheme {
                NodeHost(
                    lifecycle = AndroidLifecycle(LocalLifecycleOwner.current.lifecycle),
                    integrationPoint = appyxV2IntegrationPoint,
                ) {
                    RootNode(buildContext = it, koin = koinApplication.koin)
                }
            }
        }
    }
}
