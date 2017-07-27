package apps.babycaretimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Chronometer.OnChronometerTickListener;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.receivers.AlarmReceiver;
import apps.babycaretimer.receivers.ScreenManagementAlarmReceiver;

/**
 * This is the Activity for all Alarms.
 * 
 * @author Camille Sévigny
 */
public class AlarmActivity extends Activity {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private boolean _debug = false;
	private Context _context = null;
	private SharedPreferences _preferences = null;
	private KeyguardManager _keyguardManager = null;
    private int _alarmType = 0;
    private LinearLayout _alarmMainLinearLayout = null;
    private TextView _alarmHeaderTextView = null;
    private TextView _displayAlarmInfoTextView = null;
    private TextView _displayTimerInfoTextView = null;
    private Button _snoozeButton = null;
    private Button _dismissButton = null;
    private long _baseTime = 0;
    private long _alarmTime = 0;
    private Chronometer _masterChronometer = null;
    private long _hours = 0;
    private long _minutes = 0;
	private boolean _masterBlink = false;
	private long _timerOffset = 0;
	private PendingIntent _screenTimeoutPendingIntent = null;
	
	//Stored User Preferences
	private boolean _landscapeScreenEnabled = false;
	private boolean _hapticFeedbackEnabled = true;
	private String _appTheme = "0";
	private boolean _secondsEnabled = true;
	private boolean _blinkEnabled = false;
	private boolean _blurScreen = false;
	private boolean _dimScreen = true;
	private String _dimScreenAmount = null;
	private boolean _screenEnabled = true;
	private boolean _keyguardEnabled = true;	
	private String _snoozeAmount = null;
    private boolean _recurringAlarm = false;  
    private boolean _dismissNotificationFlag = false;
    private AcquireKeyguardHandler _acquireKeyguardHandler = null;
	
	//================================================================================
	// Public Methods
	//================================================================================
    
