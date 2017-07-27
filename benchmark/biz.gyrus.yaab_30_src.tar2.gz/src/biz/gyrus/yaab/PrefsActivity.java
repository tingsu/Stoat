package biz.gyrus.yaab;

import biz.gyrus.yaab.BrightnessController.ServiceStatus;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity {

	private final static String P_AUTOSTART = "autostart_preference";
	private final static String P_FOREGROUNDSERVICE = "fgservice_preference";
	private final static String P_ALWAYSFGSERVICE = "always_fgsrv_preference";
	private final static String P_ALERTKEEPALIVE = "alert_keepalive_preference";
	private final static String P_RANGES = "ranges_screen";
	private final static String P_SMOOTH_BRIGHTNESS = "smooth_brightness_preference";
	private final static String P_LOW_NIGHTMODE_VALUES = "low_nightmode_values_preference";
	private final static String P_NIGHTFALL_DELAY = "nightfall_delay_preference"; 
	
	private CheckBoxPreference	p_bAutoStart;
	private CheckBoxPreference	p_bFgService;
	private CheckBoxPreference	p_bAlwaysFgService;
	private CheckBoxPreference	p_bAlertKeepalive;
	private PreferenceScreen	p_sRanges;
	private CheckBoxPreference	p_bSmoothBrightness;
	private CheckBoxPreference	p_bLowNightmodeValues;
	private CheckBoxPreference	p_bNightfallDelay;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		ThemeHelper.onActivityApplyCurrentTheme(this);
		
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.appprefs);
		
		p_bAutoStart = (CheckBoxPreference) findPreference(P_AUTOSTART);
		p_bFgService = (CheckBoxPreference) findPreference(P_FOREGROUNDSERVICE);
		p_bAlwaysFgService = (CheckBoxPreference) findPreference(P_ALWAYSFGSERVICE);
		p_bAlertKeepalive = (CheckBoxPreference) findPreference(P_ALERTKEEPALIVE);
		p_sRanges = (PreferenceScreen) findPreference(P_RANGES);
		p_bSmoothBrightness = (CheckBoxPreference) findPreference(P_SMOOTH_BRIGHTNESS);
		p_bLowNightmodeValues = (CheckBoxPreference) findPreference(P_LOW_NIGHTMODE_VALUES);
		p_bNightfallDelay = (CheckBoxPreference) findPreference(P_NIGHTFALL_DELAY);
		
		p_bAutoStart.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setAutostart(bNewVal);
				
				return true;
			}
		});
		
		p_bFgService.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setPersistNotification(bNewVal);
				
				LightMonitorService lms = LightMonitorService.getInstance();
				if(lms != null)
					lms.showNotificationIcon(bNewVal && BrightnessController.get().getServiceStatus() == ServiceStatus.Running);
				
				return true;
			}
		});
		
		p_bAlwaysFgService.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setPersistAlwaysNotification(bNewVal);
				
				return true;
			}
		});
		
		p_bAlertKeepalive.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setAlertKeepalive(bNewVal);
				
				LightMonitorService lms = LightMonitorService.getInstance();
				if(lms != null)
					lms.startAlertKeepalive(bNewVal);
				
				return true;
			}
		});
		
		p_sRanges.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				try
				{
					Intent rangesIntent = new Intent(PrefsActivity.this, RangesActivity.class);
			        startActivity(rangesIntent);
			        Log.i(Globals.TAG, "Ranges activity started");
				} catch(Exception e)
				{
					Log.e(Globals.TAG, e.getMessage(), e);
				}
				return true;
			}
		});
		
		p_bSmoothBrightness.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setSmoothApplyBrightness(bNewVal);
				
				BrightnessController.get().setSmoothApplyBrightness(bNewVal);
				
				return true;
			}
		});
		
		p_bLowNightmodeValues.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setLowNightmodeValues(bNewVal);
				
				BrightnessController.get().setLowNightmodeValues(bNewVal);
				
				return true;
			}
		});
		
		p_bNightfallDelay.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean bNewVal = (Boolean) newValue;
				
				AppSettings as = new AppSettings(PrefsActivity.this);
				as.setNightfallDelay(bNewVal);
				
				BrightnessController.get().setNightFallDelay(bNewVal);
				
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		loadPrefs();
		setTitle(R.string.preferences);
	}
	
	protected void loadPrefs()
	{
		AppSettings as = new AppSettings(this);
		
		p_bAutoStart.setChecked(as.getAutostart());
		p_bFgService.setChecked(as.getPersistNotification());
		p_bAlwaysFgService.setChecked(as.getPersistAlwaysNotification());
		p_bAlertKeepalive.setChecked(as.getAlertKeepalive());
		p_bSmoothBrightness.setChecked(as.getSmoothApplyBrightness());
		p_bLowNightmodeValues.setChecked(as.getLowNightmodeValues());
		p_bNightfallDelay.setChecked(as.getNightfallDelay());
	}
	
}
