package com.sunyata.kindmind.Database;

import com.sunyata.kindmind.util.DatabaseU;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Overview: DatabaseHelperM is a helper class that is used for database creation and upgrade between versions
 * Extends: SQLiteOpenHelper
 * Used in: ContentProviderM.onCreate, (Android OS ?)
 * Notes: 
 * Improvements: 
 * Documentation: 
 *  https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
 */
public class DatabaseHelperM extends SQLiteOpenHelper{

	//-------------------Constants

	public static final String DATABASE_NAME = "kindmind.db";
	private static final int DATABASE_VERSION = 57;
	//-PLEASE BE CAREFUL WHEN UPDATING THIS and add changes to the three onUpgrade methods
	private static DatabaseHelperM sDatabaseHelper;
	private static Context sContext = null; //-Used backup in onUpgrade
	
	
	//-------------------Constructor, singleton get, and onCreate
	
	public static DatabaseHelperM get(Context inContext){
		sContext = inContext;
		if (sDatabaseHelper == null){
			sDatabaseHelper = new DatabaseHelperM(inContext.getApplicationContext());
		}
		return sDatabaseHelper;
	}
	
	private DatabaseHelperM(Context inContext) {
		super(inContext, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase inDatabase) {
		ItemTableM.createTable(inDatabase);
		PatternsTableM.createTable(inDatabase);
	}
	
	
	//-------------------onUpgrade

	/*
	 * Overview: onUpgrade backs up the database and then upgrades each of the tables.
	 * Notes: 1. SQLite is limited in its ALTER TABLE features compared to SQL, SQLite does not support "REMOVE COLUMN"
	 * or "ALTER COLUMN".
	 * 2. This class does not block the startup of the application, therefore we cannot trust that changes to
	 *  the shared preferences file will be written before MainActivityC.onCreate()
	 * Improvements: Here are places where onUpgrade is discussed:
	 *  http://stackoverflow.com/questions/3505900/sqliteopenhelper-onupgrade-confusion-android
	 *  "Enterprise Android" may have something on this
	 *  http://codeblow.com/questions/sqliteopenhelper-onupgrade-confusion-android/
	 *  **http://stackoverflow.com/questions/13554959/replace-sql-database-but-keep-old-records**
	 */
	@Override
	public void onUpgrade(SQLiteDatabase inDatabase, int inOldVersion, int inNewVersion) {
		//Making a backup of the previous version of the database file
		DatabaseU.databaseBackupInternal(sContext, DATABASE_NAME, inOldVersion);
		
		//Upgrading for all the tables
		if(inOldVersion == 46 && inNewVersion == 47){
			ItemTableM.upgradeTable(inDatabase, inOldVersion, inNewVersion);
			
		}else{
	    	ItemTableM.upgradeTable(inDatabase, inOldVersion, inNewVersion);
			PatternsTableM.upgradeTable(inDatabase, inOldVersion, inNewVersion);
		}
	}
}
