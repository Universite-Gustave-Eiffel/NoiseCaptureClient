package org.noiseplanet.noisecapture.util

import androidx.compose.ui.platform.ClipEntry

actual fun clipEntry(string: String): ClipEntry? {
    return ClipEntry.withPlainText(string)
}
