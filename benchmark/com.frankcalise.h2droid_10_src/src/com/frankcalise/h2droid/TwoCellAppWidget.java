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

public class TwoCellAppWidget extends AppWidgetProvider {
	
	private double mAmount;
	private double mPercentGoal;
	
	// Intent to listen for to update Widget UI
	public static String FORCE_WIDGET_UPDATE = 
		"com.frankcalise.h2droid.FORCE_WIDGET_UPDATE2";
	
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
			
			// Update the UI
			updateAmount(context);
		} else {
			Log.d("WIDGET", intent.getAction());
		}
	}
	
	public void updateAmount(Context context,
							 AppWidgetManager appWidgetManager,
							 int[] appWidgetIds) {
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
    	Intent addEntryAppIntent = new Intent(context, CustomEntryActivity.class);
    	
    	// Create new RemoteViews to set the text displayed
    	// by the widget's TextView
    	final int N = appWidgetIds.length;
    	for (int i = 0; i < N; i++) {
    		int appWidgetId = appWidgetIds[i];
    		RemoteViews views = new RemoteViews(context.getPackageName(),
    											R.layout.two_cell_widget);
    		// Set text for TextViews
    		views.setTextViewText(R.id.widget_amount_text, String.format("%.1f fl oz", mAmount));
    		views.setTextColor(R.id.widget_percent_text, goalColor);
    		views.setTextViewText(R.id.widget_percent_text, String.format("%.1f%%", mPercentGoal));
    		
    		// Set onClick so user can launch the app
    		// by touching the widget
    		views.setOnClickPendingIntent(R.id.widget_title_text, PendingIntent.getActivity(context, 0, launchAppIntent, 0));
    		views.setOnClickPendingIntent(R.id.widget_amount_text, PendingIntent.getActivity(context, 0, launchAppIntent, 0));
    		views.setOnClickPendingIntent(R.id.widget_percent_text, PendingIntent.getActivity(context, 0, launchAppIntent, 0));
    		views.setOnClickPendingIntent(R.id.widget_add_button, PendingIntent.getActivity(context, 0, addEntryAppIntent, 0));
    		
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
