package org.noiseplanet.noisecapture.audio.signal.bluestein

import org.noiseplanet.noisecapture.audio.signal.fft.iFFT
import org.noiseplanet.noisecapture.audio.signal.fft.nextPowerOfTwo
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Chirp transform for non power of two fft
 *
 * [Details](https://gist.github.com/fnielsen/99b981b9da34ae3d5035)
 */
class Bluestein(private val windowLength: Int) {

    companion object {

        operator fun Double.plus(other: Complex) = Complex(this + other.real, other.imag)
        operator fun Double.minus(other: Complex) = Complex(this - other.real, -other.imag)
        operator fun Double.times(other: Complex) = Complex(
            this * other.real - this * other.imag,
            this * other.imag + this * other.real
        )

        operator fun Double.div(other: Complex) = Complex(
            (this * other.real) /
                (other.real * other.real + other.imag * other.imag),
            (-this * other.imag) / (other.real * other.real + other.imag * other.imag)
        )

        @Suppress("TooManyFunctions")
        data class Complex(val real: Double, val imag: Double) {

            operator fun plus(other: Complex) = Complex(real + other.real, imag + other.imag)
            operator fun minus(other: Complex) = Complex(real - other.real, imag - other.imag)
            operator fun times(other: Complex) = Complex(
                real * other.real - imag * other.imag,
                real * other.imag + imag * other.real
            )

            operator fun times(d: Double): Complex = Complex(real * d, imag * d)

            operator fun div(c: Double): Complex = Complex(real / c, imag / c)
            operator fun div(other: Complex) = Complex(
                (real * other.real + imag * other.imag) / (other.real * other.real + other.imag * other.imag),
                (imag * other.real - real * other.imag) / (other.real * other.real + other.imag * other.imag)
            )

            fun module(): Double = sqrt(real.pow(2) + imag.pow(2))
            fun arg(): Double {
                return when {
                    this.real == 0.0 && this.imag > 0.0 -> PI / 2
                    this.real == 0.0 && this.imag < 0.0 -> -PI / 2
                    this.real > 0.0 -> atan(this.imag / this.real)
                    this.real < 0.0 && this.imag >= 0.0 -> atan(this.imag / this.real) + PI
                    this.real < 0.0 && this.imag < 0.0 -> atan(this.imag / this.real) - PI
                    else -> 0.0
                }
            }

            fun ln(): Complex {
                return Complex(ln(this.module()), arg())
            }

            fun exp(): Complex {
                val r: Double = exp(this.real)
                return Complex(r * cos(this.imag), r * sin(this.imag))
            }

            fun pow(c: Complex): Complex = (c * this.ln()).exp()
            fun pow(c: Double): Complex = (this.ln() * c).exp()

            operator fun unaryMinus() = Complex(-real, -imag)

            override fun toString() = "$real ${if (imag >= 0) '+' else ' '} ${imag}i"
        }

        val Double.im get() = Complex(0.0, this)
    }

    val n = windowLength
    val m = n
    val w = (Complex(0.0, -2.0) * PI / m.toDouble()).exp()
    val a = 1.0
    val chirp = ((1 - n)..<max(m, n)).foldIndexed(
        DoubleArray((max(m, n) - (1 - n)) * 2)
    ) { index, realImagArray, i ->
        val c = w.pow(i.toDouble().pow(2) / 2.0)
        realImagArray[index * 2] = c.real
        realImagArray[index * 2 + 1] = c.imag
        realImagArray
    }
    val n2 = nextPowerOfTwo(m + n - 1)
    val ichirp = (0..<n2).foldIndexed(DoubleArray(n2 * 2)) { index, realImagArray, i ->
        if (i < m + n - 1) {
            val c = 1.0 / Complex(chirp[index * 2], chirp[index * 2 + 1])
            realImagArray[index * 2] = c.real
            realImagArray[index * 2 + 1] = c.imag
        }
        realImagArray
    }

    init {
        org.noiseplanet.noisecapture.audio.signal.fft.fft(ichirp.size / 2, ichirp)
    }

    fun fft(x: DoubleArray): DoubleArray {
        require(x.size == windowLength * 2)
        val xp = (0..<n2).foldIndexed(DoubleArray(n2 * 2)) { index, realImagArray, i ->
            if (i < n) {
                val realIndex = index * 2
                val imIndex = index * 2 + 1
                val chirpOffset = (n - 1) * 2
                val c = Complex(x[realIndex], x[imIndex]) * Complex(
                    chirp[chirpOffset + realIndex],
                    chirp[chirpOffset + imIndex]
                )
                realImagArray[index * 2] = c.real
                realImagArray[index * 2 + 1] = c.imag
            }
            realImagArray
        }
        org.noiseplanet.noisecapture.audio.signal.fft.fft(xp.size / 2, xp)
        val r = (0..<n2).foldIndexed(DoubleArray(n2 * 2)) { index, realImagArray, i ->
            val realIndex = index * 2
            val imIndex = index * 2 + 1
            val c =
                Complex(xp[realIndex], xp[imIndex]) * Complex(ichirp[realIndex], ichirp[imIndex])
            realImagArray[index * 2] = c.real
            realImagArray[index * 2 + 1] = c.imag
            realImagArray
        }
        iFFT(r.size / 2, r)
        return (n - 1..<m + n - 1).foldIndexed(DoubleArray(windowLength * 2)) { index, realImagArray, i ->
            val realIndex = i * 2
            val imIndex = i * 2 + 1
            val c = Complex(r[realIndex], r[imIndex]) * Complex(chirp[realIndex], chirp[imIndex])
            realImagArray[index * 2] = c.real
            realImagArray[index * 2 + 1] = c.imag
            realImagArray
        }
    }
}
