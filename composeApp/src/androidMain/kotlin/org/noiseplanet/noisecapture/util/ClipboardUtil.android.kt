package org.noiseplanet.noisecapture.util

import android.content.ClipData
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry

actual suspend fun Clipboard.setClipEntry(text: String) {
    ClipData.newPlainText("NoiseCapture", text).toClipEntry()
    setClipEntry(text)
}
