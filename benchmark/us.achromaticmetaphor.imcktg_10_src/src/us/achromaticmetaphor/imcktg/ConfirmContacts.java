package us.achromaticmetaphor.imcktg;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfirmContacts extends Activity implements TextToSpeech.OnInitListener {

  private static final String extrakeyprefix = "us.achromaticmetaphor.imcktg.ConfirmContacts";
  public static final String extrakeySelection = extrakeyprefix + ".selection";
  public static final String extrakeyTonestring = extrakeyprefix + ".tonestring";
  public static final String extrakeyFordefault = extrakeyprefix + ".forDefault";
  public static final String extrakeyFilename = extrakeyprefix + ".filename";

  private int outstandingTones;
  private TextToSpeech tts;
  private String previewText;
  private ProgressDialog pdia;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_confirm_contacts);

    tts = new TextToSpeech(this, this);

    if (getIntent().getBooleanExtra(extrakeyFordefault, false))
      previewText = getIntent().getStringExtra(extrakeyTonestring);
    else {
      long [] selection = getIntent().getLongArrayExtra(extrakeySelection);
      if (selection.length == 0)
        previewText = "preview";
      else
        previewText = nameForContact(contactUriForID(selection[0]));
    }

    ((SeekBar) findViewById(R.id.WPM_input)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ((TextView) findViewById(R.id.WPM_hint)).setText("" + wpm() + " wpm");
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    ((SeekBar) findViewById(R.id.FREQ_input)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ((TextView) findViewById(R.id.FREQ_hint)).setText("" + freqRescaled(20, 4410) + "Hz / " + freqNote().toUpperCase(Locale.getDefault()) + freqOctave());
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    tts.shutdown();
  }

  private class Listener implements AsyncGenerateMorseTones.Listener {
    @Override
    public void onFinished(Tone tone) {
      ConfirmContacts.this.decrementOutstandingTones();
    }
  }

  private void checkDone() {
    if (outstandingTones <= 0) {
      pdia.dismiss();
      finish();
    }
  }

  public synchronized void decrementOutstandingTones() {
    outstandingTones--;
    checkDone();
  }

  private Uri contactUriForID(long id) {
    return Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,  "" + id);
  }

  private String nameForContact(Uri contacturi) {
    Cursor cursor = getContentResolver().query(contacturi,
                new String [] {ContactsContract.Contacts.DISPLAY_NAME},
                null, null, null);
    cursor.moveToNext();
    return cursor.getString(0);
  }

  public void generateAndAssignTones(View view) {
    generateAndAssignTones(spinnerGen());
  }

  public void generateAndAssignTones(ToneGenerator gen) {
    pdia = ProgressDialog.show(this, "Generating", "Please wait", true, false);
    if (getIntent().getBooleanExtra(extrakeyFordefault, false)) {
      final String tonestring = getIntent().getStringExtra(extrakeyTonestring);
      outstandingTones = 1;
      final AsyncGenerateMorseTones async = new AsyncGenerateMorseTones();
      async.execute(new AsyncGenerateMorseTones.Params(new Listener(), this, tonestring, gen, null, getIntent()));
    }
    else {
      final long [] selection = getIntent().getLongArrayExtra(extrakeySelection);
      outstandingTones = selection.length;
      for (long id : selection) {
        final Uri contacturi = contactUriForID(id);
        final String name = nameForContact(contacturi);
        final AsyncGenerateMorseTones async = new AsyncGenerateMorseTones();
        async.execute(new AsyncGenerateMorseTones.Params(new Listener(), this, name, gen, contacturi, getIntent()));
      }
    }
    checkDone();
  }

  public ToneGenerator spinnerGen() {
    String sel = (String) ((Spinner) findViewById(R.id.format_spinner)).getSelectedItem();
    return sel.equals("Morse (WAV)") ? pcmGen() : sel.equals("Morse (iMelody)") ? imyGen() : ttsGen();
  }

  private class OAFCL implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
    private final AudioManager aman;
    private final Tone preview;

    public OAFCL(AudioManager aman, Tone preview) {
      this.aman = aman;
      this.preview = preview;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {}

    @Override
    public void onCompletion(MediaPlayer player) {
      player.release();
      aman.abandonAudioFocus(this);
      preview.delete(ConfirmContacts.this);
    }
  }

  private int freqRescaled(int min, int max) {
    ProgressBar pbar = ((ProgressBar) findViewById(R.id.FREQ_input));
    final int freq = pbar.getProgress();
    final int pmax = pbar.getMax();
    return min + ((max - min) * freq / pmax);
  }

  private float freqRescaled() {
    return (freqRescaled(20, 4410) + 2195) / 2195.0f;
  }

  private int wpm() {
    return 1 + ((ProgressBar) findViewById(R.id.WPM_input)).getProgress();
  }

  private ToneGenerator pcmGen() {
    return new MorsePCM(freqRescaled(20, 4410), wpm(), repeatCount());
  }

  private int repeatCount() {
    try {
      return Integer.parseInt(((TextView) findViewById(R.id.RC_input)).getText().toString());
    }
      catch (NumberFormatException nfe) {
        return 0;
      }
  }

  private int freqTone() {
    return freqRescaled(0, 62);
  }

  private int freqOctave() {
    return freqTone() / 7;
  }

  private String freqNote() {
    return "cdefgab".substring(freqTone() % 7).substring(0, 1);
  }

  private ToneGenerator imyGen() {
    return new MorseIMelody(freqOctave(), freqNote(), wpm(), repeatCount());
  }

  private ToneGenerator ttsGen() {
    return new TTS(tts, freqRescaled(), wpm() / 20.0f, repeatCount());
  }

  public void previewTone(View view) {
    previewTone(spinnerGen());
  }

  public void previewTone(ToneGenerator gen) {
    AudioManager aman = (AudioManager) getSystemService(AUDIO_SERVICE);
    MediaPlayer player;
    try {
      Intent i = new Intent();
      i.putExtra(extrakeyFilename, Tone.tmpFilename());
      Tone preview = Tone.generateTone(this, previewText, gen, i);
      player = MediaPlayer.create(this, preview.contentUri());
      OAFCL oafcl = new OAFCL(aman, preview);
      aman.requestAudioFocus(oafcl, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
      player.setOnCompletionListener(oafcl);
      player.start();
    } catch (IOException e) {
    }
  }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS)
      enableTTS();
  }

  private void enableTTS() {
  }

}
