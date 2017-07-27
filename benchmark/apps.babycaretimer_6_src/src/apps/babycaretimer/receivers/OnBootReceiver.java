package apps.babycaretimer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.services.OnBootBroadcastReceiverService;
import apps.babycaretimer.services.WakefulIntentService;

/**
 * This class listens for the OnBoot event from the users phone.
 * Updates the applications timers in order to function after a device shut down.
 * 
 * @author Camille Sévigny
 */
public class OnBootReceiver extends BroadcastReceiver {

	//================================================================================
    // Properties
    //================================================================================

	private boolean _debug = false;

	//================================================================================
	// Public Methods
	//================================================================================
    
	/**
	 * Receives a notification that the phone was restarted.
	 * This function starts the service that will handle the work to setup calendar event notifications.
	 * 
	 * @param context - Application Context.
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("OnBootReceiver.onReceive()");
		try{
			WakefulIntentService.sendWakefulWork(context, new Intent(context, OnBootBroadcastReceiverService.class));
		}catch(Exception ex){
			if (_debug) Log.e("OnBootReceiver.onReceive() ERROR: " + ex.toString());
		}
	}

}