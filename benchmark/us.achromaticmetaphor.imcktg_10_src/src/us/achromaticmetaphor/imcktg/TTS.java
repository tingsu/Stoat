package us.achromaticmetaphor.imcktg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import android.speech.tts.TextToSpeech;

public class TTS extends ToneGenerator implements TextToSpeech.OnUtteranceCompletedListener {

  private TextToSpeech tts;
  private Map<String, Semaphore> semas;
  private final int repeatCount;

  public TTS(TextToSpeech tts) {
    this(tts, 1.0f, 0.8f, 0);
  }

  public TTS(TextToSpeech tts, float pitch, float srate, int repeatCount) {
    this.tts = tts;
    this.repeatCount = repeatCount;
    tts.setOnUtteranceCompletedListener(this);
    tts.setPitch(pitch);
    tts.setSpeechRate(srate);
    semas = new ConcurrentHashMap<String, Semaphore>();
  }

  @Override
  public void writeTone(File tone, String s, boolean extend) throws IOException {
    String uid = UUID.randomUUID().toString();
    semas.put(uid, new Semaphore(0));
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uid);
    tts.synthesizeToFile(s, params, Tone.tmpfile(tone).getAbsolutePath());
    semas.get(uid).acquireUninterruptibly();
    semas.remove(uid);
    Tone.tmpRename(tone);
    if (extend)
      Tone.waveAppendSilence(tone, 2);
    if (repeatCount > 0)
      Tone.waveRepeat(tone, repeatCount);
  }

  @Override
  public String filenameExt() {
    return ".wav";
  }

  @Override
  public void writeTone(OutputStream out, String s) throws IOException {
    throw new IllegalArgumentException("method not implemented");
  }

  @Override
  public String filenameTypePrefix() {
    return "TextToSpeech:" + repeatCount + ":";
  }

  @Override
  public void onUtteranceCompleted(String uid) {
    semas.get(uid).release();
  }

}
