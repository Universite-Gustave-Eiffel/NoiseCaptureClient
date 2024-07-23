import acoustics
import numpy as np
import numpy.fft
import pandas
from decimal import Decimal
from math import *

def generate_sinus(duration, sample_rate, frequency):
    return np.sin(2 * np.pi * np.arange(duration * sample_rate) * frequency /
                  sample_rate)

def test2():
    def compute_auto_spectrum(x, fs, N=None):
        N = N if N else x.shape[-1]
        fr = np.fft.rfft(x, n=N) / N
        f = np.fft.rfftfreq(N, 1.0 / fs)
        return f, (fr * fr.conj()).real

    # Example usage:
    # Assume x and fs are your input signal and sampling frequency
    x = np.random.rand(32)  # Replace this with your actual data
    fs = 1000  # Replace this with your actual sampling frequency

    f, auto_spectrum = acoustics.signal.auto_spectrum(x, fs)
    print(auto_spectrum)
    f, auto_spectrum = compute_auto_spectrum(x, fs)
    print(auto_spectrum)

def main():
    duration = 1.0
    fs = 32768
    expected_power = 94
    peak = 10**(expected_power/20)*sqrt(2)
    samples = generate_sinus(duration, fs, 1000)
    samples += generate_sinus(duration, fs, 1600)
    samples += generate_sinus(duration, fs, 4000)
    samples += generate_sinus(duration, fs, 125)
    samples *= peak
    s = acoustics.Signal(samples, fs)
    bands = acoustics.signal.NOMINAL_THIRD_OCTAVE_CENTER_FREQUENCIES[:-2]
    fob = acoustics.signal.OctaveBand(center=bands, fraction=3)
    f, a = acoustics.signal.auto_spectrum(s, fs, N=len(s))
    fr = numpy.fft.rfft(s)
    #print(a)
    #print(fr)
    bands, power = acoustics.signal.third_octaves(s, fs, frequencies=bands, ref=1.0)
    print(pandas.DataFrame(power, columns=["dB"], index=["%s Hz" % Decimal(f) for f in fob.nominal]))


if __name__ == '__main__':
    main()
