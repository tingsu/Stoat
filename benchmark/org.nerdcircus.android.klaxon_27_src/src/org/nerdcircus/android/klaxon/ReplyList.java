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

package org.nerdcircus.android.klaxon;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.nerdcircus.android.klaxon.Pager.Replies;

import android.util.Log;

public class ReplyList extends ListActivity
{
    private String TAG = "ReplyList";

    //menu constants.
    private int MENU_ACTIONS_GROUP = Menu.FIRST;
    private int MENU_ALWAYS_GROUP = Menu.FIRST + 1;
    private int MENU_ADD = Menu.FIRST + 2;

    private Cursor mCursor;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        setContentView(R.layout.replylist);
        
        String[] cols = new String[] {Replies._ID, Replies.NAME, Replies.BODY, Replies.ACK_STATUS };
        mCursor = Pager.Replies.query(this.getContentResolver(), cols);
        startManagingCursor(mCursor);
        ListAdapter adapter = new ReplyAdapter(this, 
                                               R.layout.replylist_item,
                                               mCursor);
        setListAdapter(adapter);
    }

    public void onListItemClick(ListView parent, View v, int position, long id){
        Log.d(TAG, "Item clicked!");
        Uri uri = Uri.withAppendedPath(Pager.Replies.CONTENT_URI, ""+id);
        Log.d(TAG, "intent that started us: " + this.getIntent().getAction());
        if( this.getIntent().getAction().equals(Intent.ACTION_PICK) ){
            //we're picking responses, not editing them.
            Log.d(TAG, "pick action. returning result.");
            //Note: this reuses the sent Intent, so we dont lose the 'page_uri' data, if included.
            setResult(RESULT_OK, new Intent(this.getIntent()).setData(uri));
            finish();
        }
        else {
            Log.d(TAG, "not picking, edit.");
            Intent i = new Intent(Intent.ACTION_EDIT, uri);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final Context appcontext = (Context)this;
        MenuItem mi;
        mi = menu.add(MENU_ACTIONS_GROUP, MENU_ADD, Menu.NONE, R.string.add_reply);
        Intent i = new Intent(Intent.ACTION_INSERT,
                              Replies.CONTENT_URI);
        mi.setIntent(i);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.d(TAG, "preparing options menu");
        super.onPrepareOptionsMenu(menu);
        final boolean haveItems = mCursor.getCount() > 0;
        menu.setGroupVisible(MENU_ACTIONS_GROUP, haveItems);
        menu.setGroupVisible(MENU_ALWAYS_GROUP, true);
        return true;
    }

}

