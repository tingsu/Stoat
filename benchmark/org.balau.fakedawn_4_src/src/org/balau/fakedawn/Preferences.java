/**
 *   Copyright 2012 Francesco Balducci
 *
 *   This file is part of FakeDawn.
 *
 *   FakeDawn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   FakeDawn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.balau.fakedawn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.balau.fakedawn.ColorPickerDialog.OnColorChangedListener;
import org.balau.fakedawn.TimeSlider.DawnTime;
import org.balau.fakedawn.TimeSlider.OnTimesChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TimePicker;
import android.widget.ToggleButton;

/**
 * @author francesco
 *
 */
public class Preferences extends Activity implements OnClickListener, OnSeekBarChangeListener, OnColorChangedListener, OnTimeSetListener, OnTimesChangedListener {

	private static final int REQUEST_PICK_SOUND = 0;
	private static final int COLOR_OPAQUE = 0xFF000000;
	private static final int COLOR_RGB_MASK = 0x00FFFFFF;

	private static final int TIME_DAWN_START = 0;
	private static final int TIME_DAWN_END = 1;
	private static final int TIME_SOUND_START = 2;
	private static final int TIME_SOUND_END = 3;

	private Uri m_soundUri;
	private VolumePreview m_preview = new VolumePreview();
	private int m_dawnColor;
	private int m_clickedTime;

	private Handler m_sliderResizerHandler = new Handler();
	private Runnable m_sliderResizer = new Runnable() {

		@Override
		public void run() {
			resizeSliders();
		}
	};
	private static final int RESIZE_SLIDERS_DELAY_MILLIS = 1000;
	private static final int SLIDERS_PADDING_MINUTES = 10;

