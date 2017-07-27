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

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

/**
 * @author francesco
 *
 */
public class DawnSound extends Service implements OnCompletionListener, OnErrorListener {

	public static final String EXTRA_SOUND_URI = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_URI";
	public static final String EXTRA_SOUND_START_MILLIS = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_START_MILLIS";
	public static final String EXTRA_SOUND_END_MILLIS = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_END_MILLIS";
	public static final String EXTRA_SOUND_VOLUME = "org.balau.fakedawn.DawnSound.EXTRA_SOUND_VOLUME";
	public static final String EXTRA_VIBRATE = "org.balau.fakedawn.DawnSound.EXTRA_VIBRATE";
	public static final String EXTRA_INTENT_TYPE = "org.balau.fakedawn.DawnSound.EXTRA_INTENT_TYPE";
	public static final String EXTRA_INTENT_TYPE_START = "org.balau.fakedawn.DawnSound.EXTRA_INTENT_TYPE_START";
	public static final String EXTRA_INTENT_TYPE_INACTIVE = "org.balau.fakedawn.DawnSound.EXTRA_INTENT_TYPE_INACTIVE";
	public static final String EXTRA_INTENT_TYPE_ACTIVE = "org.balau.fakedawn.DawnSound.EXTRA_INTENT_TYPE_ACTIVE";

	private static final int TIMER_VOLUME_UPDATE_MILLIS = 10*1000;
	private static final long TIMEOUT_INACTIVE_MILLIS = 10*1000;

	private Handler m_volumeUpdateHandler = new Handler();
	private Runnable m_volumeUpdater = new Runnable() {
		
		@Override
		public void run() {
			if(m_soundInitialized)
			{
				updateVolume(System.currentTimeMillis());
				if(!m_player.isPlaying())
				{
					m_player.start();
				}
				if(m_vibrate)
				{
					m_vibrator.vibrate(m_vibratePattern, 0);
				}
				m_volumeUpdateHandler.postDelayed(m_volumeUpdater, TIMER_VOLUME_UPDATE_MILLIS);
			}
		}
	};
	private Handler m_inactiveTimeoutHandler = new Handler();
	private Runnable m_inactiveTimeout = new Runnable() {
		
		@Override
		public void run() {
			Log.d("FakeDawn", "Dawn inactive timeout.");
			stopSelf();
		}
	};
	
	private long m_soundStartMillis;
	private long m_soundEndMillis;
	private MediaPlayer m_player = new MediaPlayer();
	private boolean m_soundInitialized = false;
	private int m_volume;
	private Uri m_soundUri;

	private Vibrator m_vibrator = null;
	private boolean m_vibrate = false;
	private long[] m_vibratePattern = {0, 1000, 1000};

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("FakeDawn", "DawnSound onDestroy");
		if(m_soundInitialized)
		{
			m_soundInitialized = false;
			if(m_player.isPlaying())
			{
				m_player.stop();
			}
		}
		m_volumeUpdateHandler.removeCallbacks(m_volumeUpdater);
		m_inactiveTimeoutHandler.removeCallbacks(m_inactiveTimeout);
		if(m_vibrate)
		{
			m_vibrate = false;
			m_vibrator.cancel();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String intentType;
		int result = Service.START_REDELIVER_INTENT;
		
		intentType = intent.getStringExtra(EXTRA_INTENT_TYPE);
		if (intentType.equals(EXTRA_INTENT_TYPE_START))
		{
			result = onIntentStart(intent, flags, startId);
		}
		else if(intentType.equals(EXTRA_INTENT_TYPE_ACTIVE))
		{
			result = onIntentActive(intent, flags, startId);
		}
		else if(intentType.equals(EXTRA_INTENT_TYPE_INACTIVE))
		{
			result = onIntentInactive(intent, flags, startId);
		}
		else
		{
			Log.e("FakeDawn", String.format("DawnSound received intent with unknown type '%s'", intentType));
		}
		return result;
	}

