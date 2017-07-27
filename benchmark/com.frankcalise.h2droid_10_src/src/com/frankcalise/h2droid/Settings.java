package com.frankcalise.h2droid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String OPT_AMOUNT = "SETTING_GOAL";
	private static final String OPT_UNITS = "SETTING_UNITS";
	private static final String OPT_TOASTS = "SETTING_TOASTS";
	private static final String OPT_LARGE_UNITS = "SETTING_LARGE_UNITS";
	private static final String OPT_ENABLE_VOL_UP = "SETTING_ENABLE_VOL_UP";
	private static final String OPT_ENABLE_VOL_DOWN = "SETTING_ENABLE_VOL_DOWN";
	private static final String OPT_VOL_UP_AMOUNT = "SETTING_VOL_UP_AMOUNT";
	private static final String OPT_ENABLE_REMINDERS = "SETTING_ENABLE_REMINDERS";
	private static final String OPT_REMINDER_INTERVAL = "SETTING_REMINDER_INTERVAL";
	private static final String OPT_REMINDER_LED = "SETTING_REMINDER_LIGHT";
	private static final String OPT_REMINDER_SOUND = "SETTING_REMINDER_SOUND";
	private static final String OPT_REMINDER_VIB = "SETTING_REMINDER_VIB";
	private static final String OPT_AMOUNT_DEF = "64";
	private static final String OPT_UNITS_DEF = "1"; // US system
	private static final String OPT_REMINDER_INT_DEF = "60";
	private static final String OPT_REMINDER_SLEEP = "SETTING_REMINDER_SLEEP_TIME";
	private static final String OPT_REMINDER_WAKE = "SETTING_REMINDER_WAKE_TIME";
	private static final String[] OPT_FAV_AMOUNT_DEF = {"8", "16", "16.9", "20", "33.8"};
	private static final double DEFAULT_AMOUNT = 64.0;
	private static final double DEFAULT_FAV_AMOUNT = 8.0;
	private static final int DEFAULT_REMINDER_INT = 60;
	public static final int UNITS_METRIC = 0;
	public static final int UNITS_US = 1;
	private static final String[] OPT_FAV_AMOUNT = {"FAV_AMOUNT_ONE", 
													"FAV_AMOUNT_TWO",
													"FAV_AMOUNT_THREE",
													"FAV_AMOUNT_FOUR",
													"FAV_AMOUNT_FIVE"};
	private static final String ONE_SRV_AMOUNT = "ONE_SRV_AMOUNT";
	
	private EditTextPreference mEditTextPref1;
	private EditTextPreference mEditTextPref2;
	private EditTextPreference mEditTextPref3;
	private EditTextPreference mEditTextPref4;
	private EditTextPreference mEditTextPref5;
	private EditTextPreference mEditTextPrefSrv;
	private ListPreference mUnitsPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		
		// Load XML prefs file
		addPreferencesFromResource(R.xml.settings);
		
		// Get reference to preferences to update summaries
		mEditTextPref1 = (EditTextPreference)getPreferenceScreen().findPreference(OPT_FAV_AMOUNT[0]);
		mEditTextPref2 = (EditTextPreference)getPreferenceScreen().findPreference(OPT_FAV_AMOUNT[1]);
		mEditTextPref3 = (EditTextPreference)getPreferenceScreen().findPreference(OPT_FAV_AMOUNT[2]);
		mEditTextPref4 = (EditTextPreference)getPreferenceScreen().findPreference(OPT_FAV_AMOUNT[3]);
		mEditTextPref5 = (EditTextPreference)getPreferenceScreen().findPreference(OPT_FAV_AMOUNT[4]);
		mEditTextPrefSrv = (EditTextPreference)getPreferenceScreen().findPreference(ONE_SRV_AMOUNT);
		mUnitsPref = (ListPreference)getPreferenceScreen().findPreference(OPT_UNITS);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Setup initial values
		mEditTextPref1.setSummary("Current amount is " + getPreferenceScreen().getSharedPreferences().getString(OPT_FAV_AMOUNT[0], OPT_FAV_AMOUNT_DEF[0]));
		mEditTextPref2.setSummary("Current amount is " + getPreferenceScreen().getSharedPreferences().getString(OPT_FAV_AMOUNT[1], OPT_FAV_AMOUNT_DEF[1]));
		mEditTextPref3.setSummary("Current amount is " + getPreferenceScreen().getSharedPreferences().getString(OPT_FAV_AMOUNT[2], OPT_FAV_AMOUNT_DEF[2]));
		mEditTextPref4.setSummary("Current amount is " + getPreferenceScreen().getSharedPreferences().getString(OPT_FAV_AMOUNT[3], OPT_FAV_AMOUNT_DEF[3]));
		mEditTextPref5.setSummary("Current amount is " + getPreferenceScreen().getSharedPreferences().getString(OPT_FAV_AMOUNT[4], OPT_FAV_AMOUNT_DEF[4]));
		mEditTextPrefSrv.setSummary("Current amount is " + getPreferenceScreen().getSharedPreferences().getString(ONE_SRV_AMOUNT, String.format("%s", DEFAULT_FAV_AMOUNT)));
		
		String unitsPref = getPreferenceScreen().getSharedPreferences().getString(OPT_UNITS, OPT_UNITS_DEF);

		if (unitsPref.equals(String.valueOf(UNITS_US))) {
			mUnitsPref.setSummary("US");
		} else {
			mUnitsPref.setSummary("Metric");
		}
		
		
		// Register listener for when a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Unregister listener for when a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	/** Get the current value of daily goal amount */
	public static double getAmount(Context context) {
		try {
			String prefString = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_AMOUNT, OPT_AMOUNT_DEF);
			
			double prefAmount = Double.valueOf(prefString).doubleValue();
			
			return prefAmount;
		} catch (NumberFormatException nfe) {
			return DEFAULT_AMOUNT;
		} 
	}
	
	public static String getFavoriteAmountString(int favIndex, Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(OPT_FAV_AMOUNT[favIndex], OPT_FAV_AMOUNT_DEF[favIndex]);
	}
	
	public static double getOneServingAmount(Context context) {
		return Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(ONE_SRV_AMOUNT, String.format("%s", DEFAULT_FAV_AMOUNT)));
	}
	
	public static String[] getArrayOfFavoriteAmounts(Context context) {
		String[] favAmounts = new String[5];
    	
    	favAmounts[0] = PreferenceManager.getDefaultSharedPreferences(context)
    						.getString(OPT_FAV_AMOUNT[0], OPT_FAV_AMOUNT_DEF[0]);
    	favAmounts[1] = PreferenceManager.getDefaultSharedPreferences(context)
							.getString(OPT_FAV_AMOUNT[1], OPT_FAV_AMOUNT_DEF[1]);
    	favAmounts[2] = PreferenceManager.getDefaultSharedPreferences(context)
							.getString(OPT_FAV_AMOUNT[2], OPT_FAV_AMOUNT_DEF[2]);
    	favAmounts[3] = PreferenceManager.getDefaultSharedPreferences(context)
							.getString(OPT_FAV_AMOUNT[3], OPT_FAV_AMOUNT_DEF[3]);
    	favAmounts[4] = PreferenceManager.getDefaultSharedPreferences(context)
							.getString(OPT_FAV_AMOUNT[4], OPT_FAV_AMOUNT_DEF[4]);
    
    	return favAmounts;
	}
	
	public static int getUnitSystem(Context context) {
		try {
			String prefString = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_UNITS, OPT_UNITS_DEF);
			
			int prefAmount = Integer.parseInt(prefString);
			
			return prefAmount;
		} catch (NumberFormatException nfe) {
			return UNITS_US;
		} 
	}
	
	public static double getFavoriteAmountDouble(int favIndex, Context context) {
		try {
			String prefString = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_FAV_AMOUNT[favIndex], OPT_FAV_AMOUNT_DEF[favIndex]);
			
			double prefAmount = Double.valueOf(prefString).doubleValue();
			
			return prefAmount;
		} catch (NumberFormatException nfe) {
			return DEFAULT_FAV_AMOUNT;
		}
	}
	
	public static boolean getToastsSetting(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean(OPT_TOASTS, true);
	}
	
	public static boolean getLargeUnitsSetting(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean(OPT_LARGE_UNITS, false);
	}
	
	public static boolean getOverrideVolumeUp(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean(OPT_ENABLE_VOL_UP, false);
	}
	
	public static boolean getOverrideVolumeDown(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean(OPT_ENABLE_VOL_DOWN, false);
	}
	
	public static boolean getReminderEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getBoolean(OPT_ENABLE_REMINDERS, false);
	}
	
	public static int getReminderInterval(Context context) {
		int reminderInterval = 0;
		try {
			String prefString = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_REMINDER_INTERVAL, OPT_REMINDER_INT_DEF);
			
			reminderInterval = Integer.parseInt(prefString);
		} catch (NumberFormatException nfe) {
			reminderInterval = DEFAULT_REMINDER_INT;
		}
		
		return reminderInterval;
	}
	
	public static Notification getReminderNotification(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean remindersEnabled = prefs.getBoolean(OPT_ENABLE_REMINDERS, false);
		
		if (remindersEnabled) {
			// Reminders are enabled

			// Get light, sound, and vibrate settings
			boolean showLight = prefs.getBoolean(OPT_REMINDER_LED, false);
			String soundPref = prefs.getString(OPT_REMINDER_SOUND, "silent");
			boolean vibrate = prefs.getBoolean(OPT_REMINDER_VIB, false);
			
			// Get the reminder interval user has chosen
			// Default is 60 minutes after last entry
			int reminderInterval = 0;
			try {
				String prefString = prefs.getString(OPT_REMINDER_INTERVAL, OPT_REMINDER_INT_DEF);
				
				reminderInterval = Integer.parseInt(prefString);
			} catch (NumberFormatException nfe) {
				reminderInterval = DEFAULT_REMINDER_INT;
			}
			
			// Create the calendar object for the notification
			// adjusted for user's interval preference
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, reminderInterval);
			
			// Setup notification object
			String wordMinutes = "minute";
			if (reminderInterval != 1) {
				wordMinutes += "s";
			}
			String reminderMsg = String.format("Hydrate - %d %s since last entry", reminderInterval, wordMinutes);
			Notification reminder = new Notification(R.drawable.icon,
													 reminderMsg,
													 cal.getTimeInMillis());
			reminder.flags |= Notification.FLAG_AUTO_CANCEL;
			
			// Setup the blinking LED if necessary
			if (showLight) {
				reminder.ledARGB = 0xff0000ff; // Blue LED for water if possible
				reminder.ledOffMS = 3000;
				reminder.ledOnMS = 1000;
				reminder.flags |= Notification.FLAG_SHOW_LIGHTS;
			}
			
			// Setup the sound if necessary
			if (!soundPref.equals("silent")) {
				reminder.sound = Uri.parse(soundPref); 
			}
			
			// Setup vibrate if necessary
			if (vibrate) {
				reminder.defaults |= Notification.DEFAULT_VIBRATE;
			}
			
			return reminder;
		} else {
			return null;	
		}
		
	}
	
	public static double getVolumeUpAmount(Context context) {
		try {
			String prefString = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_VOL_UP_AMOUNT, OPT_FAV_AMOUNT_DEF[0]);
			
			double prefAmount = Double.valueOf(prefString).doubleValue();
			
			return prefAmount;
		} catch (NumberFormatException nfe) {
			return DEFAULT_FAV_AMOUNT;
		}
	}
	
	public static Date getWakeTime(Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-d-y HH:mm");
		String wakeTime = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_REMINDER_WAKE, "-1");
		Date wakeDate;
		try {
			wakeDate = sdf.parse(wakeTime);
			return wakeDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Date getSleepTime(Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-d-y HH:mm");
		String sleepTime = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_REMINDER_SLEEP, "-1");
		Date sleepDate;
		try {
			sleepDate = sdf.parse(sleepTime);
			return sleepDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean isDuringSleepHours(Context context, String entryDateStr) {
		boolean result = false;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date entryDate = null;
		try {
			entryDate = sdf.parse(entryDateStr);
		} catch (ParseException e) {
			return true;
		}
		Date sleepDate = new Date();
		Date wakeDate = new Date();
		
		String sleepTime = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_REMINDER_SLEEP, "-1");
		String sleepDateStr = entryDateStr.substring(0, 11) + sleepTime + ":00";
		String wakeTime = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_REMINDER_WAKE, "-1");
		String wakeDateStr = entryDateStr.substring(0, 11) + wakeTime + ":00";
		
		try {
			sleepDate = sdf.parse(sleepDateStr);
			wakeDate = sdf.parse(wakeDateStr);
		} catch (ParseException pe) {
			Log.d("SLEEP_HOURS", "catch sdf " + pe.getMessage());
			return true;
		}
		
		try {
			int sleep = Integer.parseInt(sleepTime.replace(":", ""));
			int wake = Integer.parseInt(wakeTime.replace(":", ""));

			if (wake < sleep && entryDate.getHours() >= 12){
				// need to add a day to the date
				wakeDate.setDate(wakeDate.getDate()+1);
			} else {
				sleepDate.setDate(sleepDate.getDate()-1);
			}
			
			// check current time is between the limits set by user
			if (entryDate.after(sleepDate) && entryDate.before(wakeDate)) {
				result = true;
			}
			
		} catch (NumberFormatException nfe) {
			result = true;
		}
		
		return result;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		// Update the preference summaries for favorite amounts
		if (key.equals(OPT_FAV_AMOUNT[0])) {
			mEditTextPref1.setSummary("Current amount is " + sharedPreferences.getString(key, OPT_FAV_AMOUNT_DEF[0]));
		} else if (key.equals(OPT_FAV_AMOUNT[1])) {
			mEditTextPref2.setSummary("Current amount is " + sharedPreferences.getString(key, OPT_FAV_AMOUNT_DEF[1]));
		} else if (key.equals(OPT_FAV_AMOUNT[2])) {
			mEditTextPref3.setSummary("Current amount is " + sharedPreferences.getString(key, OPT_FAV_AMOUNT_DEF[2]));
		} else if (key.equals(OPT_FAV_AMOUNT[3])) {
			mEditTextPref4.setSummary("Current amount is " + sharedPreferences.getString(key, OPT_FAV_AMOUNT_DEF[3]));
		} else if (key.equals(OPT_FAV_AMOUNT[4])) {
			mEditTextPref5.setSummary("Current amount is " + sharedPreferences.getString(key, OPT_FAV_AMOUNT_DEF[4]));
		} else if (key.equals(ONE_SRV_AMOUNT)) {
			mEditTextPrefSrv.setSummary("Current amount is " + sharedPreferences.getString(key, String.format("%s", DEFAULT_FAV_AMOUNT)));
		} else if (key.equals(OPT_UNITS)) {
			String unitsPref = sharedPreferences.getString(key, OPT_UNITS_DEF);
			if (unitsPref.equals(String.valueOf(UNITS_US))) {
				mUnitsPref.setSummary("US");
			} else {
				mUnitsPref.setSummary("Metric");
			}
			
			// Update the units on the widget
			SharedPreferences localData = getSharedPreferences("hydrate_data", 0);
			Intent widgetIntent = new Intent(AppWidget.FORCE_WIDGET_UPDATE);
			widgetIntent.putExtra("AMOUNT", Double.valueOf(localData.getString("amount", "0")));
	    	widgetIntent.putExtra("PERCENT", Double.valueOf(localData.getString("percent", "0")));
	    	widgetIntent.putExtra("UNITS", Integer.valueOf(unitsPref));
	    	this.sendBroadcast(widgetIntent);
		} else if (key.equals(OPT_ENABLE_REMINDERS)) {
			boolean useReminders = sharedPreferences.getBoolean(OPT_ENABLE_REMINDERS, false);
			
			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			
			if (useReminders) {
				ContentResolver cr = getContentResolver();
		    	
		    	// Return all saved entries, grouped by date
		    	Cursor c = cr.query(Uri.withAppendedPath(WaterProvider.CONTENT_URI, "latest"),
		    						null, null, null, null);
		    	
		    	if (c.moveToFirst()) {
	    			String strDate = c.getString(WaterProvider.DATE_COLUMN);
	    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    			
	    			try {
	    				// make date object from string result
	    				Date date = sdf.parse(strDate);
		    		
	    				// create the calendar object
		    			Calendar cal = Calendar.getInstance();
		    			cal.setTime(date);
		    			
		    			// add X minutes to the calendar object
		    			int addMinutes = Settings.getReminderInterval(this);
		    			cal.add(Calendar.MINUTE, addMinutes);
		    			// set up the new alarm
		    			Intent intent = new Intent(this, AlarmReceiver.class);
		    			PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		    			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	    			} catch (ParseException e) {
	    				e.printStackTrace();
	    			}
		    	}
		    	
		    	c.close();
			} else {
				// disabling reminders, remove any pending intent
				// so an already registered alarm does not go off
				Intent cancelIntent = new Intent(this, AlarmReceiver.class);
				PendingIntent cancelSender = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);
				am.cancel(cancelSender);
			}
		}
	}
}
