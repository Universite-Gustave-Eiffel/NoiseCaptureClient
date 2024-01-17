package org.noise_planet.noisecapture.shared.signal

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.log2

/**
 * Adapted from:
 * https://jenshee.dk/signalprocessing/realfft.pdf
 * @link https://github.com/Grubleren
 */

fun fftFloat(length: Int, riArray: FloatArray) {
    require(isPowerOfTwo(length))
    val m = log2(length.toDouble()).toInt()
    val n = (2.0f.pow(m) + 0.5).toInt()
    require(n <= riArray.size)
    var wCos: Float
    var wSin: Float
    val n2 = n shr 1
    var l1 = n
    var l2: Int

    for (l in 0 until m) {
        l2 = l1 shr 1
        wCos = 1.0f
        wSin = 0.0f
        val uCos : Float = cos(PI.toFloat() / l2)
        val uSin  : Float = -sin(PI.toFloat() / l2)

        for (j in 0 until l2) {
            for (i in j until n step l1) {
                val ir = 2 * i
                val ii = ir + 1
                val l22 = 2 * l2
                val sumRe = riArray[ir] + riArray[ir + l22]
                val sumIm = riArray[ii] + riArray[ii + l22]
                val diffRe = riArray[ir] - riArray[ir + l22]
                val diffIm = riArray[ii] - riArray[ii + l22]
                riArray[ir + l22] = (diffRe * wCos - diffIm * wSin)
                riArray[ii + l22] = (diffRe * wSin + diffIm * wCos)
                riArray[ir] = sumRe
                riArray[ii] = sumIm
            }
            val w : Float= wCos * uCos - wSin * uSin
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

fun iFFTFloat(length: Int, riArray: FloatArray) {
    require(isPowerOfTwo(length))
    val m = log2(length.toDouble()).toInt()
    val n = (2.0.pow(m) + 0.5).toInt()
    require(n <= riArray.size)
    var wCos: Double
    var wSin: Double
    val n2 = n shr 1
    var l1 = n
    var l2: Int

    for (l in 0 until m) {
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
                riArray[ir + l22] = (diffRe * wCos - diffIm * wSin).toFloat()
                riArray[ii + l22] = (diffRe * wSin + diffIm * wCos).toFloat()
                riArray[ir] = sumRe.toFloat()
                riArray[ii] = sumIm.toFloat()
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


fun realFFTFloat(realArray: FloatArray): FloatArray {
    require(isPowerOfTwo(realArray.size))
    val outArray = FloatArray(realArray.size + 2)
    realArray.copyInto(outArray)
    val m = log2(realArray.size.toDouble()).toInt()
    val n = (2.0.pow(m) + 0.5).toInt()
    fftFloat(n/2, outArray)
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
            outArray[k2] * a[k2] - outArray[k2 + 1] * a[k2 + 1] + outArray[n - k2] * b[k2] + outArray[n - k2 + 1] * b[k2 + 1]
        val xi =
            outArray[k2] * a[k2 + 1] + outArray[k2 + 1] * a[k2] + outArray[n - k2] * b[k2 + 1] - outArray[n - k2 + 1] * b[k2]
        val xrN =
            outArray[n - k2] * a[n - k2] - outArray[n - k2 + 1] * a[n - k2 + 1] + outArray[k2] * b[n - k2] + outArray[k2 + 1] * b[n - k2 + 1]
        val xiN =
            outArray[n - k2] * a[n - k2 + 1] + outArray[n - k2 + 1] * a[n - k2] + outArray[k2] * b[n - k2 + 1] - outArray[k2 + 1] * b[n - k2]
        outArray[k2] = xr.toFloat()
        outArray[k2 + 1] = xi.toFloat()
        outArray[n - k2] = xrN.toFloat()
        outArray[n - k2 + 1] = xiN.toFloat()
    }

    val tmp = outArray[0]
    outArray[n] = outArray[0] - outArray[1]
    outArray[0] = tmp + outArray[1]
    outArray[1] = 0.0f
    outArray[n+1] = 0.0f

    return outArray
}


fun realIFFTFloat(realArray: FloatArray) : FloatArray {
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
            outArray[k2] * a[k2] + outArray[k2 + 1] * a[k2 + 1] + outArray[n - k2] * b[k2] - outArray[n - k2 + 1] * b[k2 + 1]
        val xi =
            -outArray[k2] * a[k2 + 1] + outArray[k2 + 1] * a[k2] - outArray[n - k2] * b[k2 + 1] - outArray[n - k2 + 1] * b[k2]
        val xrN =
            outArray[n - k2] * a[n - k2] + outArray[n - k2 + 1] * a[n - k2 + 1] + outArray[k2] * b[n - k2] - outArray[k2 + 1] * b[n - k2 + 1]
        val xiN =
            -outArray[n - k2] * a[n - k2 + 1] + outArray[n - k2 + 1] * a[n - k2] - outArray[k2] * b[n - k2 + 1] - outArray[k2 + 1] * b[n - k2]
        outArray[k2] = xr.toFloat()
        outArray[k2 + 1] = xi.toFloat()
        outArray[n - k2] = xrN.toFloat()
        outArray[n - k2 + 1] = xiN.toFloat()
    }

    val temp = outArray[0]
    outArray[0] = (0.5 * outArray[0] + 0.5 * outArray[n]).toFloat()
    outArray[1] = (0.5 * temp - 0.5 * outArray[n]).toFloat()
    iFFTFloat(n/2, outArray)
    return outArray.copyOf(outArray.size - 2)
}
