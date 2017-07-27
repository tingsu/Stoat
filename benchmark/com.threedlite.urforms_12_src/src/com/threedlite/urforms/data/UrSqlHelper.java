package com.threedlite.urforms.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UrSqlHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "urforms.db";
	private static final int DATABASE_VERSION = 1;


	public UrSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		
		database.execSQL(EntityDao.TABLE_CREATE);
		database.execSQL(AttributeDao.TABLE_CREATE);
		database.execSQL(DataDao.TABLE_CREATE);
		database.execSQL(DataDao.INDEX_FOR_ONE_ROW_CREATE);
		database.execSQL(DataDao.INDEX_FOR_JOIN_CREATE);
		database.execSQL(DataDao.INDEX_FOR_NUM_CREATE);
		
		// version code 8+
		database.execSQL(BlobDataDao.TABLE_CREATE);
		database.execSQL(BlobDataDao.INDEX_GUID);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		
		if (oldVersion <= 7) {
			database.execSQL(BlobDataDao.TABLE_CREATE);
			database.execSQL(BlobDataDao.INDEX_GUID);
		}
		
	}

}