	private boolean m_showHelp = false;
	private HelpListener m_helpListener = new HelpListener();

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preferences);

		Button saveButton = (Button) findViewById(R.id.buttonSave);
		saveButton.setOnClickListener(this);
		Button discardButton = (Button) findViewById(R.id.buttonDiscard);
		discardButton.setOnClickListener(this);
		Button soundButton = (Button) findViewById(R.id.buttonSound);
		soundButton.setOnClickListener(this);

		SeekBar seekBarVolume = (SeekBar)findViewById(R.id.seekBarVolume);
		seekBarVolume.setOnSeekBarChangeListener(this);

		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);

		CheckBox cb;

		ToggleButton alarmEnabledButton = (ToggleButton) findViewById(R.id.toggleButtonAlarmEnabled);
		alarmEnabledButton.setChecked(pref.getBoolean("enabled", false));

		TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);

		lightSlider.setOnClickListener(this);
		lightSlider.setOnTimesChangedListener(this);
		soundSlider.setOnClickListener(this);
		soundSlider.setOnTimesChangedListener(this);

		//TODO: defaults as fields
		DawnTime dawnStart = new DawnTime(
				pref.getInt("dawn_start_hour", 8),
				pref.getInt("dawn_start_minute", 0));
		lightSlider.setLeftTime(dawnStart.getMinutes() + pref.getInt("light_start", 0));
		lightSlider.setRightTime(dawnStart.getMinutes() + pref.getInt("light_max", 15));
		soundSlider.setLeftTime(dawnStart.getMinutes() + pref.getInt("sound_start", 15));
		soundSlider.setRightTime(dawnStart.getMinutes() + pref.getInt("sound_max", 30));

		cb = (CheckBox) findViewById(R.id.checkBoxMondays);
		cb.setChecked(pref.getBoolean("mondays", true));
		cb = (CheckBox) findViewById(R.id.checkBoxTuesdays);
		cb.setChecked(pref.getBoolean("tuesdays", true));
		cb = (CheckBox) findViewById(R.id.checkBoxWednesdays);
		cb.setChecked(pref.getBoolean("wednesdays", true));
		cb = (CheckBox) findViewById(R.id.checkBoxThursdays);
		cb.setChecked(pref.getBoolean("thursdays", true));
		cb = (CheckBox) findViewById(R.id.checkBoxFridays);
		cb.setChecked(pref.getBoolean("fridays", true));
		cb = (CheckBox) findViewById(R.id.checkBoxSaturdays);
		cb.setChecked(pref.getBoolean("saturdays", true));
		cb = (CheckBox) findViewById(R.id.checkBoxSundays);
		cb.setChecked(pref.getBoolean("sundays", true));

		updateColor(pref.getInt("color", 0x4040FF));

		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		seekBarVolume.setMax(maxVolume);
		int volume = pref.getInt("volume", maxVolume);
		if(volume < 0) volume = 0;
		if(volume > maxVolume) volume = maxVolume;
		seekBarVolume.setProgress(volume);

		ToggleButton vibrateButton = (ToggleButton) findViewById(R.id.toggleButtonVibrate);
		vibrateButton.setChecked(pref.getBoolean("vibrate", false));

		Uri sound = Uri.parse(
				pref.getString("sound", Settings.System.DEFAULT_ALARM_ALERT_URI.toString()));
		changeSound(sound);

		resizeSliders();

		String firstTimeVersion = pref.getString("first_time_version", "");
		String version = "";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_showHelp = firstTimeVersion == "" || !firstTimeVersion.equals(version);

		Log.d("FakeDawn", "Preferences loaded.");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if(m_showHelp)
		{
			showHelp();
		}
	}

	private void showHelp()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("Close", (DialogInterface.OnClickListener) m_helpListener);
		builder.setNeutralButton("Read License", (DialogInterface.OnClickListener) m_helpListener);
		String message = "";
		message = message.concat("");
		message += "Fake Dawn gradually increases brightness and sound volume to lead you out of deep sleep and wake you up gently.\n\n";
		message += "Choose when the brightness will start to rise and when it reaches the max using the first horizontal bar.\n\n";
		message += "Adjust when and how the sound will play using the second bar.";
		builder.setMessage(message);
		builder.create().show();
	}

	private class HelpListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {

			SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
			String version = "";
			try {
				version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("first_time_version", version);
			editor.commit();

			switch(which)
			{
			case DialogInterface.BUTTON_POSITIVE:
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				startActivity(new Intent(getApplicationContext(), License.class));
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}

	}

	private void resizeSliders()
	{
		TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);

		DawnTime light_start = lightSlider.getLeftTime();
		DawnTime light_end = lightSlider.getRightTime();
		DawnTime sound_start;
		DawnTime sound_end;

		if(soundSlider.isEnabled())
		{
			sound_start = soundSlider.getLeftTime();
			sound_end  = soundSlider.getRightTime();
		}
		else
		{
			//If disabled, it follows the end of the light slider.
			sound_start = lightSlider.getRightTime();
			sound_end  = new DawnTime(sound_start.getMinutes());
			soundSlider.setLeftTime(sound_start.getHour(), sound_start.getMinute());
			soundSlider.setRightTime(sound_end.getHour(), sound_end.getMinute());
		}

		int minTime = Math.max(
				Math.min(
						light_start.getMinutes(),
						sound_start.getMinutes()) - SLIDERS_PADDING_MINUTES,
						0);
		int maxTime = Math.max(
				light_end.getMinutes(),
				sound_end.getMinutes()) + SLIDERS_PADDING_MINUTES;

		int minutes_in_day = 60*24;
		if(minTime + SLIDERS_PADDING_MINUTES >= minutes_in_day)
		{
			// shift everything to the day
			int days = (minTime+SLIDERS_PADDING_MINUTES)/minutes_in_day; //floor
			int minutes_to_subtract = days*minutes_in_day;

			minTime = Math.max(minTime - minutes_to_subtract, 0);
			maxTime -= minutes_to_subtract;
			light_start = new DawnTime(light_start.getMinutes() - minutes_to_subtract);
			light_end = new DawnTime(light_end.getMinutes() - minutes_to_subtract);
			sound_start = new DawnTime(sound_start.getMinutes() - minutes_to_subtract);
			sound_end = new DawnTime(sound_end.getMinutes() - minutes_to_subtract);

			lightSlider.setLeftTime(light_start.getHour(), light_start.getMinute());
			lightSlider.setRightTime(light_end.getHour(), light_end.getMinute());
			soundSlider.setLeftTime(sound_start.getHour(), sound_start.getMinute());
			soundSlider.setRightTime(sound_end.getHour(), sound_end.getMinute());

		}
		DawnTime start = new DawnTime(minTime);

		lightSlider.setStartTime(start.getHour(), start.getMinute());
		lightSlider.setSpanTime(maxTime - minTime);

		soundSlider.setStartTime(start.getHour(), start.getMinute());
		soundSlider.setSpanTime(maxTime - minTime);

	}

	private void updateColor(int color)
	{
		m_dawnColor = color & COLOR_RGB_MASK;
		TimeSlider ts = (TimeSlider)findViewById(R.id.timeSlider1);
		ts.setRectColor(m_dawnColor|COLOR_OPAQUE);
	}

	public void onClick(View v) {
		ColorPickerDialog colorDialog;
		TimeSlider ts;
		TimePickerDialog tpd;

		switch(v.getId())
		{
		case R.id.buttonSave:
			SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
			TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);
			int dawn_start_minutes;
			if(lightSlider.getLeftTime().getMinutes() < soundSlider.getLeftTime().getMinutes())
			{
				editor.putInt("dawn_start_hour", lightSlider.getLeftTime().getHourOfDay());
				editor.putInt("dawn_start_minute", lightSlider.getLeftTime().getMinute());
				dawn_start_minutes = lightSlider.getLeftTime().getMinutes();
			}
			else
			{
				editor.putInt("dawn_start_hour", soundSlider.getLeftTime().getHourOfDay());
				editor.putInt("dawn_start_minute", soundSlider.getLeftTime().getMinute());
				dawn_start_minutes = soundSlider.getLeftTime().getMinutes();
			}

			editor.putInt("color", m_dawnColor);


			CheckBox cb;

			ToggleButton alarmEnabledButton = (ToggleButton) findViewById(R.id.toggleButtonAlarmEnabled);
			editor.putBoolean("enabled", alarmEnabledButton.isChecked());

			cb = (CheckBox) findViewById(R.id.checkBoxMondays);
			editor.putBoolean("mondays", cb.isChecked());
			cb = (CheckBox) findViewById(R.id.checkBoxTuesdays);
			editor.putBoolean("tuesdays", cb.isChecked());
			cb = (CheckBox) findViewById(R.id.checkBoxWednesdays);
			editor.putBoolean("wednesdays", cb.isChecked());
			cb = (CheckBox) findViewById(R.id.checkBoxThursdays);
			editor.putBoolean("thursdays", cb.isChecked());
			cb = (CheckBox) findViewById(R.id.checkBoxFridays);
			editor.putBoolean("fridays", cb.isChecked());
			cb = (CheckBox) findViewById(R.id.checkBoxSaturdays);
			editor.putBoolean("saturdays", cb.isChecked());
			cb = (CheckBox) findViewById(R.id.checkBoxSundays);
			editor.putBoolean("sundays", cb.isChecked());

			editor.putInt("light_start",lightSlider.getLeftTime().getMinutes() - dawn_start_minutes);
			editor.putInt("light_max",lightSlider.getRightTime().getMinutes() - dawn_start_minutes);

			if(m_soundUri == null)
			{
				editor.putString("sound", "");
			}
			else
			{
				editor.putString("sound", m_soundUri.toString());
			}

			editor.putInt("sound_start",soundSlider.getLeftTime().getMinutes() - dawn_start_minutes);
			editor.putInt("sound_max",soundSlider.getRightTime().getMinutes() - dawn_start_minutes);

			SeekBar sb = (SeekBar)findViewById(R.id.seekBarVolume);
			editor.putInt("volume", sb.getProgress());

			ToggleButton vibrateButton = (ToggleButton) findViewById(R.id.toggleButtonVibrate);
			editor.putBoolean("vibrate", vibrateButton.isChecked());

			editor.commit();

			Intent updateAlarm = new Intent(getApplicationContext(), Alarm.class);
			updateAlarm.putExtra(Alarm.EXTRA_SHOW_TOAST, true);
			getApplicationContext().startService(updateAlarm);
			Log.d("FakeDawn", "Preferences saved.");
			finish();
			break;
		case R.id.buttonDiscard:
			finish();
			break;
		case R.id.buttonSound:
			Intent pickSound = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			pickSound.putExtra(
					RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
					true);
			pickSound.putExtra(
					RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
					false);
			pickSound.putExtra(
					RingtoneManager.EXTRA_RINGTONE_TYPE,
					RingtoneManager.TYPE_ALL);
			pickSound.putExtra(
					RingtoneManager.EXTRA_RINGTONE_TITLE,
					"Pick Alarm Sound");
			if(m_soundUri != null)
			{
				pickSound.putExtra(
						RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
						m_soundUri);
			}
			startActivityForResult(pickSound, REQUEST_PICK_SOUND);
			break;
		case R.id.timeSlider1:
			ts = (TimeSlider)v;
			switch(ts.getLastTouched())
			{
			case TimeSlider.TOUCH_ALL:
				colorDialog = new ColorPickerDialog(this, this, m_dawnColor);
				colorDialog.show();
				break;
			case TimeSlider.TOUCH_LEFT:
				tpd = new TimePickerDialog(
						this, 
						this, 
						ts.getLeftTime().getHourOfDay(),
						ts.getLeftTime().getMinute(),
						true);
				m_clickedTime = TIME_DAWN_START;
				tpd.show();
				break;
			case TimeSlider.TOUCH_RIGHT:
				tpd = new TimePickerDialog(
						this, 
						this, 
						ts.getRightTime().getHourOfDay(),
						ts.getRightTime().getMinute(),
						true);
				m_clickedTime = TIME_DAWN_END;
				tpd.show();
				break;
			}
			break;
		case R.id.timeSlider2:
			ts = (TimeSlider)v;
			switch(ts.getLastTouched())
			{
			case TimeSlider.TOUCH_ALL:
				break;
			case TimeSlider.TOUCH_LEFT:
				tpd = new TimePickerDialog(
						this, 
						this, 
						ts.getLeftTime().getHourOfDay(),
						ts.getLeftTime().getMinute(),
						true);
				m_clickedTime = TIME_SOUND_START;
				tpd.show();
				break;
			case TimeSlider.TOUCH_RIGHT:
				tpd = new TimePickerDialog(
						this, 
						this, 
						ts.getRightTime().getHourOfDay(),
						ts.getRightTime().getMinute(),
						true);
				m_clickedTime = TIME_SOUND_END;
				tpd.show();
				break;
			}
			break;
		}
	}

	private void setSoundViewsEnabled(boolean enabled)
	{
		SeekBar seekBarVolume = (SeekBar)findViewById(R.id.seekBarVolume);
		ToggleButton vibrateButton = (ToggleButton) findViewById(R.id.toggleButtonVibrate);
		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);

		seekBarVolume.setEnabled(enabled);
		vibrateButton.setEnabled(enabled);
		soundSlider.setEnabled(enabled);
	}

	private void setSoundButtonText(String text)
	{
		Button soundButton = (Button) findViewById(R.id.buttonSound);
		soundButton.setText(text);
	}

	private void disableSound()
	{
		setSoundButtonText("Silent");
		setSoundViewsEnabled(false);
		m_preview.stop();
	}

	private void enableSound(Uri sound)
	{
		setSoundButtonText(
				RingtoneManager.getRingtone(this, sound).getTitle(this));
		setSoundViewsEnabled(true);
		m_preview.setSoundUri(this, sound);
	}

	private void changeSound(Uri sound)
	{
		Uri sanitizedSound;
		if (sound == null || sound.toString().isEmpty())
		{
			sanitizedSound = null;
		}
		else
		{
			sanitizedSound = checkSound(this, sound);
		}

		if (sanitizedSound == null)
		{
			m_soundUri = null;
			disableSound();
		}
		else
		{
			m_soundUri = sanitizedSound;
			enableSound(sanitizedSound);
		}
	}

	public static Uri checkSound(Context context, Uri sound) {
		//TODO: move in other class?
		Uri[] sounds = {
				sound,
				Settings.System.DEFAULT_ALARM_ALERT_URI,
				Settings.System.DEFAULT_RINGTONE_URI,
				Settings.System.DEFAULT_NOTIFICATION_URI,
		};
		for (Uri s: sounds)
		{
			try {
				InputStream tmp = context.getContentResolver().openInputStream(s);
				tmp.close();
				//TODO: toast if not first.
				return s;
			} catch (FileNotFoundException e) {
				continue;
			} catch (IOException e) {
				continue;
			}
		}
		//TODO: error toast 
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_PICK_SOUND)
		{
			if(resultCode == RESULT_OK)
			{
				Uri sound = (Uri) data.getParcelableExtra(
						RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				changeSound(sound);
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(seekBar.getId() == R.id.seekBarVolume)
		{
			if(fromUser)
			{
				m_preview.previewVolume(progress);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		m_preview.stop();
		m_sliderResizerHandler.removeCallbacks(m_sliderResizer);
	}

	private class VolumePreview implements OnCompletionListener, OnErrorListener {

		/**
		 * 
		 */
		public VolumePreview() {
			m_player.setOnErrorListener(this);
			m_player.setOnCompletionListener(this);
		}

		private MediaPlayer m_player = new MediaPlayer();
		private boolean m_playerReady = false;

		public void setSoundUri(Context context, Uri soundUri) {
			m_player.reset();
			m_player.setAudioStreamType(AudioManager.STREAM_ALARM);
			if(soundUri != null)
			{
				try {
					m_player.setDataSource(context, soundUri);
					m_playerReady = true;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					Log.e("FakeDawn", "setSoundUri", e);
				} catch (SecurityException e) {
					e.printStackTrace();
					Log.e("FakeDawn", "setSoundUri", e);
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.e("FakeDawn", "setSoundUri", e);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("FakeDawn", "setSoundUri", e);
				}
			}
		}

		public void stop()
		{
			if(m_playerReady)
			{
				if(m_player.isPlaying())
				{
					m_player.stop();
				}
			}
		}

		public void previewVolume(int volume)
		{
			if(m_playerReady)
			{
				try {
					if(!m_player.isPlaying())
					{
						m_player.prepare();
						m_player.start();
					}
					AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
					int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
					if(volume < 0) volume = 0;
					if(volume > maxVolume) volume = maxVolume;
					am.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.e("FakeDawn", "previewVolume", e);
					m_playerReady = false;
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("FakeDawn", "previewVolume", e);
					m_playerReady = false;
				}
			}
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e("FakeDawn", String.format("MediaPlayer error. what: %d, extra: %d", what, extra));
			mp.reset();
			m_playerReady = false;
			return true;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.stop();
		}

	}

	@Override
	public void colorChanged(int color) {
		updateColor(color);		
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);
		DawnTime dt = new DawnTime(hourOfDay, minute);
		int new_minutes = dt.getMinutes();
		int delta_minutes = 0;

		switch(m_clickedTime)
		{
		case TIME_DAWN_START:
			delta_minutes = new_minutes - lightSlider.getLeftTime().getMinutes(); 
			break;
		case TIME_DAWN_END:
			delta_minutes = new_minutes - lightSlider.getRightTime().getMinutes(); 
			break;
		case TIME_SOUND_START:
			delta_minutes = new_minutes - soundSlider.getLeftTime().getMinutes(); 
			break;
		case TIME_SOUND_END:
			delta_minutes = new_minutes - soundSlider.getRightTime().getMinutes(); 
			break;
		}
		// We need to distinguish the direction, because a marker can't be set to surpass the other.
		// Left marker must stay left, right marker must stay right.
		if (delta_minutes > 0)
		{
			// We shift everything to the right, so we move first the right markers, then the left ones.
			lightSlider.setRightTime(lightSlider.getRightTime().getMinutes() + delta_minutes);
			soundSlider.setRightTime(soundSlider.getRightTime().getMinutes() + delta_minutes);
			lightSlider.setLeftTime(lightSlider.getLeftTime().getMinutes() + delta_minutes);
			soundSlider.setLeftTime(soundSlider.getLeftTime().getMinutes() + delta_minutes);
		}
		else if (delta_minutes < 0)
		{
			// We shift everything to the left, so we move first the left markers, then the right ones.
			lightSlider.setLeftTime(lightSlider.getLeftTime().getMinutes() + delta_minutes);
			soundSlider.setLeftTime(soundSlider.getLeftTime().getMinutes() + delta_minutes);
			lightSlider.setRightTime(lightSlider.getRightTime().getMinutes() + delta_minutes);
			soundSlider.setRightTime(soundSlider.getRightTime().getMinutes() + delta_minutes);
		}
		resizeSliders();
	}

	@Override
	public void onTimesChanged(TimeSlider s) {
		m_sliderResizerHandler.removeCallbacks(m_sliderResizer);
		m_sliderResizerHandler.postDelayed(m_sliderResizer, RESIZE_SLIDERS_DELAY_MILLIS);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_MENU)
		{
			showHelp();
			return true;
		}
		else
			return super.onKeyDown(keyCode, event);
	}
}
