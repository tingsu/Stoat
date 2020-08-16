package org.paulmach.textedit;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/* DefaultDirPreference
 * 		Special to show and browser for the default directory
 */
public class DefaultDirPreference extends Preference
{
	private final static int REQUEST_FILE_BROWSER = 1;
	
	// This is the constructor called by the inflater
	public DefaultDirPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		// define what happens when we click the preference
		setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference arg0)
			{
				// figure out what is currently selected
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
				String current = sharedPref.getString("defaultdir", "/sdcard/");
	
				// figure out what to display. should just be the directory
				String location = "/sdcard/";
				
				File f = new File(current);
				if (f.toString().equals("/"))
					location = "/";
				else if (f.isDirectory())
					location = f.toString() + "/";	
				else if (f.getParent().toString().equals("/"))
					location = "/";
				else
					location = f.getParent() + "/";
				
				if (f != null)
					location = f.toString();

				// start the intent
				Intent intent = new Intent(getContext(), FileBrowser.class);
				intent.setAction(location);
				((Activity) getContext()).startActivityForResult(intent, REQUEST_FILE_BROWSER);
				
				return false;
			}
		});
		
		// so the summary says the current
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		String current = sharedPref.getString("defaultdir", "/sdcard/");
		
		this.setSummary(current);
	}
} // end class DefaultDirPreference