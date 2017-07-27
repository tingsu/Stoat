package apps.babycaretimer.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.receivers.AlarmReceiver;

/**
 * This class does the work of the BroadcastReceiver.
 * 
 * @author Camille Sévigny
 */
public class OnBootBroadcastReceiverService extends WakefulIntentService {
	
	//================================================================================
    // Properties
    //================================================================================
	
	boolean _debug = false;
	Context _context = null;
	SharedPreferences _preferences = null;

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Class Constructor.
	 */
	public OnBootBroadcastReceiverService() {
		super("OnBootBroadcastReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("OnBootBroadcastReceiverService.OnBootBroadcastReceiverService()");
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
		if (_debug) Log.v("OnBootBroadcastReceiverService.doWakefulWork()");
		_context = getApplicationContext();
		_preferences = PreferenceManager.getDefaultSharedPreferences(_context);
		manageAppTimers();
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Manage the application timers due to the phone restarting.
	 * Setup existing alarms as well where appropriate.
	 */
	private void manageAppTimers() {
		if (_debug) Log.v("OnBootBroadcastReceiverService.manageAppTimers()");
		long currentTime = System.currentTimeMillis();
		//Setup the timeOffset times and reset the base times.
		boolean diaperTimerActive = Common.isTimerActive(_context, Constants.TYPE_DIAPER);
		boolean bottleTimerActive = Common.isTimerActive(_context, Constants.TYPE_BOTTLE);
		boolean sleepTimerActive = Common.isTimerActive(_context, Constants.TYPE_SLEEP);
		boolean customTimerActive = Common.isTimerActive(_context, Constants.TYPE_CUSTOM);
		long diaperTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_DIAPER);
		long bottleTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_BOTTLE);
		long sleepTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_SLEEP);
		long customTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_CUSTOM);
		long diaperTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_DIAPER);
		long bottleTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_BOTTLE);
		long sleepTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_SLEEP);
		long customTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_CUSTOM);	
		diaperTimerOffset = diaperTimerActive ? (currentTime - diaperTimerStartTime) : 0;
		bottleTimerOffset = bottleTimerActive ? (currentTime - bottleTimerStartTime) : 0;
		sleepTimerOffset = sleepTimerActive ? (currentTime - sleepTimerStartTime) : 0;
		customTimerOffset = customTimerActive ? (currentTime - customTimerStartTime) : 0;
		SharedPreferences.Editor editor = _preferences.edit();
		editor.putLong(Constants.DIAPER_BASE_TIME_KEY, 0);
		editor.putLong(Constants.DIAPER_TIMER_OFFSET_KEY, diaperTimerOffset);
		editor.putLong(Constants.BOTTLE_BASE_TIME_KEY, 0);
		editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, bottleTimerOffset);
		editor.putLong(Constants.SLEEP_BASE_TIME_KEY, 0);
		editor.putLong(Constants.SLEEP_TIMER_OFFSET_KEY, sleepTimerOffset);
		editor.putLong(Constants.CUSTOM_BASE_TIME_KEY, 0);
		editor.putLong(Constants.CUSTOM_TIMER_OFFSET_KEY, customTimerOffset);
		editor.commit();	
		//Setup any alarms that are active.
		boolean diaperAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_DIAPER);
		boolean bottleAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_BOTTLE);
		boolean sleepAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_SLEEP);
		boolean customAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_CUSTOM);
		long diaperAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_DIAPER);
		long bottleAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_BOTTLE);
		long sleepAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_SLEEP);
		long customAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_CUSTOM);
		if(diaperAlarmActive){		
			setAlarm(Constants.TYPE_DIAPER, diaperAlarmTime, diaperTimerOffset);	
		}
		if(bottleAlarmActive){
			setAlarm(Constants.TYPE_BOTTLE, bottleAlarmTime, bottleTimerOffset);
		}
		if(sleepAlarmActive){
			setAlarm(Constants.TYPE_SLEEP, sleepAlarmTime, sleepTimerOffset);
		}
		if(customAlarmActive){	
			setAlarm(Constants.TYPE_CUSTOM, customAlarmTime, customTimerOffset);
		}
	}
	
	/**
	 * Set the alarm based on the currently stored data.
	 * 
	 * @param alarmType - The type of alarm to set.
	 * @param alarmTime - The time (duration) the alarm is set for.
	 * @param timerOffset - The offset from the current elapsed time of the timer.
	 */
	private void setAlarm(int alarmType, long alarmTime, long timerOffset){
    	if (_debug) Log.v("OnBootBroadcastReceiverService.setAlarm()");
    	try{
			//Set an alarm to go off at the appropriate time and trigger the AlarmActivity.
			long elapsedTime = SystemClock.elapsedRealtime() + timerOffset;
			if (_debug) Log.v("OnBootBroadcastReceiverService.setAlarm() elapsedTime: " + elapsedTime);
			long alarmAlertTime = System.currentTimeMillis() + alarmTime - elapsedTime;
			if (_debug) Log.v("OnBootBroadcastReceiverService.setAlarm() AlarmTime: " + alarmTime + " ElapsedTime:" + elapsedTime + " AlarmAlertTime: " + alarmAlertTime);
			AlarmManager alarmManager = (AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(_context, AlarmReceiver.class);
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.ALARM_TYPE, alarmType);
			bundle.putBoolean(Constants.ALARM_SNOOZE, false);
			alarmIntent.putExtras(bundle);
			alarmIntent.setAction("apps.babycaretimer.action." + String.valueOf(alarmType));
	    	alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, alarmIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmAlertTime, pendingIntent);
    	}catch(Exception ex){
			if (_debug) Log.e("OnBootBroadcastReceiverService.setAlarm() ERROR: " + ex.toString());
		}
	}
	
}