	private int onIntentInactive(Intent intent, int flags, int startId) {
		m_inactiveTimeoutHandler.removeCallbacks(m_inactiveTimeout);
		m_inactiveTimeoutHandler.postDelayed(m_inactiveTimeout, TIMEOUT_INACTIVE_MILLIS);
		Log.d("FakeDawn", "Dawn is inactive, setting timeout to stop sound...");
		return START_STICKY; //TODO: check for problems when start intent is sent, then inactive is sent and then service is killed.
	}

	private int onIntentActive(Intent intent, int flags, int startId) {
		m_inactiveTimeoutHandler.removeCallbacks(m_inactiveTimeout);
		Log.d("FakeDawn", "Dawn is now active, cancelling timeout.");
		return START_STICKY;  //TODO: check for problems when start intent is sent, then active is sent and then service is killed.
	}

	private void configure(Intent intent)
	{
		m_soundStartMillis = intent.getLongExtra(EXTRA_SOUND_START_MILLIS, 0);
		m_soundEndMillis = intent.getLongExtra(EXTRA_SOUND_END_MILLIS, 0);
		m_vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, false);
		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		m_volume = intent.getIntExtra(EXTRA_SOUND_VOLUME, maxVolume);
		if(m_volume < 0) m_volume = 0;
		if(m_volume > maxVolume) m_volume = maxVolume;
		String sound = intent.getStringExtra(EXTRA_SOUND_URI);
		if (sound.equals(""))
		{
			m_soundUri = null;
		}
		else
		{
			m_soundUri = Preferences.checkSound(this, Uri.parse(sound));
		}
	}
	
	private int onIntentStart(Intent intent, int flags, int startId) {

		if(!m_soundInitialized)
		{
			//TODO: move in onCreate
			m_player.setOnCompletionListener(this);
			m_player.setOnErrorListener(this);
			m_player.reset();
			m_player.setAudioStreamType(AudioManager.STREAM_ALARM);

			configure(intent);
			if(m_soundUri != null)
			{
				int volume = m_volume;
				AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
				am.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
				//TODO: move in common function that prepares MediaPlayer
				try {
					m_player.setDataSource(this, m_soundUri);
					m_player.prepare();
					m_player.setLooping(true);
					m_soundInitialized = true;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(m_soundInitialized)
			{
				if(m_vibrate)
				{
					m_vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					if(m_vibrator == null)
					{
						m_vibrate = false;
					}
					else
					{
						//TODO: check hasVibrator with reflection.
						//m_vibrate = m_vibrator.hasVibrator();
					}
				}
				long delay = m_soundStartMillis - System.currentTimeMillis();
				if (delay < 0) {
					delay = 0;
				}
				m_volumeUpdateHandler.postDelayed(m_volumeUpdater, delay);
				Log.d("FakeDawn", 
						String.format("Sound scheduled in %d seconds.", delay/1000));
			}
		}
		return START_REDELIVER_INTENT;
	}

	private void updateVolume(long currentTimeMillis)
	{
		float volume;
		long millis_from_start;
		long soundRiseDurationMillis;
		
		millis_from_start = currentTimeMillis - m_soundStartMillis;
		soundRiseDurationMillis = m_soundEndMillis - m_soundStartMillis;
		if(soundRiseDurationMillis > 0)
		{
			volume = Math.max(
					0.0F,
					Math.min(
							1.0F,
							((float)millis_from_start)/((float)soundRiseDurationMillis))
					);
		}
		else
		{
			volume = (millis_from_start >= 0)?1.0F:0.0F;
		}
		m_player.setVolume(volume, volume);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("FakeDawn", String.format("MediaPlayer error. what: %d, extra: %d", what, extra));
		m_soundInitialized = false;
		mp.reset();
		m_volumeUpdateHandler.removeCallbacks(m_volumeUpdater);
		stopSelf();
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.w("FakeDawn", "Sound completed even if looping.");
		if(m_soundInitialized)
		{
			mp.start();
		}
	}	
}
