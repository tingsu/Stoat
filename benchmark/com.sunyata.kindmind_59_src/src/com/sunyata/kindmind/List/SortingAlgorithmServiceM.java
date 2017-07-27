package com.sunyata.kindmind.List;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.Database.PatternsTableM;
import com.sunyata.kindmind.util.DatabaseU;
import com.sunyata.kindmind.util.DbgU;

/**
 * Overview: SortingAlgorithmM handles sorting for the three instances of \ref ListFragmentC
 * contained in the (single instance of) \ref MainActivityC
 * 
 * Sorting is done by extracting values from the database and working with them, then updating
 * values in the kindsort column in the items table in the database
 */
public class SortingAlgorithmServiceM extends IntentService {
	public static final double PATTERN_MULTIPLIER = 12;
	public static final double SIMPLE_PATTERN_MATCH_ADDITION = 1;
	public static final int UPDATE_SERVICE_DONE = 89742;
	private static final int OLD_PATTERN_TIME_NOT_SET = -2;
	private static final String TAG = "SortingAlgorithmServiceM";

	public SortingAlgorithmServiceM() {
		super(TAG);
	}

	/**
	 * \brief updateOnBackgroundThread updates the sort values for all list items
	 * 
	 * These things will have effect on the sort value:
	 * + 1. The number of times an item has been marked (has effect even when no checkbox is active)
	 * + 2. The history of correlations between a checked list item and other items. Ex: If an item
	 * has been checked and saved with another previously and the first item is now checked, the
	 * second will get an increase in sort value
	 *  
	 * Used in:
	 * + Called when a user checks or unchecks a checkbox
	 * 
	 * Notes:
	 * + In cases where the relevance is zero we still use the SIMPLE_PATTERN_MATCH_ADDITION
	 * constant, once for each time that the item has been checked and saved into the pattern table
	 *  
	 * Improvements:
	 * + To cut down on object creation and thereby memory usage, remove the PatternM private class
	 * and store "relevance" values in an array to reduce object creation
	 * + Updating the sort values after the cursor has been closed
	 * http://stackoverflow.com/questions/11633581/attempt-to-re-open-an-already-closed-object-java-lang-illegalstateexception
	 * + Algorithm improvements: Many ideas, one is to use the time stamp from the patterns table to
	 * reduce relevance for patterns from a long time back
	 */
	@Override
	protected void onHandleIntent(Intent inIntent) {
		ArrayList<Long> tCheckedItems = new ArrayList<Long>();
		ArrayList<Double> tUpdateListSortValue = new ArrayList<Double>();
		ArrayList<Uri> tUpdateListUri = new ArrayList<Uri>();
		
		//1. Go through all checked/active items and store them in an array
		String tItemSel = ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE;
		Cursor tItemCr = getApplicationContext().getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tItemSel, null, null);
		tItemCr.moveToFirst();
		try{
			for(; tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
				tCheckedItems.add(Long.parseLong(tItemCr.getString(
						tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID))));
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}
		}
		
		
		//2. Going through all patterns and saving each pattern in a matrix with pattern relevance
		// added..
		ArrayList<Pattern> tPatternMatrix = new ArrayList<Pattern>();
		Cursor tPatternCr = getApplicationContext().getContentResolver().query(
				ContentProviderM.PATTERNS_CONTENT_URI, null, null, null,
				PatternsTableM.COLUMN_CREATE_TIME);
		tPatternCr.moveToFirst();
		try{
			long tOldPatternTime = OLD_PATTERN_TIME_NOT_SET;
			for(; tPatternCr.isAfterLast() == false; tPatternCr.moveToNext()){
				
				long tNewPatternTime = Long.parseLong(tPatternCr.getString(
						tPatternCr.getColumnIndexOrThrow(PatternsTableM.COLUMN_CREATE_TIME)));
				//-the time is used to identify one specific pattern (can be the same over several rows
				// of the "patterns" table)
				
				long tItemRefId = Long.parseLong(tPatternCr.getString(
						tPatternCr.getColumnIndexOrThrow(PatternsTableM.COLUMN_ITEM_REFERENCE)));
				//-will be compared to the list from step 1 above
				
				//..check to see if we have gone into a new pattern in the list..
				if(tNewPatternTime != tOldPatternTime){
					tOldPatternTime = tNewPatternTime;
					
					//..if so create a new pattern list and add it to the matrix
					Pattern tPatternList = new Pattern();
					tPatternMatrix.add(tPatternList);
				}
				
				//..adding the item reference to the end of the last pattern in the matrix
				tPatternMatrix.get(tPatternMatrix.size() - 1).list.add(tItemRefId);
			}
		}catch(Exception e){Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}finally{
			if(tPatternCr != null){
				tPatternCr.close();
			}
		}
		

		//3. Going through the newly created matrix and comparing with the list from step 1 to
		// update relevance..
		for(Pattern p : tPatternMatrix){
			float tNumberOfMatches = 0;
			float tDivisor = (p.list.size() + tCheckedItems.size());
			if(tDivisor == 0){
				continue;
			}
			for(Long ci : tCheckedItems){
				if(p.list.contains(ci)){ //-PLEASE NOTE: ".contains" tests .equals (not object identity)
					tNumberOfMatches++;
				}
			}
			//..updating the relevance
			p.relevance = tNumberOfMatches / tDivisor;
		}
		
		
		//4. Going through all list items and using the relevance to update the kindsort value
		// for each item..
		tItemCr = getApplicationContext().getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, ContentProviderM.sSortType);
		tItemCr.moveToFirst();
		try{
			for(; tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
				double tNewKindSortValue = 0; 
				long tItemId = Long.parseLong(tItemCr.getString(
						tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID)));
				Uri tItemUri = Uri.parse(ContentProviderM.ITEM_CONTENT_URI + "/" + tItemId);
				
				for(Pattern p : tPatternMatrix){
					if(p.list.contains(tItemId)){
						//..calculating and adding to the kindsort value
						tNewKindSortValue = tNewKindSortValue
								+ SIMPLE_PATTERN_MATCH_ADDITION
								+ p.relevance * PATTERN_MULTIPLIER;
					}
				}
				//..adding value to list (will be written to database after the cursor has been closed)
				tUpdateListSortValue.add(tNewKindSortValue);
				tUpdateListUri.add(tItemUri);
			}
		}catch(Exception e){Log.wtf(DbgU.getAppTag(), DbgU.getMethodName());
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}
		}
		
		
		//5. Updating the kindsort values in the database
		ContentValues tUpdateVal;
		for(int i = 0; i < tUpdateListSortValue.size(); i++){
			tUpdateVal = new ContentValues();
			tUpdateVal.put(ItemTableM.COLUMN_KINDSORT_VALUE, tUpdateListSortValue.get(i));
			getApplicationContext().getContentResolver().update(
					tUpdateListUri.get(i), tUpdateVal, null, null);
		}
		
		
		//6. Communicating the result (stopping showing the progress bar (aka "loading spinner"))
		ResultReceiver tResultReceiver = inIntent.getParcelableExtra(
				ListFragmentC.EXTRA_KINDSORT_RESULT);
		if(tResultReceiver != null){
			tResultReceiver.send(UPDATE_SERVICE_DONE, null);
		}
	}
	
	private static class Pattern{
		public float relevance;
		public ArrayList<Long> list;
		public Pattern(){
			relevance = 0;
			list = new ArrayList<Long>();
		}
	}
}
