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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * @author francesco
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	//TODO: Use LocalBroadcastManager instead?
	static final String ACTION_START_ALARM = "org.balau.fakedawn.AlarmReceiver.ACTION_START_ALARM";
	static final String ACTION_STOP_ALARM = "org.balau.fakedawn.AlarmReceiver.ACTION_STOP_ALARM";

	//TODO: synchronized access?
	private static WakeLock m_alarmWakeLock = null;
	private static final long WAKE_LOCK_TIMEOUT_MILLIS = 1000*10;

	private void releaseWakeLock(boolean expectedHeld) {
		if(AlarmReceiver.m_alarmWakeLock != null)
		{
			if(!expectedHeld)
				Log.w("FakeDawn", "ACTION_START_ALARM received but WakeLock already present.");
			if(AlarmReceiver.m_alarmWakeLock.isHeld())
			{
				if(!expectedHeld)
					Log.w("FakeDawn", "ACTION_START_ALARM received but WakeLock already held.");
				AlarmReceiver.m_alarmWakeLock.release();
				AlarmReceiver.m_alarmWakeLock = null;
			}
			else
			{
				if(expectedHeld)
					Log.w("FakeDawn", "ACTION_STOP_ALARM received but no WakeLock held.");
			}
		}
		else
		{
			if(expectedHeld)
				Log.w("FakeDawn", "ACTION_STOP_ALARM received but no WakeLock present.");
		}
	}
	
	public static Calendar getAlarmStart(SharedPreferences pref)
	{
		Calendar rightNow = Calendar.getInstance();

		long rightNowMillis = rightNow.getTimeInMillis();
		int hour = pref.getInt("dawn_start_hour", 8);
		int minute = pref.getInt("dawn_start_minute", 0);
		Calendar alarmStart = (Calendar) rightNow.clone();
		alarmStart.set(Calendar.HOUR_OF_DAY, hour);
		alarmStart.set(Calendar.MINUTE, minute);
		long halfDayMillis = 1000L*60L*60L*12L; 
		long alarmStartMillis;
		alarmStartMillis = alarmStart.getTimeInMillis();

		if(alarmStartMillis - rightNowMillis > halfDayMillis)
		{
			alarmStart.add(Calendar.DAY_OF_YEAR, -1);
		}
		else if(alarmStartMillis - rightNowMillis < -halfDayMillis)
		{
			alarmStart.add(Calendar.DAY_OF_YEAR, 1);
		}

		return alarmStart;
	}
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(ACTION_START_ALARM))
		{
			Log.d("FakeDawn", "ACTION_START_ALARM received.");
			
			// Tell Alarm Service to set next alarm.
			Intent updateAlarm = new Intent(context, Alarm.class);
			context.startService(updateAlarm);
			
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			releaseWakeLock(false);
			AlarmReceiver.m_alarmWakeLock = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK,
					"FakeDawn.AlarmReceiver");
			AlarmReceiver.m_alarmWakeLock.acquire(WAKE_LOCK_TIMEOUT_MILLIS); //TODO: use WakefulBroadcastReceiver instead?
			Intent openDawn = new Intent(context, Dawn.class);
			openDawn.setFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK|
					Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|
					Intent.FLAG_FROM_BACKGROUND);
			Log.d("FakeDawn", "Starting Dawn Activity.");
			context.startActivity(openDawn);
			//TODO: start sound service?
		}
		else if(intent.getAction().equals(ACTION_STOP_ALARM))
		{
			Log.d("FakeDawn", "ACTION_STOP_ALARM received.");
			releaseWakeLock(true);
			//TODO: stop service and activity?
		}
	}

}
