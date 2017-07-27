package com.frankcalise.h2droid;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class WaterProvider extends ContentProvider {
	public static final Uri CONTENT_URI = 
		Uri.parse("content://com.frankcalise.provider.h2droid/entries");
	
	private SQLiteDatabase waterDB;
	
	private static final String TAG = "WaterProvider";
	private static final String DATABASE_NAME = "water.db";
	private static final int DATABASE_VERSION = 1;
	private static final String ENTRIES_TABLE = "entries";
	
	// Column names
	public static final String KEY_ID = "_id";
	public static final String KEY_DATE = "date";
	public static final String KEY_AMOUNT = "amount";
	public static final String KEY_UNITS = "units";
	
	// Column indexes
	public static final int ID_COLUMN = 0;
	public static final int DATE_COLUMN = 1;
	public static final int AMOUNT_COLUMN = 2;
	public static final int UNITS_COLUMN = 3;
	
	// Create the constants used to differentiate between
	// the different URI requests
	private static final int ENTRIES = 1;
	private static final int ENTRY_ID = 2;
	private static final int ENTRIES_GROUP_DATE = 3;
	private static final int ENTRIES_LATEST = 4;
	
	private static final UriMatcher uriMatcher;
	
	// Allocate the UriMatcher object, where a URI ending in 'entries'
	// will correspond to a request for all entries, and 'entries'
	// with a trailing '/[rowID]' will represent a single row
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.frankcalise.provider.h2droid", "entries", ENTRIES);
		uriMatcher.addURI("com.frankcalise.provider.h2droid", "entries/#", ENTRY_ID);
		uriMatcher.addURI("com.frankcalise.provider.h2droid", "entries/group_date", ENTRIES_GROUP_DATE);
		uriMatcher.addURI("com.frankcalise.provider.h2droid", "entries/latest", ENTRIES_LATEST);
	}
	
	/** Helper class for opening, creating, and managing
	 * database version control
	 */
	private static class waterDatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE = 
			"CREATE TABLE " + ENTRIES_TABLE + " ("
			+ KEY_ID + " integer primary key autoincrement, "
			+ KEY_DATE + " TEXT, "
			+ KEY_AMOUNT + " FLOAT, "
			+ KEY_UNITS + " TEXT);";
		
		public waterDatabaseHelper(Context context, String name,
								   CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,
							  int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					    + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + ENTRIES_TABLE);
			onCreate(db);
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		
		switch (uriMatcher.match(uri)) {
			case ENTRIES:
				count = waterDB.delete(ENTRIES_TABLE, selection, selectionArgs);
				break;
			case ENTRY_ID:
				String segment = uri.getPathSegments().get(1);
				count = waterDB.delete(ENTRIES_TABLE, KEY_ID + "="
									   + segment 
									   + (!TextUtils.isEmpty(selection) ? " AND ("
									   + selection + ')' : ""), selectionArgs);
				break;
				
			default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/** Return a String for each URI supported */
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case ENTRIES:
				return "vnd.android.cursor.dir/vnd.frankcalise.h2droid";
			case ENTRY_ID:
				return "vnd.android.cursor.item/vnd.frankcalise.h2droid";
			case ENTRIES_GROUP_DATE:
				return "vnd.android.cursor.dir.date/vnd.frankcalise.h2droid";
			case ENTRIES_LATEST:
				return "vnd.android.cursor.item.latest/vnd.frankcalise.h2droid";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri _uri, ContentValues _values) {
		// Insert the new row, will return row
		// number if successful
		long rowID = waterDB.insert(ENTRIES_TABLE, "entry", _values);
		
		// Return a URI to the newly inserted row on success
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		throw new SQLException("Failed to insert row into " + _uri);
	}

	/** Create a new instance of the database helper
	 *  and open connection to the database.
	 */
	@Override
	public boolean onCreate() {
		Context context = getContext();
		
		waterDatabaseHelper dbHelper;
		dbHelper = new waterDatabaseHelper(context, DATABASE_NAME, null,
										   DATABASE_VERSION);
		waterDB = dbHelper.getWritableDatabase();
		return (waterDB == null) ? false : true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		String groupBy = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		qb.setTables(ENTRIES_TABLE);
		
		// If this is a row query, limit result set to 
		// passed in row
		switch (uriMatcher.match(uri)) {
			case ENTRY_ID:
				qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
				break;
			case ENTRIES_GROUP_DATE:
				return waterDB.rawQuery("SELECT _id, date, SUM(amount) as amount FROM entries GROUP BY date(date) ORDER BY date DESC", null);
			case ENTRIES_LATEST:
				return waterDB.rawQuery("SELECT _id, date FROM entries ORDER BY date DESC LIMIT 1", null);
			default: break;
		}
		
		// If no sort order is specified, sort by date / time
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_DATE;
		} else {
			orderBy = sortOrder;
		}
		
		Log.d("CONTENTPROVIDER", qb.toString());
		
		// Apply the query to the underlying database
		Cursor c = qb.query(waterDB,
							projection,
							selection, selectionArgs,
							groupBy, null,
							orderBy);
		
		// Register the contexts ContentResolver to be 
		// notified if the cursor result set changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		// Return a cursor to the query result
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		
		switch (uriMatcher.match(uri)) {
			case ENTRIES:
				count = waterDB.update(ENTRIES_TABLE, values, selection, selectionArgs);
				break;
			case ENTRY_ID:
				String segment = uri.getPathSegments().get(1);
				count = waterDB.update(ENTRIES_TABLE, values, KEY_ID
									  + "=" + segment
									  + (!TextUtils.isEmpty(selection) ? " AND ("
									  + selection + ')' : ""), selectionArgs);
				break;
				default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
