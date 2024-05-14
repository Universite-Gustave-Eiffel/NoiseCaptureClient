package org.noise_planet.noisecapture.shared.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import org.noise_planet.noisecapture.shared.FFT_SIZE
import org.noise_planet.noisecapture.shared.signal.SpectrumData
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


/**
 * Convert FFT result into spectrogram bitmap bytearray
 */
class SpectrogramBitmap {
    companion object {
        val bmpHeader = intArrayOf( // All values are little-endian
            0x42, 0x4D,             // Signature 'BM'
            0xaa, 0x00, 0x00, 0x00, // Size: 170 bytes
            0x00, 0x00,             // Unused
            0x00, 0x00,             // Unused
            0x8a, 0x00, 0x00, 0x00, // Offset to image data
            0x7c, 0x00, 0x00, 0x00, // DIB header size (124 bytes)
            0x04, 0x00, 0x00, 0x00, // Width (4px)
            0x02, 0x00, 0x00, 0x00, // Height (2px)
            0x01, 0x00,             // Planes (1)
            0x20, 0x00,             // Bits per pixel (32)
            0x03, 0x00, 0x00, 0x00, // Format (bitfield = use bitfields | no compression)
            0x20, 0x00, 0x00, 0x00, // Image raw size (32 bytes)
            0x13, 0x0B, 0x00, 0x00, // Horizontal print resolution (2835 = 72dpi * 39.3701)
            0x13, 0x0B, 0x00, 0x00, // Vertical print resolution (2835 = 72dpi * 39.3701)
            0x00, 0x00, 0x00, 0x00, // Colors in palette (none)
            0x00, 0x00, 0x00, 0x00, // Important colors (0 = all)
            0x00, 0x00, 0xFF, 0x00, // R bitmask (00FF0000)
            0x00, 0xFF, 0x00, 0x00, // G bitmask (0000FF00)
            0xFF, 0x00, 0x00, 0x00, // B bitmask (000000FF)
            0x00, 0x00, 0x00, 0xFF, // A bitmask (FF000000)
            0x42, 0x47, 0x52, 0x73, // sRGB color space
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Unused R, G, B entries for color space
            0x00, 0x00, 0x00, 0x00, // Unused Gamma X entry for color space
            0x00, 0x00, 0x00, 0x00, // Unused Gamma Y entry for color space
            0x00, 0x00, 0x00, 0x00, // Unused Gamma Z entry for color space
            0x00, 0x00, 0x00, 0x00, // Unknown
            0x00, 0x00, 0x00, 0x00, // Unknown
            0x00, 0x00, 0x00, 0x00, // Unknown
            0x00, 0x00, 0x00, 0x00 // Unknown
            // Image data after this
        ).map { it.toByte() }.toByteArray()

        val sizeIndex = 2
        val widthIndex = 18
        val heightIndex = 22
        val rawSizeIndex = 34

        fun parseColor(colorString: String): Int {
            var color = colorString.substring(1).toLong(16)
            if (colorString.length == 7) {
                // Set the alpha value
                color = color or 0x00000000ff000000L
            } else if (colorString.length != 9) {
                throw IllegalArgumentException("Unknown color")
            }
            return color.toInt()
        }

        fun String.toComposeColor(): Color {
            return Color(parseColor(this))
        }

        enum class SCALE_MODE {
            SCALE_LINEAR,
            SCALE_LOG
        }

        val frequencyLegendPositionLog =
            intArrayOf(63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000, 24000)

        val frequencyLegendPositionLinear = IntArray(24) { it * 1000 + 1000}

        val colorRamp = arrayOf(
            "#303030".toComposeColor(),
            "#2D3C2D".toComposeColor(),
            "#2A482A".toComposeColor(),
            "#275427".toComposeColor(),
            "#246024".toComposeColor(),
            "#216C21".toComposeColor(),
            "#3F8E19".toComposeColor(),
            "#61A514".toComposeColor(),
            "#82BB0F".toComposeColor(),
            "#A4D20A".toComposeColor(),
            "#C5E805".toComposeColor(),
            "#E7FF00".toComposeColor(),
            "#EBD400".toComposeColor(),
            "#EFAA00".toComposeColor(),
            "#F37F00".toComposeColor(),
            "#F75500".toComposeColor(),
            "#FB2A00".toComposeColor(),
        )
        fun createSpectrogram(size: IntSize, scaleMode: SCALE_MODE, sampleRate: Double) : SpectrogramDataModel {
            val byteArray = ByteArray(bmpHeader.size + Int.SIZE_BYTES * size.width * size.height)
            bmpHeader.copyInto(byteArray)
            // fill with changing header data
            val rawPixelSize = size.width*size.height*Int.SIZE_BYTES
            rawPixelSize.toLittleEndianBytes().copyInto(byteArray, rawSizeIndex)
            (rawPixelSize+bmpHeader.size).toLittleEndianBytes().copyInto(byteArray, sizeIndex)
            size.width.toLittleEndianBytes().copyInto(byteArray, widthIndex)
            size.height.toLittleEndianBytes().copyInto(byteArray, heightIndex)
            return SpectrogramDataModel(size, byteArray, scaleMode = scaleMode, sampleRate = sampleRate)
        }

        /**
         * Convert Int into little endian array of bytes
         */
        fun Int.toLittleEndianBytes() : ByteArray = byteArrayOf(this.toByte(), this.ushr(8).toByte(),
            this.ushr(16).toByte(), this.ushr(24).toByte())

    }

