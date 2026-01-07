package org.noiseplanet.noisecapture.util

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun clipEntry(string: String): ClipEntry? {
    return ClipData.newPlainText("NoiseCapture", string).toClipEntry()
}
