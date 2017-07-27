package us.achromaticmetaphor.imcktg;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class IMelodyFormat {

  public static final String styleContinuous = "S1";
  public static final int repeatForever = 0;
  public static final int defaultOctave = 4;
  public static final String octavePrefix = "*";
  public static final String rest = "r";
  public static final String durationSuffixDotted = ".";
  public static final String headerSeparator = ":";
  public static final String lineEnding = "\r\n";
  public static final String lineContinuation = lineEnding + " ";
  public static final int beatMin = 25;
  public static final int beatMax = 900;
  public static final int octaveMin = 0;
  public static final int octaveMax = 8;
  public static final int volumeMin = 0;
  public static final int volumeMax = 15;

  private static final Set<String> validNotes;
  private static final Set<String> validStyles;

  public static String note(int octave, String tone) {
    if (octave < octaveMin || octave > octaveMax)
      throw new IllegalArgumentException("invalid octave: " + octave);
    if (! isValidNote(tone))
      throw new IllegalArgumentException("invalid tone: " + tone);

    return (octave == defaultOctave ? "" : octavePrefix + octave) + tone;
  }

  public static String duration(int dur) {
    if (dur < 0 || dur > 5)
      throw new IllegalArgumentException();
    return "" + dur;
  }

  public static String durationDotted(int dur) {
    return duration(dur) + durationSuffixDotted;
  }

  public static boolean isValidNote(String note) {
    return validNotes.contains(note);
  }

  public static void writeHeader(PrintStream out, String key, String value) {
    out.print(key + headerSeparator + value + lineEnding);
  }

  public static void writeRequiredHeaders(PrintStream out) {
    writeHeader(out, "BEGIN", "IMELODY");
    writeHeader(out, "VERSION", "1.2");
    writeHeader(out, "FORMAT", "CLASS1.0");
  }

  public static void writeBeatHeader(PrintStream out, int beat) {
    if (beat < beatMin || beat > beatMax)
      throw new IllegalArgumentException("invalid beat: " + beat);
    writeHeader(out, "BEAT", "" + beat);
  }

  public static void writeStyleHeader(PrintStream out, String style) {
    if (! validStyles.contains(style))
      throw new IllegalArgumentException("invalid style: " + style);
    writeHeader(out, "STYLE", style);
  }

  public static void writeVolumeHeader(PrintStream out, int volume) {
    if (volume < volumeMin || volume > volumeMax)
      throw new IllegalArgumentException("invalid volume: " + volume);
    writeHeader(out, "VOLUME", "V" + volume);
  }

  public static void writeNameHeader(PrintStream out, String name) {
    if (name.contains("\n"))
      throw new IllegalArgumentException("invalid name: " + name);
    writeHeader(out, "NAME", name);
  }

  public static void writeNameHeaderMangled(PrintStream out, String name) {
    writeNameHeader(out, name.replaceAll("\n", ""));
  }

  public static void writeBeginMelody(PrintStream out) {
    writeHeader(out, "MELODY", "");
    out.print(" ");
  }

  public static void writeEndMelody(PrintStream out) {
    out.print(lineEnding);
  }

  public static void writeRequiredFooters(PrintStream out) {
    writeHeader(out, "END", "IMELODY");
  }

  static {
    Set<String> s = new HashSet<String>();
    for (char note : new char [] {'a', 'b', 'c', 'd', 'e', 'f', 'g'})
      for (String prefix : new String [] {"", "#", "&"})
        s.add(prefix + note);
    validNotes = Collections.unmodifiableSet(s);
  }

  static {
    Set<String> s = new HashSet<String>();
    for (char style : new char [] {'0', '1', '2'})
      s.add("S" + style);
    validStyles = Collections.unmodifiableSet(s);
  }

  public static void beginRepeatBlock(PrintStream out) {
    out.print("(");
  }

  public static void endRepeatBlock(PrintStream out, int repeatCount) {
    if (repeatCount < 0)
      throw new IllegalArgumentException("invalid repeat count: " + repeatCount);
    out.print("@" + repeatCount + ")");
  }

}
