import numpy as np
from scipy import signal
import matplotlib.pyplot as plt
import acoustics

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
magnitude_squared = (X_fft * np.conj(X_fft)).real[:-1]

# Divide the magnitude squared by the number of samples to obtain the power spectral density
power_spectral_density = magnitude_squared / len(x)**2

# Pad the power spectral density with zeros to match the shape of the frequencies
power_spectral_density_padded = np.pad(power_spectral_density, (0, len(power_spectral_density)), 'constant')

# Get the frequencies corresponding to the power spectral density
freqs = np.fft.fftfreq(len(power_spectral_density_padded), 1 / fs)

# Shift the frequencies and power spectral density to center the spectrum
freqs, power_spectral_density_padded = np.fft.fftshift(freqs), np.fft.fftshift(power_spectral_density_padded)

# Plot the power spectral density
plt.scatter(freqs, power_spectral_density_padded, marker="x")

# compare with acoustics
f, auto_spectrum = acoustics.signal.auto_spectrum(x, fs)

plt.scatter(f, auto_spectrum, marker="+")


plt.show()
