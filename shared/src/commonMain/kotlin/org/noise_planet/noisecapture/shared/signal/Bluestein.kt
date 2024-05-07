package org.noise_planet.noisecapture.shared.signal

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Bluestein(val window_length : Int) {
    companion object {
        operator fun Double.plus(other: Complex) = Complex(this + other.real, other.imag)
        operator fun Double.minus(other: Complex) = Complex(this - other.real, -other.imag)
        operator fun Double.times(other: Complex) = Complex(
            this * other.real - this * other.imag,
            this * other.imag + this * other.real
        )
        operator fun Double.div(other: Complex) = Complex(
            (this * other.real + this * other.imag) / (other.real * other.real + other.imag * other.imag),
            (this * other.imag - this * other.real) / (other.real * other.real + other.imag * other.imag)
        )

        data class Complex(val real: Double, val imag: Double) {
            operator fun plus(other: Complex) = Complex(real + other.real, imag + other.imag)
            operator fun minus(other: Complex) = Complex(real - other.real, imag - other.imag)
            operator fun times(other: Complex) = Complex(
                real * other.real - imag * other.imag,
                real * other.imag + imag * other.real
            )
            operator fun times(d: Double):Complex =Complex(real * d, imag * d )

            operator fun div(c: Double):Complex  = Complex(real / c, imag / c)
            operator fun div(other: Complex) = Complex(
                (real * other.real + imag * other.imag) / (other.real * other.real + other.imag * other.imag),
                (imag * other.real - real * other.imag) / (other.real * other.real + other.imag * other.imag)
            )

            fun module():Double = sqrt(real.pow(2) + imag.pow(2))
            fun arg(): Double {
                return when  {
                    this.real==0.0 && this.imag>0.0 -> PI/2
                    this.real==0.0 && this.imag<0.0 -> -PI/2
                    this.real>0.0  -> atan(this.imag/this.real)
                    this.real<0.0 && this.imag>=0.0 -> atan(this.imag/this.real)+ PI
                    this.real<0.0 && this.imag<0.0 -> atan(this.imag/this.real)-PI
                    else ->  0.0
                }
            }
            fun ln():Complex  {
                return Complex(ln(this.module()),arg())
            }

            fun exp():Complex  {
                val r: Double = exp(this.real)
                return Complex(r*cos(this.imag),r*sin(this.imag))
            }
            fun pow(c:Complex):Complex = (c*this.ln()).exp()
            fun pow(c:Double):Complex = (this.ln()*c).exp()

            operator fun unaryMinus() = Complex(-real, -imag)

            override fun toString() = "$real ${if(imag>=0)'+' else ' '} ${imag}i"
        }

        val Double.im get() = Complex(0.0, this)
    }
    val n = window_length
    val m = n
    val w = (Complex(0.0, -2.0) * PI / m.toDouble()).exp()
    val chirp = ((1-n)..<max(m, n)).foldIndexed(DoubleArray((max(m, n)-(1 - n))*2)) {
        index, realImagArray, i -> val c = w.pow(i.toDouble().pow(2) / 2.0)
        realImagArray[index*2] = c.real
        realImagArray[index*2+1] = c.imag
        realImagArray
    }
}