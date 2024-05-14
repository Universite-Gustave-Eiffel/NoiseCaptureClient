import numpy as np
import scipy
from math import *

def czt(x, m=None, w=None, a=None):
    # Translated from GNU Octave's czt.m

    n = len(x)
    if m is None: m = n
    if w is None: w = np.exp(-2j * pi / m)
    if a is None: a = 1
    # does not change
    chirp = w ** (np.arange(1 - n, max(m, n)) ** 2 / 2.0)
    print("chirp:", cround(chirp, 4))
    N2 = int(2 ** ceil(log2(m + n - 1)))  # next power of 2
    print("n2:", N2)
    ichirpp = np.fft.fft(np.append(1 / chirp[: m + n - 1], np.zeros(N2 - (m + n - 1))))
    print("ichirp:", cround(ichirpp, 4))
    # change with x
    xp = np.append(x * a**-np.arange(n).astype(np.float32) * chirp[n - 1: n + n - 1], np.zeros(N2 - n))
    r = np.fft.ifft(np.fft.fft(xp) * ichirpp)
    return r[n - 1: m + n - 1] * chirp[n - 1: m + n - 1]


def cround(z, d=None):
    return np.round(z.real, d) + 1j * np.round(z.imag, d)

def kotlindouble(ar, d = 1e-10):
    return np.round(np.hstack((ar.real[:, None], ar.imag[:, None])).flatten(), d)

def generate_sinus(duration, sample_rate, frequency):
    return np.sin(2 * np.pi * np.arange(duration * sample_rate) * frequency /
                  sample_rate)


# Your real data 'x'
arr = np.array([-6, -5, -4, -3, -2, -1,  0,  1,  2,  3,  4,  5])
print(kotlindouble(czt(arr), 10))  # [ 6.0+0.j    -1.5+0.866j -1.5-0.866j]
print(kotlindouble(np.fft.fft(arr), 10))  # [ 6.0+0.j    -1.5+0.866j -1.5-0.866j]
