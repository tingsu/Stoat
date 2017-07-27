package apps.babycaretimer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import apps.babycaretimer.preferences.MainPreferenceActivity;
import apps.babycaretimer.receivers.AlarmReceiver;
import apps.babycaretimer.common.Common;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;

/**
 * This class is the main activity of this application.
 * 
 * @author Camille Sévigny
 */
public class TimerActivity extends Activity {

	// ================================================================================
	// Constants
	// ================================================================================
	
	private static final int MENU_ITEM_SETTINGS = R.id.app_settings;
	
	private static final int DIAPER_LINEAR_LAYOUT_ID = R.id.diaper_linear_layout;
	private static final int BOTTLE_LINEAR_LAYOUT_ID = R.id.bottle_linear_layout;
	private static final int SLEEP_LINEAR_LAYOUT_ID = R.id.sleep_linear_layout;
	private static final int CUSTOM_LINEAR_LAYOUT_ID = R.id.custom_linear_layout;

	private static final int DIAPER_SET_ALARM_CONTEXT_MENU = R.id.diaper_set_alarm;
	private static final int DIAPER_CANCEL_ALARM_CONTEXT_MENU = R.id.diaper_cancel_alarm;
	private static final int DIAPER_RESTART_TIMER_CONTEXT_MENU = R.id.diaper_restart_timer;
	private static final int DIAPER_STOP_TIMER_CONTEXT_MENU = R.id.diaper_stop_timer;
	private static final int BOTTLE_SET_ALARM_CONTEXT_MENU = R.id.bottle_set_alarm;
	private static final int BOTTLE_CANCEL_ALARM_CONTEXT_MENU = R.id.bottle_cancel_alarm;
	private static final int BOTTLE_RESTART_TIMER_CONTEXT_MENU = R.id.bottle_restart_timer;
	private static final int BOTTLE_STOP_TIMER_CONTEXT_MENU = R.id.bottle_stop_timer;
	private static final int SLEEP_SET_ALARM_CONTEXT_MENU = R.id.sleep_set_alarm;
	private static final int SLEEP_CANCEL_ALARM_CONTEXT_MENU = R.id.sleep_cancel_alarm;
	private static final int SLEEP_RESTART_TIMER_CONTEXT_MENU = R.id.sleep_restart_timer;
	private static final int SLEEP_STOP_TIMER_CONTEXT_MENU = R.id.sleep_stop_timer;
	private static final int CUSTOM_SET_ALARM_CONTEXT_MENU = R.id.custom_set_alarm;
	private static final int CUSTOM_CANCEL_ALARM_CONTEXT_MENU = R.id.custom_cancel_alarm;
	private static final int CUSTOM_RESTART_TIMER_CONTEXT_MENU = R.id.custom_restart_timer;
	private static final int CUSTOM_STOP_TIMER_CONTEXT_MENU = R.id.custom_stop_timer;
	
	// ================================================================================
	// Properties
	// ================================================================================

	private boolean _debug = false;
	private Context _context = null;
	private SharedPreferences _preferences = null;

	private ImageButton _diaperImageButton = null;
	private ImageButton _bottleImageButton = null;
	private ImageButton _sleepImageButton = null;
	private ImageButton _customImageButton = null;

	private TextView _diaperTextView = null;
	private TextView _bottleTextView = null;
	private TextView _sleepTextView = null;
	private TextView _customTextView = null;

	private TextView _diaperAlarmTextView = null;
	private TextView _bottleAlarmTextView = null;
	private TextView _sleepAlarmTextView = null;
	private TextView _customAlarmTextView = null;

	private TextView _LTextView = null;
	private TextView _RTextView = null;

	private TextView _diaperTotalTextView = null;
	private TextView _bottleTotalTextView = null;
	private TextView _sleepTotalTextView = null;
	private TextView _customTotalTextView = null;

	private LinearLayout _diaperLinearLayout = null;
	private LinearLayout _bottleLinearLayout = null;
	private LinearLayout _sleepLinearLayout = null;
	private LinearLayout _customLinearLayout = null;

	private ImageView _diaperImageView = null;
	private ImageView _bottleImageView = null;
	private ImageView _sleepImageView = null;
	private ImageView _customImageView = null;

	private SeekBar _seekBar = null;

	private Chronometer _masterChronometer = null;

	private boolean _masterBlink = false;
	
	private boolean _onCreateFlag = true;
	
	//Stored User Preferences
	private boolean _landscapeScreenEnabled = false;
	private boolean _hapticFeedbackEnabled = true;
	private boolean _confirmResetCounters = true;
	private String _appTheme = "0";
	private boolean _secondsEnabled = true;
	private boolean _blinkEnabled = false;

	private int _breastFeedingSide = 0;

	private boolean _diaperAlarmActive = false;
	private boolean _bottleAlarmActive = false;
	private boolean _sleepAlarmActive = false;
	private boolean _customAlarmActive = false;

	private boolean _diaperAlarmSnooze = false;
	private boolean _bottleAlarmSnooze = false;
	private boolean _sleepAlarmSnooze = false;
	private boolean _customAlarmSnooze = false;
	
	private long _diaperAlarmTime = 0;
	private long _bottleAlarmTime = 0;
	private long _sleepAlarmTime = 0;
	private long _customAlarmTime = 0;

	private boolean _diaperAlarmRecurring = false;
	private boolean _bottleAlarmRecurring = false;
	private boolean _sleepAlarmRecurring = false;
	private boolean _customAlarmRecurring = false;
	
	private long _diaperBaseTime = 0;
	private long _bottleBaseTime = 0;
	private long _sleepBaseTime = 0;
	private long _customBaseTime = 0;
	
	private int _diaperCount = 0;
	private int _bottleCount = 0;
	private int _sleepCount = 0;
	private int _customCount = 0;
	
	private boolean _diaperTimerActive = false;
	private boolean _bottleTimerActive = false;
	private boolean _sleepTimerActive = false;
	private boolean _customTimerActive = false;
	
	private long _diaperTimerOffset = 0;
	private long _bottleTimerOffset = 0;
	private long _sleepTimerOffset = 0;
	private long _customTimerOffset = 0;

	private long _diaperTimerStartTime = 0;
	private long _bottleTimerStartTime = 0;
	private long _sleepTimerStartTime = 0;
	private long _customTimerStartTime = 0;
	
	private long _totalSleepTime = 0;
	
	// ================================================================================
	// Public Methods
	// ================================================================================

