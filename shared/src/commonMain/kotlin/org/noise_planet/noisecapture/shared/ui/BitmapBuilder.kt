package org.noise_planet.noisecapture.shared.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.jetbrains.skia.*

interface BitmapBuilderScope {
    operator fun set(index: Int, color: Color)
    operator fun set(x: Int, y: Int, color: Color)
}

fun buildBitmap(width: Int, height: Int, block: BitmapBuilderScope.() -> Unit = {}): Bitmap {
    val builder = BitmapBuilder(width, height)
    block(builder)
    return builder.build()
}

/**
 * https://avwie.github.io/mandelbrot-in-compose-multiplatform
 * Generate bitmap for multiplatform thanks to skia/skiko
 */
class BitmapBuilder(private val width: Int, private val height: Int): BitmapBuilderScope {
    private val bytes = ByteArray(width * height * ColorType.RGBA_8888.bytesPerPixel)

    override fun set(index: Int, color: Color) {
        val argb = color.toArgb()
        val pixelPos = index * ColorType.RGBA_8888.bytesPerPixel
        bytes[pixelPos] = argb.shr(16).and(0xFF).toByte() // Extract red component
        bytes[pixelPos + 1] = argb.shr(8).and(0xFF).toByte() // Extract green component
        bytes[pixelPos + 2] = argb.and(0xFF).toByte() // Extract blue component
        bytes[pixelPos + 3] = 0xFF.toByte() // Set alpha component as opaque
    }

    override operator fun set(x: Int, y: Int, color: Color) {
        require(x in 0 until width)
        require(y in 0 until height)
        val index = y * width + x
        set(index, color)
    }

    fun build(): Bitmap {
        val bitmap = Bitmap()
        val info = ImageInfo(
            colorInfo = ColorInfo(
                colorType = ColorType.RGBA_8888,
                alphaType = ColorAlphaType.PREMUL,
                colorSpace = ColorSpace.sRGB
            ),
            width = width,
            height = height
        )
        bitmap.allocPixels(info)
        bitmap.installPixels(bytes)
        return bitmap
    }
}