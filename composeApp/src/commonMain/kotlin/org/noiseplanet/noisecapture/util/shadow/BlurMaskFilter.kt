package org.noiseplanet.noisecapture.util.shadow

import androidx.compose.ui.graphics.NativePaint

/**
 * Sets the `maskFilter` property to a blur mask filter with the given radius, based on the
 * current platform.
 */
expect fun NativePaint.setBlurMaskFilter(blurRadius: Float)
