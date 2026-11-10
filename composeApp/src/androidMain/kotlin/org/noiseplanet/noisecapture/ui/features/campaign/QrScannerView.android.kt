package org.noiseplanet.noisecapture.ui.features.campaign

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.log.Logger

@Composable
actual fun QrScannerView(
    onResult: (String) -> Unit,
    modifier: Modifier,
) {
    // - Properties

    val context: Context = LocalContext.current
    val logger: Logger = koinInject()


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
            }
    }
}
