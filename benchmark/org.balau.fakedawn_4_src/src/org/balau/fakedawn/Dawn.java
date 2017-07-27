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

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class Dawn extends Activity implements OnClickListener {

	private static int BRIGHTNESS_UPDATE_MILLIS = 10*1000;
	private static final String ALARM_START_MILLIS = "ALARM_START_MILLIS";
	private static int COLOR_OPAQUE = 0xFF000000;

	private long m_alarmStartMillis;
	private long m_alarmEndMillis;
	private Handler m_brightnessUpdaterHandler = new Handler();
	private Runnable m_brightnessUpdater = new Runnable() {
		
		@Override
		public void run() {
			updateBrightness(System.currentTimeMillis());
			m_brightnessUpdaterHandler.postDelayed(m_brightnessUpdater, BRIGHTNESS_UPDATE_MILLIS);
		}
	};

	private int m_dawnColor;
	private boolean m_ending;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dawn);
		Window mainWindow = getWindow();
		mainWindow.addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN|
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		mainWindow.clearFlags(
				WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		WindowManager.LayoutParams mainWindowParams = mainWindow.getAttributes();
		mainWindowParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		mainWindowParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		mainWindow.setAttributes(mainWindowParams);

		findViewById(R.id.dawn_background).setOnClickListener(this);

		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
		Calendar alarmStart = AlarmReceiver.getAlarmStart(pref);

		long dawnStartMillis = alarmStart.getTimeInMillis();
		m_alarmStartMillis =
				dawnStartMillis + (pref.getInt("light_start", 0)*1000L*60L);
		if(savedInstanceState != null)
		{
			if(savedInstanceState.containsKey(ALARM_START_MILLIS))
			{
				m_alarmStartMillis = savedInstanceState.getLong(ALARM_START_MILLIS);
			}
		}
		m_alarmEndMillis = dawnStartMillis + (1000L*60L*pref.getInt("light_max", 15));

		//TODO: consistent default preferences.
		m_dawnColor = pref.getInt("color", 0x4040FF);
		Intent sound = new Intent(getApplicationContext(), DawnSound.class);
		sound.putExtra(DawnSound.EXTRA_INTENT_TYPE, DawnSound.EXTRA_INTENT_TYPE_START);
		sound.putExtra(DawnSound.EXTRA_VIBRATE, pref.getBoolean("vibrate", false));
		long soundStart = dawnStartMillis + (pref.getInt("sound_start", 15)*1000L*60L);
		long soundEnd = dawnStartMillis + (pref.getInt("sound_max", 15)*1000L*60L);
		sound.putExtra(DawnSound.EXTRA_SOUND_START_MILLIS, soundStart);
		sound.putExtra(DawnSound.EXTRA_SOUND_END_MILLIS, soundEnd);
		sound.putExtra(DawnSound.EXTRA_SOUND_URI, 
				pref.getString("sound", Settings.System.DEFAULT_ALARM_ALERT_URI.toString()));
		if(pref.contains("volume"))
		{
			sound.putExtra(DawnSound.EXTRA_SOUND_VOLUME, pref.getInt("volume", 0));
		}
		startService(sound);

		updateBrightness(System.currentTimeMillis());

	}

	private void endDawn()
	{
		Context appContext = getApplicationContext();
		Intent sound = new Intent(appContext, DawnSound.class);
		stopService(sound);
		Intent stopAlarm = new Intent(appContext, AlarmReceiver.class);
		stopAlarm.setAction(AlarmReceiver.ACTION_STOP_ALARM);
		appContext.sendBroadcast(stopAlarm);
		m_ending = true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		endDawn();
		this.finish();
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View v) {
		endDawn();
		this.finish();
	}

	private int getColor(int rgb, int percent)
	{
		int r, g, b;
		int rgb_new;

		r = (rgb >> 16)&0xFF;
		g = (rgb >>  8)&0xFF;
		b = (rgb >>  0)&0xFF;

		if(percent > 100) percent = 100;
		if(percent < 0) percent = 0;

		r = (r*percent)/100;
		g = (g*percent)/100;
		b = (b*percent)/100;

		rgb_new = (r<<16) | (g<<8) | (b<<0);

		return rgb_new;
	}

	private void updateBrightness(long currentTimeMillis)
	{
		long level_percent;
		long millis_from_start;
		long dawnDurationMillis;
		int rgb;

		millis_from_start = currentTimeMillis - m_alarmStartMillis; 
		dawnDurationMillis = m_alarmEndMillis - m_alarmStartMillis; 
		if(dawnDurationMillis > 0)
		{
			level_percent = (100 * millis_from_start) / dawnDurationMillis;
			if(level_percent < 0) level_percent = 0;
			if(level_percent > 100) level_percent = 100;
		}
		else
		{
			level_percent = (millis_from_start >= 0)?100:0;
		}
		rgb = COLOR_OPAQUE | getColor(m_dawnColor, (int)level_percent);
		findViewById(R.id.dawn_background).setBackgroundColor(rgb);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		m_brightnessUpdater.run();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		m_brightnessUpdaterHandler.removeCallbacks(m_brightnessUpdater);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (!m_ending)
		{
			Intent sound = new Intent(getApplicationContext(), DawnSound.class);
			sound.putExtra(DawnSound.EXTRA_INTENT_TYPE, DawnSound.EXTRA_INTENT_TYPE_INACTIVE);
			startService(sound);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!m_ending)
		{
			Intent sound = new Intent(getApplicationContext(), DawnSound.class);
			sound.putExtra(DawnSound.EXTRA_INTENT_TYPE, DawnSound.EXTRA_INTENT_TYPE_ACTIVE);
			startService(sound);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ALARM_START_MILLIS, m_alarmStartMillis);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!m_ending)
		{
			endDawn();
		}
	}

}