    /**
     * @constructor
     * @si
     */
    data class SpectrogramDataModel(val size: IntSize, val byteArray: ByteArray,
                                    var offset : Int = 0, val scaleMode: SCALE_MODE,
                                    val sampleRate: Double) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as SpectrogramDataModel

            if (size != other.size) return false
            return byteArray.contentEquals(other.byteArray)
        }

        override fun hashCode(): Int {
            var result = size.hashCode()
            result = 31 * result + byteArray.contentHashCode()
            return result
        }

        fun pushSpectrumToSpectrogramData(fftResult : SpectrumData,
                                           mindB : Double, rangedB : Double, gain : Double) {
            // generate columns of pixels
            // merge power of each frequencies following the destination bitmap resolution
            val hertzBySpectrumCell = sampleRate / FFT_SIZE.toDouble()
            val frequencyLegendPosition = when (scaleMode) {
                SCALE_MODE.SCALE_LOG -> frequencyLegendPositionLog
                else -> frequencyLegendPositionLinear
            }
            var lastProcessFrequencyIndex = 0
            val freqByPixel = fftResult.spectrum.size / size.height.toDouble()
            for (pixel in 0..<size.height) {
                var freqStart: Int
                var freqEnd: Int
                if (scaleMode == SCALE_MODE.SCALE_LOG) {
                    freqStart = lastProcessFrequencyIndex
                    val fMax = sampleRate / 2
                    val fMin = frequencyLegendPosition[0]
                    val r = fMax / fMin.toDouble()
                    val f = fMin * 10.0.pow(pixel * log10(r) / size.height)
                    val nextFrequencyIndex =
                        min(fftResult.spectrum.size, (f / hertzBySpectrumCell).toInt())
                    freqEnd =
                        min(fftResult.spectrum.size, (f / hertzBySpectrumCell).toInt() + 1)
                    lastProcessFrequencyIndex = min(fftResult.spectrum.size, nextFrequencyIndex)
                } else {
                    freqStart = floor(pixel * freqByPixel).toInt()
                    freqEnd = min(
                        (pixel + 1) * freqByPixel,
                        fftResult.spectrum.size.toDouble()
                    ).toInt()
                }
                var sumVal = 0.0
                for (idFreq in freqStart..<freqEnd) {
                    sumVal += 10.0.pow(fftResult.spectrum[idFreq] / 10.0)
                }
                sumVal = max(0.0, 10 * log10(sumVal/(freqEnd-freqStart)) + gain)
                val colorIndex = min(
                    colorRamp.size - 1, max(
                        0, (((sumVal - mindB) / rangedB) *
                                colorRamp.size).toInt()
                    )
                )
                val pixelColor = colorRamp[colorIndex].toArgb()
                val columnOffset = offset % size.width
                val pixelIndex = bmpHeader.size + size.width * Int.SIZE_BYTES *
                        pixel + columnOffset * Int.SIZE_BYTES
                pixelColor.toLittleEndianBytes().copyInto(byteArray, pixelIndex)
            }
            offset += 1
        }
    }
}
