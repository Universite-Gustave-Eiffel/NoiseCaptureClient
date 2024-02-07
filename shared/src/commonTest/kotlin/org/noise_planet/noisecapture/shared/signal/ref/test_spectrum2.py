import numpy as np
from scipy import signal
import matplotlib.pyplot as plt
import acoustics

# Assuming 'fs' is your sampling frequency
fs = 8192


def generate_sinus(duration, sample_rate, frequency):
    return np.sin(2 * np.pi * np.arange(duration * sample_rate) * frequency /
                  sample_rate)

duration = 1.0
fs = 64
expected_power = 94
peak = 10 ** (expected_power / 20) * np.sqrt(2)
x = generate_sinus(duration, fs, 5)
x += generate_sinus(duration, fs, 12)
x += generate_sinus(duration, fs, 20)
x += generate_sinus(duration, fs, 28)
x *= peak

# Compute the Fourier transform of the real data using rfft
X_fft = np.fft.rfft(x)

#print(",\n".join([ str(v) for v in list(np.hstack((X_fft.real[:, None], X_fft.imag[:, None])).flatten())]))

X_fft_flat = np.hstack((X_fft.real[:, None], X_fft.imag[:, None])).flatten()

magnitude_squared_flat = (X_fft.imag * X_fft.imag)[:-1]

# Calculate the magnitude squared of the Fourier transformed signal
magnitude_squared = (X_fft * np.conj(X_fft)).real[:-1]

# plt.scatter(range(len(magnitude_squared_flat)), magnitude_squared_flat, marker="+")
# plt.scatter(range(len(magnitude_squared)), magnitude_squared, marker="x")
# print(",\n".join([str(v) for v in magnitude_squared]))
# plt.show()
# exit(0)

# Divide the magnitude squared by the number of samples to obtain the power spectral density
power_spectral_density = magnitude_squared / len(x)**2

# Pad the power spectral density with zeros to match the shape of the frequencies
power_spectral_density_padded = np.pad(power_spectral_density, (0, len(power_spectral_density)), 'constant')

# Get the frequencies corresponding to the power spectral density
freqs = np.fft.fftfreq(len(power_spectral_density_padded), 1 / fs)

# Shift the frequencies and power spectral density to center the spectrum
freqs, power_spectral_density_padded = np.fft.fftshift(freqs), np.fft.fftshift(power_spectral_density_padded)

# Plot the power spectral density
plt.scatter(freqs, 10*np.log10(power_spectral_density_padded * 2), marker="x")

# compare with acoustics
f, auto_spectrum = acoustics.signal.auto_spectrum(x, fs)

plt.scatter(f, 10*np.log10(auto_spectrum * 2), marker="+")


plt.show()
