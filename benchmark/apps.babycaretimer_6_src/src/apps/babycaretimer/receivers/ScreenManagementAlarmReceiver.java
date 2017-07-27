package apps.babycaretimer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.services.ScreenManagementAlarmBroadcastReceiverService;
import apps.babycaretimer.services.WakefulIntentService;

/**
 * This class listens for a screen timeout alarm.
 * 
 * @author Camille Sévigny
 */
public class ScreenManagementAlarmReceiver extends BroadcastReceiver {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private boolean _debug = false;
	  
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Receives a notification of a Screen Management Alarm.
	 * This function removes the KeyguardLock and WakeLock help by this application.
	 * 
	 * @param context - Application Context.
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("ScreenManagementAlarmReceiver.onReceive()");
		try{
			WakefulIntentService.sendWakefulWork(context, new Intent(context, ScreenManagementAlarmBroadcastReceiverService.class));
		}catch(Exception ex){
			if (_debug) Log.v("ScreenManagementAlarmReceiver.onReceive() ERROR: " + ex.toString());
		}
	}
	
}
