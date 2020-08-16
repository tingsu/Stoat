package org.paulmach.textedit;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

/* ClearListPreference
 * 		The Special clear recent file list preference
 * 		Needs it's own special class so you can just click on it */
public class ClearRecentFileListPreference extends Preference
{
	// This is the constructor called by the inflater
	public ClearRecentFileListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onClick() {
	    // Data has changed, notify so UI can be refreshed!
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
		editor.putInt("rf_numfiles", 0);
		editor.commit();
		
		Toast.makeText(getContext(), R.string.onListCleared, Toast.LENGTH_SHORT).show();
        notifyChanged();
    }
	
} // end class ClearListPreference