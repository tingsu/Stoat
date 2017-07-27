package com.sunyata.kindmind.util;

import java.io.File;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sunyata.kindmind.SortTypeM;
import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;
import com.sunyata.kindmind.List.ListTypeM;

public class DatabaseU {
	
	public static boolean sqlToBoolean(Cursor inCursor, String inColumn){
		long tmpItemIsActiveInteger = inCursor.getLong(inCursor.getColumnIndexOrThrow(inColumn));
		if(tmpItemIsActiveInteger == ItemTableM.FALSE){
			return false;
		}else{
			return true;
		}
	}
	
	//Cmp with method getListOfNamesForActivatedData
	public static int getActiveListItemCount(Context inContext, int inListTypeInt){
		int retCount = DbgU.NO_VALUE_SET;
		
		String tSel =
				ItemTableM.COLUMN_ACTIVE + " != " + ItemTableM.FALSE + " AND " +
				ItemTableM.COLUMN_LIST_TYPE + "=" + inListTypeInt;
		Cursor tmpCursor = inContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, tSel, null, ContentProviderM.sSortType);
		try{
			if(tmpCursor != null){
				
				retCount = tmpCursor.getCount();
				
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor is null",
						new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), e);
		}finally{
			if(tmpCursor != null){
				tmpCursor.close();
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
			}
		}
		if(retCount == DbgU.NO_VALUE_SET){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName(), new Exception());
		}
		
		return retCount;
	}
	
	public static Long getIdFromUri(Uri inUri){
		return Long.parseLong(inUri.toString().substring(inUri.toString().lastIndexOf("/") + 1));
	}
	public static Uri getItemUriFromId(long inId){
		return Uri.withAppendedPath(ContentProviderM.ITEM_CONTENT_URI, String.valueOf(inId));
	}
	/*
	 * Overview: databaseBackupInternal does a backup of the database file to internal storage
	 * Details: The name of the backup file includes version and date/time
	 * Used in: DatabaseHelperM.onUpgrade()
	 * Uses app internal: Utils.copyFile()
	 */
	public static void databaseBackupInternal(Context inContext, String inDataBaseName, int inOldVersion){
		Log.d(DbgU.getAppTag(),"Database backup");

		//Construction of the dir path and file name for the backup file
		String tmpDestinationPath = inContext.getDir("db_backup", Context.MODE_PRIVATE).toString();
		Calendar tmpCal = Calendar.getInstance();
		String tmpTimeString = "-"
				+ tmpCal.get(Calendar.YEAR) + "-"
				+ tmpCal.get(Calendar.MONTH) + "-"
				+ tmpCal.get(Calendar.DAY_OF_MONTH) + "-"
				+ tmpCal.get(Calendar.HOUR_OF_DAY) + "-"
				+ tmpCal.get(Calendar.MINUTE) + "-"
				+ tmpCal.get(Calendar.SECOND);
		String tmpVersionString = "-DatabaseVer" + inOldVersion;
		
		//Creating the new dir and file and getting reference to the existing database file
		File tmpSourceFile = inContext.getDatabasePath(inDataBaseName);
		File tmpDestinationFile = new File(tmpDestinationPath,
				"kindmind-" + tmpVersionString + tmpTimeString + ".db");
		//-tmpDestinationPath will be created internally but automatically gets an "app_" prefix.
		// Please note that standard directories (like /databases) are not be available for security reasons

		//Copying the file
		FileU.copyFile(tmpSourceFile, tmpDestinationFile);
	}
	
	public static Context getContentProviderContext(Context inOtherContext) {
		Context retContext = null;
		String tmpPackageName = "com.sunyata.kindmind";
		try {
			retContext = inOtherContext.createPackageContext(tmpPackageName, Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName()
					+ "Package name " + tmpPackageName + " not found");
			e.printStackTrace();
		}
		return retContext;
	}
	
	
	public static void setItemTableSortType(SortTypeM inSortType) {
		switch(inSortType){
		case ALPHABETASORT:
			ContentProviderM.sSortType = ItemTableM.COLUMN_NAME + " ASC";
			break;
		case KINDSORT:
			ContentProviderM.sSortType = ItemTableM.COLUMN_ACTIVE + " DESC" + ", "
					+ ItemTableM.COLUMN_KINDSORT_VALUE + " DESC";
			break;
		default:
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Case not covered");
			break;
		}
	}
	
	

	
	
	///@name Adding new items
	///@{
	
