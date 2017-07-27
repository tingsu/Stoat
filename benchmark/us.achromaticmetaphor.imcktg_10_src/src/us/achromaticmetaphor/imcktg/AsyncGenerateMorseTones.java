package us.achromaticmetaphor.imcktg;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

public class AsyncGenerateMorseTones extends AsyncTask<AsyncGenerateMorseTones.Params, Void, Tone []> {

  public interface Listener {
    public abstract void onFinished(Tone tone);
  }

  public static class Params {

    private final Context c;
    private final String s;
    private final ToneGenerator gen;
    private final Listener l;
    private final Uri contacturi;
    private final Intent i;

    public Params(Listener l, Context c, String s, ToneGenerator gen, Uri contacturi, Intent i) {
      this.l = l;
      this.c = c;
      this.s = s;
      this.gen = gen;
      this.contacturi = contacturi;
      this.i = i;
    }

    public Tone gentone() {
      try {
        Tone tone = Tone.generateTone(c, s, gen, i);
        if (contacturi != null)
          tone.assign(c, contacturi);
        else
          tone.assignDefault(c, i);
        return tone;
      } catch (IOException e) {
        return null;
      }
    }
  }

  private Params [] params;

  @Override
  protected Tone [] doInBackground(Params... params) {
    this.params = params;
    Tone [] tones = new Tone [params.length];
    for (int i = 0; i < params.length; i++)
      tones[i] = params[i].gentone();
    return tones;
  }

  @Override
  protected void onPostExecute(Tone [] tones) {
    for (int i = 0; i < tones.length; i++)
      params[i].l.onFinished(tones[i]);
  }

}
