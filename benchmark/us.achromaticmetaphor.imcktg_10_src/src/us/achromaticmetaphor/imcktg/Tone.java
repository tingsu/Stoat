package us.achromaticmetaphor.imcktg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;

public class Tone {

  private static final String extrakeyprefix = "us.achromaticmetaphor.imcktg";
  public static final String extrakeyRingtone = extrakeyprefix + ".ringtone";
  public static final String extrakeyNotification = extrakeyprefix + ".notification";
  public static final String extrakeyAlarm = extrakeyprefix + ".alarm";

  private File file;
  private Uri contenturi;

  public File file() {
    return file;
  }

  public Uri contentUri() {
    return contenturi;
  }

  private Tone(File f) {
    file = f;
  }

  protected static final String morsePostPause = "        ";
  private static final char [] hexdig = "0123456789ABCDEF".toCharArray();

  private static String hexPP(byte [] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(hexdig[b & 0xf]);
      sb.append(hexdig[(b >> 4) & 0xf]);
    }
    return sb.toString();
  }

  protected static String filenameTransform(String s) {
    try {
      MessageDigest dig = MessageDigest.getInstance("SHA-256");
      return hexPP(dig.digest(s.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      return s;
    }
  }

  protected static String tmpFilename() {
    return filenameTransform("us.achromaticmetaphor.imcktg.preview");
  }

  private static File getToneFilename(Context c, String s, String ext, String typePrefix, Intent i) {
    String userFilename = i.getStringExtra(ConfirmContacts.extrakeyFilename);
    File rtdir;
    File tone;
    if (userFilename == null) {
      rtdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
      tone = new File(rtdir, filenameTransform("us.achromaticmetaphor.imcktg:" + typePrefix + s) + ext);
    }
    else {
      rtdir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "IMCKTG");
      tone = new File(rtdir, userFilename + ext);
    }
    rtdir.mkdirs();
    return tone;
  }

  protected static File tmpfile(File f) {
    return new File(f.getAbsolutePath() + ".tmp");
  }

  protected static Tone generateTone(Context c, String s, ToneGenerator gen, File file) throws IOException {
    Tone tone = new Tone(file);
    gen.writeTone(tone.file(), s, true);
    tone.generateToneTail(c, s);
    return tone;
  }

  protected static Tone generateTone(Context c, String s, ToneGenerator gen, Intent i) throws IOException {
    return generateTone(c, s, gen, getToneFilename(c, s, gen.filenameExt(), gen.filenameTypePrefix(), i));
  }

  protected static void tmpRename(File tone) {
    tmpfile(tone).renameTo(tone);
  }

  protected void expunge(Context c) {
    c.getContentResolver().delete(toneStoreUri(c), MediaStore.Audio.Media.DATA + " = ?",
                new String [] {file().getAbsolutePath()});
  }

  protected void delete(Context c) {
    expunge(c);
    file().delete();
  }

  protected Uri toneStoreUri(Context c) {
    return MediaStore.Audio.Media.getContentUriForPath(file().getAbsolutePath());
  }

  protected void generateToneTail(Context c, String s) {
    expunge(c);
    ContentValues storevalues = new ContentValues();
    storevalues.put(MediaStore.Audio.Media.DATA, file().getAbsolutePath());
    storevalues.put(MediaStore.Audio.Media.TITLE, s);
    storevalues.put(MediaStore.Audio.Media.IS_MUSIC, false);
    storevalues.put(MediaStore.Audio.Media.IS_ALARM, false);
    storevalues.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
    storevalues.put(MediaStore.Audio.Media.IS_RINGTONE, false);
    contenturi = c.getContentResolver().insert(toneStoreUri(c), storevalues);
  }

  protected void assign(Context c, Uri contacturi) {
    ContentValues values = new ContentValues();
    values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, contentUri().toString());
    c.getContentResolver().update(contacturi, values, null, null);
  }

  protected void assignDefault(Context context, Intent intent) {
    if (intent.getBooleanExtra(extrakeyRingtone, false))
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, contentUri());
    if (intent.getBooleanExtra(extrakeyNotification, false))
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, contentUri());
    if (intent.getBooleanExtra(extrakeyAlarm, false))
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, contentUri());

  }

  private static int waveReadInt(byte [] header, int offset) {
    return header[offset] | (header[offset+1]<<8) | (header[offset+2]<<16) | (header[offset+3]<<24);
  }

  private static short waveReadShort(byte [] header, int offset) {
    return (short) (header[offset] | (header[offset+1]<<8));
  }

  private static void waveWriteInt(byte [] header, int offset, int val) {
    header[offset] = (byte) (val & 255);
    header[offset+1] = (byte) ((val >> 8) & 255);
    header[offset+2] = (byte) ((val >> 16) & 255);
    header[offset+3] = (byte) ((val >> 24) & 255);
  }

  protected static void waveRepeat(File tone, int repeatCount) throws IOException {
    if (repeatCount > 0) {
      // TODO :: actually parse RIFF
      File tmp = tmpfile(tone);
      File samples = tmpfile(tmp);
      byte [] header = new byte[44];
      InputStream in = new BufferedInputStream(new FileInputStream(tone));
      OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
      in.read(header);

      final int subchunk2size = waveReadInt(header, 40);
      final int chunksize = waveReadInt(header, 4);

      waveWriteInt(header, 4, chunksize + (subchunk2size * (repeatCount-1)));
      waveWriteInt(header, 40, subchunk2size * repeatCount);

      out.write(header);

      final long len = tone.length() - 44;
      OutputStream tout = new BufferedOutputStream(new FileOutputStream(samples));
      for (long i = 0; i < len; i++)
        tout.write(in.read());

      in.close();
      tout.flush();
      tout.close();

      for (int i = 0; i <= repeatCount; i++) {
        InputStream tin = new BufferedInputStream(new FileInputStream(samples));
        for (long j = 0; j < len; j++)
          out.write(tin.read());
        tin.close();
      }

      out.flush();
      out.close();
      samples.delete();
      tone.delete();
      tmp.renameTo(tone);
    }
  }

  protected static void waveAppendSilence(File tone, int seconds) throws IOException {
    if (seconds > 0) {
      // TODO :: actually parse RIFF
      File tmp = tmpfile(tone);
      byte [] header = new byte[44];
      InputStream in = new BufferedInputStream(new FileInputStream(tone));
      OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
      in.read(header);

      final int byteRate = waveReadInt(header, 28);
      final short bitsPerSample = waveReadShort(header, 34);
      final int subchunk2size = waveReadInt(header, 40);
      final int chunksize = waveReadInt(header, 4);
      final int newSamples = 2 * byteRate;

      final byte silentByte = bitsPerSample == 8 ? Byte.MAX_VALUE : 0;

      waveWriteInt(header, 4, chunksize + newSamples);
      waveWriteInt(header, 40, subchunk2size + newSamples);

      out.write(header);

      final long len = tone.length() - 44;
      for (long i = 0; i < len; i++)
        out.write(in.read());

      for (long i = 0; i < newSamples; i++)
        out.write(silentByte);

      in.close();
      out.flush();
      out.close();
      tone.delete();
      tmp.renameTo(tone);
    }
  }
}
