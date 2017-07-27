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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.content.SharedPreferences;
import android.content.ContentValues;

import org.nerdcircus.android.klaxon.Pager;
import org.nerdcircus.android.klaxon.Pager.Replies;
import org.nerdcircus.android.klaxon.AckStatusAdapter;

import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ReplyEditor extends Activity
{
    private String TAG = "ReplyEditor";

    private Uri mContentURI;
    private Cursor mCursor;
    private EditText mSubjectView;
    private EditText mBodyView;
    private ImageView mIconView;
    private CheckBox mCheckBox;
    private Spinner mAckStatusSpinner;

    private ArrayList mAckStatusList = new ArrayList();

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.replyeditor);


        /* stash our statuses in the ack status list*/
        mAckStatusList.add(new Integer(0));
        mAckStatusList.add(new Integer(1));
        mAckStatusList.add(new Integer(2));

        mSubjectView = (EditText) findViewById(R.id.subject);
        //mSubjectView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        //mSubjectView.setTextSize((float)(mSubjectView.getTextSize() * 1.25));

        mBodyView = (EditText) findViewById(R.id.body);
        mIconView = (ImageView) findViewById(R.id.icon);
        mCheckBox = (CheckBox) findViewById(R.id.show_in_menu);
        mAckStatusSpinner = (Spinner) findViewById(R.id.ack_status_spinner);
        mAckStatusSpinner.setAdapter(new AckStatusAdapter(this, R.layout.ackstatusspinner, mAckStatusList));

        Intent i = getIntent();
        mContentURI = i.getData();
        
        /* add onclick listeners for cancel and delete. */
        Button button = (Button) findViewById(R.id.delete_button);
        if(i.getAction().equals(Intent.ACTION_EDIT)){
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    doDelete();
                    finish();
                }
            });
        }
        else { button.setVisibility(View.GONE); } //dont show the button if not editing.

        button = (Button) findViewById(R.id.cancel_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        button = (Button) findViewById(R.id.save_button);
        if(i.getAction().equals(Intent.ACTION_EDIT)){
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    doSave();
                    finish();
                }
            });
        }
        else { 
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    doInsert();
                    finish();
                }
            });
        }

        if(i.getAction().equals(Intent.ACTION_EDIT)){
            Log.d(TAG, "displaying: "+mContentURI.toString());
            mCursor = managedQuery(mContentURI,  
                        new String[] {Replies._ID, Replies.NAME, Replies.BODY, Replies.ACK_STATUS, Replies.SHOW_IN_MENU},
                        null, null, null);
            mCursor.moveToFirst();

            mSubjectView.setText(mCursor.getString(mCursor.getColumnIndex(Replies.NAME)));
            mBodyView.setText(mCursor.getString(mCursor.getColumnIndex(Replies.BODY)));
            if( mCursor.getShort(mCursor.getColumnIndex(Replies.SHOW_IN_MENU)) == 1 ){
                mCheckBox.setChecked(true);
            }
            else{
                mCheckBox.setChecked(false);
            }

            int status = mCursor.getInt(mCursor.getColumnIndex(Replies.ACK_STATUS));
            mAckStatusSpinner.setSelection(status);
            mIconView.setImageResource(Pager.getStatusResId(status));

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //TODO: add a "save" and "discard" menu.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return true;
    }

    /* this function actually does the saving of various bits..*/
    private void doInsert(){
        ContentValues cv = new ContentValues();
        cv.put(Replies.NAME, mSubjectView.getText().toString());
        cv.put(Replies.BODY, mBodyView.getText().toString());
        cv.put(Replies.ACK_STATUS, mAckStatusSpinner.getSelectedItemPosition());
        if(mCheckBox.isChecked()){
            cv.put(Replies.SHOW_IN_MENU, 1);
        }
        else {
            cv.put(Replies.SHOW_IN_MENU, 0);
        }
        this.getContentResolver().insert(Replies.CONTENT_URI, cv);
    }
    private void doSave(){
        ContentValues cv = new ContentValues();
        cv.put(Replies.NAME, mSubjectView.getText().toString());
        cv.put(Replies.BODY, mBodyView.getText().toString());
        cv.put(Replies.ACK_STATUS, mAckStatusSpinner.getSelectedItemPosition());
        if(mCheckBox.isChecked()){
            cv.put(Replies.SHOW_IN_MENU, 1);
        }
        else {
            cv.put(Replies.SHOW_IN_MENU, 0);
        }
        this.getContentResolver().update(mContentURI, cv, null, null);
    }
    private void doDelete(){
        //FIXME: ensure that the content uri is a specific item.
        this.getContentResolver().delete(mContentURI, null, null);
    }

}

