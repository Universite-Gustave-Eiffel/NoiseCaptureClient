import numpy as np
import scipy
from math import *

def czt(x, m=None, w=None, a=None):
    # Translated from GNU Octave's czt.m

    n = len(x)
    if m is None: m = n
    if w is None: w = np.exp(-2j * pi / m)
    if a is None: a = 1

    chirp = w ** (np.arange(1 - n, max(m, n)) ** 2 / 2.0)
    N2 = int(2 ** ceil(log2(m + n - 1)))  # next power of 2
    xp = np.append(x * a**-np.arange(n).astype(np.float32) * chirp[n - 1: n + n - 1], np.zeros(N2 - n))
    ichirpp = np.append(1 / chirp[: m + n - 1], np.zeros(N2 - (m + n - 1)))
    r = np.fft.ifft(np.fft.fft(xp) * np.fft.fft(ichirpp))
    return r[n - 1: m + n - 1] * chirp[n - 1: m + n - 1]


def cround(z, d=None):
    return np.round(z.real, d) + 1j * np.round(z.imag, d)


def generate_sinus(duration, sample_rate, frequency):
    return np.sin(2 * np.pi * np.arange(duration * sample_rate) * frequency /
                  sample_rate)


# Your real data 'x'
arr = np.random.randn(16)
print(arr)
print(cround(czt(arr), 4))  # [ 6.0+0.j    -1.5+0.866j -1.5-0.866j]
print(cround(np.fft.fft(arr), 4))  # [ 6.0+0.j    -1.5+0.866j -1.5-0.866j]
