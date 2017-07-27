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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Alarm extends Service {

	public static final String EXTRA_SHOW_TOAST = "org.balau.fakedawn.Alarm.EXTRA_SHOW_TOAST";
	private static final long TOLERANCE_MILLIS = 1000*10;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		boolean showToast;
		if(intent != null)
		{
			showToast = intent.getBooleanExtra(EXTRA_SHOW_TOAST, false);
		}
		else // intent is null when Service is restarted
		{
			showToast = false;
		}

		cancel();
		String message;
		if(getPreferences().getBoolean("enabled", false))
		{
			Calendar nextAlarmTime = getNextAlarmTime();
			if (nextAlarmTime == null)
			{
				message = "No week day selected! Fake Dawn Alarm Disabled.";	
			}
			else
			{
				set(nextAlarmTime);
				message = nextAlarmMessage(nextAlarmTime);
			}
		}
		else
		{
			message = "Fake Dawn Alarm Disabled.";
		}
		Log.d("FakeDawn", message);
		if(showToast)
		{
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
		}
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}
	
	private PendingIntent getOpenDawnPendingIntent()
	{
		Intent openDawn = new Intent(AlarmReceiver.ACTION_START_ALARM);
		PendingIntent openDawnPendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 
				0, 
				openDawn,
				0);
		return openDawnPendingIntent;
	}
	
	private AlarmManager getAlarmManager()
	{
		return (AlarmManager) getSystemService(ALARM_SERVICE);
	}
	
	private SharedPreferences getPreferences()
	{
		return getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
	}
	
	private void cancel()
	{
		getAlarmManager().cancel(
				getOpenDawnPendingIntent());
	}
	
	public static boolean shouldFire(SharedPreferences pref, int dayOfWeek)
	{
		String day;
		//TODO: dictionary?
		switch (dayOfWeek) {
		case Calendar.MONDAY:
			day = "mondays";
			break;
		case Calendar.TUESDAY:
			day = "tuesdays";
			break;
		case Calendar.WEDNESDAY:
			day = "wednesdays";
			break;
		case Calendar.THURSDAY:
			day = "thursdays";
			break;
		case Calendar.FRIDAY:
			day = "fridays";
			break;
		case Calendar.SATURDAY:
			day = "saturdays";
			break;
		case Calendar.SUNDAY:
			day = "sundays";
			break;
		default:
			day = "NON_EXISTING_WEEKDAY";
			break;
		}
		return pref.getBoolean(day, false);
	}
	
	private boolean shouldFire(int dayOfWeek)
	{
		return shouldFire(getPreferences(), dayOfWeek);
	}
	
	private Calendar getNextAlarmTime()
	{
		SharedPreferences pref = getPreferences();
		Calendar nextAlarmTime = Calendar.getInstance();
		nextAlarmTime.set(Calendar.HOUR_OF_DAY, pref.getInt("dawn_start_hour", 8));
		nextAlarmTime.set(Calendar.MINUTE, pref.getInt("dawn_start_minute", 0));
		nextAlarmTime.set(Calendar.SECOND, 0);
		if(nextAlarmTime.getTimeInMillis() < System.currentTimeMillis() + TOLERANCE_MILLIS)
		{
			nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
			//TODO: check if enough?
		}
		int ndays = 0;
		while(!shouldFire(nextAlarmTime.get(Calendar.DAY_OF_WEEK)))
		{
			nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
			ndays++;
			if(ndays >= 7) // No weekday is ticked.
			{
				return null;
			}
		}
		
		return nextAlarmTime;
	}
	
	private void set(AlarmManager alarmManager, int type, long triggerAtMillis, PendingIntent operation)
	{
    	// API 19 changed set() behaviour and added setExact
		// https://developer.android.com/reference/android/app/AlarmManager.html#set(int, long, android.app.PendingIntent)
		// Using setExact if it exists, otherwise fall back to set.
	    try {
	        Method setExact = AlarmManager.class.getDeclaredMethod(
	            "setExact", int.class, long.class, PendingIntent.class);
	        setExact.invoke(alarmManager, type,
	        		triggerAtMillis, operation);
	      } catch (NoSuchMethodException e) {
	        alarmManager.set(type,
	        		triggerAtMillis, operation);
	      } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	      } catch (IllegalArgumentException e) {
	        throw new RuntimeException(e);
	      } catch (InvocationTargetException e) {
	        throw new RuntimeException(e);
	      }
	}
	
	private void set(Calendar nextAlarmTime)
	{
		AlarmManager alarmManager = getAlarmManager();
		PendingIntent openDawnIntent = getOpenDawnPendingIntent();
		set(
				alarmManager,
				AlarmManager.RTC_WAKEUP, 
				nextAlarmTime.getTimeInMillis(), 
				openDawnIntent);
	}
	
	private String getPlural(long n, String name)
	{
		String plural;
		if(n != 1)
		{
			plural = "s";
		}
		else
		{
			plural = "";
		}
		return String.format("%d %s%s", n, name, plural);
	}

	private String buildMessage(String majorTime, String minorTime)
	{
		return String.format(
				"Fake Dawn starting in %s and %s.",
				majorTime,
				minorTime);
	}
	
	private String nextAlarmMessage(Calendar nextAlarmTime)
	{
		long elapsed = nextAlarmTime.getTimeInMillis() - System.currentTimeMillis();
		long dayMillis = 1000*60*60*24;
		long elapsedDays = elapsed / dayMillis;
		elapsed -= elapsedDays * dayMillis;
		long hourMillis = 1000*60*60;
		long elapsedHours = elapsed / hourMillis;
		String message;
		if (elapsedDays > 0)
		{
			message = buildMessage(
					getPlural(elapsedDays, "day"),
					getPlural(elapsedHours, "hour"));
		}
		else
		{
			elapsed -= elapsedHours * hourMillis;
			long minuteMillis = 1000*60;
			long elapsedMinutes = elapsed / minuteMillis;
			if (elapsedHours > 0)
			{
				message = buildMessage(
						getPlural(elapsedHours, "hour"),
						getPlural(elapsedMinutes, "minute"));
			}
			else
			{
				elapsed -= elapsedMinutes * minuteMillis;
				long secondMillis = 1000;
				long elapsedSeconds = elapsed / secondMillis;
				message = buildMessage(
						getPlural(elapsedMinutes, "minute"),
						getPlural(elapsedSeconds, "second"));
			}
		}
		return message;
	}
}
