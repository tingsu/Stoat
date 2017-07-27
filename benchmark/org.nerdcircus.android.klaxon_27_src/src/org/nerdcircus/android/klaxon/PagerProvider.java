/* 
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Content provider to store pages
 */

package org.nerdcircus.android.klaxon;

import android.content.ContentProvider;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteQueryBuilder;
import android.content.res.Resources;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.nerdcircus.android.klaxon.Pager;
import org.nerdcircus.android.klaxon.Pager.Pages;
import org.nerdcircus.android.klaxon.Pager.Replies;

import java.util.HashMap;
import java.util.Map;

public class PagerProvider extends ContentProvider {
    private DatabaseHelper mDbHelper;

    private static final String TAG = "PagerProvider";
    private static final String DATABASE_NAME = "pager.db";
    private static final int DATABASE_VERSION = 4;

    //URI parser return values.
    private static final int PAGES = 1;
    private static final int PAGE_ID = 2;
    private static final int REPLIES = 3;
    private static final int REPLY_ID = 4;

    private static final UriMatcher URL_MATCHER;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating 'pages' table from db helper");
            db.execSQL("CREATE TABLE pages (_id INTEGER PRIMARY KEY,"
                    + "subject TEXT," 
                    + "body TEXT," 
                    + "sc_addr TEXT,"
                    + "sender_addr TEXT,"
                    + "ack_status INTEGER,"
                    + "created INTEGER,"
                    + "email_from_addr TEXT,"
                    + "transport TEXT"
                    + ");");
            addReplyTable(db);
            addReplyShortlistField(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // v2 introduces the reply table.
            if (oldVersion < 2 && newVersion >= 2){
                addReplyTable(db);
            }
            // v5 includes the 'transport' column.
            if (oldVersion < 3 && newVersion >= 3){
                addTransportColumnToPagesTable(db);
            }
            if (oldVersion < 4 && newVersion >= 4){
                addReplyShortlistField(db);
            }
            //NOTE: any additions here, should also be reflected in the onCreate above.
            else {
                Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will do nothing.");
            }
        }

        ///////////////
        // Database Migration functions
        ///////////////

        /** create the Reply table in the database.
         * used in initial creation and/or db upgrades
         */
        private void addReplyTable(SQLiteDatabase db){
            Log.d(TAG, "Creating 'replies' table");
            db.execSQL("CREATE TABLE replies (_id INTEGER PRIMARY KEY, "
                    + "name TEXT, " 
                    + "body TEXT, " 
                    + "ack_status INTEGER" //indicates positive/negative reply.
                    + ");");
            //FIXME: use resources for these values.
            db.execSQL("insert into replies(name, body, ack_status)"
                    + "values(\"Yes\", \"Yes\", " + Pager.STATUS_ACK + ");");
            db.execSQL("insert into replies(name, body, ack_status)"
                    + "values(\"No\", \"No\", " + Pager.STATUS_NACK + ");");
        }

        private void addTransportColumnToPagesTable(SQLiteDatabase db){
            Log.d(TAG, "adding type column to pages table.");
            db.execSQL("ALTER TABLE pages ADD COLUMN transport TEXT;");
        }

        private void addReplyShortlistField(SQLiteDatabase db){
            Log.d(TAG, "adding shortlist column to reply table");
            //the cursor interface doesnt do booleans...
            db.execSQL("ALTER TABLE replies ADD COLUMN show_in_menu INT default 0;");
            ////FIXME: use resources for these values.
            db.execSQL("update replies set show_in_menu = 1 where name == \"Yes\";");
            db.execSQL("update replies set show_in_menu = 1 where name == \"No\";");
        }
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return (mDbHelper == null) ? false : true;
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (URL_MATCHER.match(url)) {
        case PAGES:
            qb.setTables("pages");
            break;
        case PAGE_ID:
            qb.setTables("pages");
            //append our page id
            qb.appendWhere("_id=" + url.getLastPathSegment());
            break;
        case REPLIES:
            qb.setTables("replies");
            break;
        case REPLY_ID:
            qb.setTables("replies");
            //append our page id
            qb.appendWhere("_id=" + url.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URL: " + url);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sort)) {
            orderBy = getDefaultSortOrderByType(URL_MATCHER.match(url));
        } else {
            orderBy = sort;
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sort);
        c.setNotificationUri(getContext().getContentResolver(), url);
        return c;
    }

