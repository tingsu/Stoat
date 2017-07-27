/*
	Copyright 2010 Kwok Ho Yin

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

    	http://www.apache.org/licenses/LICENSE-2.0

   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.
*/

package com.hykwok.CurrencyConverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CurrencyListAdapter extends BaseAdapter {
	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:CurrencyListAdapter";

	private LayoutInflater mInflater;
	private Bitmap[] mIcon;
	private String[] mName;	
	
	public CurrencyListAdapter(Context context, String[] name, Integer[] bitmapID) {
		mInflater = LayoutInflater.from(context);
		
		mIcon = new Bitmap[bitmapID.length];
		
		// load all bitmap and name
		for(int i=0; i<bitmapID.length; i++) {
			mIcon[i] = BitmapFactory.decodeResource(context.getResources(), bitmapID[i].intValue());
		}
		
		mName = name.clone();
	}
		
	public int getCount() {
		return mIcon.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder	holder;
		
		try {
			if(convertView == null) {
				// uses currencylist.xml to display each currency selection
				convertView = mInflater.inflate(R.layout.currencylist, null);
				// then create a holder for this view for faster access
				holder = new ViewHolder();
				
				holder.icon = (ImageView) convertView.findViewById(R.id.list_icon);
				holder.name = (TextView) convertView.findViewById(R.id.list_text);
				
				// store this holder in the list
				convertView.setTag(holder);
			} else {
				// load the holder of this view
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.icon.setImageBitmap(mIcon[position]);
			holder.name.setText(mName[position]);
		} catch (Exception e) {
			Log.e(TAG, "getView:" + e.toString());
		}
		
		return convertView;
	}

	/* class ViewHolder */
	private class ViewHolder {
		ImageView	icon;
		TextView	name;
	}
}
