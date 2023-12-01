package org.noise_planet.noisecapture.starter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.bumble.appyx.navigation.integration.NodeActivity
import com.bumble.appyx.navigation.integration.NodeHost
import com.bumble.appyx.navigation.platform.AndroidLifecycle
import org.koin.dsl.module
import org.noise_planet.noisecapture.shared.child.root.RootNode
import org.noise_planet.noisecapture.shared.initKoin
import org.noise_planet.noisecapture.shared.ui.theme.AppyxStarterKitTheme

class MainActivity : NodeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val koinApplication = initKoin(
            additionalModules = listOf(
                module {
                    single<Context> { applicationContext }
                    single<Activity> { this@MainActivity }
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
