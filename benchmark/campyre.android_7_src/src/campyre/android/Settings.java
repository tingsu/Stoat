package campyre.android;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity {
	
	public static final String NUMBER_MESSAGES_KEY = "number_messages";
	public static final int NUMBER_MESSAGES_DEFAULT = 80;
	
	public static final String ENTRY_EXIT_KEY = "entry_exit";
	public static final boolean ENTRY_EXIT_DEFAULT = true;
	public static final String TIMESTAMPS_KEY = "timestamps";
	public static final boolean TIMESTAMPS_DEFAULT = true;
	
	public static final String LOAD_IMAGES_KEY = "load_images";
	public static final boolean LOAD_IMAGES_DEFAULT = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_titled);
		
		addPreferencesFromResource(R.xml.settings);
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		
		setupControls();
	}
	
	public void setupControls() {
		Utils.setTitle(this, R.string.menu_settings);
		updateNumberMessagesSummary(Utils.getStringPreference(this, NUMBER_MESSAGES_KEY, NUMBER_MESSAGES_DEFAULT + ""));
		
		findPreference(NUMBER_MESSAGES_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateNumberMessagesSummary((String) newValue);
				return true;
			}
		});
	}
	
	private void updateNumberMessagesSummary(String value) {
		String summary = "Show " + value + " messages when in a room.";
		findPreference(NUMBER_MESSAGES_KEY).setSummary(summary);
	}
}
