package us.achromaticmetaphor.imcktg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ToneGenerator {

  public abstract void writeTone(OutputStream out, String s) throws IOException;

  public void writeTone(OutputStream out, String s, boolean extend) throws IOException {
    if (extend)
      s += Tone.morsePostPause;
    writeTone(out, s);
  }

  public void writeTone(File out, String s, boolean extend) throws IOException {
    writeTone(new BufferedOutputStream(new FileOutputStream(out)), s, extend);
  }

  public abstract String filenameExt();
  public abstract String filenameTypePrefix();
}
