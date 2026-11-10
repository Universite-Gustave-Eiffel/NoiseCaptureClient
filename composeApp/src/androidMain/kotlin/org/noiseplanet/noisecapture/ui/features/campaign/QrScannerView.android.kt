package org.noiseplanet.noisecapture.ui.features.campaign

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_scan_not_available_dialog_body_android
import noisecapture.composeapp.generated.resources.campaign_scan_not_available_dialog_title
import noisecapture.composeapp.generated.resources.cancel
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialog
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialogViewModel
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
actual fun QrScannerView(
    onResult: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    // - Properties

    val context: Context = LocalContext.current
    val logger: Logger = koinInject()

    var showFeatureUnavailableDialog: Boolean by remember { mutableStateOf(false) }


    // - Lifecycle

    LaunchedEffect(Unit) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode -> barcode.rawValue?.let { onResult(it) } }
            .addOnFailureListener { error ->
                logger.error("Error while opening code scanner", error)
                showFeatureUnavailableDialog = true
            }
    }


    // - Layout

    if (showFeatureUnavailableDialog) {
        NCDialog(
            viewModel = NCDialogViewModel(
                title = Res.string.campaign_scan_not_available_dialog_title,
                text = Res.string.campaign_scan_not_available_dialog_body_android,
                containerColors = Color.Noise.six.secondaryContainerColors(hasDropShadow = true),
                dismissButtonContent = ButtonContent(title = Res.string.cancel),
                dismissButtonColors = Color.Noise.six.transparentContainerColors()
            ),
            onDismissRequest = onDismissRequest
        )
    }
}