	/**
	 * Called when the activity is created. Set up views and buttons.
	 */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
	    _debug = Log.getDebug();
	    if (_debug) Log.v("AlarmActivity.onCreate()");
	    _context = getApplicationContext();
	    _keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
	    _acquireKeyguardHandler = new AcquireKeyguardHandler();
	    final Bundle extrasBundle = getIntent().getExtras();
	    _alarmType = extrasBundle.getInt(Constants.ALARM_TYPE);
	    _alarmTime = Common.getAlarmTime(_context, _alarmType);
	    loadUserPreferences();
	    Common.acquireWakeLock(_context);
	    parseAlarmTime();
	    initTimer();
	    //Don't rotate the Activity when the screen rotates based on the user preferences.
	    if(!_landscapeScreenEnabled){
	    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    //Get main window for this Activity.
	    Window mainWindow = getWindow();
	    //Turn Screen On Flags
	    if(_screenEnabled){
	    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		    //Unlock Keyguard Flags
		    if(_keyguardEnabled){
		    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
		    						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		    }
	    	//Set Background Blur Flags
		    if(_blurScreen){
		    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		    }
		    //Set Background Dim Flags
		    if(_dimScreen){
		    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 
			    WindowManager.LayoutParams params = mainWindow.getAttributes(); 
			    int dimAmt = Integer.parseInt(_dimScreenAmount);
			    params.dimAmount = dimAmt / 100f; 
			    mainWindow.setAttributes(params); 
		    }
	    }
	    boolean inKeyguardMode = _keyguardManager.inKeyguardRestrictedInputMode();
	    if(inKeyguardMode && _screenEnabled){
	    	super.setTheme(android.R.style.Theme);
		    if(_appTheme.equals("0")){
			    setContentView(R.layout.alarm_activity_keyguard_boy);
		    }else{
			    setContentView(R.layout.alarm_activity_keyguard_girl);
		    }
	    }else{
		    if(_appTheme.equals("0")){
			    setContentView(R.layout.alarm_activity_boy);
		    }else{
			    setContentView(R.layout.alarm_activity_girl);
		    }
	    }
	    setupViews();
	    setupButtons();
	    setupChronometer();
	    if(inKeyguardMode){
	    	_acquireKeyguardHandler.sleep(1000);
	    }
	    setScreenTimeoutAlarm();
    }
	
	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Activity was started after it stopped or for the first time.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		_debug = Log.getDebug();
	    if (_debug) Log.v("AlarmActivity.onStart()");
	}
	  
	/**
	 * Activity was resumed after it was stopped or paused.
	 */
	@Override
	protected void onResume() {
	    _debug = Log.getDebug();
	    if (_debug) Log.v("AlarmActivity.onResume()");
	    loadUserPreferences();
	    Common.acquireWakeLock(_context);
	    setScreenTimeoutAlarm();
	    super.onResume();
	}
	  
	/**
	 * Activity was paused due to a new Activity being started or other reason.
	 */
	@Override
	protected void onPause() {
	    if (_debug) Log.v("AlarmActivity.onPause()");
	    cancelScreenTimeout();
	    Common.clearWakeLock();
	    super.onPause();
	}
	  
	/**
	 * Activity was stopped due to a new Activity being started or other reason.
	 */
	@Override
	protected void onStop() {
	    super.onStop();
	    if (_debug) Log.v("AlarmActivity.onStop()");
	    finishActivity();
	}
	  
	/**
	 * Activity was stopped and closed out completely.
	 */
	@Override
	protected void onDestroy() {
	    if (_debug) Log.v("AlarmActivity.onDestroy()");
	    Common.clearWakeLock();
	    cancelScreenTimeout();
	    super.onDestroy();
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
    
	/**
	 * Performs haptic feedback based on the users preferences.
	 * 
	 * @param hapticFeedbackConstant - What type of action the feedback is responding to.
	 */
	private void customPerformHapticFeedback(int hapticFeedbackConstant){ 
    	if (_debug) Log.v("AlarmActivity.customPerformHapticFeedback()");
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		//Perform the haptic feedback based on the users preferences.
		if(_hapticFeedbackEnabled){
			if(hapticFeedbackConstant == HapticFeedbackConstants.VIRTUAL_KEY){
				//performHapticFeedback(hapticFeedbackConstant);
				vibrator.vibrate(50);
			}
			if(hapticFeedbackConstant == HapticFeedbackConstants.LONG_PRESS){
				//performHapticFeedback(hapticFeedbackConstant);
				vibrator.vibrate(100);
			}
		}
	}
    
	/**
	 * Parse the hours and minutes from the alarm time.
	 */
	private void parseAlarmTime(){
    	if (_debug) Log.v("AlarmActivity.parseAlarmTime() _alarmTime: " + _alarmTime);
		long timeValue = _alarmTime / 1000;
    	_hours = (timeValue / 3600);
    	timeValue = timeValue - _hours * 3600;
    	_minutes = (timeValue / 60);
	}
	
    /**
     * Set up the views for this Activity.
     */
    private void setupViews(){
    	if (_debug) Log.v("AlarmActivity.setupViews()");
    	_alarmMainLinearLayout =  (LinearLayout) findViewById(R.id.alarm_main_linear_layout);
    	_alarmHeaderTextView = (TextView) findViewById(R.id.alarm_header_text_view);
    	_displayAlarmInfoTextView = (TextView) findViewById(R.id.display_alarm_info_text_view);
    	_displayTimerInfoTextView = (TextView) findViewById(R.id.display_timer_info_text_view);
    	switch(_alarmType){
			case Constants.TYPE_DIAPER:{
				_alarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_diaper, 0);
				break;
			}
			case Constants.TYPE_BOTTLE:{
				_alarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_bottle, 0);
				break;
			}
			case Constants.TYPE_SLEEP:{
				_alarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_sleep, 0);
				break;
			}
			case Constants.TYPE_CUSTOM:{
				_alarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_star, 0);
				break;
			}
		}
    	//Set the background image if the phone is in lock mode.
		if(_keyguardManager.inKeyguardRestrictedInputMode()){
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(_context);
			_alarmMainLinearLayout.setBackgroundDrawable(wallpaperManager.getDrawable());
		}
    }
    
    /**
     * Set up the buttons for this Activity.
     */
    private void setupButtons(){
    	if (_debug) Log.v("AlarmActivity.setupButtons()");
    	_snoozeButton = (Button) findViewById(R.id.snooze_button);
    	_snoozeButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	if (_debug) Log.v("Snooze Button Clicked()");
		    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		    	snoozeAlarm();
		    	finish();
		    }
		});    	
    	_dismissButton = (Button) findViewById(R.id.dismiss_button);
    	_dismissButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	if (_debug) Log.v("Dismiss Button Clicked()");
		    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		    	dismissAlarm();
		    	finish();
		    }
		});
    }
    
    /**
     * Set up the timers for this Activity.
     */
    private void setupChronometer(){
    	if (_debug) Log.v("AlarmActivity.setupChronometers()");
    	_masterChronometer = (Chronometer) findViewById(R.id.masterChronometer);
    	_masterChronometer.setOnChronometerTickListener(new OnChronometerTickListener(){
            public void onChronometerTick(Chronometer chronometer) {
        		//Toggle the master blink flag.
        		if(_masterBlink){
        			_masterBlink = false;
        		}else{
        			_masterBlink = true;
        		}
            	updateDisplayInfoTextView();
            }
        });  
    	_masterChronometer.setBase(_baseTime);
    	_masterChronometer.start();
    }
    
    /**
     * Update the display info TextView with the alarm time & current elapsed time.
     */
    private void updateDisplayInfoTextView(){
    	//if (_debug) Log.v("AlarmActivity.updateTimerTextView()");	
    	long timeValue = (SystemClock.elapsedRealtime() + _timerOffset - _baseTime) / 1000;
    	long hoursValue = (timeValue / 3600);
    	String hours = String.valueOf(hoursValue);
    	timeValue = timeValue - (timeValue / 3600) * 3600;
    	long minutesValue = (timeValue / 60);
    	String minutes = "00";
    	if(minutesValue < 10){
    		minutes = "0" + String.valueOf(minutesValue);
    	}else{
    		minutes = String.valueOf(minutesValue);
    	}    
    	long secondsValue = (timeValue % 60);
    	String seconds = "00";
    	if(secondsValue < 10){
    		seconds = "0" + String.valueOf(secondsValue);
    	}else{
    		seconds = String.valueOf(secondsValue);
    	}
    	_displayAlarmInfoTextView.setText(String.valueOf(_hours) + ":" + formatNumber(_minutes));
    	if(_secondsEnabled){
    		_displayTimerInfoTextView.setText("(" + hours + ":" + minutes + ":" + seconds + ")");
    	}else if(_blinkEnabled){
    		if(_masterBlink){
    			_displayTimerInfoTextView.setText("(" + hours + ":" + minutes + ")");
    		}else{
    			_displayTimerInfoTextView.setText("(" + hours + " " + minutes + ")");
    		}
    	}else{
    		_displayTimerInfoTextView.setText("(" + hours + ":" + minutes + ")");
    	}
    }
    
    /**
     * Snooze the alarm.
     */
    private void snoozeAlarm(){
    	if (_debug) Log.v("AlarmActivity.snoozeAlarm()");	
    	Common.clearKeyguardLock();
    	long snoozeInMiliSeconds = Long.parseLong(_snoozeAmount) * 60 * 1000;
		AlarmManager alarmManager = (AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(_context, AlarmReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ALARM_TYPE, _alarmType);
		bundle.putBoolean(Constants.ALARM_SNOOZE, true);
		alarmIntent.putExtras(bundle);
		alarmIntent.setAction("apps.babycaretimer.action." + String.valueOf(_alarmType));
    	alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, alarmIntent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeInMiliSeconds, pendingIntent);
    	SharedPreferences.Editor editor = _preferences.edit();
    	switch(_alarmType){
			case Constants.TYPE_DIAPER:{
				editor.putBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, true);
				break;
			}
			case Constants.TYPE_BOTTLE:{
				editor.putBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, true);
				break;
			}
			case Constants.TYPE_SLEEP:{
				editor.putBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, true);
				break;
			}
			case Constants.TYPE_CUSTOM:{
				editor.putBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, true);
				break;
			}
		}
		editor.commit(); 
		removeNotification();
    	Toast.makeText(_context, _context.getString(R.string.alarm_snooze_message, _snoozeAmount), Toast.LENGTH_LONG).show();
    }
    
    /**
     * Dismiss the alarm.
     */
    private void dismissAlarm(){
        if (_debug) Log.v("AlarmActivity.dismissAlarm()");
        Common.clearKeyguardLock();
        SharedPreferences.Editor editor = _preferences.edit();      
        switch(_alarmType){
	        case Constants.TYPE_DIAPER:{
	            if(!_recurringAlarm){
	            	editor.putBoolean(Constants.DIAPER_ALARM_ACTIVE_KEY, false);
	            }
	            editor.putBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, false);
	            break;
	        }
	        case Constants.TYPE_BOTTLE:{
                if(!_recurringAlarm){
                	editor.putBoolean(Constants.BOTTLE_ALARM_ACTIVE_KEY, false);
                }
                editor.putBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, false);
                break;
	        }
	        case Constants.TYPE_SLEEP:{
                if(!_recurringAlarm){
                	editor.putBoolean(Constants.SLEEP_ALARM_ACTIVE_KEY, false);
                }
                editor.putBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, false);
                break;
	        }
	        case Constants.TYPE_CUSTOM:{
                if(!_recurringAlarm){
                	editor.putBoolean(Constants.CUSTOM_ALARM_ACTIVE_KEY, false);
                }
                editor.putBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, false);
                break;
	        }
        }
		editor.commit(); 
		removeNotification();
    }
	
	/**
	 * Format a number so that it is always 2 digits.
	 * 
	 * @param number - The number we want to format.
	 * 
	 * @return String - The formatted number.
	 */
	private String formatNumber(long number){
    	//if (_debug) Log.v("AlarmActivity.formatNumber()");
		if(number < 10){
			return "0" + String.valueOf(number);
		}else{
			return String.valueOf(number);
		}
	}

	/**
	 * Start the timers in the activity if they should be running/timing. 
	 * This is based on the persisted base values store in the activity preferences.
	 */
	private void initTimer() {
		if (_debug) Log.v("AlarmActivity.initTimers()");
		try{
			long currentElapsedTime = SystemClock.elapsedRealtime();
			long currentTime = System.currentTimeMillis();
			SharedPreferences.Editor editor = _preferences.edit();
			switch(_alarmType) {
				case Constants.TYPE_DIAPER: {
					if(Common.isTimerActive(_context, Constants.TYPE_DIAPER)){
						if (currentElapsedTime < _baseTime) {
							if (_debug) Log.v("AlarmActivity.initTimers() Phone was restarted...");
							long timerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_DIAPER);
							if(timerStartTime == 0){
								_baseTime = 0;
								_timerOffset = 0;
								editor.putLong(Constants.DIAPER_BASE_TIME_KEY, _baseTime);
								editor.putLong(Constants.DIAPER_TIMER_OFFSET_KEY, _timerOffset);
								editor.commit();
								return;
							}
							_baseTime = 0;
							_timerOffset = currentTime - timerStartTime;
							editor.putLong(Constants.DIAPER_BASE_TIME_KEY, _baseTime);
							editor.putLong(Constants.DIAPER_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}
					}else{
						_timerOffset = 0;
						editor.putLong(Constants.DIAPER_TIMER_OFFSET_KEY, _timerOffset);
						editor.commit();
					}
					break;
				}
				case Constants.TYPE_BOTTLE: {
					if(Common.isTimerActive(_context, Constants.TYPE_BOTTLE)){
						if (currentElapsedTime < _baseTime) {
							if (_debug) Log.v("AlarmActivity.initTimers() Phone was restarted...");
							long timerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_BOTTLE);
							if(timerStartTime == 0){
								_baseTime = 0;
								_timerOffset = 0;
								editor.putLong(Constants.BOTTLE_BASE_TIME_KEY, _baseTime);
								editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, _timerOffset);
								editor.commit();
								return;
							}
							_baseTime = 0;
							_timerOffset = currentTime - timerStartTime;
							editor.putLong(Constants.BOTTLE_BASE_TIME_KEY, _baseTime);
							editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}else{
							_timerOffset = 0;
							editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}
					}
					break;
				}
				case Constants.TYPE_SLEEP: {
					if(Common.isTimerActive(_context, Constants.TYPE_SLEEP)){
						if (currentElapsedTime < _baseTime) {
							if (_debug) Log.v("AlarmActivity.initTimers() Phone was restarted...");
							long timerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_SLEEP);
							if(timerStartTime == 0){
								_baseTime = 0;
								_timerOffset = 0;
								editor.putLong(Constants.SLEEP_BASE_TIME_KEY, _baseTime);
								editor.putLong(Constants.SLEEP_TIMER_OFFSET_KEY, _timerOffset);
								editor.commit();
								return;
							}
							_baseTime = 0;
							_timerOffset = currentTime - timerStartTime;
							editor.putLong(Constants.SLEEP_BASE_TIME_KEY, _baseTime);
							editor.putLong(Constants.SLEEP_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}else{
							_timerOffset = 0;
							editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}
					}
					break;
				}
				case Constants.TYPE_CUSTOM: {
					if(Common.isTimerActive(_context, Constants.TYPE_CUSTOM)){
						if (currentElapsedTime < _baseTime) {
							if (_debug) Log.v("AlarmActivity.initTimers() Phone was restarted...");
							long timerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_CUSTOM);
							if(timerStartTime == 0){
								_baseTime = 0;
								_timerOffset = 0;
								editor.putLong(Constants.CUSTOM_BASE_TIME_KEY, _baseTime);
								editor.putLong(Constants.CUSTOM_TIMER_OFFSET_KEY, _timerOffset);
								editor.commit();
								return;
							}
							_baseTime = 0;
							_timerOffset = currentTime - timerStartTime;
							editor.putLong(Constants.CUSTOM_BASE_TIME_KEY, 0);
							editor.putLong(Constants.CUSTOM_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}else{
							_timerOffset = 0;
							editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, _timerOffset);
							editor.commit();
						}
					}
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("AlarmActivity.initTimer() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Load the user preferences here and store them locally.
	 */
	private void loadUserPreferences(){
    	if (_debug) Log.v("AlarmActivity.loadUserPreferences()");
		try{
		    _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
			_landscapeScreenEnabled = _preferences.getBoolean(Constants.LANDSCAPE_SCREEN_ENABLED_KEY, false);
			_hapticFeedbackEnabled = _preferences.getBoolean(Constants.HAPTIC_FEEDBACK_ENABLED_KEY, true);
			_appTheme = _preferences.getString(Constants.APP_THEME_KEY, "0");
			_secondsEnabled = _preferences.getBoolean(Constants.SECONDS_ENABLED_KEY, true);
			_blinkEnabled = _preferences.getBoolean(Constants.BLINK_ENABLED_KEY, false);
			_snoozeAmount = _preferences.getString(Constants.SNOOZE_AMOUNT_KEY, "10");
			_blurScreen = _preferences.getBoolean(Constants.BLUR_SCREEN_ENABLED_KEY, false);
			_dimScreen = _preferences.getBoolean(Constants.DIM_SCREEN_ENABLED_KEY, true);
			_dimScreenAmount = _preferences.getString(Constants.DIM_SCREEN_AMOUNT_KEY, "50");
			_screenEnabled = _preferences.getBoolean(Constants.SCREEN_ENABLED_KEY, true);
			_keyguardEnabled = _preferences.getBoolean(Constants.KEYGUARD_ENABLED_KEY, true);
		    _baseTime = Common.getBaseTime(_context, _alarmType);
			_timerOffset = Common.getTimerOffset(_context, _alarmType);	
            _recurringAlarm = Common.isAlarmRecurring(_context, _alarmType);
            //Special setting adjustment based on the "Turn Screen On" settings
            if(!_screenEnabled){
            	_keyguardEnabled =  false;
            }
		}catch(Exception ex){
			if (_debug) Log.e("AlarmActivity.loadUserPreferences() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Remove a status bar notification.
	 */
	private void removeNotification(){
    	if (_debug) Log.v("AlarmActivity.removeNotification()");
    	//If this function was already called, ignore and return.
    	if(_dismissNotificationFlag){
    		return;
    	}
    	try{
			//Update the notification count in the user preferences and remove if the count is 0.
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
			int alarmCount = preferences.getInt(Constants.ALARM_NOTIFICATION_COUNT_KEY, 0);
			if(alarmCount > 0){
				alarmCount -= 1;
			}else{
				alarmCount = 0;
			}
			SharedPreferences.Editor editor = preferences.edit();
	    	editor.putInt(Constants.ALARM_NOTIFICATION_COUNT_KEY, alarmCount);
	        editor.commit();
	        if(alarmCount == 0){
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
	        }
	        _dismissNotificationFlag = true;
    	}catch(Exception ex){
    		if (_debug) Log.e("AlarmActivity.removeNotification() ERROR: " + ex.toString());
    	}
	}

	/**
	 * This class is a Handler that executes in it's own thread and is used to delay the releasing of the Keyguard.
	 * 
	 * @author Camille Sévigny
	 */
	class AcquireKeyguardHandler extends Handler {

		/**
		 * Handles the delayed function call when the sleep period is over.
		 * 
		 * @param msg - Message to be handled.
		 */
		@Override
		public void handleMessage(Message msg) {
			if (_debug) Log.v("AcquireKeyguardHandler.handleMessage()");
	    	Common.acquireKeyguardLock(_context);
		}
		    
		/**
		 * Put the thread to sleep for a period of time.
		 * 
		 * @param delayMillis - Delay time in milliseconds.
		 */
		public void sleep(long delayMillis) {
			if (_debug) Log.v("AcquireKeyguardHandler.sleep()");
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}

	};
	
	/**
	 * Sets the alarm that will clear the KeyguardLock & WakeLock.
	 */
	public void setScreenTimeoutAlarm(){
		if (_debug) Log.v("AlarmActivity.setScreenTimeoutAlarm()");
		long scheduledAlarmTime = System.currentTimeMillis() + (Long.parseLong(_preferences.getString(Constants.SCREEN_TIMEOUT_KEY, "300")) * 1000);
		AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
    	Intent intent = new Intent(_context, ScreenManagementAlarmReceiver.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    	_screenTimeoutPendingIntent = PendingIntent.getBroadcast(_context, 0, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledAlarmTime, _screenTimeoutPendingIntent);
	}
	
	/**
	 * Cancel the screen timeout alarm.
	 */
	private void cancelScreenTimeout(){
		if (_debug) Log.v("AlarmActivity.cancelScreenTimeout()");
		if (_screenTimeoutPendingIntent != null) {
	    	AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(_screenTimeoutPendingIntent);
	    	_screenTimeoutPendingIntent.cancel();
	    	_screenTimeoutPendingIntent = null;
		}
	}
	
	/**
	 * Customized activity finish.
	 * This closes this activity screen.
	 */
	private void finishActivity() {
		if (_debug) Log.v("AlarmActivity.finishActivity()");	
	    Common.clearKeyguardLock();
	    Common.clearWakeLock();
	    cancelScreenTimeout();
	    //Finish the activity.
	    finish();
	}
	
}