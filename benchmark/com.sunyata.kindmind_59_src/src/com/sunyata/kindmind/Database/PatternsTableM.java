package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.util.DbgU;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class PatternsTableM {

	public static final String TABLE_PATTERNS = "patterns";
	//-Plural here (and not for the list_item table) is intentional since one pattern will be made of several lines
	// in this table (but not all lines so we will have several patterns in this table)
	public static final String COLUMN_ID = BaseColumns._ID; //Could maybe remove this and use time as key instead
	public static final String COLUMN_CREATE_TIME = "create_time";
	//public static final String COLUMN_RELEVANCE = "relevance";
	public static final String COLUMN_ITEM_REFERENCE = "item_id";
	
	private static final String CREATE_DATABASE =
			"CREATE TABLE " + TABLE_PATTERNS + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_CREATE_TIME + " INTEGER NOT NULL, " //Potentially used for two things: Grouping and relevance
			+ COLUMN_ITEM_REFERENCE + " INTEGER REFERENCES " + ItemTableM.TABLE_ITEM + "(" + BaseColumns._ID + ")"
				+ " NOT NULL"
			+ ");";
	
	public static void createTable(SQLiteDatabase inDatabase) {
		inDatabase.execSQL(CREATE_DATABASE);
		Log.i(DbgU.getAppTag(), "Database version = " + inDatabase.getVersion());
	}

	public static void upgradeTable(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		
		Log.w(DbgU.getAppTag(), "Upgrade removed the database with a previous version" +
				" and created a new one, all previous data was deleted");
		
		inDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PATTERNS);
		createTable(inDatabase);
		
	}
}
