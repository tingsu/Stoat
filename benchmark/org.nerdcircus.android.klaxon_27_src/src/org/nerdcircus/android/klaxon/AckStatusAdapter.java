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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import org.nerdcircus.android.klaxon.Pager;

/* class to represent the various ack statuses to be show in a spinner.*/
public class AckStatusAdapter extends ArrayAdapter {

    private static int ICON_RESOURCE_ID = R.id.icon;


    public AckStatusAdapter(Context context, int textViewResourceId, List<Integer> items){
        super(context, textViewResourceId, items);
    }

    public ImageView getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = new ImageView(getContext());
        }
        ((ImageView)convertView).setImageResource(Pager.getStatusResId(position));
        return (ImageView)convertView;
    }

    public ImageView getDropDownView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = new ImageView(getContext());
        }
        ((ImageView)convertView).setImageResource(Pager.getStatusResId(position));
        return (ImageView)convertView;
    }

}
