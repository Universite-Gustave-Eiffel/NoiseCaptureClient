package org.noiseplanet.noisecapture

import App
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.ui.graphics.toArgb
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate
import org.noiseplanet.noisecapture.permission.toPermission
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.ui.theme.OnSurfaceLight
import org.noiseplanet.noisecapture.ui.theme.SurfaceLight
import org.noiseplanet.noisecapture.util.AndroidNotificationProvider
import org.noiseplanet.noisecapture.util.NotificationProvider

/**
 * Android app entry point
 */
@SuppressLint("SourceLockedOrientationActivity")
class MainActivity : ComponentActivity() {

    // - Properties

    private lateinit var permissionService: PermissionService
    private lateinit var logger: Logger

    private lateinit var filePickerEventBus: AndroidFilePickerEventBus
    private var filePickerIntentLauncher: ActivityResultLauncher<Intent>? = null

    private val scope = CoroutineScope(Dispatchers.Main)


    // - Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = SurfaceLight.toArgb(),
                darkScrim = OnSurfaceLight.toArgb(),
            )
        )

        initKoin(
            additionalModules = listOf(
                module {
                    single<Context> { applicationContext }
                    single<Activity> { this@MainActivity }
                    single<NotificationProvider> { AndroidNotificationProvider() }
                },
                platformModule
            )
        )

        setContent {
            // Lock orientation on phones only (i.e. devices with compact width or height)
            val sizeClas = currentWindowAdaptiveInfoV2().windowSizeClass
            val isCompact = sizeClas.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND ||
                sizeClas.minHeightDp < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND

            if (isCompact) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            App()
        }

        permissionService = get()
        logger = get()
        filePickerEventBus = get()

        subscribeToFilePickerEventBus()
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


    // - Private functions

    /**
     * Listen for new file picker events and launch file picker intent on new events.
     */
    private fun subscribeToFilePickerEventBus() {
        // Hold reference to pending file
        var pendingEvent: FilePickerEvent? = null

        // Prepare file picker intent launcher
        filePickerIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        // Output stream is open, read bytes from local storage and write
                        // to destination URI
                        pendingEvent?.let { outputStream.write(it.file.readBytes()) }
                    }
                }
            }
            // If needed, delete the file once the picker is dismissed
            pendingEvent?.apply {
                if (deleteAfterUse) {
                    file.delete()
                }
            }
            // Drop reference
            pendingEvent = null
        }

        // Listen to file picker events coming from event bus
        scope.launch {
            filePickerEventBus.events.collect { event ->
                // Prepare intent with file path, and launch file picker
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, event.file.name)
                }
                pendingEvent = event
                filePickerIntentLauncher?.launch(intent)
            }
        }
    }
}
