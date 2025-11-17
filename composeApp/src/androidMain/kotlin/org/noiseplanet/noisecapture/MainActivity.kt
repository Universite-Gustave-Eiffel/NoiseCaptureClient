package org.noiseplanet.noisecapture

import App
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate
import org.noiseplanet.noisecapture.permission.toPermission
import org.noiseplanet.noisecapture.services.permission.PermissionService

/**
 * Android app entry point
 */
class MainActivity : ComponentActivity() {

    // - Properties

    private lateinit var permissionService: PermissionService
    private lateinit var logger: Logger


    // - Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        initKoin(
            additionalModules = listOf(
                module {
                    single<Context> { applicationContext }
                    single<Activity> { this@MainActivity }
                },
                platformModule
            )
        )

        setContent {
            // Lock orientation on phones only (i.e. devices with compact width or height)
            val sizeClas = currentWindowAdaptiveInfo().windowSizeClass
            val isCompact = sizeClas.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND ||
                sizeClas.minHeightDp < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND

            if (isCompact) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            App()
        }

        permissionService = get()
        logger = get()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int,
    ) {
        logger.debug("PERMISSION RESULT:")
        logger.debug("  - permissions: ${permissions.toList()}")
        logger.debug("  - grantResults: ${grantResults.toList()}")

        val permission = permissions.toList().filterNotNull().toPermission() ?: return
        val delegate: PermissionDelegate = get(named(permission.name))
        delegate.checkPermissionState()
    }
}
