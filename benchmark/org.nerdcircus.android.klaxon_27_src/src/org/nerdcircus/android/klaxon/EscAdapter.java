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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.nerdcircus.android.klaxon.Pager;

import android.util.Log;

public class EscAdapter extends ResourceCursorAdapter
{
    private String TAG = "EscAdapter";

    public EscAdapter(Context context, int layout, Cursor c){
        super(context, layout, c);
    }

    public void bindView(View view, Context context, Cursor cursor){
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView subject = (TextView) view.findViewById(R.id.subject);
        //subject.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        subject.setText(cursor.getString(cursor.getColumnIndex(Pager.Pages.SUBJECT)));
        int status = cursor.getInt(cursor.getColumnIndex(Pager.Pages.ACK_STATUS));
        icon.setImageResource(Pager.getStatusResId(status));
    }

    

}