/*
	public static boolean isFirstTimeApplicationStarted(Context inContext){
		boolean retVal = PreferenceManager.getDefaultSharedPreferences(inContext).getBoolean(
				PREF_APP_VERSION_CODE, true); //Default is true (if no value has been written)
		return retVal;
	}
*/

	/**
	 * 
	 * Synonyms can be found on these sites:
	 * - http://www.synonym.com/synonyms/
	 * - http://thesaurus.com/
	 * @param iCt
	 */
	public static void createOrUpdateAllStartupItems(Context iCt) {
		Log.i(DbgU.getAppTag(), "Creating startup items");
		
		//Uri tUri = null; //-used for adding actions to items
		
		createStartupItem(iCt, ListTypeM.FEELINGS, "Anger");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Anxiety");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Concern");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Depression"); //Fatigue, Low energy
		createStartupItem(iCt, ListTypeM.FEELINGS, "Dissapointment");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Embarrassment");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Frustration");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Guilt");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Hurt");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Resentment");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Sadness");
		createStartupItem(iCt, ListTypeM.FEELINGS, "Tiredness");
		//Vulnarable
		//Suspicious
		//Shameful
		//Tiredness
		//TODO: Hint: Please remember the distinction between feelings and thoughts, feelings are based in
		//the body while thoughts are often ________

		createStartupItem(iCt, ListTypeM.NEEDS, "Acceptance");
		createStartupItem(iCt, ListTypeM.NEEDS, "Appreciation");
		//createStartupItem(iCt, ListTypeM.NEEDS, "Authenticity");
		createStartupItem(iCt, ListTypeM.NEEDS, "Community/Connection");
		createStartupItem(iCt, ListTypeM.NEEDS, "Consideration/Care"); //Care, cmp Love, cmp Trust, cmp Support
		createStartupItem(iCt, ListTypeM.NEEDS, "Contribution/Meaning"); //Meaning, to matter
		//createStartupItem(iCt, ListTypeM.NEEDS, "Creativity");
		createStartupItem(iCt, ListTypeM.NEEDS, "Emotional safety/Peace"); //Emotional safety
		createStartupItem(iCt, ListTypeM.NEEDS, "Empathy");
		createStartupItem(iCt, ListTypeM.NEEDS, "Exercise/Movement"); //Exercise, movement, Physical comfort
		createStartupItem(iCt, ListTypeM.NEEDS, "Freedom");
		createStartupItem(iCt, ListTypeM.NEEDS, "Inspiration/Creativity");
		//createStartupItem(iCt, ListTypeM.NEEDS, "Love");
		createStartupItem(iCt, ListTypeM.NEEDS, "Mourning");
		createStartupItem(iCt, ListTypeM.NEEDS, "Play/Fun"); //Play
		createStartupItem(iCt, ListTypeM.NEEDS, "Rest");
		//createStartupItem(iCt, ListTypeM.NEEDS, "Support");
		createStartupItem(iCt, ListTypeM.NEEDS, "Transcendence/Communion");
		createStartupItem(iCt, ListTypeM.NEEDS, "Trust");
		createStartupItem(iCt, ListTypeM.NEEDS, "Understanding"); //cmp Authenticity
		//TODO: Hint: Please remember that needs can be fulfilled by any person, including oneself
		//For example you can give support for yourself
		

		/*
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Awareness of a feeling in the body");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Calling a friend");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Enumerating good things");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Focusing on a need");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Breathing with the feeling");
		//tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Looking at a plant or a tree");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Seeing alternative ways to respond");
		ItemActionsU.addAction(iCt, tUri, "http://www.youtube.com/user/baynvc/", false);
		ItemActionsU.addAction(iCt, tUri, "http://www.youtube.com/user/MettaCenter/", false);
		ItemActionsU.addAction(iCt, tUri, "http://www.youtube.com/channel/UCH6fuu1ChJNrs7ecSDdR8QQ", false);
		ItemActionsU.addAction(iCt, tUri, "http://yourskillfulmeans.com/", false);
		ItemActionsU.addAction(iCt, tUri, "https://sites.google.com/site/mindfulnessandhealing/home", false);
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Seeing the bigger perspective");
		ItemActionsU.addAction(iCt, tUri, "http://apod.nasa.gov/apod/", false);
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Stretching");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Thinking about a time when you helped someone");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Thinking about someone who shares your experience");
		tUri = createStartupItem(iCt, ListTypeM.KINDNESS, "Thinking about an inspiring person");
		ItemActionsU.addAction(iCt, tUri, "http://dalailama.com/gallery", false);
		*/
		
		//ItemActionsU.addAction(iCt, tUri, "");
		/*Search: "thich nhat hanh breathing difficult emotions cooking"
Recognizing, Embracing, Relieving the Suffering of Anger

The first function of mindfulness is to recognize, not to fight. "Breathing in, I know that anger has manifested in me. Hello, my little anger." And breathing out, "I will take good care of you."

Once we have recognized our anger, we embrace it. This is the second function of mindfulness and it is a very pleasant practice. Instead of fighting, we are taking good care of our emotion. If you know how to embrace your anger, something will change.

It is like cooking potatoes. You cover the pot and then the water will begin to boil. You must keep the stove on for at least twenty minutes for the potatoes to cook. Your anger is a kind of potato and you cannot eat a raw potato.

Mindfulness is like the fire cooking the potatoes of anger. The first few minutes of recognizing and embracing your anger with tenderness can bring results. You get some relief. Anger is still there, but you do not suffer so much anymore, because you know how to take care of your baby. So the third function of mindfulness is soothing, relieving. Anger is there, but it is being taken care of. The situation is no longer in chaos, with the crying baby left all alone. The mother is there to take care of the baby and the situation is under control.
		 */

	}
	private static Uri createStartupItem(Context iContext, int iListTypeInt, String iName){
		ContentValues tContentValsToIns = new ContentValues();

		//Using the name to see if the item has already been added
		String tName = "";
		Cursor tItemCr = iContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, null);
		try{
			if(tItemCr != null){

				for(tItemCr.moveToFirst(); tItemCr.isAfterLast() == false; tItemCr.moveToNext()){
					tName = tItemCr.getString(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_NAME));
					if(iName.compareTo(tName) == 0){
						
						//Item with same name already exists, returning the item that already has been created
						//(This enables adding new actions to old items)
						Uri tUri = DatabaseU.getItemUriFromId(tItemCr.getLong(
								tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID)));
						return tUri;
						
					}
				}

			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor null", new Exception());
			}
		}catch(Exception e){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Exception when using cursor", e);
		}finally{
			if(tItemCr != null){
				tItemCr.close();
			}else{
				Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor null when trying to close");
			}
		}

		tContentValsToIns.put(ItemTableM.COLUMN_LIST_TYPE, iListTypeInt);
		tContentValsToIns.put(ItemTableM.COLUMN_NAME, iName);
		/*
		tContentValsToIns.put(ItemTableM.COLUMN_ACTIONS, "http://apod.nasa.gov/apod/astropix.html"
				+ ItemActionsU.ACTIONS_SEPARATOR);
		*/

		Uri rNewItemUri = iContext.getContentResolver().insert(
				ContentProviderM.ITEM_CONTENT_URI, tContentValsToIns);
		
		return rNewItemUri;
	}
	
	///@}
	
}