
/**
 * @class RawAudioProcessor
 * @extends AudioWorkletProcessor
 */
class RawAudioProcessor extends AudioWorkletProcessor {

  constructor() {
    super();
  }

  process(inputs, outputs) {
    // This example only handles mono channel.
    const inputChannelData = inputs[0][0];
    this.port.postMessage(inputChannelData);
    return true;
  }
}

registerProcessor("raw_audio_processor", RawAudioProcessor);
