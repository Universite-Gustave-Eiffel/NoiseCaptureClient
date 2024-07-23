@file:Suppress("LongMethod")

package org.noiseplanet.noisecapture.audio.signal

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin

/**
 * Adapted from:
 * https://jenshee.dk/signalprocessing/realfft.pdf
 * @link https://github.com/Grubleren
 */


/**
 * The bitwise AND operation of the number and its predecessor (number - 1) should result in 0.
 * This is because powers of two in binary have only one bit set, and subtracting 1 from them flips
 * that bit and sets all the lower bits to 1. So, the bitwise AND with the original number should
 * be 0 if it is a power of two.
 */
fun isPowerOfTwo(number: Int): Boolean {
    return number > 0 && (number and (number - 1)) == 0
}

// TODO: Add documentation and move to utility folder along with isPowerOfTwo
fun nextPowerOfTwo(number: Int) = 2.0.pow(ceil(log2(number.toDouble()))).toInt()

/**
 * Inplace fft
 * @param length Size of real+imaginary to process
 * @param riArray real and imaginary array ex: first real[0] then first imaginary [1], second real[2] then second imaginary [3]..
 */
fun fft(length: Int, riArray: DoubleArray) {
    require(isPowerOfTwo(length))
    val m = log2(length.toDouble()).toInt()
    val n = (2.0.pow(m) + 0.5).toInt()
    require(n <= riArray.size)
    var wCos: Double
    var wSin: Double
    val n2 = n shr 1
    var l1 = n
    var l2: Int

    repeat(m) {
        l2 = l1 shr 1
        wCos = 1.0
        wSin = 0.0
        val uCos = cos(PI / l2)
        val uSin = -sin(PI / l2)

        for (j in 0 until l2) {
            for (i in j until n step l1) {
                val ir = 2 * i
                val ii = ir + 1
                val l22 = 2 * l2
                val sumRe = riArray[ir] + riArray[ir + l22]
                val sumIm = riArray[ii] + riArray[ii + l22]
                val diffRe = riArray[ir] - riArray[ir + l22]
                val diffIm = riArray[ii] - riArray[ii + l22]
                riArray[ir + l22] = diffRe * wCos - diffIm * wSin
                riArray[ii + l22] = diffRe * wSin + diffIm * wCos
                riArray[ir] = sumRe
                riArray[ii] = sumIm
            }
            val w = wCos * uCos - wSin * uSin
            wSin = wCos * uSin + wSin * uCos
            wCos = w
        }
        l1 = l1 shr 1
    }

    var k: Int
    var j = 0
    val n1 = n - 1

    for (i in 0 until n1) {
        if (i < j) {
            val jr = 2 * j
            val ji = jr + 1
            val ir = 2 * i
            val ii = ir + 1
            val tre = riArray[jr]
            val tim = riArray[ji]
            riArray[jr] = riArray[ir]
            riArray[ji] = riArray[ii]
            riArray[ir] = tre
            riArray[ii] = tim
        }
        k = n2
        while (k <= j) {
            j -= k
            k = k shr 1
        }
        j += k
    }
}

fun iFFT(length: Int, riArray: DoubleArray) {
    require(isPowerOfTwo(length))
    val m = log2(length.toDouble()).toInt()
    val n = (2.0.pow(m) + 0.5).toInt()
    require(n <= riArray.size)
    var wCos: Double
    var wSin: Double
    val n2 = n shr 1
    var l1 = n
    var l2: Int

    repeat(m) {
        l2 = l1 shr 1
        wCos = 1.0
        wSin = 0.0
        val uCos = cos(PI / l2)
        val uSin = sin(PI / l2)

        for (j in 0 until l2) {
            for (i in j until n step l1) {
                val ir = 2 * i
                val ii = ir + 1
                val l22 = 2 * l2
                val sumRe = 0.5 * (riArray[ir] + riArray[ir + l22])
                val sumIm = 0.5 * (riArray[ii] + riArray[ii + l22])
                val diffRe = 0.5 * (riArray[ir] - riArray[ir + l22])
                val diffIm = 0.5 * (riArray[ii] - riArray[ii + l22])
                riArray[ir + l22] = diffRe * wCos - diffIm * wSin
                riArray[ii + l22] = diffRe * wSin + diffIm * wCos
                riArray[ir] = sumRe
                riArray[ii] = sumIm
            }
            val w = wCos * uCos - wSin * uSin
            wSin = wCos * uSin + wSin * uCos
            wCos = w
        }
        l1 = l1 shr 1
    }

    var k: Int
    var j = 0
    val n1 = n - 1

    for (i in 0 until n1) {
        if (i < j) {
            val jr = 2 * j
            val ji = jr + 1
            val ir = 2 * i
            val ii = ir + 1
            val tre = riArray[jr]
            val tim = riArray[ji]
            riArray[jr] = riArray[ir]
            riArray[ji] = riArray[ii]
            riArray[ir] = tre
            riArray[ii] = tim
        }
        k = n2
        while (k <= j) {
            j -= k
            k = k shr 1
        }
        j += k
    }
}


