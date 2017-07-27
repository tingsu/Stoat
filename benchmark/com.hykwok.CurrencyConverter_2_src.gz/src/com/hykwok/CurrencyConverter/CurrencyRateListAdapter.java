/*
	Copyright 2010-2012 Kwok Ho Yin

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

import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CurrencyRateListAdapter extends BaseAdapter {
	// This variable is used for debug log (LogCat)
	private static final String TAG = "CC:CurrencyRateListAdapter";

	private LayoutInflater mInflater;
	private Bitmap[] mIcon;
	private String[] mName;	
	private Cursor	 mRateData;
	private double   mRate[];
	private String   mDisplayrate[];
	
	private int   	 mBaseCurrencyPosition;
	
	public CurrencyRateListAdapter(Context context, Integer[] name, Integer[] bitmapID, Cursor rate_data) {
		mInflater = LayoutInflater.from(context);
		
		mIcon = new Bitmap[bitmapID.length];
		
		// load all bitmap and name
		for(int i=0; i<bitmapID.length; i++) {
			mIcon[i] = BitmapFactory.decodeResource(context.getResources(), bitmapID[i].intValue());
		}
		
		mName = new String[name.length];
		
		for(int j=0; j<name.length; j++) {
			mName[j] = context.getString(name[j]);
		}
				
		mRateData = rate_data;
		
		// update currency rate
		updateCurrencyRate();
				
		// set default currency
		mBaseCurrencyPosition = 0;
	}
	
	@Override
	public void finalize() {
		Log.d(TAG, "Close SQL cursor...");
		mRateData.close();
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
		
		//Log.d(TAG, ">>>>> getView: position=" + Integer.toString(position));
		
		try {
			if(convertView == null) {
				// uses currencyratelist.xml to display each currency selection
				convertView = mInflater.inflate(R.layout.currencyratelist, null);
				// then create a holder for this view for faster access
				holder = new ViewHolder();
				
				holder.icon = (ImageView) convertView.findViewById(R.id.ratelist_icon);
				holder.name = (TextView) convertView.findViewById(R.id.ratelist_text);
				holder.rate = (TextView) convertView.findViewById(R.id.ratelist_ratetext);
				
				// store this holder in the list
				convertView.setTag(holder);
			} else {
				// load the holder of this view
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.icon.setImageBitmap(mIcon[position]);
			holder.name.setText(mName[position]);
			holder.rate.setText(mDisplayrate[position]);
				
		} catch (Exception e) {
			Log.e(TAG, "getView:" + e.toString());
		}
		
		//Log.d(TAG, "<<<<< getView: position=" + Integer.toString(position));
		
		return convertView;
	}

	public void SetBaseCurrencyIndex(int value) {
		mBaseCurrencyPosition = value;
		
		// update display rate
		double	rate_base = 1.0;
		
		if(mBaseCurrencyPosition < mRate.length) {
			rate_base = mRate[mBaseCurrencyPosition];
		}
		
		mDisplayrate = new String[mRateData.getCount()];
		
		for(int i=0; i<mRateData.getCount(); i++) {
			mDisplayrate[i] = String.format(Locale.US, "%.3f", mRate[i] / rate_base);
		}
	}
	
	public String getDisplayString(int position) {
		String result = "1.000";
		
		if(position < mRate.length) {
			result = mDisplayrate[position];
		}
		
		return result;
	}
	
	public void updateCurrencyRate() {
		Log.d(TAG, ">>>>> updateCurrencyRate");
				
		// update currency rate data
		mRateData.requery();
		
		mRate = new double[mRateData.getCount()];
		
		int cnt = mRateData.getCount();
		int colcnt = mRateData.getColumnCount();
		
		for(int i=0; i<cnt; i++) {
			if(mRateData.moveToPosition(i) == true) {
				if(colcnt == 1) {
					// only currency rate data in the query result set
					mRate[i] = mRateData.getDouble(0);
				} else {
					// all data in the query result set
					// So the rate data in the 2nd column (refer to CurrencyConverterDB class
					mRate[i] = mRateData.getDouble(1);
				}
			} else {
				mRate[i] = 1.0;
			}
		}
		
		// deactive currency rate data
		mRateData.deactivate();
		
		Log.d(TAG, "<<<<< updateCurrencyRate");
	}	
	
	public double getCurrencyRate(int position) {
		double rate_sel = 1.0;
		
		if(position < mRate.length) {
			rate_sel = mRate[position];
		}
		
		return rate_sel;
	}
	
	/* class ViewHolder */
	private class ViewHolder {
		ImageView	icon;
		TextView	name;
		TextView	rate;
	}
}
