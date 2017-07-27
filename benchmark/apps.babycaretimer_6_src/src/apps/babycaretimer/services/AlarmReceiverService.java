package apps.babycaretimer.services;

import java.util.ArrayList;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import apps.babycaretimer.AlarmActivity;
import apps.babycaretimer.R;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;

/**
 * This service starts the AlarmActivity for all alarms.
 * 
 * @author Camille Sévigny
 */
public class AlarmReceiverService extends WakefulIntentService {
	
	//================================================================================
    // Properties
    //================================================================================

	private static boolean _debug = false;
	private static Context _context = null;
	private static Vibrator _vibrator = null;
	private static MediaPlayer _mediaPlayer = null;

	//================================================================================
    // Accessors
    //================================================================================
	
	/**
	 * Cancel the vibrator.
	 */
	public static void cancelVibrator(){
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmReceiverService.cancelVibrator()");
		try{
			if(_vibrator != null){
				_vibrator.cancel();
			}
		}catch(Exception ex){
			if (_debug) Log.e("AlarmReceiverService.cancelVibrator() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Stop the mediaPlayer.
	 */
	public static void stopMediaPlayer(){
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmReceiverService.stopMediaPlayer()");
		try{
			if(_mediaPlayer != null){
				_mediaPlayer.stop();
			}
		}catch(Exception ex){
			if (_debug) Log.e("AlarmReceiverService.stopMediaPlayer() ERROR: " + ex.toString());
		}
	}
	
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * Class Constructor.
	 */
	public AlarmReceiverService() {
		super("AlarmReceiverService");
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmReceiverService.AlarmReceiverService()");
	}

	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Display the alarm for this timer.
	 * 
	 * @param intent - Intent object that we are working with.
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		_debug = Log.getDebug();
		if (_debug) Log.v("AlarmReceiverService.doWakefulWork()");
		startAlarmActivity(intent);
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Display the alarm for this timer.
	 * 
	 * @param intent - Intent object that we are working with.
	 */
	private void startAlarmActivity(Intent intent) {
		if (_debug) Log.v("AlarmReceiverService.startAlarmActivity()");
		try{
			_context = getApplicationContext();
			if (_debug) Log.v("AlarmReceiverService.startAlarmActivity() 1");
	    	Bundle bundle = intent.getExtras();
	    	if (_debug) Log.v("AlarmReceiverService.startAlarmActivity() 2");
			//Check the state of the users phone.
			TelephonyManager telemanager = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
			boolean callStateIdle = telemanager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
			if(callStateIdle){
				Intent alarmIntent = new Intent(_context, AlarmActivity.class);
				if (_debug) Log.v("AlarmReceiverService.startAlarmActivity() 3");
		    	alarmIntent.putExtras(bundle);
		    	if (_debug) Log.v("AlarmReceiverService.startAlarmActivity()4");
		    	alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK  | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    	Common.acquireWakeLock(_context);
		    	if (_debug) Log.v("AlarmReceiverService.startAlarmActivity() 5");
		    	_context.startActivity(alarmIntent);
			}
		    //Alarm Status Bar Notification
			createNotification(bundle, callStateIdle);
		}catch(Exception ex){
			if (_debug) Log.e("AlarmReceiverService.startAlarmActivity() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Create a status bar notification.
	 * 
	 * @param context - The applications context.
	 * @param bundle - The bundle associated with this request.
	 */
	private void createNotification(Bundle bundle, boolean callStateIdle){
		if (_debug) Log.v("AlarmReceiverService.createNotification()");
		try{
			AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			boolean inNormalMode = audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
			boolean inVibrateMode = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
			if(!preferences.getBoolean(Constants.ALARM_NOTIFICATIONS_ENABLED_KEY, true)){
				return;
			}
			//Sound preferences
			String notificationSound = preferences.getString(Constants.ALARM_NOTIFICATION_SOUND_KEY, "content://settings/system/alarm_alert");
			boolean soundEnabled = false;
			if(notificationSound != null && !notificationSound.equals("")){
				soundEnabled = true;
			}
			boolean soundInCallEnabled = preferences.getBoolean(Constants.ALARM_NOTIFICATION_IN_CALL_VIBRATE_ENABLED_KEY, false);
			//Vibrate preferences
			String notificationVibrate = null;
			boolean vibrateEnabled = false;
			notificationVibrate = preferences.getString(Constants.ALARM_NOTIFICATION_VIBRATE_SETTING_KEY, Constants.ALARM_NOTIFICATION_VIBRATE_ALWAYS_VALUE);
			if(notificationVibrate.equals(Constants.ALARM_NOTIFICATION_VIBRATE_ALWAYS_VALUE)){
				vibrateEnabled = true;
			}else if(notificationVibrate.equals(Constants.ALARM_NOTIFICATION_VIBRATE_WHEN_VIBRATE_MODE_VALUE) && inVibrateMode){
				vibrateEnabled = true;
			}
			boolean vibrateInCallEnabled = preferences.getBoolean(Constants.ALARM_NOTIFICATION_IN_CALL_VIBRATE_ENABLED_KEY, false);
			String vibratePattern = null;
			if(vibrateEnabled){
				vibratePattern = preferences.getString(Constants.ALARM_NOTIFICATION_VIBRATE_PATTERN_KEY, Constants.ALARM_NOTIFICATION_VIBRATE_DEFAULT);
				if(vibratePattern.equals(Constants.ALARM_NOTIFICATION_VIBRATE_PATTERN_CUSTOM_VALUE_KEY)){
					vibratePattern = preferences.getString(Constants.ALARM_NOTIFICATION_VIBRATE_PATTERN_CUSTOM_KEY, Constants.ALARM_NOTIFICATION_VIBRATE_DEFAULT);
				}
			}
			//LED preferences
			boolean ledEnabled = preferences.getBoolean(Constants.ALARM_NOTIFICATION_LED_ENABLED_KEY, true);
			String ledPattern = null;
			int ledColor = Color.parseColor(Constants.ALARM_NOTIFICATION_LED_COLOR_DEFAULT);
			String ledColorString = null;
			if(ledEnabled){
				//LED Color
				ledColorString = preferences.getString(Constants.ALARM_NOTIFICATION_LED_COLOR_KEY, Constants.ALARM_NOTIFICATION_LED_COLOR_DEFAULT);
				if(ledColorString.equals(Constants.ALARM_NOTIFICATION_LED_COLOR_CUSTOM_VALUE_KEY)){
					ledColorString = preferences.getString(Constants.ALARM_NOTIFICATION_LED_COLOR_CUSTOM_KEY, Constants.ALARM_NOTIFICATION_LED_COLOR_DEFAULT);
				}
				try{
					ledColor = Color.parseColor(ledColorString);
				}catch(Exception ex){
					//Do Nothing
				}
				//LED Pattern
				ledPattern = preferences.getString(Constants.ALARM_NOTIFICATION_LED_PATTERN_KEY, Constants.ALARM_NOTIFICATION_LED_PATTERN_DEFAULT);
				if(ledPattern.equals(Constants.ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_VALUE_KEY)){
					ledPattern = preferences.getString(Constants.ALARM_NOTIFICATION_LED_PATTERN_CUSTOM_KEY, Constants.ALARM_NOTIFICATION_LED_PATTERN_DEFAULT);
				}
			}
			int alarmType = bundle.getInt(Constants.ALARM_TYPE);
			int icon = 0;
			int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
			if(currentAPIVersion >= 8){
				icon = R.drawable.stat_notify_alarm_froyo;
			}else{
				icon = R.drawable.stat_notify_alarm_gingerbread;
			}
			CharSequence tickerText = null;
			CharSequence contentTitle = _context.getText(R.string.notifications_content_title);  
			CharSequence contentText = _context.getText(R.string.notifications_content_text);
			switch(alarmType){
				case Constants.TYPE_DIAPER:{	
					tickerText = _context.getText(R.string.notifications_diaper_ticker_text);
					break;
				}
				case Constants.TYPE_BOTTLE:{
					tickerText = _context.getText(R.string.notifications_bottle_ticker_text);
					break;
				}
				case Constants.TYPE_SLEEP:{
					tickerText = _context.getText(R.string.notifications_sleep_ticker_text);
					break;
				}
				case Constants.TYPE_CUSTOM:{
					tickerText = _context.getText(R.string.notifications_custom_ticker_text);
					break;
				}
			}
			//Setup the notification
			Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
			//Set notification flags
			notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_INSISTENT;
			//Setup the notification vibration
			if(vibrateEnabled && callStateIdle){
				long[] vibrationPattern = parseVibratePattern(vibratePattern);
				if(vibrationPattern == null){
					notification.defaults |= Notification.DEFAULT_VIBRATE;
				}else{
					notification.vibrate = vibrationPattern;
				}
			}else if(vibrateEnabled && !callStateIdle && vibrateInCallEnabled && (inVibrateMode || inNormalMode)){
				long[] vibrationPattern = parseVibratePattern(vibratePattern);
				if(vibrationPattern == null){
					//Do Nothing
				}else{
					try{
						_vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						_vibrator.vibrate(vibrationPattern, 0);
					}catch(Exception ex){
						if (_debug) Log.e("AlarmReceiverService.createNotification() Notification Vibrator ERROR: " + ex.toString());
					}
				}
			}
			//Setup the notification sound
			notification.audioStreamType = Notification.STREAM_DEFAULT;
			if(soundEnabled && callStateIdle){
				try{
					notification.sound = Uri.parse(notificationSound);
				}catch(Exception ex){
					if (_debug) Log.e("AlarmReceiverService.createNotification() Notification Sound Set ERROR: " + ex.toString());
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
			}else if(soundEnabled && !callStateIdle && soundInCallEnabled && inNormalMode){
				try{
					new playNotificationMediaFileAsyncTask().execute(notificationSound);
				}catch(Exception ex){
					if (_debug) Log.e("AlarmReceiverService.createNotification() Notification Sound Play ERROR: " + ex.toString());
				}
			}
			//Setup the notification LED lights
			if(ledEnabled){
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
				try{
					int[] ledPatternArray = parseLEDPattern(ledPattern);
					if(ledPatternArray == null){
						notification.defaults |= Notification.DEFAULT_LIGHTS;
					}else{
						//LED Color
				        notification.ledARGB = ledColor;
						//LED Pattern
						notification.ledOnMS = ledPatternArray[0];
				        notification.ledOffMS = ledPatternArray[1];
					}
				}catch(Exception ex){
					notification.defaults |= Notification.DEFAULT_LIGHTS;
				}
			}
			//Set notification information
			//Content Intent
			Intent notificationContentIntent = new Intent(_context, AlarmNotificationContentIntentService.class);
			PendingIntent contentIntent = PendingIntent.getService(_context, 0, notificationContentIntent, 0);
			//Delete Intent
			Intent notificationDeleteIntent = new Intent(_context, AlarmNotificationDeleteIntentService.class);
			PendingIntent deleteIntent = PendingIntent.getService(_context, 0, notificationDeleteIntent, 0);
			//Special case when phone is in keyguard mode
			KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
			boolean screenEnabled = preferences.getBoolean(Constants.SCREEN_ENABLED_KEY, true);
			if(keyguardManager.inKeyguardRestrictedInputMode() && screenEnabled){
				contentIntent = deleteIntent;
				contentText = _context.getText(R.string.notifications_content_lockscreen_text);
		    }
			//Set notification intent values
			notification.deleteIntent = deleteIntent;
			notification.setLatestEventInfo(_context, contentTitle, contentText, contentIntent);
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(1, notification);
			//Update the notification count and store in the user preferences
			int alarmCount = preferences.getInt(Constants.ALARM_NOTIFICATION_COUNT_KEY, 0);
			alarmCount += 1;
			SharedPreferences.Editor editor = preferences.edit();
	    	editor.putInt(Constants.ALARM_NOTIFICATION_COUNT_KEY, alarmCount);
	        editor.commit();
		}catch(Exception ex){
			if (_debug) Log.e("AlarmReceiverService.createNotification() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Play a notification sound through the media player.
	 * 
	 * @author Camille Sévigny
	 */
	private static class playNotificationMediaFileAsyncTask extends AsyncTask<String, Void, Void> {
	    
	    /**
	     * Do this work in the background.
	     * 
	     * @param params - The URI of the notification sound.
	     */
	    protected Void doInBackground(String... params) {
			if (_debug) Log.v("AlarmReceiverService.playNotificationMediaFileAsyncTask.doInBackground()");
			MediaPlayer mediaPlayer = null;
			try{
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setLooping(true);
				mediaPlayer.setDataSource(_context,  Uri.parse(params[0]));
				mediaPlayer.prepare();
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
	                public void onCompletion(MediaPlayer mediaPlayer) {
	                	mediaPlayer.release();
	                	mediaPlayer = null;
	                }
				});	
		    	return null;
			}catch(Exception ex){
				if (_debug) Log.e("AlarmReceiverService.playNotificationMediaFileAsyncTask.doInBackground() ERROR: " + ex.toString());
				mediaPlayer.release();
            	mediaPlayer = null;
				return null;
			}
	    }
	    
	    /**
	     * Nothing needs to happen once the media file has been played.
	     * 
	     * @param result - Void.
	     */
	    protected void onPostExecute(Void result) {
			if (_debug) Log.v("AlarmReceiverService.playNotificationMediaFileAsyncTask.onPostExecute()");
	    }
	    
	}
	
	/**
	 * Parse a vibration pattern.
	 * 
	 * @param vibratePattern - The vibrate pattern to verify.
	 * 
	 * @return boolean - Returns True if the vibrate pattern is valid.
	 */
	private long[] parseVibratePattern(String vibratePattern){
		if (_debug) Log.v("AlarmReceiverService.parseVibratePattern()");
	    final int VIBRATE_PATTERN_MAX_LENGTH = 60000;
	    final int VIBRATE_PATTERN_MAX_SIZE = 100;
		ArrayList<Long> vibratePatternArrayList = new ArrayList<Long>();
		long[] vibratePatternArray = null;
		String[] vibratePatternStringArray = vibratePattern.split(",");
		int arraySize = vibratePatternStringArray.length;
	    for (int i = 0; i < arraySize; i++) {
	    	long vibrateLength = 0;
	    	try {
	    		vibrateLength = Long.parseLong(vibratePatternStringArray[i].trim());
	    	} catch (Exception ex) {
	    		if (_debug) Log.e("AlarmReceiverService.parseVibratePattern() ERROR: " + ex.toString());
	    		return null;
	    	}
	    	if(vibrateLength < 0){
	    		vibrateLength = 0;
	    	}
	    	if(vibrateLength > VIBRATE_PATTERN_MAX_LENGTH){
	    		vibrateLength = VIBRATE_PATTERN_MAX_LENGTH;
	    	}
	    	vibratePatternArrayList.add(vibrateLength);
	    }
	    arraySize = vibratePatternArrayList.size();
	    if (arraySize > VIBRATE_PATTERN_MAX_SIZE){
	    	arraySize = VIBRATE_PATTERN_MAX_SIZE;
	    }
	    vibratePatternArray = new long[arraySize];
	    for (int i = 0; i < arraySize; i++) {
	    	vibratePatternArray[i] = vibratePatternArrayList.get(i);
	    }
		return vibratePatternArray;
	}
	
	/**
	 * Parse an led blink pattern.
	 * 
	 * @param ledPattern - The blink pattern to verify.
	 * 
	 * @return boolean - Returns True if the blink pattern is valid.
	 */
	private int[] parseLEDPattern(String ledPattern){
		if (_debug) Log.v("AlarmReceiverService.parseLEDPattern()");
	    final int LED_PATTERN_MAX_LENGTH = 60000;
		int[] ledPatternArray = {0, 0};
		String[] ledPatternStringArray = ledPattern.split(",");
		if(ledPatternStringArray.length != 2){
			return null;
		}
	    for (int i = 0; i < 2; i++) {
	    	int blinkLength = 0;
	    	try {
	    		blinkLength = Integer.parseInt(ledPatternStringArray[i].trim());
	    	} catch (Exception ex) {
	    		if (_debug) Log.e("AlarmReceiverService.parseLEDPattern() ERROR: " + ex.toString());
	    		return null;
	    	}
	    	if(blinkLength < 0){
	    		blinkLength = 0;
	    	}
	    	if(blinkLength > LED_PATTERN_MAX_LENGTH){
	    		blinkLength = LED_PATTERN_MAX_LENGTH;
	    	}
	    	ledPatternArray[i] = blinkLength;
	    }
		return ledPatternArray;
	}
	
}