package com.sunyata.kindmind.List;

import com.sunyata.kindmind.util.DbgU;

import android.util.Log;
/*
 * Overview: 
 * 
 * Details: 
 * 
 * Extends: 
 * 
 * Implements: 
 * 
 * Sections:
 * 
 * Used in: 
 * 
 * Uses app internal: 
 * 
 * Uses Android lib: 
 * 
 * In: 
 * 
 * Out: 
 * 
 * Does: 
 * 
 * Shows user: 
 * 
 * Notes: 
 * 
 * Improvements: 
 * 
 * Documentation: 
 * 
 */
public class ListTypeM {
	public static final int NOT_SET = -1;
	public static final int FEELINGS = 0;
	public static final int NEEDS = 1;
	public static final int KINDNESS = 2;
	//-these constants are also used for the tabs and viewpager positions
	
	public static final int NUMBER_OF_TYPES = 3;
	
	public static String getListTypeString(int inListType){
		switch(inListType){
		case FEELINGS: return "Feelings";
		case NEEDS: return "Needs";
		case KINDNESS: return "Kindness";
		case NOT_SET:
		default:
			Log.wtf(DbgU.getAppTag(), "Error in getListTypeString: Case not Covered or value has not been set");
			return "";
		}
	}
}
