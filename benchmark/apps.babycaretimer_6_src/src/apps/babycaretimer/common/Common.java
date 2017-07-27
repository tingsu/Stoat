package apps.babycaretimer.common;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import apps.babycaretimer.log.Log;

/**
 * This class is a collection of methods that are used more than once.
 * If a method is used more than once it is put here and made static so that 
 * it becomes accessible to all classes in the application.
 * 
 * @author Camille Sévigny
 */
public class Common {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private static boolean _debug = false;
	private static PowerManager.WakeLock _partialWakeLock = null;
	private static PowerManager.WakeLock _wakeLock = null;
	private static KeyguardLock _keyguardLock = null;
	
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Get the base time of the specific alarm type.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return long - The base time of the timer.
	 */
	public static long getBaseTime(Context context, int alarmType){
    	if (_debug) Log.v("Common.getBaseTime()");
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	long baseTime = 0;
    	try{
			switch(alarmType){
				case Constants.TYPE_DIAPER:{
					baseTime = preferences.getLong(Constants.DIAPER_BASE_TIME_KEY, 0);
					break;
				}
				case Constants.TYPE_BOTTLE:{
					baseTime = preferences.getLong(Constants.BOTTLE_BASE_TIME_KEY, 0);
					break;
				}
				case Constants.TYPE_SLEEP:{
					baseTime = preferences.getLong(Constants.SLEEP_BASE_TIME_KEY, 0);
					break;
				}
				case Constants.TYPE_CUSTOM:{
					baseTime = preferences.getLong(Constants.CUSTOM_BASE_TIME_KEY, 0);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.getBaseTime() ERROR: " + ex.toString());
		}
		return baseTime;
	}
	
	/**
	 * Checks if the alarm is still active or not.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return boolean - Returns true if the alarm is still active and has not been canceled.
	 */
	public static boolean isAlarmActive(Context context, int alarmType){
		if (_debug) Log.v("Common.alarmIsActive()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isActive = false;
		try{
	    	switch(alarmType){
				case Constants.TYPE_DIAPER:{
					isActive = preferences.getBoolean(Constants.DIAPER_ALARM_ACTIVE_KEY, false);
					break;
				}
				case Constants.TYPE_BOTTLE:{
					isActive = preferences.getBoolean(Constants.BOTTLE_ALARM_ACTIVE_KEY, false);
					break;
				}
				case Constants.TYPE_SLEEP:{
					isActive = preferences.getBoolean(Constants.SLEEP_ALARM_ACTIVE_KEY, false);
					break;
				}
				case Constants.TYPE_CUSTOM:{
					isActive = preferences.getBoolean(Constants.CUSTOM_ALARM_ACTIVE_KEY, false);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.alarmIsActive() ERROR: " + ex.toString());
		}
		return isActive;
	}
	
	/**
	 * Checks if the timer is running or not.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return boolean - Returns true if the timer is still active and has not been stopped.
	 */
	public static boolean isTimerActive(Context context, int timerType){
		if (_debug) Log.v("Common.timerIsActive()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isActive = false;
		try{
	    	switch(timerType){
				case Constants.TYPE_DIAPER:{
					isActive = preferences.getBoolean(Constants.DIAPER_TIMER_ACTIVE_KEY, false);
					break;
				}
				case Constants.TYPE_BOTTLE:{
					isActive = preferences.getBoolean(Constants.BOTTLE_TIMER_ACTIVE_KEY, false);
					break;
				}
				case Constants.TYPE_SLEEP:{
					isActive = preferences.getBoolean(Constants.SLEEP_TIMER_ACTIVE_KEY, false);
					break;
				}
				case Constants.TYPE_CUSTOM:{
					isActive = preferences.getBoolean(Constants.CUSTOM_TIMER_ACTIVE_KEY, false);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.timerIsActive() ERROR: " + ex.toString());
		}
		return isActive;
	}

	/**
	 * Get the alarm timer start information for this alarm type.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return string - The alarm timer start value stored for this alarm type.
	 */
	public static long getTimerStartTime(Context context, int alarmType){
		if (_debug) Log.v("Common.getAlarmStartTime()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		long alarmTimerStart = 0;
		try{
			switch (alarmType) {
				case Constants.TYPE_DIAPER: {
					alarmTimerStart = preferences.getLong(Constants.DIAPER_TIMER_START_KEY, 0);
					break;
				}
				case Constants.TYPE_BOTTLE: {
					alarmTimerStart = preferences.getLong(Constants.BOTTLE_TIMER_START_KEY, 0);
					break;
				}
				case Constants.TYPE_SLEEP: {
					alarmTimerStart = preferences.getLong(Constants.SLEEP_TIMER_START_KEY, 0);
					break;
				}
				case Constants.TYPE_CUSTOM: {
					alarmTimerStart = preferences.getLong(Constants.CUSTOM_TIMER_START_KEY, 0);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.getAlarmStartTime() ERROR: " + ex.toString());
		}
		return alarmTimerStart;
	}

	/**
	 * Get the alarm timer Offset information for this alarm type.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return string - The alarm timer Offset value stored for this alarm type.
	 */
	public static long getTimerOffset(Context context, int alarmType){
		if (_debug) Log.v("Common.getTimerOffset()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		long alarmTimerOffset = 0;
		try{
			switch (alarmType) {
				case Constants.TYPE_DIAPER: {
					alarmTimerOffset = preferences.getLong(Constants.DIAPER_TIMER_OFFSET_KEY, 0);
					break;
				}
				case Constants.TYPE_BOTTLE: {
					alarmTimerOffset = preferences.getLong(Constants.BOTTLE_TIMER_OFFSET_KEY, 0);
					break;
				}
				case Constants.TYPE_SLEEP: {
					alarmTimerOffset = preferences.getLong(Constants.SLEEP_TIMER_OFFSET_KEY, 0);
					break;
				}
				case Constants.TYPE_CUSTOM: {
					alarmTimerOffset = preferences.getLong(Constants.CUSTOM_TIMER_OFFSET_KEY, 0);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.getTimerOffset() ERROR: " + ex.toString());
		}
		return alarmTimerOffset;
	}	
	
	/**
	 * Get the alarm time information for this alarm type.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return string - The alarm time stored for this alarm type.
	 */
	public static long getAlarmTime(Context context, int alarmType){
		if (_debug) Log.v("Common.getAlarmTime()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		long alarmTime = 0;
		try{
			switch (alarmType) {
				case Constants.TYPE_DIAPER: {
					alarmTime = preferences.getLong(Constants.DIAPER_ALARM_TIME_KEY, 0);
					break;
				}
				case Constants.TYPE_BOTTLE: {
					alarmTime = preferences.getLong(Constants.BOTTLE_ALARM_TIME_KEY, 0);
					break;
				}
				case Constants.TYPE_SLEEP: {
					alarmTime = preferences.getLong(Constants.SLEEP_ALARM_TIME_KEY, 0);
					break;
				}
				case Constants.TYPE_CUSTOM: {
					alarmTime = preferences.getLong(Constants.CUSTOM_ALARM_TIME_KEY, 0);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.getAlarmTime() ERROR: " + ex.toString());
		}
		return alarmTime;
	}
	
	/**
	 * Checks if the alarm is in snooze mode or not.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return boolean - Returns true if the alarm is in snooze mode.
	 */
	public static boolean isAlarmSnoozed(Context context, int timerType){
		if (_debug) Log.v("Common.isAlarmSnoozed()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isSnoozed = false;
		try{
	    	switch(timerType){
				case Constants.TYPE_DIAPER:{
					isSnoozed = preferences.getBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, false);
					break;
				}
				case Constants.TYPE_BOTTLE:{
					isSnoozed = preferences.getBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, false);
					break;
				}
				case Constants.TYPE_SLEEP:{
					isSnoozed = preferences.getBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, false);
					break;
				}
				case Constants.TYPE_CUSTOM:{
					isSnoozed = preferences.getBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, false);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.isAlarmSnoozed() ERROR: " + ex.toString());
		}
		return isSnoozed;
	}
	
	/**
	 * Checks if the alarm is recurring.
	 * 
	 * @param context - Application context.
	 * @param alarmType - The alarm type.
	 * 
	 * @return boolean - Returns true if the alarm is recurring.
	 */
	public static boolean isAlarmRecurring(Context context, int alarmType){
		if (_debug) Log.v("Common.isAlarmRecurring()");
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isRecurring = false;
		try{
			switch (alarmType) {
				case Constants.TYPE_DIAPER: {
					isRecurring = preferences.getBoolean(Constants.DIAPER_ALARM_RECURRING_KEY, true);
					break;
				}
				case Constants.TYPE_BOTTLE: {
					isRecurring = preferences.getBoolean(Constants.BOTTLE_ALARM_RECURRING_KEY, true);
					break;
				}
				case Constants.TYPE_SLEEP: {
					isRecurring = preferences.getBoolean(Constants.SLEEP_ALARM_RECURRING_KEY, true);
					break;
				}
				case Constants.TYPE_CUSTOM: {
					isRecurring = preferences.getBoolean(Constants.CUSTOM_ALARM_RECURRING_KEY, true);
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.isAlarmRecurring() ERROR: " + ex.toString());
		}
		return isRecurring;
	}
	
	/**
	 * Aquire a global partial wakelock within this context.
	 * 
	 * @param context - The application context.
	 */
	public static void acquirePartialWakeLock(Context context){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.aquirePartialWakelock()");
		try{
			if(_partialWakeLock == null){
		    	PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		    	_partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.BABY_CARE_TIMER_WAKELOCK);
		    	_partialWakeLock.setReferenceCounted(false);
			}
			_partialWakeLock.acquire();
		}catch(Exception ex){
			if (_debug) Log.e("Common.aquirePartialWakelock() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Release the global partial wakelock within this context.
	 * 
	 * @param context - The application context.
	 */
	public static void clearPartialWakeLock(){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.clearPartialWakelock()");
		try{
	    	if(_partialWakeLock != null){
	    		_partialWakeLock.release();
	    	}
		}catch(Exception ex){
			if (_debug) Log.e("Common.clearPartialWakelock() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Function that acquires the WakeLock for this Activity.
	 * The type flags for the WakeLock will be determined by the user preferences. 
	 * 
	 * @param context - The application context.
	 */
	public static void acquireWakeLock(Context context){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.aquireWakelock()");
		try{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			if(_wakeLock == null){
				PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				if(preferences.getBoolean(Constants.SCREEN_ENABLED_KEY, true)){
					if(preferences.getBoolean(Constants.SCREEN_DIM_ENABLED_KEY, true)){
						_wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, Constants.BABY_CARE_TIMER_WAKELOCK);
					}else{
						_wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, Constants.BABY_CARE_TIMER_WAKELOCK);
					}
				}else{
					_wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.BABY_CARE_TIMER_WAKELOCK);
				}
				_wakeLock.setReferenceCounted(false);
			}
			if(_wakeLock != null){
				_wakeLock.acquire();
			}
			Common.clearPartialWakeLock();
		}catch(Exception ex){
			if (_debug) Log.e("Common.aquireWakelock() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Release the global wakelock within this context.
	 * 
	 * @param context - The application context.
	 */
	public static void clearWakeLock(){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.clearWakelock()");
		try{
			Common.clearPartialWakeLock();
	    	if(_wakeLock != null){
	    		_wakeLock.release();
	    	}
		}catch(Exception ex){
			if (_debug) Log.e("Common.clearWakelock() ERROR: " + ex.toString());
		}
	}

	/**
	 * Function that disables the Keyguard for this Activity.
	 * The removal of the Keyguard will be determined by the user preferences. 
	 * 
	 * @param context - The current context of this Activity.
	 */
	public static void acquireKeyguardLock(Context context){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.acquireKeyguardLock()");
		try{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			if(keyguardManager.inKeyguardRestrictedInputMode() && preferences.getBoolean(Constants.SCREEN_ENABLED_KEY, true) && preferences.getBoolean(Constants.KEYGUARD_ENABLED_KEY, true)){
				if(_keyguardLock == null){
					_keyguardLock = keyguardManager.newKeyguardLock(Constants.BABY_CARE_TIMER_KEYGUARD);
				}
				_keyguardLock.disableKeyguard();
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.acquireKeyguardLock() ERROR: " + ex.toString());
		}
	}

	/**
	 * Re-Enables the Keyguard for this Activity.
	 */
	public static void clearKeyguardLock(){
		_debug = Log.getDebug();
		if (_debug) Log.v("Common.clearKeyguardLock()");
		try{
			if(_keyguardLock != null){
				_keyguardLock.reenableKeyguard();
			}
		}catch(Exception ex){
			if (_debug) Log.e("Common.clearKeyguardLock() ERROR: " + ex.toString());
		}
	}
	
}