fun realFFT(realArray: DoubleArray): DoubleArray {
    require(isPowerOfTwo(realArray.size))
    val outArray = DoubleArray(realArray.size + 2)
    realArray.copyInto(outArray)
    val m = log2(realArray.size.toDouble()).toInt()
    val n = (2.0.pow(m) + 0.5).toInt()
    fft(n / 2, outArray)
    val a = DoubleArray(n)
    val b = DoubleArray(n)

    for (i in 0 until n / 2) {
        a[2 * i] = 0.5 * (1 - sin(2 * PI / n * i))
        a[2 * i + 1] = -0.5 * cos(2 * PI / n * i)
        b[2 * i] = 0.5 * (1 + sin(2 * PI / n * i))
        b[2 * i + 1] = 0.5 * cos(2 * PI / n * i)
    }

    for (k in 1 until n / 4 + 1) {
        val k2 = 2 * k
        val xr =
            outArray[k2] * a[k2] -
                outArray[k2 + 1] * a[k2 + 1] +
                outArray[n - k2] * b[k2] +
                outArray[n - k2 + 1] * b[k2 + 1]
        val xi =
            outArray[k2] * a[k2 + 1] +
                outArray[k2 + 1] * a[k2] +
                outArray[n - k2] * b[k2 + 1] -
                outArray[n - k2 + 1] * b[k2]
        val xrN =
            outArray[n - k2] * a[n - k2] -
                outArray[n - k2 + 1] * a[n - k2 + 1] +
                outArray[k2] * b[n - k2] +
                outArray[k2 + 1] * b[n - k2 + 1]
        val xiN =
            outArray[n - k2] * a[n - k2 + 1] +
                outArray[n - k2 + 1] * a[n - k2] +
                outArray[k2] * b[n - k2 + 1] -
                outArray[k2 + 1] * b[n - k2]
        outArray[k2] = xr
        outArray[k2 + 1] = xi
        outArray[n - k2] = xrN
        outArray[n - k2 + 1] = xiN
    }

    val tmp = outArray[0]
    outArray[n] = outArray[0] - outArray[1]
    outArray[0] = tmp + outArray[1]
    outArray[1] = 0.0
    outArray[n + 1] = 0.0

    return outArray
}


fun realIFFT(realArray: DoubleArray): DoubleArray {
    require(isPowerOfTwo(realArray.size - 2))
    val outArray = realArray.copyOf()
    val m = log2((realArray.size - 2).toDouble()).toInt()
    val n = (2.0.pow(m) + 0.5).toInt()
    val a = DoubleArray(n)
    val b = DoubleArray(n)

    for (i in 0 until n / 2) {
        a[2 * i] = 0.5 * (1 - sin(2 * PI / n * i))
        a[2 * i + 1] = -0.5 * cos(2 * PI / n * i)
        b[2 * i] = 0.5 * (1 + sin(2 * PI / n * i))
        b[2 * i + 1] = 0.5 * cos(2 * PI / n * i)
    }

    for (k in 1 until n / 4 + 1) {
        val k2 = 2 * k
        val xr =
            outArray[k2] * a[k2] +
                outArray[k2 + 1] * a[k2 + 1] +
                outArray[n - k2] * b[k2] -
                outArray[n - k2 + 1] * b[k2 + 1]
        val xi =
            -outArray[k2] * a[k2 + 1] +
                outArray[k2 + 1] * a[k2] -
                outArray[n - k2] * b[k2 + 1] -
                outArray[n - k2 + 1] * b[k2]
        val xrN =
            outArray[n - k2] * a[n - k2] +
                outArray[n - k2 + 1] * a[n - k2 + 1] +
                outArray[k2] * b[n - k2] -
                outArray[k2 + 1] * b[n - k2 + 1]
        val xiN =
            -outArray[n - k2] * a[n - k2 + 1] +
                outArray[n - k2 + 1] * a[n - k2] -
                outArray[k2] * b[n - k2 + 1] -
                outArray[k2 + 1] * b[n - k2]
        outArray[k2] = xr
        outArray[k2 + 1] = xi
        outArray[n - k2] = xrN
        outArray[n - k2 + 1] = xiN
    }

    val temp = outArray[0]
    outArray[0] = 0.5 * outArray[0] + 0.5 * outArray[n]
    outArray[1] = 0.5 * temp - 0.5 * outArray[n]
    iFFT(n / 2, outArray)
    return outArray.copyOf(outArray.size - 2)
}
