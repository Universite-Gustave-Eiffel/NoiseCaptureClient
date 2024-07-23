package org.noiseplanet.noisecapture.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Converts a byte array to a compose ImageBitmap using platform specific implementations
 */
expect fun ByteArray.toImageBitmap(): ImageBitmap
