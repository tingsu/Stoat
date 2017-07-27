package us.achromaticmetaphor.imcktg;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Locale;

public class MorsePCM extends ToneGenerator {

  private static final int secondsPerMinute = 60;

  private static final int defaultFrequency = 800;
  private static final int defaultWordsPerMinute = 20;
  private static final int defaultRepeatCount = 0;

  public static void main(String [] argVector) throws IOException {
    (new MorsePCM()).writeWithWavHeader(System.out, argVector[0]);
  }

  private final int sampleRate;
  private byte [] [] samplevec;
  private byte [] silent;

  private final int samplesPerPulse;
  private final int freq;
  private final int wpm;
  private final int repeatCount;

  public MorsePCM() {
    this(defaultRepeatCount);
  }

  public MorsePCM(int repeatCount) {
    this(defaultWordsPerMinute, repeatCount);
  }

  public MorsePCM(int wpm, int repeatCount) {
    this(defaultFrequency, wpm, repeatCount);
  }

  public MorsePCM(int freq, int wpm, int repeatCount) {
    this(freq, freq * 10, wpm, repeatCount);
  }

  public MorsePCM(int freq, int sampleRate, int wpm, int repeatCount) {
    this.sampleRate = sampleRate;
    this.freq = freq;
    this.wpm = wpm;
    this.repeatCount = repeatCount;

    samplesPerPulse = secondsPerMinute * sampleRate / wpm / Morse.unitsPerWord;

    silent = new byte [samplesPerPulse];
    Arrays.fill(silent, Byte.MAX_VALUE);

    samplevec = new byte [sampleRate / freq] [samplesPerPulse];
    final double scale = Math.PI * 2 * freq / sampleRate;
    for (int i = 0; i < samplevec.length; i++)
      for (int j = 0; j < samplevec[i].length; j++)
        samplevec[i][j] = (byte) (Byte.MAX_VALUE + (Byte.MAX_VALUE * Math.sin((i * samplevec[i].length + j) * scale)));
  }

  public void writeWithWavHeader(OutputStream out, String s) throws IOException {
    MorseWriter writer = new MorseWriter(out);
    Iterable<String> mcs = Morse.morse(s);
    writer.writeWavHeader(Morse.numPulses(mcs) * samplesPerPulse * (repeatCount + 1));
    writer.writeMorse(mcs);
    writer.flush();
  }

  public void writeWithHeaders(OutputStream out, String s) throws IOException {
    MorseWriter writer = new MorseWriter(out);
    Iterable<String> mcs = Morse.morse(s);
    final int nsamples = Morse.numPulses(mcs) * samplesPerPulse * (repeatCount + 1);
    writer.writeHttpHeaders(nsamples, to83(s));
    writer.writeWavHeader(nsamples);
    writer.writeMorse(mcs);
    writer.flush();
  }

  private static String to83(String s) {
    s = s.toLowerCase(Locale.getDefault()).replaceAll("[^abcdefghijklmnopqrstuvwxyz0123456789]", "");
    return s.length() > 8 ? s.substring(0, 8) : s;
  }

  private static void mSHORTle(byte [] b, int offset, short s) {
    b[offset] = (byte) s;
    b[offset+1] = (byte) (s >> 8);
  }

  private static void mSHORTle(byte [] b, int offset, int s) {
    mSHORTle(b, offset, (short) s);
  }

  private static void mINTle(byte [] b, int offset, int i) {
    mSHORTle(b, offset, i);
    mSHORTle(b, offset + 2, i >> 16);
  }

  private class MorseWriter {

    private OutputStream out;
    private int pulsesWritten;

    public MorseWriter(OutputStream out) {
      this.out = new BufferedOutputStream(out);
      pulsesWritten = 0;
    }

    public void writeWavHeader(int samples) throws IOException {
      byte [] wav = new byte [44];

      mINTle(wav, 0, 0x46464952); // RIFF
      mINTle(wav, 4, samples + wav.length - 8); // length of rest of stream
      mINTle(wav, 8, 0x45564157); // WAVE
      mINTle(wav, 12, 0x20746d66); // fmt<sp>
      mINTle(wav, 16, 16); // size of "fmt " subchunk
      mSHORTle(wav, 20, 1); // audio format
      mSHORTle(wav, 22, 1); // number of channels
      mINTle(wav, 24, sampleRate); // sample rate
      mINTle(wav, 28, sampleRate); // byte rate
      mSHORTle(wav, 32, 1); // bytes per sample * number of channels
      mSHORTle(wav, 34, 8); // bits per sample
      mINTle(wav, 36, 0x61746164); // data
      mINTle(wav, 40, samples); // size of "data" subchunk

      out.write(wav);
    }

    public void writeHttpHeaders(int nsamples, String fnhint) throws IOException {
      final int filelen = nsamples + 44;
      OutputStreamWriter writer = new OutputStreamWriter(out);
      writer.write("Content-Type: audio/wav\n");
      writer.write("Content-Length: " + filelen + "\n");
      writer.write("Content-Range: bytes 0-" + (filelen - 1) + "/" + filelen + "\n");
      writer.write("Content-Disposition: filename=\"" + fnhint + ".wav\"\n");
      writer.write("\n");
      writer.flush();
    }

    private void writeSamplePulse() throws IOException {
      out.write(samplevec[pulsesWritten]);
      pulsesWritten++;
      pulsesWritten %= samplevec.length;
    }

    private void writeSilentPulse() throws IOException {
      out.write(silent);
    }

    private void writePulse(int pulse) throws IOException {
      if (pulse == 0)
        writeSilentPulse();
      else
        writeSamplePulse();
    }

    private void writePulses(int n, int pulses) throws IOException {
      while (n-- > 0) {
        writePulse(pulses & 1);
        pulses >>= 1;
      }
    }

    private void writeMorseChar(char c) throws IOException {
      writePulses(c == '-' ? 4 : 2, c == '.' ? 1 : c == '-' ? 7 : 0);
    }

    private void writeMorseString(String mcs) throws IOException {
      for (int i = 0; i < mcs.length(); i++)
        writeMorseChar(mcs.charAt(i));
      writePulses(2, 0);
    }

    public void writeMorse(Iterable<String> morse) throws IOException {
      for (int i = 0; i <= repeatCount; i++)
        for (String s : morse)
          writeMorseString(s);
    }

    public void flush() throws IOException {
      out.flush();
    }

  }

  @Override
  public void writeTone(OutputStream out, String s) throws IOException {
    writeWithWavHeader(out, s);
  }

  @Override
  public String filenameExt() {
    return ".wav";
  }

  @Override
  public String filenameTypePrefix() {
    return "RIFF.WAV:" + freq + ":" + wpm + ":" + repeatCount + ":";
  }

}
