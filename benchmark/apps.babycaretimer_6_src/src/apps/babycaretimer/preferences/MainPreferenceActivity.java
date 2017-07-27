package apps.babycaretimer.preferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import apps.babycaretimer.common.Constants;
import apps.babycaretimer.log.Log;
import apps.babycaretimer.R;

/**
 * This is the applications preference Activity.
 * 
 * @author Camille Sévigny
 */
public class MainPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	//================================================================================
    // Constants
    //================================================================================
	
	//Google Market URL
	private static final String RATE_APP_ANDROID_URL = "http://market.android.com/details?id=apps.babycaretimer";
	//Amazon Appstore URL
	private static final String RATE_APP_AMAZON_URL = "http://www.amazon.com/gp/mas/dl/android?p=apps.babycaretimer";
	
	private static final String LANDSCAPE_SCREEN_ENABLED_KEY = "landscape_screen_enabled";
	
	//================================================================================
    // Properties
    //================================================================================

    private boolean _debug = false;
    private Context _context = null;
    private SharedPreferences _preferences = null;
    private String _appVersion = null;
	
	//================================================================================
	// Public Methods
	//================================================================================
	
	/**
	 * The preference Activity was created.
	 * 
	 * @param savedInstanceState - Information about the current state of the PreferenceActivity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    _debug = Log.getDebug();
	    if (_debug) Log.v("MainPreferenceActivity.onCreate()");
	    _context = MainPreferenceActivity.this;
	    _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
	    //_preferences.registerOnSharedPreferenceChangeListener(this);
	    //Don't rotate the Activity when the screen rotates based on the user preferences.
	    if(!_preferences.getBoolean(LANDSCAPE_SCREEN_ENABLED_KEY, false)){
	    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
	    addPreferencesFromResource(R.xml.preferences);
	    _appVersion = getApplicationVersion();
	    setupCustomPreferences();
	    setupRateAppPreference();
	    setupImportPreferences();
	    updateNotificationSoundSettings();
	}

	/**
	 * When a SharedPreference is changed this registered function is called.
	 * 
	 * @param sharedPreferences - The Preference object who's key was changed.
	 * @param key - The String value of the preference Key who's preference value was changed.
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (_debug) Log.v("MainPreferenceActivity.onSharedPreferenceChanged() Key: " + key);
		if(key.equals(Constants.ALARM_NOTIFICATION_SOUND_KEY)){
			updateNotificationSoundSettings();
		}

	}
	
	//================================================================================
	// Protected Methods
	//================================================================================
	
	/**
	 * Activity was resumed after it was stopped or paused.
	 */
	@Override
	protected void onResume() {
	    super.onResume();
	    _debug = Log.getDebug();
	    if (_debug) Log.v("MainPreferenceActivity.onResume()");
	    _preferences.registerOnSharedPreferenceChangeListener(this);
	    setupImportPreferences();
	    updateNotificationSoundSettings();
	}
	
	/**
	 * Activity was paused due to a new Activity being started or other reason.
	 */
    @Override
    protected void onPause() {
        super.onPause();
        if (_debug) Log.v("MainPreferenceActivity.onPause()");
        _preferences.unregisterOnSharedPreferenceChangeListener(this);
    }
	  
	/**
	 * Activity was stopped due to a new Activity being started or other reason.
	 */
	@Override
	protected void onStop() {
	    super.onStop();
	    if (_debug) Log.v("MainPreferenceActivity.onStop()");
	}
	  
	/**
	 * Activity was stopped and closed out completely.
	 */
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (_debug) Log.v("MainPreferenceActivity.onDestroy()");
	}
	
	//================================================================================
	// Private Methods
	//================================================================================
	
	/**
	 * Setup the custom Preference buttons.
	 */
	private void setupCustomPreferences(){
		if (_debug) Log.v("MainPreferenceActivity.setupCustomPreferences()");
		//Rate This App Preference/Button
		Preference rateAppPref = (Preference)findPreference("rate_app");
		rateAppPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	if (_debug) Log.v("Rate This App Button Clicked()");
		    	try{
			    	String rateAppURL = "";
			    	if(Log.getShowAndroidRateAppLink()) rateAppURL = RATE_APP_ANDROID_URL;
			    	if(Log.getShowAmazonRateAppLink()) rateAppURL = RATE_APP_AMAZON_URL;
			    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rateAppURL));			    	
			    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    		startActivity(intent);
		    	}catch(Exception ex){
	 	    		if (_debug) Log.e("MainPreferenceActivity.setupCustomPreferences() Rate This App Button ERROR: " + ex.toString());
	 	    		Toast.makeText(_context, _context.getString(R.string.app_android_rate_app_error), Toast.LENGTH_SHORT).show();
	 	    		return false;
		    	}
	            return true;
           }
		});
		//Email Developer Preference/Button
		Preference emailDeveloperPref = (Preference)findPreference("email_developer");
		emailDeveloperPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	if (_debug) Log.v("Email Developer Button Clicked()");
		    	try{
			    	Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:babycaretimer@gmail.com"));
			    	intent.putExtra("subject", "Baby Care Timer App Feedback");
			    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		    		startActivity(intent);
		    	}catch(Exception ex){
	 	    		if (_debug) Log.e("MainPreferenceActivity.setupCustomPreferences() Email Developer Button ERROR: " + ex.toString());
	 	    		Toast.makeText(_context, _context.getString(R.string.app_android_email_app_error), Toast.LENGTH_SHORT).show();
	 	    		return false;
		    	}
	            return true;
           }
		});
		//About Preference/Button
		Preference aboutPreferencesPref = (Preference)findPreference("application_about");
		aboutPreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	if (_debug) Log.v("About Button Clicked()");
				if(_preferences.getString(Constants.APP_THEME_KEY, "0").equals("0")){
					return displayHTMLAlertDialog(_context.getString(R.string.app_name_formatted_version, _appVersion), R.drawable.ic_launcher_babycaretimer_blue, _context.getString(R.string.preference_about_text));
				} else {
					return displayHTMLAlertDialog(_context.getString(R.string.app_name_formatted_version, _appVersion), R.drawable.ic_launcher_babycaretimer_pink, _context.getString(R.string.preference_about_text));
				}
        	}
		});
		//License Preference/Button
		Preference licensePreferencesPref = (Preference)findPreference("application_license");
		licensePreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	if (_debug) Log.v("License Button Clicked()");
	            return displayHTMLAlertDialog(_context.getString(R.string.app_license), R.drawable.ic_dialog_info, _context.getString(R.string.eula_text));
        	}
		});
		//Export Preferences Preference/Button
		Preference exportPreferencesPref = (Preference)findPreference("export_preferences");
		exportPreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	if (_debug) Log.v("Export Preferences Button Clicked()");
		    	try{
			    	//Run this process in the background in an AsyncTask.
			    	new exportPreferencesAsyncTask().execute();
		    	}catch(Exception ex){
	 	    		if (_debug) Log.e("MainPreferenceActivity.setupCustomPreferences() Export Preferences Button ERROR: " + ex.toString());
	 	    		return false;
		    	}
	            return true;
           }
		});
		//Import Preferences Preference/Button
		Preference importPreferencesPref = (Preference)findPreference("import_preferences");
		importPreferencesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
		    	if (_debug) Log.v("Import Preferences Button Clicked()");
		    	try{
			    	//Run this process in the background in an AsyncTask.
			    	new importPreferencesAsyncTask().execute();
		    	}catch(Exception ex){
	 	    		if (_debug) Log.e("MainPreferenceActivity.setupCustomPreferences() Import Preferences Button ERROR: " + ex.toString());
	 	    		return false;
		    	}
	            return true;
           }
		});
	}
	
	/**
	 * Removes the "Rate App" link from the application if not in the Android or Amazon stores.
	 */
	private void setupRateAppPreference(){
		if (_debug) Log.v("MainPreferenceActivity.setupRateAppPreference()");
		boolean showRateAppCategory = false;
		if(Log.getShowAndroidRateAppLink()) showRateAppCategory = true;
		if(Log.getShowAmazonRateAppLink()) showRateAppCategory = true;
		if(!showRateAppCategory){
			PreferenceScreen mainPreferences = this.getPreferenceScreen();
			PreferenceCategory rateAppCategory = (PreferenceCategory) findPreference("rate_app_category");
			mainPreferences.removePreference(rateAppCategory);
		}
	}
	
	/**
	 * Export application preferences.
	 * 
	 * @author Camille Sévigny
	 */
	private class exportPreferencesAsyncTask extends AsyncTask<Void, Void, Boolean> {
		//ProgressDialog to display while the task is running.
		private ProgressDialog dialog;
		/**
		 * Setup the Progress Dialog.
		 */
	    protected void onPreExecute() {
			if (_debug) Log.v("MainPreferenceActivity.exportPreferencesAsyncTask.onPreExecute()");
	        dialog = ProgressDialog.show(MainPreferenceActivity.this, "", _context.getString(R.string.preference_export_preferences_progress_text), true);
	    }
	    /**
	     * Do this work in the background.
	     * 
	     * @param params
	     */
	    protected Boolean doInBackground(Void... params) {
			if (_debug) Log.v("MainPreferenceActivity.exportPreferencesAsyncTask.doInBackground()");
	    	return exportApplicationPreferences();
	    }
	    /**
	     * Stop the Progress Dialog and do any post background work.
	     * 
	     * @param result
	     */
	    protected void onPostExecute(Boolean successful) {
			if (_debug) Log.v("MainPreferenceActivity.exportPreferencesAsyncTask.onPostExecute()");
	        dialog.dismiss();
	        if(successful){
	    		setupImportPreferences();
	        	Toast.makeText(_context, _context.getString(R.string.preference_export_preferences_finish_text), Toast.LENGTH_LONG).show();
	        }else{
	        	Toast.makeText(_context, _context.getString(R.string.preference_export_preferences_error_text), Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
	/**
	 * Export the application preferences to the SD card.
	 * 
	 * @return boolean - True if the operation was successful, false otherwise.
	 */
	private boolean exportApplicationPreferences(){
		if (_debug) Log.v("MainPreferenceActivity.exportApplicationPreferences()");
		//Check state of external storage.
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    //We can read and write the media. Do nothing.
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media.
			if (_debug) Log.e("MainPreferenceActivity.exportApplicationPreferences() External Storage Read Only State");
		    return false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need to know is we can neither read nor write
			if (_debug) Log.e("MainPreferenceActivity.exportApplicationPreferences() External Storage Can't Write Or Read State");
		    return false;
		}
    	File preferencesFilePath = Environment.getExternalStoragePublicDirectory("Baby Care Timer/Preferences");
    	File preferencesFile = new File(preferencesFilePath, "BabyCareTimerPreferences.txt");
    	try{
    		preferencesFilePath.mkdirs();
    		//Delete previous file if it exists.
    		if(preferencesFile.exists()){
    			preferencesFile.delete();   			
    		}
    		preferencesFile.createNewFile();
    		//Write each preference to the text file.
			BufferedWriter buf = new BufferedWriter(new FileWriter(preferencesFile, true)); 		
			Map<String, ?> applicationPreferencesMap = _preferences.getAll();
			for (Map.Entry<String, ?> entry : applicationPreferencesMap.entrySet()) {
			    String key = entry.getKey();
			    Object value = entry.getValue();
			    if(value instanceof String){
			    	buf.append(key + "|" + value + "|string");
			    }else if(value instanceof Boolean){
			    	buf.append(key + "|" + value + "|boolean");
			    }else if(value instanceof Integer){
			    	buf.append(key + "|" + value + "|int");
			    }else if(value instanceof Long){
			    	buf.append(key + "|" + value + "|long");
			    }else if(value instanceof Float){
			    	buf.append(key + "|" + value + "|float");
			    }
				buf.newLine();
			}
			buf.close();
		}catch (Exception ex){
			if (_debug) Log.e("MainPreferenceActivity.exportApplicationPreferences() Wrtie File ERROR: " + ex.toString());
			return false;
		}
		return true;
	}
	
	/**
	 * Import application preferences.
	 * 
	 * @author Camille Sévigny
	 */
	private class importPreferencesAsyncTask extends AsyncTask<Void, Void, Boolean> {
		//ProgressDialog to display while the task is running.
		private ProgressDialog dialog;
		/**
		 * Setup the Progress Dialog.
		 */
	    protected void onPreExecute() {
			if (_debug) Log.v("MainPreferenceActivity.importPreferencesAsyncTask.onPreExecute()");
	        dialog = ProgressDialog.show(MainPreferenceActivity.this, "", _context.getString(R.string.preference_import_preferences_progress_text), true);
	    }
	    /**
	     * Do this work in the background.
	     * 
	     * @param params
	     */
	    protected Boolean doInBackground(Void... params) {
			if (_debug) Log.v("MainPreferenceActivity.importPreferencesAsyncTask.doInBackground()");
	    	return importApplicationPreferences();
	    }
	    /**
	     * Stop the Progress Dialog and do any post background work.
	     * 
	     * @param result
	     */
	    protected void onPostExecute(Boolean successful) {
			if (_debug) Log.v("MainPreferenceActivity.importPreferencesAsyncTask.onPostExecute()");
	        dialog.dismiss();
	        if(successful){
	        	Toast.makeText(_context, _context.getString(R.string.preference_import_preferences_finish_text), Toast.LENGTH_LONG).show();
	        }else{
	        	Toast.makeText(_context, _context.getString(R.string.preference_import_preferences_error_text), Toast.LENGTH_LONG).show();
	        }
	        reloadPreferenceActivity();
	    }
	}
	
	/**
	 * Import the application preferences from the SD card.
	 * 
	 * @return boolean - True if the operation was successful, false otherwise.
	 */
	private boolean importApplicationPreferences(){
		if (_debug) Log.v("MainPreferenceActivity.importApplicationPreferences()");
		//Check state of external storage.
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    //We can read and write the media. Do nothing.
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media. Do nothing.
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need to know is we can neither read nor write
			if (_debug) Log.e("MainPreferenceActivity.importApplicationPreferences() External Storage Can't Write Or Read State");
		    return false;
		}
    	if (!checkPreferencesFileExists("Baby Care Timer/Preferences/", "BabyCareTimerPreferences.txt")){
    		if (_debug) Log.v("MainPreferenceActivity.importApplicationPreferences() Preference file does not exist.");
			return false;
		}
    	try {
    		File preferencesFilePath = Environment.getExternalStoragePublicDirectory("Baby Care Timer/Preferences/");
        	File preferencesFile = new File(preferencesFilePath, "BabyCareTimerPreferences.txt");
    		SharedPreferences.Editor editor = _preferences.edit();
    	    BufferedReader br = new BufferedReader(new FileReader(preferencesFile));
    	    String line;
    	    while ((line = br.readLine()) != null) {
    	    	String[] preferenceInfo = line.split("\\|");
    	        if(preferenceInfo[2].toLowerCase().equals("boolean")){
    	        	editor.putBoolean(preferenceInfo[0], Boolean.parseBoolean(preferenceInfo[1])); 
	    	    }else if(preferenceInfo[2].toLowerCase().equals("string")){
	    	    	editor.putString(preferenceInfo[0], preferenceInfo[1]); 
	    	    }else if(preferenceInfo[2].toLowerCase().equals("int")){
	    	    	editor.putInt(preferenceInfo[0], Integer.parseInt(preferenceInfo[1])); 
	    	    }else if(preferenceInfo[2].toLowerCase().equals("long")){
	    	    	editor.putLong(preferenceInfo[0], Long.parseLong(preferenceInfo[1])); 
	    	    }else if(preferenceInfo[2].toLowerCase().equals("float")){
	    	    	editor.putFloat(preferenceInfo[0], Float.parseFloat(preferenceInfo[1])); 
	    	    }
    	    }
    		editor.commit();
    	}catch (IOException ex) {
    		if (_debug) Log.e("MainPreferenceActivity.importApplicationPreferences() ERROR: " + ex.toString());
    		return false;
    	}
		return true;
	}
	
	/**
	 * Display an HTML AletDialog.
	 */
	private boolean displayHTMLAlertDialog(String title, int iconResource, String content){
		if (_debug) Log.v("MainPreferenceActivity.displayHTMLAlertDialog()");
		try{
    		LayoutInflater layoutInflater = (LayoutInflater) _context.getSystemService(LAYOUT_INFLATER_SERVICE);
    		View view = layoutInflater.inflate(R.layout.html_alert_dialog, (ViewGroup) findViewById(R.id.content_scroll_view));		    		
    		TextView contentTextView = (TextView) view.findViewById(R.id.content_text_view);
    		contentTextView.setText(Html.fromHtml(content));
    		contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
    		AlertDialog.Builder builder = new AlertDialog.Builder(_context);
    		builder.setIcon(iconResource);
    		builder.setTitle(title);
    		builder.setView(view);
    		builder.setNegativeButton(R.string.ok_text, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
    		AlertDialog alertDialog = builder.create();
    		alertDialog.show();
    	}catch(Exception ex){
	    		if (_debug) Log.e("MainPreferenceActivity.displayHTMLAlertDialog() ERROR: " + ex.toString());
	    		return false;
    	}
		return true;
	}
	
	/**
	 * Sets up the import preference button. Disables if there is no import file.
	 */
	private void setupImportPreferences(){
		if (_debug) Log.v("MainPreferenceActivity.setupImportPreferences()");
		Preference importPreference = (Preference) findPreference("import_preferences");
		importPreference.setEnabled(checkPreferencesFileExists("Baby Care Timer/Preferences/", "BabyCareTimerPreferences.txt"));
	}
	
	/**
	 * Checks if the user has a preferences file on the SD card.
	 * 
	 * @return boolean - Returns true if the preference file exists.
	 */
	private boolean checkPreferencesFileExists(String directory, String file){
		if (_debug) Log.v("MainPreferenceActivity.checkPreferencesFileExists()");
		File preferencesFilePath = Environment.getExternalStoragePublicDirectory(directory);
    	File preferencesFile = new File(preferencesFilePath, file);
    	if (preferencesFile.exists()){
			return true;
		}else{
			return false;
		}
	}
		
	/**
	 * Read the Application info and return the app version number.
	 * 
	 * @return String - The version number of the aplication.
	 */
	private String getApplicationVersion(){
		if (_debug) Log.v("MainPreferenceActivity.getApplicationVersion()");
		PackageInfo packageInfo = null;
		try{
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return packageInfo.versionName;
		}catch(Exception ex){
			return "";
		}
	}
	
	/**
	 * Updates the availability of the Notification Sound Settings.
	 */
	private void updateNotificationSoundSettings(){
		if (_debug) Log.v("MainPreferenceActivity.updateNotificationSoundSettings()");
		if (_debug) Log.v("MainPreferenceActivity.updateNotificationSoundSettings() SOUND SETTING: " + _preferences.getString(Constants.ALARM_NOTIFICATION_SOUND_KEY, "content://settings/system/alarm_alert"));
		try{
			CheckBoxPreference alarmNotificationInCallSoundCheckbox = (CheckBoxPreference) findPreference(Constants.ALARM_NOTIFICATION_IN_CALL_SOUND_ENABLED_KEY);		
			if(_preferences.getString(Constants.ALARM_NOTIFICATION_SOUND_KEY, "content://settings/system/alarm_alert").equals("")){
				alarmNotificationInCallSoundCheckbox.setEnabled(false);
			}else{
				alarmNotificationInCallSoundCheckbox.setEnabled(true);
			}
		}catch(Exception ex){
			if (_debug) Log.e("MainPreferenceActivity.updateVibrateSettings() ERROR: " + ex.toString());
		}
	}
	
	/**
	 * Reload Preference Activity
	 */
	public void reloadPreferenceActivity() {
		if (_debug) Log.v("MainPreferenceActivity.reloadPreferenceActivity()");
		try{
		    Intent intent = getIntent();
		    overridePendingTransition(0, 0);
		    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		    finish();
		    overridePendingTransition(0, 0);
		    startActivity(intent);
		}catch(Exception ex){
			if (_debug) Log.e("MainPreferenceActivity.reloadPreferenceActivity() ERROR: " + ex.toString());
		}
	}
	
}