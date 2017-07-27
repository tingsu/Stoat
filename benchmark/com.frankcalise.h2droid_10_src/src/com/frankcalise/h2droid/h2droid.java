package com.frankcalise.h2droid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class h2droid extends Activity {
	private double mConsumption = 0;
	private boolean mShowToasts;
	private boolean mIsNonMetric = true;
	private static final String LOCAL_DATA = "hydrate_data";
	private Context mContext = null;
	private int mUnitsPref;
	private ContentResolver mContentResolver = null;
	private String mOneServingText = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContentResolver = getContentResolver();
        mContext = getApplicationContext();
        
        // Set up main layout
        setContentView(R.layout.main);
    }
    
    /** Called when activity returns to foreground */
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	mShowToasts = Settings.getToastsSetting(mContext);
    	mUnitsPref = Settings.getUnitSystem(mContext);
    	if (mUnitsPref == Settings.UNITS_US) {
    		mIsNonMetric = true;
    	} else {
    		mIsNonMetric = false;
    	}
    	
    	loadTodaysEntriesFromProvider();
    }
    
    /** Set up menu for main activity */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	// Inflate the main menu
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	
    	return true;
    }
    
    /** Handle menu selection */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Start activity depending on menu choice
    	switch (item.getItemId()) {
    		case R.id.menu_settings:
    			startActivity(new Intent(this, Settings.class));
    			return true;
    		case R.id.menu_facts:
    			startActivity(new Intent(this, FactsActivity.class));
    			return true;
    		case R.id.menu_history:
    			startActivity(new Intent(this, HistoryActivity.class));
    			return true;
    		default: break;
    	}
    	
    	return false;
    }
    
    /** Handle "add one serving" action */
    public void onOneServingClick(View v) {
		Entry oneServing = new Entry(Settings.getOneServingAmount(this), mIsNonMetric);
		addNewEntry(oneServing);
    }
    
    /** Handle "add two servings" action */
    public void onFavServingsClick(View v) {
    	String[] itemsArr = Settings.getArrayOfFavoriteAmounts(this);
		new AlertDialog.Builder(this)
			.setTitle("Add favorite amount")
			.setItems(itemsArr, 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						double favAmount = Settings.getFavoriteAmountDouble(which, mContext);
						Entry favServing = new Entry(favAmount, mIsNonMetric);
						addNewEntry(favServing);
					}
				})
		.show();
    }
    
    /** Handle "add custom serving" action */
    public void onCustomServingClick(View v) {
    	// adding some amount of water other than
    	// one or two servings
    	startActivity(new Intent(this, CustomEntryActivity.class));
    }
    
    /** Handle "undo last serving" action */
    public void onUndoClick(View v) {
		// remove last entry from today
		undoTodaysLastEntry();
    }
    
    private void addNewEntry(Entry _entry) {
    	// Insert the new entry into the provider
    	ContentValues values = new ContentValues();
    	
    	values.put(WaterProvider.KEY_DATE, _entry.getDate());
    	values.put(WaterProvider.KEY_AMOUNT, _entry.getMetricAmount());
    	
    	mContentResolver.insert(WaterProvider.CONTENT_URI, values);
    	
    	if (mUnitsPref == Settings.UNITS_US) {
    		mConsumption += _entry.getNonMetricAmount();
    	} else {
    		mConsumption += _entry.getMetricAmount();
    	}
    	
    	// Make a toast displaying add complete
    	double displayAmount = _entry.getNonMetricAmount();
    	String displayUnits = "fl oz";
    	if (mUnitsPref == Settings.UNITS_METRIC) {
    		displayUnits = "ml";
    		displayAmount = _entry.getMetricAmount();
    	}
    	
    	String toastMsg = String.format("Added %.1f %s", displayAmount, displayUnits);
    	Toast toast = Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.BOTTOM, 0, 0);
    	if (mShowToasts)
    		toast.show();
    	
    	// Update the amount of consumption on UI
    	updateConsumptionTextView();
    	
    	// If user wants a reminder when to drink next,
    	// setup a notification X minutes away from this entry
    	// where X is also a setting
    	if (Settings.getReminderEnabled(this)) {
    		// Get the AlarmManager service
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			
			// create the calendar object
			Calendar cal = Calendar.getInstance();
			// add X minutes to the calendar object
			cal.add(Calendar.MINUTE, Settings.getReminderInterval(this));
			
			// cancel existing alarm if any, this way latest
			// alarm will be the only one to notify user
			Intent cancelIntent = new Intent(this, AlarmReceiver.class);
			PendingIntent cancelSender = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);
			am.cancel(cancelSender);
			
			// set up the new alarm
			Intent intent = new Intent(this, AlarmReceiver.class);
			intent.putExtra("entryDate", _entry.getDate());
			PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    	}
    }
    
    private void undoTodaysLastEntry() {
    	Date now = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	String sortOrder = WaterProvider.KEY_DATE + " DESC LIMIT 1";
    	String[] projection = {WaterProvider.KEY_ID};
    	String where = "'" + sdf.format(now) + "' = date(" + WaterProvider.KEY_DATE + ")";
    	
    	Cursor c = mContentResolver.query(WaterProvider.CONTENT_URI, projection, where, null, sortOrder);
    	int results = 0;
    	if (c.moveToFirst()) {
    		final Uri uri = Uri.parse("content://com.frankcalise.provider.h2droid/entries/" + c.getInt(0));
    		results = mContentResolver.delete(uri, null, null);
    	} else {
    		//Log.d("UNDO", "no entries from today!");
    	}
    	
    	c.close();
    	
    	String toastMsg;
    	if (results > 0) {
    		loadTodaysEntriesFromProvider();
    		toastMsg = "Undoing last entry...";
    	} else {
    		toastMsg = "No entries from today!";
    	}
    	
    	Toast toast = Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT);
    	toast.setGravity(Gravity.BOTTOM, 0, 0);
    	if (mShowToasts)
    		toast.show();
    }

    private void loadTodaysEntriesFromProvider() {
    	mConsumption = 0;
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date();
    	String where = "'" + sdf.format(now) + "' = date(" + WaterProvider.KEY_DATE + ")";
    	
    	// Return all saved entries
    	Cursor c = mContentResolver.query(WaterProvider.CONTENT_URI,
    					    null, where, null, null);
    	
    	if (c.moveToFirst()) {
    		do {
    			String date = c.getString(WaterProvider.DATE_COLUMN);
    			double metricAmount = c.getDouble(WaterProvider.AMOUNT_COLUMN);
    			Entry e = new Entry(date, metricAmount, false);
    			if (mUnitsPref == Settings.UNITS_US) {
    				mConsumption += e.getNonMetricAmount();
    			} else {
    				mConsumption += e.getMetricAmount();
    			}
    		} while (c.moveToNext());
    	}
    	
    	c.close();
    	
    	updateConsumptionTextView();
    }
    
    /** Update the today's consumption TextView */
    private void updateConsumptionTextView() {
    	double prefsGoal = Settings.getAmount(mContext);
    	double percentGoal = (mConsumption / prefsGoal) * 100.0;
    	double delta = mConsumption - prefsGoal;

    	if (percentGoal > 100.0) {
    		percentGoal = 100.0;
    	}

    	// update the +N add button text according to the unit system
    	final Button nButton = (Button)findViewById(R.id.add_custom_serving_button);
    	// update one serving button
    	final Button oneSrvButton = (Button)findViewById(com.frankcalise.h2droid.R.id.add_one_serving_button);
    	
    	
    	// Show consumption amount	
    	String originalUnits = "";
    	double displayAmount = mConsumption;
    	String displayUnits = "fl oz";
    	if (mUnitsPref == Settings.UNITS_METRIC) {
    		//displayAmount = mConsumption / Entry.ouncePerMililiter;
    		displayUnits = "ml";
    		nButton.setText("+N ml");
    	} else {
    		nButton.setText("+N oz");
    	}
    	mOneServingText = String.format("%s (%s %s)", getString(com.frankcalise.h2droid.R.string.one_serving_button_label), Settings.getOneServingAmount(this), displayUnits);
    	oneSrvButton.setText(mOneServingText);
    	
    	originalUnits = displayUnits;
    	
    	if (Settings.getLargeUnitsSetting(mContext)) {
    		Amount currentAmount = new Amount(mConsumption, mUnitsPref);
    		displayAmount = currentAmount.getAmount();
    		displayUnits = currentAmount.getUnits();
    	}
    	
    	final TextView amountTextView = (TextView)findViewById(R.id.consumption_textview);
    	String dailyTotal = String.format("%.1f %s\n", displayAmount, displayUnits);
    	amountTextView.setText(dailyTotal);
    	
    	// Show delta from goal
    	final TextView overUnderTextView = (TextView)findViewById(R.id.over_under_textview);
    	String overUnder = String.format("%+.1f %s (%.1f%%)", delta, originalUnits, percentGoal);
    	overUnderTextView.setText(overUnder);
    	
    	if (delta >= 0) {
    		overUnderTextView.setTextColor(getResources().getColor(R.color.positive_delta));
    	} else {
    		overUnderTextView.setTextColor(getResources().getColor(R.color.negative_delta));
    	}
 
    	// Show current goal setting
    	final TextView goalTextView = (TextView)findViewById(R.id.goal_textview);
    	String goalText = String.format("Daily goal: %.1f %s", prefsGoal, originalUnits);
    	goalTextView.setText(goalText);	
    	
    	// Last entry
    	final TextView lastEntryTextView = (TextView) findViewById(R.id.last_entry_textview);
    	String lastEntryMsg = getLastEntry();
    	if (lastEntryMsg == null) {
    		lastEntryTextView.setVisibility(View.INVISIBLE);
    	} else {
    		lastEntryTextView.setVisibility(View.VISIBLE);
    		lastEntryTextView.setText(String.format("Last entry: %s", lastEntryMsg));	
    	}
    	
    	// Broadcast an Intent to update Widget
    	// Use putExtra so AppWidget class does not need
    	// to do ContentProvider pull
    	Intent widgetIntent = new Intent(AppWidget.FORCE_WIDGET_UPDATE);
    	widgetIntent.putExtra("AMOUNT", mConsumption);
    	widgetIntent.putExtra("PERCENT", percentGoal);
    	widgetIntent.putExtra("UNITS", mUnitsPref);
    	this.sendBroadcast(widgetIntent);
    	
    	// Save off current amount, needed if user 
    	// changes unit system settings to update
    	// widget later on
    	SharedPreferences localData = getSharedPreferences(LOCAL_DATA, 0);
    	SharedPreferences.Editor editor = localData.edit();
    	editor.putString("amount", String.valueOf(mConsumption));
    	editor.putString("percent", String.valueOf(percentGoal));
    	
    	// Commit changes
    	editor.commit();
    }
    
    // Override volume keys if user desires
    // depending on settings
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && Settings.getOverrideVolumeUp(this)) {
    		Entry e = new Entry(Settings.getVolumeUpAmount(this), mIsNonMetric);
    		addNewEntry(e);
    		return true;
    	} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && Settings.getOverrideVolumeDown(this)) {
    		undoTodaysLastEntry();
    		return true;
    	} else {
    		return super.onKeyDown(keyCode, event);
    	}
    }
    
    private String getLastEntry() {
    	String result = null;
    	String sortOrder = WaterProvider.KEY_DATE + " DESC LIMIT 1";
    	String[] projection = {WaterProvider.KEY_DATE};
    	
    	Cursor c = mContentResolver.query(WaterProvider.CONTENT_URI, projection, null, null, sortOrder);
    	if (c.moveToFirst()) {
    		result = c.getString(0);
    	}
    	c.close();
    	
    	return result;
    }
}