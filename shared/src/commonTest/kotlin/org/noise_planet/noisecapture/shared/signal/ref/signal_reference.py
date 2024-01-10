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
    fs = 32000
    power = 10**(94/20)*sqrt(2)
    samples = generate_sinus(duration, fs, 1000) * power
    samples += generate_sinus(duration, fs, 1600) * power
    samples += generate_sinus(duration, fs, 4000) * power
    samples += generate_sinus(duration, fs, 125) * power
    window = np.hanning(len(samples))
    pond = len(window)/window.sum()
    samples *= window
    samples_window = np.zeros(int(pow(2, ceil(log(len(samples))/log(2)))))
    samples_window[0:len(samples)//2] = samples[len(samples)//2:]
    samples_window[len(samples_window)-len(samples)//2:] = samples[:len(samples)//2]
    s = acoustics.Signal(samples, fs)
    bands = acoustics.signal.NOMINAL_THIRD_OCTAVE_CENTER_FREQUENCIES[:-2]
    fob = acoustics.signal.OctaveBand(center=bands, fraction=3)
    bands, power = acoustics.signal.third_octaves(s, fs, frequencies=bands,
                                                  ref=1.0)

    print(pandas.DataFrame(power, columns=["dB"], index=["%s Hz" % Decimal(f) for f in fob.nominal]))


main()