    @Override
    public String getType(Uri url) {
        switch (URL_MATCHER.match(url)) {
        case PAGES:
            return "vnd.android.cursor.dir/pages";

        case PAGE_ID:
            return "vnd.android.cursor.item/pages";

        case REPLIES:
            return "vnd.android.cursor.dir/reply";

        case REPLY_ID:
            return "vnd.android.cursor.item/reply";

        default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }
    }

    /** insert function that can be reused by all types
     */
    private Uri insert(Uri base_uri, ContentValues values, String table){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowID = db.insert(table, null, values);
        Log.d(TAG, "Got row id: " + rowID );
        if (rowID > 0) {
            Uri uri = Uri.withAppendedPath(base_uri, ""+rowID);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        throw new SQLException("Failed to insert row into " + base_uri);
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        long rowID;
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        switch(URL_MATCHER.match(url)){
        case PAGES:
            return insert(url, validatePageValues(values), Pages.TABLE_NAME);
        case REPLIES:
            return insert(url, validateReplyValues(values), Replies.TABLE_NAME);
        default:
            throw new IllegalArgumentException("Invalid URL " + url);
        }

    }

    //helper to generate "final" where clause.
    public String whereClauseWithId(Uri u, String where){
        String id = null;
        switch( URL_MATCHER.match(u)){
            case PAGE_ID:
                id = u.getLastPathSegment();
            case REPLY_ID:
                id = u.getLastPathSegment();
        }
        if( id != null ){
            return "_id=" + id 
                          + (!TextUtils.isEmpty(where) ? " AND (" + where
                          + ')' : "");
        }
        else { return where; }
    }

    @Override
    public int delete(Uri url, String where, String[] deletionArgs) {
        int count;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (URL_MATCHER.match(url)) {
        case PAGES:
        case PAGE_ID:
            count = db.delete(Pages.TABLE_NAME, whereClauseWithId(url, where), deletionArgs);
            break;
        case REPLIES:
        case REPLY_ID:
            count = db.delete(Replies.TABLE_NAME, whereClauseWithId(url, where), deletionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] selectionArgs) {
    //XXX: type-specific differences: table name, where clause (if there's an ID specified)
        int count;
        long rowId = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (URL_MATCHER.match(url)) {
        case PAGES:
        case PAGE_ID:
            count = db.update(Pages.TABLE_NAME, values, whereClauseWithId(url, where), selectionArgs);
            break;
        case REPLIES:
        case REPLY_ID:
            count = db.update(Replies.TABLE_NAME, values, whereClauseWithId(url, where), selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URL " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    private String getDefaultSortOrderByType(int type){
        switch (type) {
        case PAGES:
            return Pages.DEFAULT_SORT_ORDER;
        case PAGE_ID:
            return Pages.DEFAULT_SORT_ORDER;
        case REPLIES:
            return Replies.DEFAULT_SORT_ORDER;
        case REPLY_ID:
            return Replies.DEFAULT_SORT_ORDER;
        default:
            return "";
        }

    }

    /** sanitize and set reasonable defaults for our new Page
     */
    private ContentValues validatePageValues(ContentValues values){

        if ( ! values.containsKey(Pages.FROM_ADDR)){
            values.put(Pages.FROM_ADDR, "");
        }
        if (! values.containsKey("created")){
            Long now = Long.valueOf(System.currentTimeMillis());
            values.put("created", now);
        }
        return values;
    }

    /** sanitize and set reasonable defaults for a new Reply.
     */
    private ContentValues validateReplyValues(ContentValues values){

        if ( ! values.containsKey("ack_status")){
            values.put("ack_status", Pager.STATUS_NONE);
        }

        return values;
    }

    static {
        //tell the content uri parser how to interpret our urls.
        URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URL_MATCHER.addURI("org.nerdcircus.android.klaxon", "pages", PAGES);
        URL_MATCHER.addURI("org.nerdcircus.android.klaxon", "pages/#", PAGE_ID);
        URL_MATCHER.addURI("org.nerdcircus.android.klaxon", "reply", REPLIES);
        URL_MATCHER.addURI("org.nerdcircus.android.klaxon", "reply/#", REPLY_ID);
    }


}
