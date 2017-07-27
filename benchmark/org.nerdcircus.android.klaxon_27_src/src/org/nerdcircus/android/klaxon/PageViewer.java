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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.SharedPreferences;

import org.nerdcircus.android.klaxon.Pager;
import org.nerdcircus.android.klaxon.Pager.Replies;

import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

public class PageViewer extends Activity
{
    private String TAG = "PageViewer";
    private static int REQUEST_PICK_REPLY = 1;

    private SharedPreferences mResponses;

    private Uri mContentURI;
    private Cursor mCursor;
    private TextView mSubjectView;
    private TextView mBodyView;
    private TextView mDateView;
    private TextView mSenderView;
    private ImageView mIconView;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        mResponses = getSharedPreferences("responses", 0);

        setContentView(R.layout.escview);

        mSubjectView = (TextView) findViewById(R.id.view_subject);
        //mSubjectView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        //mSubjectView.setTextSize((float)(mSubjectView.getTextSize() * 1.25));

        mBodyView = (TextView) findViewById(R.id.view_body);
        mIconView = (ImageView) findViewById(R.id.view_icon);
        mDateView = (TextView) findViewById(R.id.datestamp);
        mSenderView = (TextView) findViewById(R.id.sender);

        Intent i = getIntent();
        mContentURI = i.getData();
        Log.d(TAG, "displaying: "+mContentURI.toString());
        mCursor = managedQuery(mContentURI,  
                    new String[] {Pager.Pages._ID, Pager.Pages.SUBJECT, Pager.Pages.BODY, Pager.Pages.ACK_STATUS, Pager.Pages.CREATED_DATE, Pager.Pages.SENDER},
                    null, null, null);
        
        mCursor.moveToNext();

        mSubjectView.setText(mCursor.getString(mCursor.getColumnIndex(Pager.Pages.SUBJECT)));
        mBodyView.setText(mCursor.getString(mCursor.getColumnIndex(Pager.Pages.BODY)));
        //make a pretty date stamp.
        Date d = new Date(mCursor.getLong(mCursor.getColumnIndex(Pager.Pages.CREATED_DATE)));
        SimpleDateFormat df = new SimpleDateFormat();
        //FIXME: use a resource for this..
        mDateView.setText("Received: " + df.format(d));
        mSenderView.setText("Sender: "+ mCursor.getString(mCursor.getColumnIndex(Pager.Pages.SENDER)));

        int status = mCursor.getInt(mCursor.getColumnIndex(Pager.Pages.ACK_STATUS));
        mIconView.setImageResource(Pager.getStatusResId(status));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        Cursor c = managedQuery(Replies.CONTENT_URI,  
                    new String[] {Replies._ID, Replies.NAME, Replies.BODY, Replies.ACK_STATUS},
                    "show_in_menu == 1", null, null);
        c.moveToFirst();
        while ( ! c.isAfterLast() ){
            addReplyMenuItem(menu,
                             c.getString(c.getColumnIndex(Replies.NAME)),
                             c.getString(c.getColumnIndex(Replies.BODY)),
                             c.getInt(c.getColumnIndex(Replies.ACK_STATUS))
                             );
            c.moveToNext();
        }
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.other);
        //make delete be last
        MenuItem delete_item = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.delete);

        return true;
    }

    private Menu addReplyMenuItem(Menu menu, String label, final String response, final int status){
        //NOTE: these cannot be done with MenuItem.setIntent(), because those
        //intents are called with Context.startActivity()
        menu.add(Menu.NONE, 0, 0, label).setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener(){
                public boolean onMenuItemClick(MenuItem item){
                    Intent i = new Intent(Pager.REPLY_ACTION, mContentURI);
                    i.putExtra("response", response);
                    i.putExtra("new_ack_status", status);
                    Log.d(TAG, "ack status for "+response+"should be: "+status);
                    sendBroadcast(i);
                    return true;
                }
            }
        );
        return menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //hook called on item click.

        if( item.getTitle() == this.getString(R.string.delete) ){
            //this is the only one that's not a reply...
            Log.d(TAG, "Deleting row.");
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mContentURI, null, null);
            finish(); //finish the PageViewer if we've deleted our page.
            return true; //consume this menu click.
        }
        else if( item.getTitle() == this.getString(R.string.other)){
            //respond with some other response.
            Intent i = new Intent(Intent.ACTION_PICK, Replies.CONTENT_URI);
            i.setType("vnd.android.cursor.item/reply");
            startActivityForResult(i, REQUEST_PICK_REPLY);
            return true; //consume this menu click.
        }
        else {
            return false;
        }
    }

     protected void onActivityResult(int requestCode, int resultCode, Intent data){
         if( requestCode == REQUEST_PICK_REPLY ){
            if(resultCode == RESULT_OK){
                //send a reply.
                Cursor c = managedQuery(data.getData(),  
                            new String[] {Replies._ID, Replies.BODY, Replies.ACK_STATUS},
                            null, null, null);
                c.moveToFirst();
                Intent i = new Intent(Pager.REPLY_ACTION, mContentURI);
                i.putExtra("response", c.getString(c.getColumnIndex(Replies.BODY)));
                i.putExtra("new_ack_status", c.getInt(c.getColumnIndex(Replies.ACK_STATUS)));
                sendBroadcast(i);
                return;
            }
            else { return; }
         }
     }

}

