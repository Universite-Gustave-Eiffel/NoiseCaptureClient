import matplotlib.pyplot as plt
import librosa
import numpy as np

# expected_power = 94.0
# peak = 10 ** (expected_power / 10.0) * np.sqrt(2)
# sr = 32768
# y = librosa.tone(1000.0, sr=sr, length=sr)
# y = y * peak

y, sr = librosa.load(librosa.ex('trumpet'))
S = np.abs(librosa.stft(y))
fig, ax = plt.subplots()
db_scale = librosa.amplitude_to_db(S, ref=1)
img = librosa.display.specshow(db_scale, y_axis='log', x_axis='time', ax=ax)

ax.set_title('Power spectrogram')

fig.colorbar(img, ax=ax, format="%+2.0f dB")

plt.show()