	/**
	 * Called when the activity is created. Set up views and timers.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_debug = Log.getDebug();
		if (_debug) Log.v("TimerActivity.onCreate()");
		_context = getApplicationContext();
	}

	/**
	 * Creates the menu item for this activity.
	 * 
	 * @param menu - Menu.
	 * 
	 * @return boolean - Returns true to indicate that the menu was created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (_debug) Log.v("TimerActivity.onCreateOptionsMenu()");
		getMenuInflater().inflate(R.menu.optionsmenu, menu);
		return true;
	}

	/**
	 * Handle the users selecting of the menu items.
	 * 
	 * @param menuItem - Menu Item.
	 * 
	 * @return boolean - Returns true to indicate that the action was handled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (_debug) Log.v("TimerActivity.onOptionsItemSelected()");
		// Handle item selection
		switch(menuItem.getItemId()) {
			case MENU_ITEM_SETTINGS: {
				launchPreferenceScreen();
				return true;
			}
		}
		return false;
	}

	/**
	 * Create Context Menu (Long-press menu).
	 * 
	 * @param contextMenu - ContextMenu
	 * @param view - View
	 * @param contextMenuInfo - ContextMenuInfo
	 */
	@Override
	public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo) {
		super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
		if (_debug) Log.v("TimerActivity.onCreateContextMenu()");
		switch(view.getId()) {
			case DIAPER_LINEAR_LAYOUT_ID: {
				MenuInflater menuInflater = getMenuInflater();
				// Add the header text to the menu.
				contextMenu.setHeaderTitle("Timer Options");
				menuInflater.inflate(R.menu.diapertimercontextmenu, contextMenu);
				if (_diaperAlarmActive) {
					MenuItem setAlarmMenuItem = contextMenu.findItem(DIAPER_SET_ALARM_CONTEXT_MENU);
					setAlarmMenuItem.setVisible(false);
				} else {
					MenuItem cancelAlarmMenuItem = contextMenu.findItem(DIAPER_CANCEL_ALARM_CONTEXT_MENU);
					cancelAlarmMenuItem.setVisible(false);
				}
				break;
			}
			case BOTTLE_LINEAR_LAYOUT_ID: {
				MenuInflater menuInflater = getMenuInflater();
				// Add the header text to the menu.
				contextMenu.setHeaderTitle("Timer Options");
				menuInflater.inflate(R.menu.bottletimercontextmenu, contextMenu);
				if (_bottleAlarmActive) {
					MenuItem setAlarmMenuItem = contextMenu.findItem(BOTTLE_SET_ALARM_CONTEXT_MENU);
					setAlarmMenuItem.setVisible(false);
				} else {
					MenuItem cancelAlarmMenuItem = contextMenu.findItem(BOTTLE_CANCEL_ALARM_CONTEXT_MENU);
					cancelAlarmMenuItem.setVisible(false);
				}
				break;
			}
			case SLEEP_LINEAR_LAYOUT_ID: {
				MenuInflater menuInflater = getMenuInflater();
				// Add the header text to the menu.
				contextMenu.setHeaderTitle("Timer Options");
				menuInflater.inflate(R.menu.sleeptimercontextmenu, contextMenu);
				if (_sleepAlarmActive) {
					MenuItem setAlarmMenuItem = contextMenu.findItem(SLEEP_SET_ALARM_CONTEXT_MENU);
					setAlarmMenuItem.setVisible(false);
				} else {
					MenuItem cancelAlarmMenuItem = contextMenu.findItem(SLEEP_CANCEL_ALARM_CONTEXT_MENU);
					cancelAlarmMenuItem.setVisible(false);
				}
				break;
			}
			case CUSTOM_LINEAR_LAYOUT_ID: {
				MenuInflater menuInflater = getMenuInflater();
				// Add the header text to the menu.
				contextMenu.setHeaderTitle("Timer Options");
				menuInflater.inflate(R.menu.customtimercontextmenu, contextMenu);
				if (_customAlarmActive) {
					MenuItem setAlarmMenuItem = contextMenu.findItem(CUSTOM_SET_ALARM_CONTEXT_MENU);
					setAlarmMenuItem.setVisible(false);
				} else {
					MenuItem cancelAlarmMenuItem = contextMenu.findItem(CUSTOM_CANCEL_ALARM_CONTEXT_MENU);
					cancelAlarmMenuItem.setVisible(false);
				}
				break;
			}
		}
	}

	/**
	 * Context Menu Item Selected (Long-press menu item selected).
	 * 
	 * @param menuItem - Create the context menu items for this Activity.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		if (_debug) Log.v("TimerActivity.onContextItemSelected()");
		// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		switch(menuItem.getItemId()) {
			case DIAPER_SET_ALARM_CONTEXT_MENU: {
				if (_diaperAlarmActive) {
					startSetAlarmActivity(Constants.TYPE_DIAPER, _diaperAlarmTime);
				}else{
					startSetAlarmActivity(Constants.TYPE_DIAPER, 0);
				}
				return true;
			}
			case DIAPER_CANCEL_ALARM_CONTEXT_MENU: {
				cancelAlarm(Constants.TYPE_DIAPER);
				return true;
			}
			case DIAPER_RESTART_TIMER_CONTEXT_MENU: {
				startDiaperChronometer();
				updateDiaperTotalSummary();
				return true;
			}
			case DIAPER_STOP_TIMER_CONTEXT_MENU: {
				stopDiaperChronometer();
				return true;
			}
			case BOTTLE_SET_ALARM_CONTEXT_MENU: {
				if (_bottleAlarmActive) {
					startSetAlarmActivity(Constants.TYPE_BOTTLE, _bottleAlarmTime);
				}else{
					startSetAlarmActivity(Constants.TYPE_BOTTLE, 0);
				}
				return true;
			}
			case BOTTLE_CANCEL_ALARM_CONTEXT_MENU: {
				cancelAlarm(Constants.TYPE_BOTTLE);
				return true;
			}
			case BOTTLE_RESTART_TIMER_CONTEXT_MENU: {
				startBottleChronometer();
				updateBottleTotalSummary();
				return true;
			}
			case BOTTLE_STOP_TIMER_CONTEXT_MENU: {
				stopBottleChronometer();
				return true;
			}
			case SLEEP_SET_ALARM_CONTEXT_MENU: {
				if (_sleepAlarmActive) {
					startSetAlarmActivity(Constants.TYPE_SLEEP, _sleepAlarmTime);
				}else{
					startSetAlarmActivity(Constants.TYPE_SLEEP, 0);
				}
				return true;
			}
			case SLEEP_CANCEL_ALARM_CONTEXT_MENU: {
				cancelAlarm(Constants.TYPE_SLEEP);
				return true;
			}
			case SLEEP_RESTART_TIMER_CONTEXT_MENU: {
				startSleepChronometer();
				updateSleepTotalSummary();
				return true;
			}
			case SLEEP_STOP_TIMER_CONTEXT_MENU: {
				stopSleepChronometer();
				return true;
			}
			case CUSTOM_SET_ALARM_CONTEXT_MENU: {
				if (_customAlarmActive) {
					startSetAlarmActivity(Constants.TYPE_CUSTOM, _customAlarmTime);
				}else{
					startSetAlarmActivity(Constants.TYPE_CUSTOM, 0);
				}
				return true;
			}
			case CUSTOM_CANCEL_ALARM_CONTEXT_MENU: {
				cancelAlarm(Constants.TYPE_CUSTOM);
				return true;
			}
			case CUSTOM_RESTART_TIMER_CONTEXT_MENU: {
				startCustomChronometer();
				updateCustomTotalSummary();
				return true;
			}
			case CUSTOM_STOP_TIMER_CONTEXT_MENU: {
				stopCustomChronometer();
				return true;
			}
			default: {
				return super.onContextItemSelected(menuItem);
			}
		}
	}

	// ================================================================================
	// Protected Methods
	// ================================================================================


    /**
     * This is called when the activity is running and it is triggered and run again for a different notification.
     * This is a copy of the onCreate() method but without the initialization calls.
     * 
     * @param intent - Activity intent.
     */
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
		_debug = Log.getDebug();
		if (_debug) Log.v("TimerActivity.onNewIntent()");
		setupApp();
	}
	
	/**
	 * Activity was started after it stopped or for the first time.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		_debug = Log.getDebug();
		if (_debug) Log.v("TimerActivity.onStart()");
	}

	/**
	 * Activity was resumed after it was stopped or paused.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		_debug = Log.getDebug();
		if (_debug) Log.v("TimerActivity.onResume()");
		setupApp();
	}

	/**
	 * Activity was paused due to a new Activity being started or other reason.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (_debug) Log.v("TimerActivity.onPause()");
	}

	/**
	 * Activity was stopped due to a new Activity being started or other reason.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if (_debug) Log.v("TimerActivity.onStop()");
	}

	/**
	 * Activity was stopped and closed out completely.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (_debug) Log.v("TimerActivity.onDestroy()");
	}

	/**
	 * Create new Dialog.
	 * 
	 * @param id - ID of the Dialog that we want to display.
	 * 
	 * @return Dialog - Popup Dialog created.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (_debug)
			Log.v("TimerActivity.onCreateDialog() ID: " + id);
		AlertDialog alertDialog = null;
		switch(id) {
			case Constants.DIAPER_DIALOG_RESET_COUNTER: {
				if (_debug) Log.v("TimerActivity.onCreateDialog() DIAPER_DIALOG_RESET_COUNTER");
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(_context.getString(R.string.reset_counter_text));
				builder.setMessage(_context.getString(R.string.reset_counter_diaper_dialog_text));
				builder.setPositiveButton(R.string.reset_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								resetDiaperCounter();
							}
						}).setNegativeButton(R.string.cancel_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								dialog.cancel();
							}
						});
				alertDialog = builder.create();
				break;
			}
			case Constants.BOTTLE_DIALOG_RESET_COUNTER: {
				if (_debug) Log.v("TimerActivity.onCreateDialog() BOTTLE_DIALOG_RESET_COUNTER");
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(_context.getString(R.string.reset_counter_text));
				builder.setMessage(_context.getString(R.string.reset_counter_bottle_dialog_text));
				builder.setPositiveButton(R.string.reset_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								resetBottleCounter();
							}
						}).setNegativeButton(R.string.cancel_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								dialog.cancel();
							}
						});
				alertDialog = builder.create();
				break;
			}
			case Constants.SLEEP_DIALOG_RESET_COUNTER: {
				if (_debug) Log.v("TimerActivity.onCreateDialog() SLEEP_DIALOG_RESET_COUNTER");
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(_context.getString(R.string.reset_counter_text));
				builder.setMessage(_context.getString(R.string.reset_counter_sleep_dialog_text));
				builder.setPositiveButton(R.string.reset_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								resetSleepCounter();
							}
						}).setNegativeButton(R.string.cancel_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								dialog.cancel();
							}
						});
				alertDialog = builder.create();
				break;
			}
			case Constants.CUSTOM_DIALOG_RESET_COUNTER: {
				if (_debug) Log.v("TimerActivity.onCreateDialog() CUSTOM_DIALOG_RESET_COUNTER");
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(_context.getString(R.string.reset_counter_text));
				builder.setMessage(_context.getString(R.string.reset_counter_custom_dialog_text));
				builder.setPositiveButton(R.string.reset_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								resetCustomCounter();
							}
						}).setNegativeButton(R.string.cancel_text,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
								dialog.cancel();
							}
						});
				alertDialog = builder.create();
				break;
			}
		}
		return alertDialog;
	}

	// ================================================================================
	// Private Methods
	// ================================================================================

	/**
	 * Performs haptic feedback based on the users preferences.
	 * 
	 * @param hapticFeedbackConstant
	 *            - What type of action the feedback is responding to.
	 */
	private void customPerformHapticFeedback(int hapticFeedbackConstant) {
		if (_debug) Log.v("TimerActivity.customPerformHapticFeedback()");
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// Perform the haptic feedback based on the users preferences.
		if (_hapticFeedbackEnabled) {
			if (hapticFeedbackConstant == HapticFeedbackConstants.VIRTUAL_KEY) {
				// performHapticFeedback(hapticFeedbackConstant);
				vibrator.vibrate(50);
			}
			if (hapticFeedbackConstant == HapticFeedbackConstants.LONG_PRESS) {
				// performHapticFeedback(hapticFeedbackConstant);
				vibrator.vibrate(100);
			}
		}
	}
	
	/**
	 * Sets up the application to be displayed.
	 */
	private void setupApp() {
		if (_debug) Log.v("TimerActivity.setupApp()");
		loadUserPreferences();
		// Don't rotate the Activity when the screen rotates based on the user preferences.
		if (!_landscapeScreenEnabled) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		if(_appTheme.equals("0")){
			setContentView(R.layout.timer_activity_boy);
		} else {
			setContentView(R.layout.timer_activity_girl);
		}
		setupViews();
		setupButtons();
		setupContextMenus();
		initLongPressView((View) findViewById(DIAPER_LINEAR_LAYOUT_ID));
		initLongPressView((View) findViewById(BOTTLE_LINEAR_LAYOUT_ID));
		initLongPressView((View) findViewById(SLEEP_LINEAR_LAYOUT_ID));
		initLongPressView((View) findViewById(CUSTOM_LINEAR_LAYOUT_ID));
		initCounters();
		setupChronometer();
		setupAlarmTextViews();
		setupSeekBar();
		if (_onCreateFlag) {
			runOnceEula();
			_onCreateFlag = false;
		}
	}

	/**
	 * Set up the views for this Activity.
	 */
	private void setupViews() {
		if (_debug) Log.v("TimerActivity.setupViews()");
		_diaperTextView = (TextView) findViewById(R.id.diaper_text_view);
		_bottleTextView = (TextView) findViewById(R.id.bottle_text_view);
		_sleepTextView = (TextView) findViewById(R.id.sleep_text_view);
		_customTextView = (TextView) findViewById(R.id.custom_text_view);
		_diaperAlarmTextView = (TextView) findViewById(R.id.diaper_alarm_text_view);
		_bottleAlarmTextView = (TextView) findViewById(R.id.bottle_alarm_text_view);
		_sleepAlarmTextView = (TextView) findViewById(R.id.sleep_alarm_text_view);
		_customAlarmTextView = (TextView) findViewById(R.id.custom_alarm_text_view);
		_diaperTotalTextView = (TextView) findViewById(R.id.diaper_total_text_view);
		_bottleTotalTextView = (TextView) findViewById(R.id.bottle_total_text_view);
		_sleepTotalTextView = (TextView) findViewById(R.id.sleep_total_text_view);
		_customTotalTextView = (TextView) findViewById(R.id.custom_total_text_view);
		if (_secondsEnabled) {
			_diaperTextView.setText("00:00:00");
			_bottleTextView.setText("00:00:00");
			_sleepTextView.setText("00:00:00");
			_customTextView.setText("00:00:00");
		} else {
			_diaperTextView.setText("00:00");
			_bottleTextView.setText("00:00");
			_sleepTextView.setText("00:00");
			_customTextView.setText("00:00");
		}
		_diaperTotalTextView.setText("Diaper Change Total: 0");
		_bottleTotalTextView.setText("Bottle Total: 0");
		_sleepTotalTextView.setText("Sleep Total: 0");
		_customTotalTextView.setText("Custom Total: 0");
		_seekBar = (SeekBar) findViewById(R.id.seek_bar);
		_LTextView = (TextView) findViewById(R.id.L_text_view);
		_RTextView = (TextView) findViewById(R.id.R_text_view);
		_diaperLinearLayout = (LinearLayout) findViewById(R.id.diaper_linear_layout);
		_bottleLinearLayout = (LinearLayout) findViewById(R.id.bottle_linear_layout);
		_sleepLinearLayout = (LinearLayout) findViewById(R.id.sleep_linear_layout);
		_customLinearLayout = (LinearLayout) findViewById(R.id.custom_linear_layout);
		_diaperImageView = (ImageView) findViewById(R.id.diaper_total_image_view);
		_bottleImageView = (ImageView) findViewById(R.id.bottle_total_image_view);
		_sleepImageView = (ImageView) findViewById(R.id.sleep_total_image_view);
		_customImageView = (ImageView) findViewById(R.id.custom_total_image_view);
	}

	/**
	 * Set up the buttons for this Activity.
	 */
	private void setupButtons() {
		if (_debug) Log.v("TimerActivity.setupButtons()");
		_diaperImageButton = (ImageButton) findViewById(R.id.diaper_button);
		_diaperImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Diaper Change ImageButton Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				startDiaperChronometer();
				updateDiaperTotalSummary();
			}
		});
		_bottleImageButton = (ImageButton) findViewById(R.id.bottle_button);
		_bottleImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Bottle ImageButton Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				startBottleChronometer();
				updateBottleTotalSummary();
			}
		});
		_sleepImageButton = (ImageButton) findViewById(R.id.sleep_button);
		_sleepImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Sleep ImageButton Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				startSleepChronometer();
				updateSleepTotalSummary();
			}
		});
		_customImageButton = (ImageButton) findViewById(R.id.custom_button);
		_customImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Custom ImageButton Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				startCustomChronometer();
				updateCustomTotalSummary();
			}
		});
		_diaperImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Diaper Reset Counter Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				if (_confirmResetCounters) {
					showDialog(Constants.DIAPER_DIALOG_RESET_COUNTER);
				} else {
					resetDiaperCounter();
				}
			}
		});
		_bottleImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Bottle Reset Counter Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				if (_confirmResetCounters) {
					showDialog(Constants.BOTTLE_DIALOG_RESET_COUNTER);
				} else {
					resetBottleCounter();
				}
			}
		});
		_sleepImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Sleep Reset Counter Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				if (_confirmResetCounters) {
					showDialog(Constants.SLEEP_DIALOG_RESET_COUNTER);
				} else {
					resetSleepCounter();
				}
			}
		});
		_customImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("Custom Reset Counter Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				if (_confirmResetCounters) {
					showDialog(Constants.CUSTOM_DIALOG_RESET_COUNTER);
				} else {
					resetCustomCounter();
				}
			}
		});
		_LTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("L Button Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				setSeekBar(0);
			}
		});
		_RTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_debug) Log.v("R Button Clicked()");
				customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				setSeekBar(1);
			}
		});
	}

	/**
	 * Setup the context menus for the various items in this Activity.
	 */
	private void setupContextMenus() {
		if (_debug) Log.v("TimerActivity.setupContextMenus()");
		registerForContextMenu(_diaperLinearLayout);
		registerForContextMenu(_bottleLinearLayout);
		registerForContextMenu(_sleepLinearLayout);
		registerForContextMenu(_customLinearLayout);
	}

	/**
	 * Creates and sets up the animation event when a long press is performed on
	 * the contact wrapper View.
	 */
	private void initLongPressView(View timerView) {
		if (_debug) Log.v("TimerActivity.initLongPressView()");
		OnTouchListener viewOnTouchListener = new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				switch(motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					if (_debug) Log.v("TimerActivity.initLongPressView() ACTION_DOWN");
					int listSelectorBackgroundResource = 0;
					// Set View background.
					if (_appTheme.equals("0")) {
						listSelectorBackgroundResource = R.drawable.list_selector_background_transition_blue;
					} else {
						listSelectorBackgroundResource = R.drawable.list_selector_background_transition_pink;
					}
					TransitionDrawable transition = (TransitionDrawable) _context.getResources().getDrawable(listSelectorBackgroundResource);
					view.setBackgroundDrawable(transition);
					transition.setCrossFadeEnabled(true);
					transition.startTransition(300);
					break;
				}
				case MotionEvent.ACTION_UP: {
					if (_debug) Log.v("TimerActivity.initLongPressView() ACTION_UP");
					int listSelectorBackgroundResource = 0;
					// Set View background.
					if (_appTheme.equals("0")) {
						listSelectorBackgroundResource = R.drawable.list_selector_background_blue;
					} else {
						listSelectorBackgroundResource = R.drawable.list_selector_background_pink;
					}
					view.setBackgroundResource(listSelectorBackgroundResource);
					break;
				}
				case MotionEvent.ACTION_CANCEL: {
					if (_debug) Log.v("TimerActivity.initLongPressView() ACTION_CANCEL");
					int listSelectorBackgroundResource = 0;
					// Set View background.
					if (_appTheme.equals("0")) {
						listSelectorBackgroundResource = R.drawable.list_selector_background_blue;
					} else {
						listSelectorBackgroundResource = R.drawable.list_selector_background_pink;
					}
					view.setBackgroundResource(listSelectorBackgroundResource);
					break;
				}
				}
				return false;
			}
		};
		timerView.setOnTouchListener(viewOnTouchListener);
	}

	/**
	 * Set up the timers for this Activity.
	 */
	private void setupChronometer() {
		if (_debug) Log.v("TimerActivity.setupChronometers()");
		_masterChronometer = (Chronometer) findViewById(R.id.master_chronometer);
		_masterChronometer.setOnChronometerTickListener(new OnChronometerTickListener() {
				public void onChronometerTick(Chronometer chronometer) {
					updateTimerTextViews();
				}
			});
		_masterChronometer.start();
	}

	/**
	 * Set up the Text View's that indicate if an alarm is on or not.
	 */
	private void setupAlarmTextViews() {
		if (_debug) Log.v("TimerActivity.setupAlarmTextViews()");
		if (_diaperAlarmActive && _diaperAlarmTime > 0) {
			_diaperAlarmTextView.setText("(" + formatAlarmTime(_diaperAlarmTime) + ")");
			if (_diaperAlarmSnooze) {
				_diaperAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_snooze, 0, 0, 0);
			} else if (_diaperAlarmRecurring) {
				_diaperAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_recurring, 0, 0, 0);
			} else {
				_diaperAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, 0, 0);
			}
			_diaperAlarmTextView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (_debug) Log.v("R Button Clicked()");
					customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					if (_diaperAlarmActive) {
						startSetAlarmActivity(Constants.TYPE_DIAPER, _diaperAlarmTime);
					}else{
						startSetAlarmActivity(Constants.TYPE_DIAPER, 0);
					}
				}
			});
		} else {
			_diaperAlarmTextView.setText("");
			_diaperAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			_diaperAlarmTextView.setOnClickListener(null);
		}
		if (_bottleAlarmActive && _bottleAlarmTime > 0) {
			_bottleAlarmTextView.setText("(" + formatAlarmTime(_bottleAlarmTime) + ")");
			if (_bottleAlarmSnooze) {
				_bottleAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_snooze, 0, 0, 0);
			} else if (_bottleAlarmRecurring) {
				_bottleAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_recurring, 0, 0, 0);
			} else {
				_bottleAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, 0, 0);
			}
			_bottleAlarmTextView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (_debug) Log.v("R Button Clicked()");
					customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					if (_bottleAlarmActive) {
						startSetAlarmActivity(Constants.TYPE_BOTTLE, _bottleAlarmTime);
					}else{
						startSetAlarmActivity(Constants.TYPE_BOTTLE, 0);
					}
				}
			});
		} else {
			_bottleAlarmTextView.setText("");
			_bottleAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			_bottleAlarmTextView.setOnClickListener(null);
		}
		if (_sleepAlarmActive && _sleepAlarmTime > 0) {
			_sleepAlarmTextView.setText("(" + formatAlarmTime(_sleepAlarmTime) + ")");
			if (_sleepAlarmSnooze) {
				_sleepAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_snooze, 0, 0, 0);
			} else if (_sleepAlarmRecurring) {
				_sleepAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_recurring, 0, 0, 0);
			} else {
				_sleepAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, 0, 0);
			}
			_sleepAlarmTextView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (_debug) Log.v("R Button Clicked()");
					customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					if (_sleepAlarmActive) {
						startSetAlarmActivity(Constants.TYPE_SLEEP, _sleepAlarmTime);
					}else{
						startSetAlarmActivity(Constants.TYPE_SLEEP, 0);
					}
				}
			});
		} else {
			_sleepAlarmTextView.setText("");
			_sleepAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			_sleepAlarmTextView.setOnClickListener(null);
		}
		if (_customAlarmActive && _customAlarmTime > 0) {
			_customAlarmTextView.setText("(" + formatAlarmTime(_customAlarmTime) + ")");
			if (_customAlarmSnooze) {
				_customAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_snooze, 0, 0, 0);
			} else if (_customAlarmRecurring) {
				_customAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_recurring, 0, 0, 0);
			} else {
				_customAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm, 0, 0, 0);
			}
			_customAlarmTextView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (_debug) Log.v("R Button Clicked()");
					customPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
					if (_customAlarmActive) {
						startSetAlarmActivity(Constants.TYPE_CUSTOM, _customAlarmTime);
					}else{
						startSetAlarmActivity(Constants.TYPE_CUSTOM, 0);
					}
				}
			});
		} else {
			_customAlarmTextView.setText("");
			_customAlarmTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			_customAlarmTextView.setOnClickListener(null);
		}
	}

	/**
	 * 
	 */
	private void updateTimerTextViews() {
		// if (_debug) Log.v("TimerActivity.updateTimerTextViews()");	
		//Toggle the master blink flag.
		if(_masterBlink){
			_masterBlink = false;
		}else{
			_masterBlink = true;
		}
		//Update Diaper Views
		if(_diaperTimerActive){
			updateTimerTextView(Constants.TYPE_DIAPER, _diaperBaseTime, _diaperTimerOffset);
		}
		//Update Bottle Views
		if(_bottleTimerActive){
			updateTimerTextView(Constants.TYPE_BOTTLE, _bottleBaseTime, _bottleTimerOffset);
		}
		//Update Sleep Views
		if(_sleepTimerActive){
			updateTimerTextView(Constants.TYPE_SLEEP, _sleepBaseTime, _sleepTimerOffset);
		}
		//Update Custom Views
		if(_customTimerActive){
			updateTimerTextView(Constants.TYPE_CUSTOM, _customBaseTime, _customTimerOffset);
		}
	}
	
	/**
	 * Update the timer View with the current elapsed time.
	 * 
	 * @param alarmType - The type of timer we are updating.
	 * @param baseTime - The current base time of the timer.
	 */
	private void updateTimerTextView(int timerType, long baseTime, long offsetTime) {
		// if (_debug) Log.v("TimerActivity.updateTimerTextView()");	
		long timeValue = SystemClock.elapsedRealtime();
		timeValue = (timeValue + offsetTime - baseTime) / 1000;
		long hoursValue = (timeValue / 3600);
		String hours = "00";
		if (hoursValue < 10) {
			hours = "0" + String.valueOf(hoursValue);
		} else {
			hours = String.valueOf(hoursValue);
		}
		timeValue = timeValue - hoursValue * 3600;
		long minutesValue = (timeValue / 60);
		String minutes = "00";
		if (minutesValue < 10) {
			minutes = "0" + String.valueOf(minutesValue);
		} else {
			minutes = String.valueOf(minutesValue);
		}
		long secondsValue = (timeValue % 60);
		String seconds = "00";
		if (secondsValue < 10) {
			seconds = "0" + String.valueOf(secondsValue);
		} else {
			seconds = String.valueOf(secondsValue);
		}
		switch(timerType) {
			case Constants.TYPE_DIAPER: {
				if(_secondsEnabled) {
					_diaperTextView.setText(hours + ":" + minutes + ":" + seconds);
				}else if(_blinkEnabled) {
					if (_masterBlink) {
						_diaperTextView.setText(hours + ":" + minutes);
					} else {
						_diaperTextView.setText(hours + " " + minutes);
					}
				}else{
					_diaperTextView.setText(hours + ":" + minutes);
				}
				break;
			}
			case Constants.TYPE_BOTTLE: {
				if(_secondsEnabled) {
					_bottleTextView.setText(hours + ":" + minutes + ":" + seconds);
				}else if(_blinkEnabled) {
					if (_masterBlink) {
						_bottleTextView.setText(hours + ":" + minutes);
					} else {
						_bottleTextView.setText(hours + " " + minutes);
					}
				}else{
					_bottleTextView.setText(hours + ":" + minutes);
				}
				break;
			}
			case Constants.TYPE_SLEEP: {
				if(_secondsEnabled) {
					_sleepTextView.setText(hours + ":" + minutes + ":" + seconds);
				}else if(_blinkEnabled) {
					if (_masterBlink) {
						_sleepTextView.setText(hours + ":" + minutes);
					} else {
						_sleepTextView.setText(hours + " " + minutes);
					}
				}else{
					_sleepTextView.setText(hours + ":" + minutes);
				}
				break;
			}
			case Constants.TYPE_CUSTOM: {
				if(_secondsEnabled) {
					_customTextView.setText(hours + ":" + minutes + ":" + seconds);
				}else if(_blinkEnabled) {
					if (_masterBlink) {
						_customTextView.setText(hours + ":" + minutes);
					} else {
						_customTextView.setText(hours + " " + minutes);
					}
				}else{
					_customTextView.setText(hours + ":" + minutes);
				}
				break;
			}
		}
	}

	/**
	 * Initialize the counter summary.
	 */
	private void initCounters() {
		if (_debug) Log.v("TimerActivity.initCounters()");
		updateTotalSummary();
	}

	/**
	 * Initialize the breast Feeding SeekBar.
	 */
	private void setupSeekBar() {
		if (_debug) Log.v("TimerActivity.setupSeekBar()");
		_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				setSeekBar(_seekBar.getProgress());
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Do Nothing.
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// Do Nothing.
			}
		});
		setSeekBar(_breastFeedingSide);
	}

	/**
	 * Set the SeekBar to a value.
	 * 
	 * @param value - The integer value to set the SeekBar to.
	 */
	private void setSeekBar(int value) {
		if (_debug) Log.v("TimerActivity.setSeekBar()");
		try{
			_seekBar.setProgress(value);
			_breastFeedingSide = value;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putInt(Constants.BREAST_FEEDING_SIDE_KEY, _breastFeedingSide);
			editor.commit();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.setSeekBar() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset and start the diaper change timer.
	 */
	private void startDiaperChronometer() {
		if (_debug) Log.v("TimerActivity.startDiaperChronometer()");
		try{
			_diaperBaseTime = SystemClock.elapsedRealtime();
			_diaperCount += 1;
			_diaperTimerStartTime = System.currentTimeMillis();
			_diaperTimerActive = true;
			_diaperTimerOffset = 0;
			// Set the start time in the preferences in order to persist through the Activity life cycle.
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.DIAPER_BASE_TIME_KEY, _diaperBaseTime);
			editor.putInt(Constants.DIAPER_COUNT_KEY, _diaperCount);
			editor.putLong(Constants.DIAPER_TIMER_START_KEY, _diaperTimerStartTime);
			editor.putBoolean(Constants.DIAPER_TIMER_ACTIVE_KEY, _diaperTimerActive);
			editor.putLong(Constants.DIAPER_TIMER_OFFSET_KEY, _diaperTimerOffset);
			editor.commit();
			setAlarm(Constants.TYPE_DIAPER);
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.startDiaperChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Stop the diaper change timer.
	 */
	private void stopDiaperChronometer() {
		if (_debug) Log.v("TimerActivity.stopDiaperChronometer()");
		try{
			if (_secondsEnabled) {
				_diaperTextView.setText("00:00:00");
			} else {
				_diaperTextView.setText("00:00");
			}
			_diaperTimerActive = false;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.DIAPER_TIMER_STOP_KEY, System.currentTimeMillis());
			editor.putBoolean(Constants.DIAPER_TIMER_ACTIVE_KEY, _diaperTimerActive);
			editor.commit();
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.stopDiaperChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset and start the bottle timer.
	 */
	private void startBottleChronometer() {
		if (_debug) Log.v("TimerActivity.startBottleChronometer()");
		try{
			_bottleBaseTime = SystemClock.elapsedRealtime();
			_bottleCount += 1;
			_bottleTimerStartTime = System.currentTimeMillis();
			_bottleTimerActive = true;
			_bottleTimerOffset = 0;
			// Set the start time in the preferences in order to persist through the Activity life cycle.
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.BOTTLE_BASE_TIME_KEY, _bottleBaseTime);
			editor.putInt(Constants.BOTTLE_COUNT_KEY, _bottleCount);
			editor.putLong(Constants.BOTTLE_TIMER_START_KEY, _bottleTimerStartTime);
			editor.putBoolean(Constants.BOTTLE_TIMER_ACTIVE_KEY, _bottleTimerActive);
			editor.putLong(Constants.BOTTLE_TIMER_OFFSET_KEY, _bottleTimerOffset);
			editor.commit();
			setAlarm(Constants.TYPE_BOTTLE);
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.startBottleChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Stop the bottle timer.
	 */
	private void stopBottleChronometer() {
		if (_debug) Log.v("TimerActivity.stopBottleChronometer()");
		try{
			if (_secondsEnabled) {
				_bottleTextView.setText("00:00:00");
			} else {
				_bottleTextView.setText("00:00");
			}
			_bottleTimerActive = false;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.BOTTLE_TIMER_STOP_KEY, System.currentTimeMillis());
			editor.putBoolean(Constants.BOTTLE_TIMER_ACTIVE_KEY, _bottleTimerActive);
			editor.commit();
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.stopBottleChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset and start the sleep timer.
	 */
	private void startSleepChronometer() {
		if (_debug) Log.v("TimerActivity.startSleepChronometer()");
		try{
			_sleepBaseTime = SystemClock.elapsedRealtime();
			_sleepCount += 1;
			_sleepTimerStartTime = System.currentTimeMillis();
			_sleepTimerActive = true;
			_sleepTimerOffset = 0;
			// Set the start time in the preferences in order to persist through the Activity life cycle.
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.SLEEP_BASE_TIME_KEY, _sleepBaseTime);
			editor.putInt(Constants.SLEEP_COUNT_KEY, _sleepCount);
			editor.putLong(Constants.SLEEP_TIMER_START_KEY, _sleepTimerStartTime);
			editor.putBoolean(Constants.SLEEP_TIMER_ACTIVE_KEY, _sleepTimerActive);
			editor.putLong(Constants.SLEEP_TIMER_OFFSET_KEY, _sleepTimerOffset);
			editor.commit();
			setAlarm(Constants.TYPE_SLEEP);
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.startSleepChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Stop the sleep timer.
	 */
	private void stopSleepChronometer() {
		if (_debug) Log.v("TimerActivity.stopSleepChronometer()");
		try{
			// Determine the total sleep time of the child.
			_totalSleepTime += _preferences.getLong(Constants.SLEEP_TIMER_STOP_KEY, 0) - _preferences.getLong(Constants.SLEEP_TIMER_START_KEY, 0);
			if (_secondsEnabled) {
				_sleepTextView.setText("00:00:00");
			} else {
				_sleepTextView.setText("00:00");
			}
			_sleepTimerActive = false;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.SLEEP_TIMER_STOP_KEY, System.currentTimeMillis());
			editor.putBoolean(Constants.SLEEP_TIMER_ACTIVE_KEY, _sleepTimerActive);
			editor.putLong(Constants.SLEEP_TOTAL_ELAPSED_TIME_KEY, _totalSleepTime);
			editor.commit();
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.stopSleepChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset and start the custom timer.
	 */
	private void startCustomChronometer() {
		if (_debug) Log.v("TimerActivity.startCustomChronometer()");
		try{
			_customBaseTime = SystemClock.elapsedRealtime();
			_customCount += 1;
			_customTimerStartTime = System.currentTimeMillis();
			_customTimerActive = true;
			_customTimerOffset = 0;
			// Set the start time in the preferences in order to persist through the Activity life cycle.
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.CUSTOM_BASE_TIME_KEY, _customBaseTime);
			editor.putInt(Constants.CUSTOM_COUNT_KEY, _customCount);
			editor.putLong(Constants.CUSTOM_TIMER_START_KEY, _customTimerStartTime);
			editor.putBoolean(Constants.CUSTOM_TIMER_ACTIVE_KEY, _customTimerActive);
			editor.putLong(Constants.CUSTOM_TIMER_OFFSET_KEY, _customTimerOffset);
			editor.commit();
			setAlarm(Constants.TYPE_CUSTOM);
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.startCustomChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Stop the custom timer.
	 */
	private void stopCustomChronometer() {
		if (_debug) Log.v("TimerActivity.stopCustomChronometer()");
		try{
			if (_secondsEnabled) {
				_customTextView.setText("00:00:00");
			} else {
				_customTextView.setText("00:00");
			}
			_customTimerActive = false;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong(Constants.CUSTOM_TIMER_STOP_KEY, System.currentTimeMillis());
			editor.putBoolean(Constants.CUSTOM_TIMER_ACTIVE_KEY, _customTimerActive);
			editor.commit();
			setupAlarmTextViews();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.stopCustomChronometer() ERROR: " + ex.toString());
		}
	}

	/**
	 * Launches the preferences screen as new intent.
	 */
	private void launchPreferenceScreen() {
		if (_debug) Log.v("TimerActivity.launchPreferenceScreen()");
		Intent intent = new Intent(_context, MainPreferenceActivity.class);
		startActivity(intent);
	}

	/**
	 * Reset the diaper change counter back to zero.
	 */
	private void resetDiaperCounter() {
		if (_debug) Log.v("TimerActivity.resetDiaperCounter()");
		try{
			_diaperCount = 0;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putInt(Constants.DIAPER_COUNT_KEY, _diaperCount);
			editor.putLong(Constants.DIAPER_COUNT_DATE_KEY, System.currentTimeMillis());
			editor.commit();
			updateDiaperTotalSummary();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.resetDiaperCounter() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset the bottle counter back to zero.
	 */
	private void resetBottleCounter() {
		if (_debug) Log.v("TimerActivity.resetBottleCounter()");
		try{
			_bottleCount = 0;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putInt(Constants.BOTTLE_COUNT_KEY, _bottleCount);
			editor.putLong(Constants.BOTTLE_COUNT_DATE_KEY, System.currentTimeMillis());
			editor.commit();
			updateBottleTotalSummary();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.resetBottleCounter() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset the sleep counter back to zero.
	 */
	private void resetSleepCounter() {
		if (_debug) Log.v("TimerActivity.resetSleepCounter()");
		try{
			_sleepCount = 0;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putInt(Constants.SLEEP_COUNT_KEY, _sleepCount);
			editor.putLong(Constants.SLEEP_COUNT_DATE_KEY, System.currentTimeMillis());
			editor.commit();
			updateSleepTotalSummary();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.resetSleepCounter() ERROR: " + ex.toString());
		}
	}

	/**
	 * Reset the custom counter back to zero.
	 */
	private void resetCustomCounter() {
		if (_debug) Log.v("TimerActivity.resetCustomCounter()");
		try{
			_customCount = 0;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putInt(Constants.CUSTOM_COUNT_KEY, _customCount);
			editor.putLong(Constants.CUSTOM_COUNT_DATE_KEY, System.currentTimeMillis());
			editor.commit();
			updateCustomTotalSummary();
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.resetCustomCounter() ERROR: " + ex.toString());
		}
	}

	/**
	 * Update the total summary for each timer.
	 */
	private void updateTotalSummary() {
		if (_debug) Log.v("TimerActivity.updateTotalSummary()");
		_diaperTotalTextView.setText(_context.getString(R.string.diaper_total_text) + ": " + String.valueOf(_diaperCount));
		_bottleTotalTextView.setText(_context.getString(R.string.bottle_total_text) + ": " + String.valueOf(_bottleCount));
		_sleepTotalTextView.setText(_context.getString(R.string.sleep_total_text) + ": " + String.valueOf(_sleepCount));
		_customTotalTextView.setText(_context.getString(R.string.custom_total_text) + ": " + String.valueOf(_customCount));
	}

	/**
	 * Update the total summary for the diaper change timer.
	 */
	private void updateDiaperTotalSummary() {
		if (_debug) Log.v("TimerActivity.updateDiaperTotalSummary()");
		_diaperTotalTextView.setText(_context.getString(R.string.diaper_total_text) + ": " + String.valueOf(_diaperCount));
	}

	/**
	 * Update the total summary for the bottle timer.
	 */
	private void updateBottleTotalSummary() {
		if (_debug) Log.v("TimerActivity.updateBottleTotalSummary()");
		_bottleTotalTextView.setText(_context.getString(R.string.bottle_total_text) + ": " + String.valueOf(_bottleCount));
	}

	/**
	 * Update the total summary for the sleep timer.
	 */
	private void updateSleepTotalSummary() {
		if (_debug) Log.v("TimerActivity.updateSleepTotalSummary()");
		_sleepTotalTextView.setText(_context.getString(R.string.sleep_total_text) + ": " + String.valueOf(_sleepCount));
	}

	/**
	 * Update the total summary for the custom timer.
	 */
	private void updateCustomTotalSummary() {
		if (_debug) Log.v("TimerActivity.updateCustomTotalSummary()");
		_customTotalTextView.setText(_context.getString(R.string.custom_total_text) + ": " + String.valueOf(_customCount));
	}

	/**
	 * Cancel an alarm.
	 * 
	 * @param alarmType - The specific alarm that we want to cancel.
	 */
	private void cancelAlarm(int alarmType) {
		if (_debug) Log.v("TimerActivity.cancelAlarm()");
		try{
			switch(alarmType) {
				case Constants.TYPE_DIAPER: {
					_diaperAlarmActive = false;
					_diaperAlarmSnooze = false;
					_diaperAlarmRecurring = false;
					_diaperAlarmTime = 0;
					//_diaperAlarmRecurring = false;
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.DIAPER_ALARM_ACTIVE_KEY, _diaperAlarmActive);
					editor.putBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, _diaperAlarmSnooze);
					editor.putBoolean(Constants.DIAPER_ALARM_RECURRING_KEY, _diaperAlarmRecurring);
					editor.putLong(Constants.DIAPER_ALARM_TIME_KEY, _diaperAlarmTime);
					editor.commit();		
					break;
				}
				case Constants.TYPE_BOTTLE: {
					_bottleAlarmActive = false;
					_bottleAlarmSnooze = false;
					_bottleAlarmRecurring = false;
					_bottleAlarmTime = 0;
					//_bottleAlarmRecurring = false;
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.BOTTLE_ALARM_ACTIVE_KEY, _bottleAlarmActive);
					editor.putBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, _bottleAlarmSnooze);
					editor.putBoolean(Constants.BOTTLE_ALARM_RECURRING_KEY, _bottleAlarmRecurring);
					editor.putLong(Constants.BOTTLE_ALARM_TIME_KEY, _bottleAlarmTime);
					editor.commit();
					break;
				}
				case Constants.TYPE_SLEEP: {
					_sleepAlarmActive = false;
					_sleepAlarmSnooze = false;
					_sleepAlarmRecurring = false;
					_sleepAlarmTime = 0;
					//_sleepAlarmRecurring = false;
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.SLEEP_ALARM_ACTIVE_KEY, _sleepAlarmActive);
					editor.putBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, _sleepAlarmSnooze);
					editor.putBoolean(Constants.SLEEP_ALARM_RECURRING_KEY, _sleepAlarmRecurring);
					editor.putLong(Constants.SLEEP_ALARM_TIME_KEY, _sleepAlarmTime);
					editor.commit();
					break;
				}
				case Constants.TYPE_CUSTOM: {
					_customAlarmActive = false;
					_customAlarmSnooze = false;
					_customAlarmRecurring = false;
					_customAlarmTime = 0;
					//_customAlarmRecurring = false;
					SharedPreferences.Editor editor = _preferences.edit();
					editor.putBoolean(Constants.CUSTOM_ALARM_ACTIVE_KEY, _customAlarmActive);
					editor.putBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, _customAlarmSnooze);
					editor.putBoolean(Constants.CUSTOM_ALARM_RECURRING_KEY, _customAlarmRecurring);
					editor.putLong(Constants.CUSTOM_ALARM_TIME_KEY, _customAlarmTime);
					editor.commit();
					break;
				}
			}
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.cancelAlarm() ERROR: " + ex.toString());
		}
		setupAlarmTextViews();
		Toast.makeText(_context, _context.getString(R.string.alarm_has_been_cancelled_text), Toast.LENGTH_LONG).show();
	}
	
	/**
	 * This displays the EULA to the user the first time the app is run.
	 */
	private void runOnceEula(){
		if (_debug) Log.v("TimerActivity.runOnceEula()");
		boolean runOnceEula = _preferences.getBoolean("runOnceEula", true);
		if(runOnceEula) {
			try{
				SharedPreferences.Editor editor = _preferences.edit();
				editor.putBoolean("runOnceEula", false);
				editor.commit();
				displayHTMLAlertDialog(_context.getString(R.string.app_license),R.drawable.ic_dialog_info,_context.getString(R.string.eula_text));
			}catch(Exception ex){
 	    		if (_debug) Log.e("TimerActivity.runOnceEula() ERROR: " + ex.toString());
	    	}
		}
	}

	/**
	 * Sets up the Intent for the SetAlarmActivity.
	 * 
	 * @param alarmType - The alarm type.
	 * @param baseTime - The base time of the alarm.
	 */
	private void startSetAlarmActivity(int alarmType, long alarmTime) {
		if (_debug) Log.v("TimerActivity.startSetAlarmActivity()");
		Intent intent = new Intent(_context, SetAlarmActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ALARM_TYPE, alarmType);
		bundle.putLong(Constants.ALARM_TIME, alarmTime);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);
	}

	/**
	 * Format the current time string so that the minutes are always 2 digits.
	 * 
	 * @param currentAlarmTime - The time string we want to format.
	 * 
	 * @return String - The formatted current time.
	 */
	private String formatAlarmTime(long alarmTime) {
		if (_debug) Log.v("TimerActivity.formatAlarmTime()");
		long timeValue = alarmTime / 1000;
    	long hours = (timeValue / 3600);
    	timeValue = timeValue - hours * 3600;
    	long minutes = (timeValue / 60);
		return String.valueOf(hours) + ":" + formatNumber(minutes);
	}

	/**
	 * Format a number so that it is always 2 digits.
	 * 
	 * @param number - The number we want to format.
	 * 
	 * @return String - The formatted number.
	 */
	private String formatNumber(long number) {
		if (_debug) Log.v("TimerActivity.formatNumber()");
		if (number < 10) {
			return "0" + String.valueOf(number);
		} else {
			return String.valueOf(number);
		}
	}

	/**
	 * Set the alarm if the alarm is recurring.
	 * 
	 * @param alarmType - The alarm type.
	 */
	private void setAlarm(int alarmType) {
		if (_debug) Log.v("TimerActivity.setAlarm()");
		boolean isRecurring = false;
		switch(alarmType) {
			case Constants.TYPE_DIAPER: {
				isRecurring = _diaperAlarmRecurring;
				break;
			}
			case Constants.TYPE_BOTTLE: {
				isRecurring = _bottleAlarmRecurring;
				break;
			}
			case Constants.TYPE_SLEEP: {
				isRecurring = _sleepAlarmRecurring;
				break;
			}
			case Constants.TYPE_CUSTOM: {
				isRecurring = _customAlarmRecurring;
				break;
			}
		}
		if (isRecurring) {
			setRecurringAlarm(alarmType);
		}
	}

	/**
	 * Set the alarm. Store the values and initialize this alarm.
	 * 
	 * @param alarmType - The alarm type.
	 */
	private void setRecurringAlarm(int alarmType) {
		if (_debug) Log.v("TimerActivity.setRecurringAlarm()");
		long baseTime = 0;
		long alarmTime = 0;
		long timerOffset = 0;
		long timerStartTime = 0;
		switch(alarmType) {
			case Constants.TYPE_DIAPER: {
				baseTime = _diaperBaseTime;
				alarmTime = _diaperAlarmTime;
				timerOffset = _diaperTimerOffset;
				timerStartTime = _diaperTimerStartTime;
				break;
			}
			case Constants.TYPE_BOTTLE: {
				baseTime = _bottleBaseTime;
				alarmTime = _bottleAlarmTime;
				timerOffset = _bottleTimerOffset;
				timerStartTime = _bottleTimerStartTime;
				break;
			}
			case Constants.TYPE_SLEEP: {
				baseTime = _sleepBaseTime;
				alarmTime = _sleepAlarmTime;
				timerOffset = _sleepTimerOffset;
				timerStartTime = _sleepTimerStartTime;
				break;
			}
			case Constants.TYPE_CUSTOM: {
				baseTime = _customBaseTime;
				alarmTime = _customAlarmTime;
				timerOffset = _customTimerOffset;
				timerStartTime = _customTimerStartTime;
				break;
			}
		}
		if(alarmTime == 0){
			if (_debug) Log.v("TimerActivity.setRecurringAlarm() Alarm time is null. Exiting...");
			return;
		}
		switch(alarmType) {
			case Constants.TYPE_DIAPER: {
				_diaperAlarmActive = true;
				_diaperAlarmSnooze = false;
				SharedPreferences.Editor editor = _preferences.edit();
				editor.putBoolean(Constants.DIAPER_ALARM_ACTIVE_KEY, _diaperAlarmActive);
				editor.putBoolean(Constants.DIAPER_ALARM_SNOOZE_KEY, _diaperAlarmSnooze);
				editor.commit();
				break;
			}
			case Constants.TYPE_BOTTLE: {
				_bottleAlarmActive = true;
				_bottleAlarmSnooze = false;
				SharedPreferences.Editor editor = _preferences.edit();
				editor.putBoolean(Constants.BOTTLE_ALARM_ACTIVE_KEY, _bottleAlarmActive);
				editor.putBoolean(Constants.BOTTLE_ALARM_SNOOZE_KEY, _bottleAlarmSnooze);
				editor.commit();
				break;
			}
			case Constants.TYPE_SLEEP: {
				_sleepAlarmActive = true;
				_sleepAlarmSnooze = false;
				SharedPreferences.Editor editor = _preferences.edit();
				editor.putBoolean(Constants.SLEEP_ALARM_ACTIVE_KEY, _sleepAlarmActive);
				editor.putBoolean(Constants.SLEEP_ALARM_SNOOZE_KEY, _sleepAlarmSnooze);
				editor.commit();
				break;
			}
			case Constants.TYPE_CUSTOM: {
				_customAlarmActive = true;
				_customAlarmSnooze = false;
				SharedPreferences.Editor editor = _preferences.edit();
				editor.putBoolean(Constants.CUSTOM_ALARM_ACTIVE_KEY, _customAlarmActive);
				editor.putBoolean(Constants.CUSTOM_ALARM_SNOOZE_KEY, _customAlarmSnooze);
				editor.commit();
				break;
			}
		}
		if (_debug) Log.v("TimerActivity.setRecurringAlarm() baseTime: " + baseTime);
		if (_debug) Log.v("TimerActivity.setRecurringAlarm() timerOffset: " + timerOffset);
		if (_debug) Log.v("TimerActivity.setRecurringAlarm() timerStartTime: " + timerStartTime);
		long elapsedTime = 0;
		if (baseTime == 0) {
			elapsedTime = SystemClock.elapsedRealtime() + timerOffset;
		}else if (timerOffset == 0) {
			elapsedTime = SystemClock.elapsedRealtime() - baseTime;
		}else{
			if (_debug) Log.v("TimerActivity.setRecurringAlarm() BaseTime and TimerOffset are null. Exiting...");
			return;
		}
		if (_debug) Log.v("TimerActivity.setRecurringAlarm() elapsedTime: " + elapsedTime);
		long alarmAlertTime = System.currentTimeMillis() + alarmTime - elapsedTime;
		if (_debug) Log.v("TimerActivity.setRecurringAlarm() AlarmTime: " + alarmTime + " ElapsedTime:" + elapsedTime + " AlarmAlertTime: " + alarmAlertTime);
		AlarmManager alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(_context, AlarmReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ALARM_TYPE, alarmType);
		bundle.putBoolean(Constants.ALARM_SNOOZE, false);
		alarmIntent.putExtras(bundle);
		alarmIntent.setAction("apps.babycaretimer.action." + String.valueOf(alarmType));
		alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 0, alarmIntent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmAlertTime, pendingIntent);
	}
	
	/**
	 * Display an HTML AletDialog.
	 */
	private boolean displayHTMLAlertDialog(String title, int iconResource, String content){
		if (_debug) Log.v("TimerActivity.displayHTMLAlertDialog()");
		try{
    		LayoutInflater layoutInflater = (LayoutInflater) _context.getSystemService(LAYOUT_INFLATER_SERVICE);
    		View view = layoutInflater.inflate(R.layout.html_alert_dialog, (ViewGroup) findViewById(R.id.content_scroll_view));		    		
    		TextView contentTextView = (TextView) view.findViewById(R.id.content_text_view);
    		contentTextView.setText(Html.fromHtml(content));
    		contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setIcon(iconResource);
    		builder.setTitle(title);
    		builder.setView(view);
    		builder.setNegativeButton(R.string.ok_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
    		AlertDialog alertDialog = builder.create();
    		alertDialog.show();
    	}catch(Exception ex){
	    		if (_debug) Log.e("TimerActivity.displayHTMLAlertDialog() ERROR: " + ex.toString());
	    		return false;
    	}
		return true;
	}
	
	/**
	 */
	private void loadUserPreferences(){
    	if (_debug) Log.v("TimerActivity.loadUserPreferences()");
		try{
			
		    _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
			_landscapeScreenEnabled = _preferences.getBoolean(Constants.LANDSCAPE_SCREEN_ENABLED_KEY, false);
			_hapticFeedbackEnabled = _preferences.getBoolean(Constants.HAPTIC_FEEDBACK_ENABLED_KEY, true);
			_confirmResetCounters = _preferences.getBoolean(Constants.CONFIRM_RESET_COUNTERS_KEY, true);
			_appTheme = _preferences.getString(Constants.APP_THEME_KEY, "0");
			_secondsEnabled = _preferences.getBoolean(Constants.SECONDS_ENABLED_KEY, true);
			_blinkEnabled = _preferences.getBoolean(Constants.BLINK_ENABLED_KEY, false);
			_breastFeedingSide = _preferences.getInt(Constants.BREAST_FEEDING_SIDE_KEY, 0);
			
			_diaperCount = _preferences.getInt(Constants.DIAPER_COUNT_KEY, 0);
			_bottleCount = _preferences.getInt(Constants.BOTTLE_COUNT_KEY, 0);
			_sleepCount = _preferences.getInt(Constants.SLEEP_COUNT_KEY, 0);
			_customCount = _preferences.getInt(Constants.CUSTOM_COUNT_KEY, 0);
			
			_diaperBaseTime = Common.getBaseTime(_context, Constants.TYPE_DIAPER);
			_bottleBaseTime = Common.getBaseTime(_context, Constants.TYPE_BOTTLE);
			_sleepBaseTime = Common.getBaseTime(_context, Constants.TYPE_SLEEP);
			_customBaseTime = Common.getBaseTime(_context, Constants.TYPE_CUSTOM);
			
			_diaperAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_DIAPER);
			_bottleAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_BOTTLE);
			_sleepAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_SLEEP);
			_customAlarmActive = Common.isAlarmActive(_context, Constants.TYPE_CUSTOM);
			
			_diaperAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_DIAPER);
			_bottleAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_BOTTLE);
			_sleepAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_SLEEP);
			_customAlarmTime = Common.getAlarmTime(_context, Constants.TYPE_CUSTOM);
			
			_diaperAlarmSnooze = Common.isAlarmSnoozed(_context, Constants.TYPE_DIAPER);
			_bottleAlarmSnooze = Common.isAlarmSnoozed(_context, Constants.TYPE_BOTTLE);
			_sleepAlarmSnooze = Common.isAlarmSnoozed(_context, Constants.TYPE_SLEEP);
			_customAlarmSnooze = Common.isAlarmSnoozed(_context, Constants.TYPE_CUSTOM);
			
			_diaperAlarmRecurring = Common.isAlarmRecurring(_context, Constants.TYPE_DIAPER);
			_bottleAlarmRecurring = Common.isAlarmRecurring(_context, Constants.TYPE_BOTTLE);
			_sleepAlarmRecurring = Common.isAlarmRecurring(_context, Constants.TYPE_SLEEP);
			_customAlarmRecurring = Common.isAlarmRecurring(_context, Constants.TYPE_CUSTOM);
						
			_diaperTimerActive = Common.isTimerActive(_context, Constants.TYPE_DIAPER);
			_bottleTimerActive = Common.isTimerActive(_context, Constants.TYPE_BOTTLE);
			_sleepTimerActive = Common.isTimerActive(_context, Constants.TYPE_SLEEP);
			_customTimerActive = Common.isTimerActive(_context, Constants.TYPE_CUSTOM);
			
			_diaperTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_DIAPER);
			_bottleTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_BOTTLE);
			_sleepTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_SLEEP);
			_customTimerStartTime = Common.getTimerStartTime(_context, Constants.TYPE_CUSTOM);
			
			_diaperTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_DIAPER);
			_bottleTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_BOTTLE);
			_sleepTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_SLEEP);
			_customTimerOffset = Common.getTimerOffset(_context, Constants.TYPE_CUSTOM);
			
			_totalSleepTime = _preferences.getLong(Constants.SLEEP_TOTAL_ELAPSED_TIME_KEY, 0);
			
		}catch(Exception ex){
			if (_debug) Log.e("TimerActivity.loadUserPreferences() ERROR: " + ex.toString());
		}
	}

}