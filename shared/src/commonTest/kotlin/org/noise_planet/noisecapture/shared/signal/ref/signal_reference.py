import acoustics
import numpy as np
import pandas
from decimal import Decimal
from math import *

def generate_sinus(duration, sample_rate, frequency):
    return np.sin(2 * np.pi * np.arange(duration * sample_rate) * frequency /
                  sample_rate)


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
    bands, power = acoustics.signal.third_octaves(s, fs, frequencies=bands,
                                                  ref=1.0)

    print(pandas.DataFrame(power, columns=["dB"], index=["%s Hz" % Decimal(f) for f in fob.nominal]))


main()
