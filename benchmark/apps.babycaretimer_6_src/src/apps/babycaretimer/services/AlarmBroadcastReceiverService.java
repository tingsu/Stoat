package apps.babycaretimer.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.receivers.AlarmReceiver;
import apps.babycaretimer.services.WakefulIntentService;

/**
 * This class does the work of the BroadcastReceiver.
 * 
 * @author Camille Sévigny
 */
public class AlarmBroadcastReceiverService extends WakefulIntentService {
	
	//================================================================================
    // Properties
    //================================================================================
	
	boolean _debug = false;
	Context _context = null;

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Class Constructor.
	 */
	public AlarmBroadcastReceiverService() {
		super("AlarmBroadcastReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmBroadcastReceiverService.AlarmBroadcastReceiverService()");
	}

	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Do the work for the service inside this function.
	 * 
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmBroadcastReceiverService.doWakefulWork()");
		_context = getApplicationContext();
		Bundle bundle = intent.getExtras();
		int alarmType = bundle.getInt(Constants.ALARM_TYPE);
		boolean alarmSnooze = bundle.getBoolean(Constants.ALARM_SNOOZE);
		if(!Common.isAlarmActive(_context, alarmType)){
			if (_debug) Log.v("AlarmReceiver.onReceive() Alarm is not active...");
			return;
		}
		if(!Common.isTimerActive(_context, alarmType)){
			if (_debug) Log.v("AlarmReceiver.onReceive() Timer is not active...");
			return;
		}
		if(alarmSnooze){
			if(!Common.isAlarmSnoozed(_context, alarmType)){
				if (_debug) Log.v("AlarmReceiver.onReceive() Alarm is a snooze alarm but the snooze has been cancelled...");
				return;
			}
		}else{
			if(!elapsedTimeReached(alarmType)){
				if (_debug) Log.v("AlarmReceiver.onReceive() Elapsed time has not been reached yet...");
				return;
			}
		}			
		Intent alarmReceiverServiceIntent = new Intent(_context, AlarmReceiverService.class);
		alarmReceiverServiceIntent.putExtras(intent.getExtras());
		WakefulIntentService.sendWakefulWork(_context, alarmReceiverServiceIntent);
	}
	
	/**
	 * Checks if the timer elapsed time has been reached or not.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return boolean - Returns true if the timer elapsed time has been reached.
	 */
	private boolean elapsedTimeReached(int alarmType){
		if (_debug) Log.v("AlarmReceiver.elapsedTimeReached()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		long alarmTime = 0;
		long timerStartTime = 0;
		long currentElapsedTime = 0;
    	switch(alarmType){
			case Constants.TYPE_DIAPER:{
				alarmTime = preferences.getLong(Constants.DIAPER_ALARM_TIME_KEY, 0);
				timerStartTime = preferences.getLong(Constants.DIAPER_START_TIME_KEY, 0);
				if(alarmTime == 0){
					if (_debug) Log.v("AlarmReceiver.elapsedTimeReached() Alarm time is null.");
					return false;
				}
				break;
			}
			case Constants.TYPE_BOTTLE:{
				alarmTime = preferences.getLong(Constants.BOTTLE_ALARM_TIME_KEY, 0);
				timerStartTime = preferences.getLong(Constants.BOTTLE_START_TIME_KEY, 0);
				if(alarmTime == 0){
					if (_debug) Log.v("AlarmReceiver.elapsedTimeReached() Alarm time is null.");
					return false;
				}
				break;
			}
			case Constants.TYPE_SLEEP:{
				alarmTime = preferences.getLong(Constants.SLEEP_ALARM_TIME_KEY, 0);
				timerStartTime = preferences.getLong(Constants.SLEEP_START_TIME_KEY, 0);
				if(alarmTime == 0){
					if (_debug) Log.v("AlarmReceiver.elapsedTimeReached() Alarm time is null.");
					return false;
				}
				break;
			}
			case Constants.TYPE_CUSTOM:{
				alarmTime = preferences.getLong(Constants.CUSTOM_ALARM_TIME_KEY, 0);
				timerStartTime = preferences.getLong(Constants.CUSTOM_START_TIME_KEY, 0);
				if(alarmTime == 0){
					if (_debug) Log.v("AlarmReceiver.elapsedTimeReached() Alarm time is null.");
					return false;
				}
				break;
			}
		}
		currentElapsedTime = System.currentTimeMillis() - timerStartTime + (10 * 1000);
		if(currentElapsedTime >= alarmTime){
			return true;
		}else{
			setAlarm(alarmType, alarmTime, currentElapsedTime);
			return false;
		}
	}
	
	/**
	 * Set the alarm.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * @param alarmTime - The time the alarm should go off.
	 * @param elapsedTime - The elapsed time of the timer.
	 */
	private void setAlarm(int alarmType, long alarmTime, long elapsedTime){
		if (_debug) Log.v("AlarmReceiver.setAlarm()");
		long alarmAlertTime = System.currentTimeMillis() + alarmTime - elapsedTime;
		if (_debug) Log.v("AlarmReceiver.setAlarm() AlarmTime: " + alarmTime + " ElapsedTime:" + elapsedTime + " AlarmAlertTime: " + alarmAlertTime);
		AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(_context, AlarmReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ALARM_TYPE, alarmType);
		bundle.putLong(Constants.ALARM_TIME, alarmTime);
		bundle.putBoolean(Constants.ALARM_SNOOZE, false);
		alarmIntent.putExtras(bundle);
		alarmIntent.setAction(String.valueOf(alarmType));
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, alarmIntent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmAlertTime, pendingIntent);
	}
		
}
