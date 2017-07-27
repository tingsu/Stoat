package com.frankcalise.h2droid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class WaterConsumption {
	private Context mContext;
	private String mDate;
	private ArrayList<Entry> mEntryList = new ArrayList<Entry>();
	private double mGoalAmount;
	private double mAmount;
	private ContentResolver mContentResolver = null;
	
	public WaterConsumption(Context _context) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	Date now = new Date();
    	mDate = sdf.format(now);
    	
    	mContext = _context;
    	mContentResolver = mContext.getContentResolver();
    	
    	loadEntries();
	}
	
	public WaterConsumption(Context _context, String _date) {
		mDate = _date;
		mContext = _context;
		mContentResolver = mContext.getContentResolver();
		
		loadEntries();
	}
	
	private void loadEntries() {
		// Clear the entry list
		mEntryList.clear();
		mAmount = 0;
		
		// Set up the where clause for the content resolver
		String where = "'" + mDate + "' = date(" + WaterProvider.KEY_DATE + ")";
		
		// Return saved entries
    	Cursor c = mContentResolver.query(WaterProvider.CONTENT_URI,
    					    null, where, null, null);
    	
    	// Build entry list from results
    	if (c.moveToFirst()) {
    		do { 
    			Entry e = new Entry(c.getString(WaterProvider.DATE_COLUMN),
    								c.getDouble(WaterProvider.AMOUNT_COLUMN),
    								false);
    			mEntryList.add(e);
    			mAmount += e.getNonMetricAmount();
    		} while (c.moveToNext());
    	}
    	
    	// Close the cursor
    	c.close();
	}
	
	public double getAmount() {
		return mAmount;
	}
	
	public double getGoalAmount() {
		return mGoalAmount;
	}
	
	public double getAmountToGoal() {
		return (mGoalAmount - mAmount);
	}
	
	public boolean isGoalMet() {
		if (mAmount >= mGoalAmount) {
			return true;
		}
		
		return false;
	}
	
	public void addEntry(Entry _entry) {
		// Insert the new entry into the provider
    	ContentValues values = new ContentValues();
    	
    	values.put(WaterProvider.KEY_DATE, _entry.getDate());
    	values.put(WaterProvider.KEY_AMOUNT, _entry.getMetricAmount());
    	
    	mContentResolver.insert(WaterProvider.CONTENT_URI, values);
	}
	
	public boolean undoLastAmount() {
    	// Set up where, sorting, column retrieval
    	String sortOrder = WaterProvider.KEY_DATE + " DESC LIMIT 1";
    	String[] projection = {WaterProvider.KEY_ID};
    	String where = "'" + mDate + "' = date(" + WaterProvider.KEY_DATE + ")";
    	
    	// Query the content resolver
    	Cursor c = mContentResolver.query(WaterProvider.CONTENT_URI, projection, where, null, sortOrder);
    	
    	// Check for results
    	int results = 0;
    	if (c.moveToFirst()) {
    		final Uri uri = Uri.parse("content://com.frankcalise.provider.h2droid/entries/" + c.getInt(0));
    		results = mContentResolver.delete(uri, null, null);
    	} 
    	
    	// Close the cursor
    	c.close();
    	
    	// Results > 0 if a row was deleted
    	if (results > 0) {
    		return true;
    	} else {
    		return false;
    	}
	}
	
	public void refreshData() {
		loadEntries();
	}
}
