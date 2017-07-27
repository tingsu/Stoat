package apps.babycaretimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.receivers.AlarmReceiver;

/**
 * This class is used for setting all alarms in this application.
 * 
 * @author Camille Sévigny
 */
public class SetAlarmActivity extends Activity {
	
	//================================================================================
    // Properties
    //================================================================================
	
	private boolean _debug = false;
	private Context _context = null;
	private SharedPreferences _preferences = null;
    private int _alarmType = 0;
	private long _alarmTime = 0;
    private NumberPicker _hoursNumberPicker = null;
    private NumberPicker _minutesNumberPicker = null;
    private Button _setAlarmButton = null;
    private Button _cancelAlarmButton = null;
    private Button _cancelButton = null;
    private TextView _setAlarmHeaderTextView = null;
	private long _baseTime = 0;
	private long _timerOffset = 0;
	private long _timerStartTime = 0;
	private CheckBox _recurringCheckbox = null;
	private boolean _recurringAlarm = true;
	
	//Stored User Preferences
	private boolean _landscapeScreenEnabled = false;
	private boolean _hapticFeedbackEnabled = true;
	private String _appTheme = "0";
	private String _alarmMaxHours = null;
	
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
	    if (_debug) Log.v("SetAlarmActivity.onCreate()");
	    _context = getApplicationContext();
	    final Bundle extrasBundle = getIntent().getExtras();
	    _alarmType = extrasBundle.getInt(Constants.ALARM_TYPE);
	    _alarmTime = extrasBundle.getLong(Constants.ALARM_TIME);
	    loadUserPreferences();
	    //Don't rotate the Activity when the screen rotates based on the user preferences.
	    if(!_landscapeScreenEnabled){
	    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    //Get main window for this Activity.
	    Window mainWindow = getWindow(); 
	    //Set Dim
    	mainWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 
	    WindowManager.LayoutParams params = mainWindow.getAttributes(); 
	    params.dimAmount = Constants.DIM_BACKGROUND_AMOUNT / 100f; 
	    mainWindow.setAttributes(params); 
	    if(_appTheme.equals("0")){
		    setContentView(R.layout.set_alarm_activity_boy);
	    }else{
		    setContentView(R.layout.set_alarm_activity_girl);
	    }
	    setupViews();
	    setupButtons();
	    setupNumberPickers();
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
	    if (_debug) Log.v("SetAlarmActivity.onStart()");
	}
	  
	/**
	 * Activity was resumed after it was stopped or paused.
	 */
	@Override
	protected void onResume() {
	    super.onResume();
	    _debug = Log.getDebug();
	    if (_debug) Log.v("SetAlarmActivity.onResume()");
	    loadUserPreferences();
	}
	  
	/**
	 * Activity was paused due to a new Activity being started or other reason.
	 */
	@Override
	protected void onPause() {
	    super.onPause();
	    if (_debug) Log.v("SetAlarmActivity.onPause()");
	}
	  
	/**
	 * Activity was stopped due to a new Activity being started or other reason.
	 */
	@Override
	protected void onStop() {
	    super.onStop();
	    if (_debug) Log.v("SetAlarmActivity.onStop()");
		finish();
	}
	  
