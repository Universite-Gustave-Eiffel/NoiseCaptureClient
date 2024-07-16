package org.noiseplanet.noisecapture.starter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.bumble.appyx.navigation.integration.NodeActivity
import com.bumble.appyx.navigation.integration.NodeHost
import com.bumble.appyx.navigation.platform.AndroidLifecycle
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.noiseplanet.noisecapture.AndroidAudioSource
import org.noiseplanet.noisecapture.AndroidDatabase
import org.noiseplanet.noisecapture.AndroidMeasurementService
import org.noiseplanet.noisecapture.DatabaseDriverFactory
import org.noiseplanet.noisecapture.shared.MeasurementService
import org.noiseplanet.noisecapture.shared.initKoin
import org.noiseplanet.noisecapture.shared.root.RootNode
import org.noiseplanet.noisecapture.shared.ui.theme.AppyxStarterKitTheme
import kotlin.reflect.KProperty

class MainActivity : NodeActivity() {

    private val androidLogger = AndroidLogger()
    private val foregroundServiceConnection = ForegroundServiceConnection()

    internal class ForegroundServiceConnection : ServiceConnection {

        @SuppressLint("MissingPermission")
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("onServiceConnected $name $service")
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            println("onServiceConnected $name")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopKoin()
    }

    override fun onRestart() {
        super.onRestart()
        println("onRestart")
    }

    override fun onStop() {
        super.onStop()
        println("OnStop")
    }

    @Suppress("UnusedParameter")
    private fun onStorageStateChange(property: KProperty<*>, oldValue: Boolean, newValue: Boolean) {
        // bind this application context to Android Foreground service if storage is launched
        // in order to avoid application shutdown by Android when moved in background
        if (newValue) {
            val intent = Intent(applicationContext, AndroidMeasurementService::class.java)
            if (applicationContext.bindService(
                    intent, foregroundServiceConnection,
                    Context.BIND_AUTO_CREATE
                )
            ) {
                androidLogger.info("Bind with foreground service")
            } else {
                androidLogger.info("Can't bind with foreground service")
            }
        } else {
            applicationContext.unbindService(foregroundServiceConnection)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val koinApplication = initKoin(
            additionalModules = listOf(
                module {
                    single<Context> { applicationContext }
                    single<Activity> { this@MainActivity }
                    single<MeasurementService> {
                        val measurementService =
                            MeasurementService(AndroidAudioSource(logger))
                        measurementService.storageObservers.add(::onStorageStateChange)
                        measurementService
                    }
                    single<DatabaseDriverFactory> { AndroidDatabase(applicationContext) }
                }
            )
        ).logger(androidLogger)
        setContent {
            AppyxStarterKitTheme {
                NodeHost(
                    lifecycle = AndroidLifecycle(LocalLifecycleOwner.current.lifecycle),
                    integrationPoint = appyxIntegrationPoint,
                ) {
                    RootNode(nodeContext = it, koin = koinApplication.koin)
                }
            }
        }
    }
}
