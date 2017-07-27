package com.frankcalise.h2droid;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EntryListAdapter extends BaseAdapter {
	
	private List<Entry> mEntryList;
	private final LayoutInflater mInflater;
	private boolean mIsDetail;
	private boolean mLargeUnits;
	private int mUnitSystem;
	
	public EntryListAdapter(List<Entry> _entryList, Context _context, boolean _isDetail) {
		this.mEntryList = _entryList;
		this.mIsDetail = _isDetail;
		
		this.mUnitSystem = Settings.getUnitSystem(_context);
		this.mLargeUnits = Settings.getLargeUnitsSetting(_context);
		
		mInflater = LayoutInflater.from(_context);
	}

	@Override
	public int getCount() {
		return mEntryList.size();
	}

	@Override
	public Entry getItem(int position) {
		return mEntryList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entry e = mEntryList.get(position);
		
		// Check to see if convertView contains a usable item
		// If null, inflate a new row item
		if (convertView == null) {
			if (mIsDetail == false) {
				convertView = mInflater.inflate(R.layout.entry_list_item, parent, false);
			} else {
				convertView = mInflater.inflate(R.layout.entry_detail_list_item, parent, false);
			}
		}
		
		double displayAmount = e.getNonMetricAmount();
		String displayUnits = "fl oz";
		if (mUnitSystem == Settings.UNITS_METRIC) {
			displayAmount = e.getMetricAmount();
			displayUnits = "ml";
		}
		
		
		if (mLargeUnits) {
			Amount currentAmount = new Amount(displayAmount, mUnitSystem);
    		displayAmount = currentAmount.getAmount();
    		displayUnits = currentAmount.getUnits();
		}
		
		if (mIsDetail == false) {
			// Set date
			((TextView)convertView.findViewById(R.id.entry_date_textview)).setText(e.getDateWithFormat("EEEE, MMMM dd, yyyy"));
			
			// Set amount
			((TextView)convertView.findViewById(R.id.entry_amount_textview)).setText(String.format("  %.1f %s", displayAmount, displayUnits));
		} else {
			// Set date
			((TextView)convertView.findViewById(R.id.entry_date_textview)).setText(e.getDateWithFormat("hh:mm:ss a"));
			
			// Set amount
			((TextView)convertView.findViewById(R.id.entry_amount_textview)).setText(String.format("%.1f %s", displayAmount, displayUnits));
		}
		
		return convertView;
	}
}
