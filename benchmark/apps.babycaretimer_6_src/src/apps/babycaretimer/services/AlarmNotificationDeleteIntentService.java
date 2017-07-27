package apps.babycaretimer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;

/**
 * This service resets the notification count held in the user preferences.
 * 
 * @author Camille Sévigny
 */
public class AlarmNotificationDeleteIntentService extends Service {
	
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
	public AlarmNotificationDeleteIntentService() {
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmNotificationDeleteIntentService.AlarmNotificationDeleteIntentService()");
	}

	/**
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		if (_debug) Log.v("AlarmNotificationDeleteIntentService.onBind()");
		return null;
	}

	/**
	 * 
	 */
	@Override
	public void onCreate() {		
		super.onCreate();
		if (_debug) Log.v("AlarmNotificationDeleteIntentService.onCreate()");
	}

	/**
	 * 
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (_debug) Log.v("AlarmNotificationDeleteIntentService.onStart()");
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
		if (_debug) Log.v("AlarmNotificationDeleteIntentService.handleNotificationIntent()");
		Context context = getApplicationContext();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(Constants.ALARM_NOTIFICATION_COUNT_KEY, 0);
		editor.commit(); 
		//Stop the AlarmActivity window.
		//TODO - Kill the alarm Activity window. I don't know how to accomplish this yet!!!
		//Stop the vibrate & sound that this service is generating for In-Call users.
		AlarmReceiverService.cancelVibrator();
		AlarmReceiverService.stopMediaPlayer();
		Intent alarmReceiverServiceIntent = new Intent(context, AlarmReceiverService.class);
		context.stopService(alarmReceiverServiceIntent);
	}
	
}