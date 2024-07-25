package org.noiseplanet.noisecapture.audio.signal

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
class BluesteinFloat(private val windowLength: Int) {

    companion object {

        const val PIF = PI.toFloat()

        operator fun Float.plus(other: Complex) = Complex(this + other.real, other.imag)
        operator fun Float.minus(other: Complex) = Complex(this - other.real, -other.imag)
        operator fun Float.times(other: Complex) = Complex(
            this * other.real - this * other.imag,
            this * other.imag + this * other.real
        )

        operator fun Float.div(other: Complex) = Complex(this, 0.0F) / other

        @Suppress("TooManyFunctions")
        data class Complex(val real: Float, val imag: Float) {
            operator fun plus(other: Complex) = Complex(real + other.real, imag + other.imag)
            operator fun minus(other: Complex) = Complex(real - other.real, imag - other.imag)
            operator fun times(other: Complex) = Complex(
                real * other.real - imag * other.imag,
                real * other.imag + imag * other.real
            )

            operator fun times(d: Float): Complex = Complex(real * d, imag * d)

            operator fun div(c: Float): Complex = Complex(real / c, imag / c)
            operator fun div(other: Complex) = Complex(
                (real * other.real + imag * other.imag) / (other.real * other.real + other.imag * other.imag),
                (imag * other.real - real * other.imag) / (other.real * other.real + other.imag * other.imag)
            )

            fun module(): Float = sqrt(real.pow(2) + imag.pow(2))
            fun arg(): Float {
                return when {
                    this.real == 0.0F && this.imag > 0.0 -> PIF / 2
                    this.real == 0.0F && this.imag < 0.0 -> -PIF / 2
                    this.real > 0.0F -> atan(this.imag / this.real)
                    this.real < 0.0F && this.imag >= 0.0 -> atan(this.imag / this.real) + PIF
                    this.real < 0.0F && this.imag < 0.0 -> atan(this.imag / this.real) - PIF
                    else -> 0.0F
                }
            }

            fun ln(): Complex {
                return Complex(ln(this.module()), arg())
            }

            fun exp(): Complex {
                val r: Float = exp(this.real)
                return Complex(r * cos(this.imag), r * sin(this.imag))
            }

            fun pow(c: Complex): Complex = (c * this.ln()).exp()
            fun pow(c: Float): Complex = (this.ln() * c).exp()

            operator fun unaryMinus() = Complex(-real, -imag)

            override fun toString() = "$real ${if (imag >= 0) '+' else ' '} ${imag}i"
        }

        val Float.im get() = Complex(0.0F, this)
    }

    val n = windowLength
    val m = n
    val w = (Complex(0.0F, -2.0F) * PIF / m.toFloat()).exp()
    val a = 1.0F
    val chirp = ((1 - n)..<max(m, n)).foldIndexed(
        FloatArray(
            (max(
                m,
                n
            ) - (1 - n)) * 2
        )
    ) { index, realImagArray, i ->
        val c = w.pow(i.toFloat().pow(2) / 2.0F)
        realImagArray[index * 2] = c.real
        realImagArray[index * 2 + 1] = c.imag
        realImagArray
    }
    val n2 = nextPowerOfTwo(m + n - 1)
    val ichirp = (0..<n2).foldIndexed(FloatArray(n2 * 2)) { index, realImagArray, i ->
        if (i < m + n - 1) {
            val c = 1.0F / Complex(chirp[index * 2], chirp[index * 2 + 1])
            realImagArray[index * 2] = c.real
            realImagArray[index * 2 + 1] = c.imag
        }
        realImagArray
    }

    init {
        fftFloat(ichirp.size / 2, ichirp)
    }

    fun fft(x : FloatArray) : FloatArray {
        val inputIm = x.size == windowLength * 2
        val xp = (0..<n2).foldIndexed(FloatArray(n2 * 2)) { index, realImagArray, i ->
            if (i < n) {
                val realIndex = if (inputIm) index * 2 else index
                val imIndex = index * 2 + 1
                val chirpOffset = (n - 1) * 2
                val c = Complex(
                    x[realIndex],
                    if (inputIm) x[imIndex] else 0F
                ) * a.pow(-i) * Complex(chirp[chirpOffset + realIndex], chirp[chirpOffset + imIndex])
                realImagArray[index * 2] = c.real
                realImagArray[index * 2 + 1] = c.imag
            }
            realImagArray
        }
        fftFloat(xp.size/2, xp)
        val r =  (0..< n2).foldIndexed(FloatArray(n2*2)) {
                index, realImagArray, i ->
            val realIndex = index*2
            val imIndex = index*2+1
            val c = Complex(xp[realIndex], xp[imIndex]) * Complex(ichirp[realIndex], ichirp[imIndex])
            realImagArray[index*2] = c.real
            realImagArray[index*2+1] = c.imag
            realImagArray
        }
        iFFTFloat(r.size/2, r)
        return (n-1..< m+n-1).foldIndexed(FloatArray(if(inputIm) windowLength*2 else windowLength)) {
                index, realImagArray, i ->
            val realIndex = i*2
            val imIndex = i*2+1
            val c = Complex(r[realIndex], r[imIndex]) * Complex(chirp[realIndex], chirp[imIndex])
            if(inputIm) {
                realImagArray[index * 2] = c.real
                realImagArray[index * 2 + 1] = c.imag
            } else {
                realImagArray[index] = c.real
            }
            realImagArray
        }
    }
}
