package apps.babycaretimer.services;

import android.content.Intent;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.services.WakefulIntentService;

public class ScreenManagementAlarmBroadcastReceiverService extends WakefulIntentService {
	
	//================================================================================
    // Properties
    //================================================================================
	
	boolean _debug = false;

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Class Constructor.
	 */
	public ScreenManagementAlarmBroadcastReceiverService() {
		super("ScreenManagementAlarmBroadcastReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("ScreenManagementAlarmBroadcastReceiverService.ScreenManagementAlarmBroadcastReceiverService()");
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
		if (_debug) Log.v("ScreenManagementAlarmBroadcastReceiverService.doWakefulWork()");
		try{
			//Release the KeyguardLock & WakeLock
			Common.clearKeyguardLock();
			Common.clearWakeLock();
		}catch(Exception ex){
			if (_debug) Log.e("ScreenManagementAlarmBroadcastReceiverService.doWakefulWork() ERROR: " + ex.toString());
		}
	}
		
}
