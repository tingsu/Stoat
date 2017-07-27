package com.sunyata.kindmind.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sunyata.kindmind.Database.ContentProviderM;
import com.sunyata.kindmind.Database.ItemTableM;

public class ItemActionsU {

	
	public static final String ACTIONS_DELINEATOR = ";";

	
	public static ArrayList<String> actionsStringToArrayList(String inActions){
		ArrayList<String> retArrayList = new ArrayList<String>(Arrays.asList(
				inActions.split(ACTIONS_DELINEATOR)));
		
		//Removing any empty strings or nulls
		retArrayList.remove("");
		retArrayList.remove(null);

		return retArrayList;
	}
	
	private static String arrayListToActionsString(ArrayList<String> iArrayList){
		String rString = ACTIONS_DELINEATOR; //-adding the delineator before the first element
		
		for(String action : iArrayList){
			rString = rString + action + ACTIONS_DELINEATOR;
		}
		
		return rString;
	}
	
	public static String removeAction(String inActions, String inActionToRemove){
		//Split the string into several parts
		ArrayList<String> tmpStringArray = actionsStringToArrayList(inActions);
		
		boolean tSuccess = tmpStringArray.remove(inActionToRemove);
		if(!tSuccess){
			Log.w(DbgU.getAppTag(), DbgU.getMethodName() + "Could not find action to remove in list");
		}
		
		return arrayListToActionsString(tmpStringArray);
		
		/*
		String[] tmpStringArray = actionsStringToArrayList(inActions);
		
		boolean tmpOneItemHasBeenRemoved = false;
		
		//Rebuild the string..
		for(int i=0; i<tmpStringArray.length; i++){
			if(tmpStringArray[i].equals(inActionToRemove) && tmpOneItemHasBeenRemoved == false){
				//..but remove the first match
				tmpOneItemHasBeenRemoved = true;
			}else{
				//..but add all other parts
				if(retString.equals("")){
					retString = tmpStringArray[i];
				}else{
					retString = retString + ACTIONS_SEPARATOR + tmpStringArray[i];
				}
			}
		}
				return retString;
		*/
	}

	public static int numberOfActions(String inActions) {
		
		return actionsStringToArrayList(inActions).size();
		
		/*
		if(inActions == ACTIONS_DELINEATOR){
			return 0;
		}
		int retInt = 1;
		for(int i=0; i < inActions.length(); i++){
			if(ACTIONS_DELINEATOR.charAt(0) == inActions.charAt(i)){
				retInt++;
			}
		}
		return retInt;
		*/
	}
	

	public static void addAction(Context iContext, Uri iItemUri, String iFilePathToAdd,
			boolean iAddIfAlreadyExists) {
		if(iItemUri == null){
			Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " iItemUri is null");
			return;
		}
		if(iFilePathToAdd == ""){
			Log.w(DbgU.getAppTag(), DbgU.getMethodName() + " tmpFilePath is empty");
			return;
		}

		//Reading the current string
		String[] tmpProjection = {ItemTableM.COLUMN_ACTIONS};
		Cursor tmpItemCur = iContext.getContentResolver().query(
				iItemUri, tmpProjection, null, null, null);
		if(!tmpItemCur.moveToFirst()){
			tmpItemCur.close();
			return;
		}
		String tmpModActions = tmpItemCur.getString(tmpItemCur.getColumnIndexOrThrow(
				ItemTableM.COLUMN_ACTIONS));
		tmpItemCur.close();

		//Verifying that the string to be added does not contain the delineator
		if(iFilePathToAdd.contains(ACTIONS_DELINEATOR)){
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() +
					" String contains separator character, exiting method");
			return;
		}
		
		if(iAddIfAlreadyExists == false){
			//If action already is present, exiting
			if(actionsStringToArrayList(tmpModActions).contains(iFilePathToAdd)){
				return;
			}
		}
		
		if(tmpModActions == null || tmpModActions.equals("")){
			tmpModActions = ACTIONS_DELINEATOR + iFilePathToAdd + ACTIONS_DELINEATOR;
		}else{
			//Updating the string with the appended file path
			tmpModActions = tmpModActions + iFilePathToAdd + ACTIONS_DELINEATOR;
		}
		
		//Writing the updated string to the database
		ContentValues tmpContentValues = new ContentValues();
		tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, tmpModActions);
		iContext.getContentResolver().update(iItemUri, tmpContentValues, null, null);
	}
	


	public static void removeActionsWithBrokenUriFilePaths(Context iContext){
		
		Cursor tItemCr = iContext.getContentResolver().query(
				ContentProviderM.ITEM_CONTENT_URI, null, null, null, null);
		try{
			if(tItemCr != null && tItemCr.moveToFirst()){

				String tActions = tItemCr.getString(tItemCr.getColumnIndexOrThrow(
						ItemTableM.COLUMN_ACTIONS));
				
				//Iterating through all the actions to find any broken file paths..
				ArrayList<String> tActionsList = actionsStringToArrayList(tActions);
				for(String action : tActionsList){
					if(isUriFile(action) && !(new File(action).exists())){
						//..removing the action
						tActionsList.remove(action);
						
						//..notifying the user and the developer
						String tMsg = action + " could not be accessed and the assoiated action was removed";
						Log.i(DbgU.getAppTag(), DbgU.getMethodName() + tMsg);
						Toast.makeText(iContext, tMsg, Toast.LENGTH_LONG).show();
					}
				}
				
				//Writing the new actions string to the database
				long tId = tItemCr.getLong(tItemCr.getColumnIndexOrThrow(ItemTableM.COLUMN_ID));
				ContentValues tmpContentValues = new ContentValues();
				tmpContentValues.put(ItemTableM.COLUMN_ACTIONS, arrayListToActionsString(tActionsList));
				iContext.getContentResolver().update(
						DatabaseU.getItemUriFromId(tId), tmpContentValues, null, null);
				
			}else{
				Log.wtf(DbgU.getAppTag(), DbgU.getMethodName() + " Cursor null or empty", new Exception());
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
	}
	
	private static boolean isUriFile(String iUriAsString){
		Uri iUri = Uri.parse(iUriAsString);
		if(iUri.getPath().startsWith("file://")){
			return true;
		}
		return false;
	}
}
