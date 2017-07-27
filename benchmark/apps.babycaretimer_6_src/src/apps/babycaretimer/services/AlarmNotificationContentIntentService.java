package apps.babycaretimer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import apps.babycaretimer.TimerActivity;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;

/**
 * This service starts the TimerActivity.
 * 
 * @author Camille Sévigny
 */
public class AlarmNotificationContentIntentService extends Service {
	
	//================================================================================
    // Properties
    //================================================================================

	private boolean _debug = false;

	//================================================================================
	// Public Methods
	//================================================================================

	/**
	 * Class Constructor.
	 */
	public AlarmNotificationContentIntentService() {
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmNotificationContentIntentService.AlarmNotificationContentIntentService()");
	}

	/**
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		if (_debug) Log.v("AlarmNotificationContentIntentService.onBind()");
		return null;
	}

	/**
	 * 
	 */
	@Override
	public void onCreate() {		
		super.onCreate();
		if (_debug) Log.v("AlarmNotificationContentIntentService.onCreate()");
	}

	/**
	 * 
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (_debug) Log.v("AlarmNotificationContentIntentService.onStart()");
		handleNotificationIntent(intent);
		this.stopSelf();
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Display the alarm for this timer.
	 * 
	 * @param intent - Intent object that we are working with.
	 */
	private void handleNotificationIntent(Intent intent) {
		if (_debug) Log.v("AlarmNotificationContentIntentService.handleNotificationIntent()");
		Context context = getApplicationContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(Constants.ALARM_NOTIFICATION_COUNT_KEY, 0);
		editor.commit();
		//Stop the vibrate & sound that this service is generating for In-Call users.
		AlarmReceiverService.cancelVibrator();
		AlarmReceiverService.stopMediaPlayer();
		Intent alarmReceiverServiceIntent = new Intent(context, AlarmReceiverService.class);
		context.stopService(alarmReceiverServiceIntent);
		//Start TimerActivity via Intent.
		Intent timerActivityIntent = new Intent(context, TimerActivity.class);
		timerActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Common.acquireWakeLock(context);
		context.startActivity(timerActivityIntent);
	}
	
}