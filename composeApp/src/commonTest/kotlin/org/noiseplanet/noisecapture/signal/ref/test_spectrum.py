import numpy as np
from scipy import signal
import matplotlib.pyplot as plt


# Assuming 'fs' is your sampling frequency
fs = 8192


def generate_sinus(duration, sample_rate, frequency):
    return np.sin(2 * np.pi * np.arange(duration * sample_rate) * frequency /
                  sample_rate)


# Your real data 'x'
x = generate_sinus(1.0, fs, 1000)

# Compute the Fourier transform of the real data using rfft
X_fft = np.fft.rfft(x)

# Calculate the magnitude squared of the Fourier transformed signal
magnitude_squared = X_fft * np.conj(X_fft)

# Divide the magnitude squared by the number of samples to obtain the power spectral density
power_spectral_density = magnitude_squared / len(x)

# Plot the power spectral density
freqs = np.linspace(0, fs / 2, len(power_spectral_density))
plt.plot(freqs, power_spectral_density)
plt.show()
