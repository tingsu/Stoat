package us.achromaticmetaphor.imcktg;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Build;

public abstract class Morse {

  public static final int unitsPerWord = 50; // PARIS method
  public static final char dotChar = '.';
  public static final char dashChar = '-';
  public static final char pauseChar = ' ';

  private static void morse(String s, List<String> sb) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
      s = Normalizer.normalize(s, Normalizer.Form.NFKD);
    s = s.toLowerCase(Locale.getDefault());
    s.replaceAll("\\s+", " ");

    for (char c : s.toCharArray())
      if(IMCmap.containsKey(c))
        sb.add(IMCmap.get(c));
  }

  public static int numPulses(String s) {
    int pulses = 0;
    for (int i = 0; i < s.length(); i++)
      pulses += s.charAt(i) == '-' ? 4 : 2;
    return pulses;
  }

  public static int numPulses(Iterable<String> mcs) {
    int pulses = 0;
    for (String s : mcs)
      pulses += 2 + numPulses(s);
    return pulses;
  }

  public static List<String> morse(String s) {
    List<String> l = new ArrayList<String>(s.length());
    morse(s, l);
    return l;
  }

  private static final Map<Character, String> IMCmap;

  static {
    final String [] IMC = {
      "a.-",
      "b-...",
      "c-.-.",
      "d-..",
      "e.",
      "f..-.",
      "g--.",
      "h....",
      "i..",
      "j.---",
      "k-.-",
      "l.-..",
      "m--",
      "n-.",
      "o---",
      "p.--.",
      "q--.-",
      "r.-.",
      "s...",
      "t-",
      "u..-",
      "v...-",
      "w.--",
      "x-..-",
      "y-.--",
      "z--..",
      "1.----",
      "2..---",
      "3...--",
      "4....-",
      "5.....",
      "6-....",
      "7--...",
      "8---..",
      "9----.",
      "0-----",
      "..-.-.-",
      ",--..--",
      ":---...",
      "?..--..",
      "'.----.",
      "--....-",
      "/-..-.",
      "(-.--.",
      ")-.--.-",
      "\".-..-.",
      "=-...-",
      "+.-.-.",
      "@.--.-.",
      "  ",
      "E........"
    };
    Map<Character, String> m = new HashMap<Character, String>();
    for (String s : IMC)
      m.put(s.charAt(0), s.substring(1));
    IMCmap = Collections.unmodifiableMap(m);
  }

}