	/**
	 * Activity was stopped and closed out completely.
	 */
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (_debug) Log.v("SetAlarmActivity.onDestroy()");
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
    	if (_debug) Log.v("SetAlarmActivity.customPerformHapticFeedback()");
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
     * Set up the views for this Activity.
     */
    private void setupViews(){
    	if (_debug) Log.v("SetAlarmActivity.setupViews()");
    	_hoursNumberPicker = (NumberPicker) findViewById(R.id.hours_number_picker);
    	_minutesNumberPicker = (NumberPicker) findViewById(R.id.minutes_number_picker);
    	_setAlarmHeaderTextView = (TextView) findViewById(R.id.set_alarm_header_text_view);
    	_recurringCheckbox = (CheckBox) findViewById(R.id.recurring_checkbox);
    	switch(_alarmType){
			case Constants.TYPE_DIAPER:{
				_setAlarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_diaper, 0);
		    	if(_alarmTime > 0){
		    		_recurringCheckbox.setChecked(_recurringAlarm);
		    	}
				break;
			}
			case Constants.TYPE_BOTTLE:{
				_setAlarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_bottle, 0);
		    	if(_alarmTime > 0){
		    		_recurringCheckbox.setChecked(_recurringAlarm);
		    	}
				break;
			}
			case Constants.TYPE_SLEEP:{
				_setAlarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_sleep, 0);
		    	if(_alarmTime > 0){
		    		_recurringCheckbox.setChecked(_recurringAlarm);
		    	}
				break;
			}
			case Constants.TYPE_CUSTOM:{
				_setAlarmHeaderTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, R.drawable.ic_star, 0);
		    	if(_alarmTime > 0){
		    		_recurringCheckbox.setChecked(_recurringAlarm);
		    	}
				break;
			}
		}
    }

    /**
     * Set up the buttons for this Activity.
     */
    private void setupButtons(){
    	if (_debug) Log.v("SetAlarmActivity.setupButtons()");
    	_setAlarmButton = (Button) findViewById(R.id.set_alarm_button);
    	_setAlarmButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	if (_debug) Log.v("Set Alarm Button Clicked()");
		    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	    		if(_hoursNumberPicker.getCurrent() == 0 && _minutesNumberPicker.getCurrent() == 0){
	    			Toast.makeText(_context, getResources().getString(R.string.alarm_error_zero_text), Toast.LENGTH_LONG).show();
	    			return;
	    		}
		    	setAlarm();
		    	finish();
		    }
		});
    	_cancelAlarmButton = (Button) findViewById(R.id.cancel_alarm_button);
    	if(_alarmTime > 0){
	    	_cancelAlarmButton.setOnClickListener(new OnClickListener() {
			    public void onClick(View v) {
			    	if (_debug) Log.v("Set Alarm Button Clicked()");
			    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			    	cancelAlarm();
			    	finish();
			    }
			});
    	}else{
        	//Hide the cancel alarm button if this is a alarm.
    		_cancelAlarmButton.setVisibility(View.GONE);
    	}
    	_cancelButton = (Button) findViewById(R.id.cancel_button);
    	_cancelButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	if (_debug) Log.v("Set Alarm Button Clicked()");
		    	customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		    	finish();
		    }
		});
    }
	
	/**
	 * Sets up the min and max values for the number pickers.
	 */
	private void setupNumberPickers(){
    	if (_debug) Log.v("SetAlarmActivity.setupNumberPickers()");
		int maxHoursPreference = Integer.parseInt(_alarmMaxHours);
		_hoursNumberPicker.setRange(0, maxHoursPreference);
		_minutesNumberPicker.setRange(0, 60);
		if(_alarmTime > 0){
			long timeValue = _alarmTime / 1000;
	    	long hours = (timeValue / 3600);
	    	timeValue = timeValue - hours * 3600;
	    	long minutes = (timeValue / 60);
			_hoursNumberPicker.setCurrent(Integer.parseInt(String.valueOf(hours)));
			_minutesNumberPicker.setCurrent(Integer.parseInt(String.valueOf(minutes)));
		}
	}
	
	/**
	 * Set the alarm. Store the values and initialize this alarm.
	 */
	private void setAlarm(){
    	if (_debug) Log.v("SetAlarmActivity.setAlarm()");
    	try{
			switch(_alarmType){
				case Constants.TYPE_DIAPER:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.DIAPER_ALARM_ACTIVE_KEY, true);
					editor.putBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, false);
					editor.putLong(Constants.DIAPER_ALARM_TIME_KEY, (_hoursNumberPicker.getCurrent() * 60 * 60 * 1000) + (_minutesNumberPicker.getCurrent() * 60 * 1000));
					editor.putBoolean(Constants.DIAPER_ALARM_RECURRING_KEY, _recurringCheckbox.isChecked());
					editor.commit();
					break;
				}
				case Constants.TYPE_BOTTLE:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.BOTTLE_ALARM_ACTIVE_KEY, true);
					editor.putBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, false);
					editor.putLong(Constants.BOTTLE_ALARM_TIME_KEY, (_hoursNumberPicker.getCurrent() * 60 * 60 * 1000) + (_minutesNumberPicker.getCurrent() * 60 * 1000));
					editor.putBoolean(Constants.BOTTLE_ALARM_RECURRING_KEY, _recurringCheckbox.isChecked());
					editor.commit();
					break;
				}
				case Constants.TYPE_SLEEP:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.SLEEP_ALARM_ACTIVE_KEY, true);
					editor.putBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, false);
					editor.putLong(Constants.SLEEP_ALARM_TIME_KEY, (_hoursNumberPicker.getCurrent() * 60 * 60 * 1000) + (_minutesNumberPicker.getCurrent() * 60 * 1000));
					editor.putBoolean(Constants.SLEEP_ALARM_RECURRING_KEY, _recurringCheckbox.isChecked());
					editor.commit();
					break;
				}
				case Constants.TYPE_CUSTOM:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.CUSTOM_ALARM_ACTIVE_KEY, true);
					editor.putBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, false);
					editor.putLong(Constants.CUSTOM_ALARM_TIME_KEY, (_hoursNumberPicker.getCurrent() * 60 * 60 * 1000) + (_minutesNumberPicker.getCurrent() * 60 * 1000));
					editor.putBoolean(Constants.CUSTOM_ALARM_RECURRING_KEY, _recurringCheckbox.isChecked());
					editor.commit();
					break;
				}
			}
			//Set an alarm to go off at the appropriate time and trigger the AlarmActivity.
			long alarmTime = (_hoursNumberPicker.getCurrent() * 60 * 60 * 1000) + (_minutesNumberPicker.getCurrent() * 60 * 1000);
			long elapsedTime = 0;
			if (_debug) Log.v("SetAlarmActivity.setAlarm() baseTime: " + _baseTime);
			if (_debug) Log.v("SetAlarmActivity.setAlarm() timerOffset: " + _timerOffset);
			if (_debug) Log.v("SetAlarmActivity.setAlarm() timerStartTime: " + _timerStartTime);
			if (_baseTime == 0) {
				elapsedTime = SystemClock.elapsedRealtime() + _timerOffset;
			}else if (_timerOffset == 0) {
				elapsedTime = SystemClock.elapsedRealtime() - _baseTime;
			}else{
				if (_debug) Log.v("SetAlarmActivity.setAlarm() BaseTime and TimerOffset are null. Exiting...");
				return;
			}
			if (_debug) Log.v("SetAlarmActivity.setAlarm() elapsedTime: " + elapsedTime);
			long alarmAlertTime = System.currentTimeMillis() + alarmTime - elapsedTime;
			if (_debug) Log.v("SetAlarmActivity.setAlarm() AlarmTime: " + alarmTime + " ElapsedTime:" + elapsedTime + " AlarmAlertTime: " + alarmAlertTime);
			AlarmManager alarmManager = (AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(_context, AlarmReceiver.class);
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.ALARM_TYPE, _alarmType);
			bundle.putBoolean(Constants.ALARM_SNOOZE, false);
			alarmIntent.putExtras(bundle);
			alarmIntent.setAction("apps.babycaretimer.action." + String.valueOf(_alarmType));
	    	alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, alarmIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmAlertTime, pendingIntent);
			//Notify the user of the set alarm.
			Toast.makeText(_context, String.format(getResources().getString(R.string.alarm_has_been_set_text), String.valueOf(_hoursNumberPicker.getCurrent()), String.valueOf(_minutesNumberPicker.getCurrent())), Toast.LENGTH_LONG).show();
    	}catch(Exception ex){
			if (_debug) Log.e("SetAlarmActivity.setAlarm() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Cancel the alarm. Store the values.
	 */
	private void cancelAlarm(){
    	if (_debug) Log.v("SetAlarmActivity.cancelAlarm()");
    	try{
			switch(_alarmType){
				case Constants.TYPE_DIAPER:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.DIAPER_ALARM_ACTIVE_KEY, false);
					editor.putString(Constants.DIAPER_ALARM_TIME_KEY, "0:0");
					editor.putBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, false);
					editor.putBoolean(Constants.DIAPER_ALARM_RECURRING_KEY, false);
					editor.commit();
					break;
				}
				case Constants.TYPE_BOTTLE:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.BOTTLE_ALARM_ACTIVE_KEY, false);
					editor.putString(Constants.BOTTLE_ALARM_TIME_KEY, "0:0");
					editor.putBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, false);
					editor.putBoolean(Constants.BOTTLE_ALARM_RECURRING_KEY, false);
					editor.commit();
					break;
				}
				case Constants.TYPE_SLEEP:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.SLEEP_ALARM_ACTIVE_KEY, false);
					editor.putString(Constants.SLEEP_ALARM_TIME_KEY, "0:0");
					editor.putBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, false);
					editor.putBoolean(Constants.SLEEP_ALARM_RECURRING_KEY, false);
					editor.commit();
					break;
				}
				case Constants.TYPE_CUSTOM:{
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.CUSTOM_ALARM_ACTIVE_KEY, false);
					editor.putString(Constants.CUSTOM_ALARM_TIME_KEY, "0:0");
					editor.putBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, false);
					editor.putBoolean(Constants.CUSTOM_ALARM_RECURRING_KEY, false);
					editor.commit();
					break;
				}
			}		
		}catch(Exception ex){
			if (_debug) Log.e("SetAlarmActivity.cancelAlarm() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Load the user preferences here and store them locally.
	 */
	private void loadUserPreferences(){
    	if (_debug) Log.v("SetAlarmActivity.loadUserPreferences()");
		try{
		    _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
			_landscapeScreenEnabled = _preferences.getBoolean(Constants.LANDSCAPE_SCREEN_ENABLED_KEY, false);
			_hapticFeedbackEnabled = _preferences.getBoolean(Constants.HAPTIC_FEEDBACK_ENABLED_KEY, true);
			_appTheme = _preferences.getString(Constants.APP_THEME_KEY, "0");	
			_alarmMaxHours = _preferences.getString(Constants.ALARM_MAX_HOURS_KEY, "48");
		    _baseTime = Common.getBaseTime(_context, _alarmType);
		    _timerOffset = Common.getTimerOffset(_context, _alarmType);
		    _timerStartTime = Common.getTimerStartTime(_context, _alarmType);
	    	_recurringAlarm = Common.isAlarmRecurring(_context, _alarmType);
		}catch(Exception ex){
			if (_debug) Log.e("SetAlarmActivity.loadUserPreferences() ERROR: " + ex.toString());
		}
	}

}