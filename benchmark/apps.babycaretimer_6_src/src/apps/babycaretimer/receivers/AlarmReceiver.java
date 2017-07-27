package apps.babycaretimer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.services.AlarmBroadcastReceiverService;
import apps.babycaretimer.services.WakefulIntentService;

/**
 * This class receives alarm broadcasts.
 * 
 * @author Camille Sévigny
 */
public class AlarmReceiver extends BroadcastReceiver {
	
	//================================================================================
    // Properties
    //================================================================================

    private boolean _debug = false;

	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * This function starts the service that will handle the work or reschedules the work if the phone is in use.
	 * 
	 * @param context - Application Context.
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmReceiver.onReceive()");
		try{
			Intent alarmBroadcastReceiverServiceIntent = new Intent(context, AlarmBroadcastReceiverService.class);
		    alarmBroadcastReceiverServiceIntent.putExtras(intent.getExtras());
			WakefulIntentService.sendWakefulWork(context, alarmBroadcastReceiverServiceIntent);
		}catch(Exception ex){
			if (_debug) Log.e("AlarmReceiver.onReceive() ERROR: " + ex.toString());
		}
	}

}