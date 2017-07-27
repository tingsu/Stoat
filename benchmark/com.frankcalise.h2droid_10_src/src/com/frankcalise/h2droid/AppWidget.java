package com.frankcalise.h2droid;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

public class AppWidget extends AppWidgetProvider {
	
	private double mAmount;
	private double mPercentGoal;
	private int mUnitSystem;
	private boolean mLargeUnits;
	
	// Intent to listen for to update Widget UI
	public static String FORCE_WIDGET_UPDATE = 
		"com.frankcalise.h2droid.FORCE_WIDGET_UPDATE";
	
	@Override
	public void onUpdate(Context context,
						 AppWidgetManager appWidgetManager,
						 int[] appWidgetIds) {

		mAmount = mPercentGoal = -1;
		updateAmount(context, appWidgetManager, appWidgetIds);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		// Check if Intent received matches widget update string
		if (FORCE_WIDGET_UPDATE.equals(intent.getAction())) {
			// Get the passed in values to display
			mAmount = intent.getExtras().getDouble("AMOUNT");
			mPercentGoal = intent.getExtras().getDouble("PERCENT");
			mUnitSystem = intent.getExtras().getInt("UNITS");
			
			// Update the UI
			updateAmount(context);
		} else {
			Log.d("WIDGET", intent.getAction());
		}
	}
	
	public void updateAmount(Context context,
							 AppWidgetManager appWidgetManager,
							 int[] appWidgetIds) {

		mUnitSystem = Settings.getUnitSystem(context);
		mLargeUnits = Settings.getLargeUnitsSetting(context);
		
		// Setup units string
		String displayUnits = "fl oz";
		if (mUnitSystem == Settings.UNITS_METRIC) {
			displayUnits = "mL";
		}
		
		// Grab the data from today's entries
		// if the widget was just added
		if (mAmount < 0) {
			mAmount = 0;
	    	
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    	Date now = new Date();
	    	String where = "'" + sdf.format(now) + "' = date(" + WaterProvider.KEY_DATE + ")";
	    	
	    	ContentResolver cr = context.getContentResolver();
	    	
	    	// Return all saved entries
	    	Cursor c = cr.query(WaterProvider.CONTENT_URI,
	    					    null, where, null, null);
	    	
	    	if (c.moveToFirst()) {
	    		do {
	    			String date = c.getString(WaterProvider.DATE_COLUMN);
	    			double metricAmount = c.getDouble(WaterProvider.AMOUNT_COLUMN);
	    			boolean isNonMetric = false;
	    			Entry e = new Entry(date, metricAmount, isNonMetric);
	    			
	    			mAmount += e.getNonMetricAmount();
	    		} while (c.moveToNext());
	    	}
	    	
	    	c.close();
		}
		
		// First time widget is placed, need percentage
		// of goal completed
		if (mPercentGoal < 0) {
			double prefsGoal = Settings.getAmount(context);
			mPercentGoal = Math.min(((mAmount / prefsGoal) * 100.0), 100.0);
		}
		
    	int goalColor = context.getResources().getColor(R.color.positive_delta);
    	
    	if (mPercentGoal < 100) {
    		goalColor = context.getResources().getColor(R.color.negative_delta);
    	}
    	
    	Intent launchAppIntent = new Intent(context, h2droid.class);
    	launchAppIntent.setAction(Intent.ACTION_MAIN);
    	launchAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	
    	double displayAmount = mAmount;
    	
    	if (mLargeUnits) {
			Amount currentAmount = new Amount(mAmount, mUnitSystem);
    		displayAmount = currentAmount.getAmount();
    		displayUnits = currentAmount.getUnits();
		}
    	
    	// Create new RemoteViews to set the text displayed
    	// by the widget's TextView
    	final int N = appWidgetIds.length;
    	for (int i = 0; i < N; i++) {
    		int appWidgetId = appWidgetIds[i];
    		RemoteViews views = new RemoteViews(context.getPackageName(),
    											R.layout.one_cell_widget);
    		// Set text for TextViews
    		views.setTextViewText(R.id.widget_amount_text, String.format("%.1f %s", displayAmount, displayUnits));
    		views.setTextColor(R.id.widget_percent_text, goalColor);
    		views.setTextViewText(R.id.widget_percent_text, String.format("%.1f%%", mPercentGoal));
    		
    		// Set onClick so user can launch the app
    		// by touching the widget
    		views.setOnClickPendingIntent(R.id.widget_background, PendingIntent.getActivity(context, 0, launchAppIntent, 0));
    		
    		appWidgetManager.updateAppWidget(appWidgetId, views);
    	}
	}
	
	// Obtain an instance of AppWidgetManager from the context
	// and use it to find widget IDs of active Hydrate widgets.
	// Then pass to updateAmount(Context, AppWidgetManager, int[])
	public void updateAmount(Context context) {
		ComponentName thisWidget = new ComponentName(context, AppWidget.class);
		
		AppWidgetManager appWidgetManager = 
			AppWidgetManager.getInstance(context);
		
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		updateAmount(context, appWidgetManager, appWidgetIds);
	}
}
