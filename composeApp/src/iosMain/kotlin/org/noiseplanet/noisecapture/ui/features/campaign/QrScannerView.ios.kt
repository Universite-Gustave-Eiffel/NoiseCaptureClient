@file:OptIn(ExperimentalForeignApi::class)

package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_scan_dialog_ios_title
import noisecapture.composeapp.generated.resources.campaign_scan_not_available_dialog_body_ios
import noisecapture.composeapp.generated.resources.campaign_scan_not_available_dialog_title
import noisecapture.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialog
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialogViewModel
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetMedium
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

@Composable
actual fun QrScannerView(
    onResult: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    // - Properties

    var permissionGranted: Boolean by remember { mutableStateOf(false) }
    val logger: Logger = koinInject()


    // - Lifecycle

    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> permissionGranted = true

            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
                    permissionGranted = it
                }
            }

            else -> permissionGranted = false
        }
    }


    // - Layout

    if (permissionGranted) {
        val controller = remember { QrScannerController(onResult, logger) }

        NCDialog(
            title = {
                Text(text = stringResource(Res.string.campaign_scan_dialog_ios_title))
            },
            text = {
                UIKitView(
                    factory = { QrScannerUIView(controller.session) },
                    onRelease = { controller.stop() },
                    update = { view -> view.syncPreviewFrame() },
                    modifier = modifier.height(300.dp)
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(16.dp)),
                )
            },
            confirmButton = {
                NCButton(
                    content = ButtonContent(title = Res.string.cancel),
                    colors = Color.Noise.two.transparentContainerColors(),
                    onClick = onDismissRequest,
                )
            },
            onDismissRequest = onDismissRequest,
        )

        LaunchedEffect(Unit) { controller.start() }

    } else {
        NCDialog(
            viewModel = NCDialogViewModel(
                title = Res.string.campaign_scan_not_available_dialog_title,
                text = Res.string.campaign_scan_not_available_dialog_body_ios,
                containerColors = Color.Noise.six.transparentContainerColors(),
                dismissButtonContent = ButtonContent(title = Res.string.cancel),
                dismissButtonColors = Color.Noise.six.transparentContainerColors(),
            ),
            onDismissRequest = onDismissRequest
        )
    }
}


private class QrScannerUIView(
    session: AVCaptureSession,
) : UIView(frame = CGRectZero.readValue()) {

    // - Properties

    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)


    // - Lifecycle

    init {
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.frame = bounds
    }


    // - Public functions

    fun syncPreviewFrame() {
        previewLayer.setFrame(bounds)
    }
}


private class QrScannerController(
    onResult: (String) -> Unit,
    private val logger: Logger,
) {

    // - Properties

    private val queue = dispatch_queue_create("qr.scanner", null)
    private val delegate = MetadataDelegate(onResult)

    val session = AVCaptureSession()


    // - Lifecycle

    init {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
        val output = AVCaptureMetadataOutput()

        session.beginConfiguration()
        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
        }
        if (session.canAddOutput(output)) {
            session.addOutput(output)
        }
        session.sessionPreset = AVCaptureSessionPresetMedium
        session.commitConfiguration()

        output.setMetadataObjectsDelegate(delegate, queue)
        output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
    }


    // - Public functions

    fun start() {
        logger.debug("Starting video capture session")
        dispatch_async(queue) {
            session.startRunning()
        }
    }

    fun stop() {
        logger.debug("Stopping video capture session")
        dispatch_async(queue) {
            session.stopRunning()
        }
    }
}


private class MetadataDelegate(
    private val onResult: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        (didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject)
            ?.stringValue
            ?.let { code ->
                dispatch_async(dispatch_get_main_queue()) {
                    onResult(code)   // hop to main before touching Compose state
                }
            }
    }
}
