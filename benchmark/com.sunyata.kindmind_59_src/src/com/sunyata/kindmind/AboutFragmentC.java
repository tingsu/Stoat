package com.sunyata.kindmind;

import com.sunyata.kindmind.util.DbgU;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragmentC extends Fragment {
	
	private TextView mVersionTextView;
	//private TextView mShareTextView;
	
	public static Fragment newInstance(){
		Fragment retFragment = new AboutFragmentC(); //-"Implicit" constructor used
		return retFragment;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
    	Log.d(DbgU.getAppTag(), DbgU.getMethodName());

    	//Inflating the layout
    	View v = inflater.inflate(R.layout.fragment_about, parent, false);
    	
    	//Using the app icon and left caret for hierarchical navigation
    	if(NavUtils.getParentActivityName(getActivity()) != null){
    		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    	}
    	
    	//Printing the version of the app
    	mVersionTextView = (TextView)v.findViewById(R.id.versionTextView);
    	PackageInfo tmpPackageInfo = null;
		try {
			tmpPackageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.wtf(DbgU.getAppTag(), DbgU.getMethodName()
					+ " NameNotFoundException " + e.getMessage());
			getActivity().finish();
		}
    	mVersionTextView.setText(tmpPackageInfo.versionName);
    	///tmpPackageInfo.packageName + " " +
    	
    	/*
    	//Clickable TextView for sharing the app
    	mShareTextView = (TextView)v.findViewById(R.id.shareTextView);
    	mShareTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.sendAsEmail(getActivity(), "Android app KindMind",
						"http://play.google.com/store/apps/details?id=com.sunyata.kindmind", null);
			}
		});
		*/
    	
    	return v;
    }
    
    
	//-------------------Options menu
	
	@Override
	public boolean onOptionsItemSelected(MenuItem inMenuItem){
		switch (inMenuItem.getItemId()){
		case android.R.id.home:
			//Navigating upwards in the activity heirarchy
			if(NavUtils.getParentActivityName(getActivity()) != null){
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(inMenuItem);
		}
	}
	
	
	
	//----------------------------Other methods
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(DbgU.getAppTag(), DbgU.getMethodName());
		setRetainInstance(true);
		//-Recommended by CommonsWare:
		// http://stackoverflow.com/questions/11160412/why-use-fragmentsetretaininstanceboolean
		// but not in Reto's book: "genereally not recommended"
		setHasOptionsMenu(true); //-for the up navigation button (left caret)
	}
	
}
