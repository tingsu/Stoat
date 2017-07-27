package fr.keuse.rightsalert.preference;

import fr.keuse.rightsalert.R;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class RightsalertPreference extends PreferenceActivity implements OnPreferenceChangeListener {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main);
		
		if(Build.VERSION.SDK_INT >= 11) {
			ActionBar actionbar = getActionBar();
			actionbar.setDisplayHomeAsUpEnabled(true);
		}
		
		String[] scores = {"score_accesscoarselocation", "score_accessfinelocation", "score_batterystats", "score_bluetooth",
				"score_bluetoothadmin", "score_brick", "score_callphone", "score_callprivileged", "score_camera",
				"score_getaccounts", "score_internet", "score_manageaccounts", "score_readcalendar", "score_readcontacts",
				"score_readhistorybookmarks", "score_readphonestate", "score_readsms", "score_sendsms", "score_alertthreshold"};
		
		for(String score : scores) {
			findPreference(score).setOnPreferenceChangeListener(this);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if("".equals(newValue)) {
			((EditTextPreference) preference).setText("0");
			return false;
		}
		return true;
	}
}
