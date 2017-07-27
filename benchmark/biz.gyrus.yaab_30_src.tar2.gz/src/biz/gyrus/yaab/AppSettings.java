package biz.gyrus.yaab;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

@SuppressLint("CommitPrefEdits")
public class AppSettings {
	
	private static final String _prefsName = "thesettings";
	private static final String _saverAppVerName = "savedByAppVersion";

	private static final String _autostartName = "autoStartOnDeviceBoot";
	private static final String _adjshiftName = "adjShift";
	private static final String _persistNotifName = "persistNotification";
	private static final String _persistNotifAlwaysName = "persistNotificationAlways";
	private static final String _alertKeepaliveName = "alertKeepalive";
	private static final String _brightnessMinRangeName = "brightnessMinRange";
	private static final String _brightnessMaxRangeName = "brightnessMaxRange";
	private static final String _allowAutoNightName = "allowAutoNight";
	private static final String _nightModeBrightnessName = "nightModeBrightness";
	private static final String _nightModeThresholdName = "nightModeThreshold";
	private static final String _smoothApplyBrightnessName = "smoothApplyBrightness";
	private static final String _lowNightmodeValuesName = "lowNightmodeValues";
	private static final String _manualNightOnName = "manualNightModeOn";
	private static final String _nightFallDelayName = "nightFallDelay";
	
	private Context _ctx = null;
	private int _verNum = 1;
	
	public AppSettings(Context ctx)
	{
		_ctx = ctx;
		try {

			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			_verNum = pInfo.versionCode;
			
		} catch (NameNotFoundException e) {
			Log.e(Globals.TAG, "Can't get package name.");
			e.printStackTrace();
		} 
	}
	
	protected SharedPreferences getSP()
	{
		SharedPreferences sp = _ctx.getSharedPreferences(_prefsName, 0);
		if(sp.getInt(_saverAppVerName, 1) < _verNum)
		{
			upgradePrefs(sp);
		}
		return sp;
	}
	
	protected void upgradePrefs(SharedPreferences sp)
	{
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "upgradePrefs starting.");
		int iPrevVer = sp.getInt(_saverAppVerName, 1); 
		SharedPreferences.Editor e = sp.edit();
		
		if(iPrevVer == 1)
		{
			e.putInt(_adjshiftName, sp.getInt(_adjshiftName, 50) - Globals.ADJUSTMENT_RANGE_INT/2);
		}
		
		if(iPrevVer < 7)
		{
			updateBootReceiverState(sp.getBoolean(_autostartName, false));
		}
		
		commitAndLog(e);
		if(Log.isLoggable(Globals.TAG, Log.DEBUG)) Log.d(Globals.TAG, "upgradePrefs done.");
	}
	
	protected void commitAndLog(SharedPreferences.Editor e)
	{
		e.putInt(_saverAppVerName, _verNum);
		if(!e.commit())
		{
			Log.e(Globals.TAG, "Failed to save settings.");
		}
	}
	
	protected void updateBootReceiverState(boolean enabled)
	{
		ComponentName cn = new ComponentName(_ctx, DeviceBootReceiver.class);
		PackageManager pm = _ctx.getPackageManager();
		pm.setComponentEnabledSetting(cn, enabled?PackageManager.COMPONENT_ENABLED_STATE_ENABLED:PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}
	
	public Boolean getAutostart()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_autostartName, true);
	}
	public void setAutostart(boolean bAutoStart)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_autostartName, bAutoStart);
		
		commitAndLog(e);
		
		// enable/disable the receiver also
		updateBootReceiverState(bAutoStart);
	}
	
	public int getAdjshift()
	{
		SharedPreferences sp = getSP();
		return sp.getInt(_adjshiftName, 0);
	}
	public void setAdjshift(int val)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putInt(_adjshiftName, val);
		
		commitAndLog(e);
	}
	
	public Boolean getPersistNotification()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_persistNotifName, Build.VERSION.SDK_INT > 10);	// offering the icon by default for Android 3.x and newer
	}
	public void setPersistNotification(boolean bPersist)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_persistNotifName, bPersist);
		
		commitAndLog(e);
	}
	
	public Boolean getPersistAlwaysNotification()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_persistNotifAlwaysName, false);
	}
	public void setPersistAlwaysNotification(boolean bPersistAlways)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_persistNotifAlwaysName, bPersistAlways);
		
		commitAndLog(e);
	}
	
	public Boolean getAlertKeepalive()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_alertKeepaliveName, false);
	}
	public void setAlertKeepalive(boolean bAlertKeepalive)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_alertKeepaliveName, bAlertKeepalive);
		
		commitAndLog(e);
	}
	
	public int getBrightnessMinRange()
	{
		SharedPreferences sp = getSP();
		return sp.getInt(_brightnessMinRangeName, Globals.MIN_BRIGHTNESS_INT);
	}
	public void setBrightnessMinRange(int val)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putInt(_brightnessMinRangeName, val);
		
		commitAndLog(e);
	}
	
	public int getBrightnessMaxRange()
	{
		SharedPreferences sp = getSP();
		return sp.getInt(_brightnessMaxRangeName, Globals.MAX_BRIGHTNESS_INT);
	}
	public void setBrightnessMaxRange(int val)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putInt(_brightnessMaxRangeName, val);
		
		commitAndLog(e);
	}
	
	public Boolean getAllowAutoNight()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_allowAutoNightName, false);
	}
	public void setAllowAutoNight(boolean bAllow)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_allowAutoNightName, bAllow);
		
		commitAndLog(e);
	}
	
	public int getNMBrightness()
	{
		SharedPreferences sp = getSP();
		return sp.getInt(_nightModeBrightnessName, (Globals.MAX_NM_BRIGHTNESS + Globals.MIN_NM_BRIGHTNESS)/2);
	}
	public void setNMBrightness(int val)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putInt(_nightModeBrightnessName, val);
		
		commitAndLog(e);
	}
	
	public int getNMThreshold()
	{
		SharedPreferences sp = getSP();
		return sp.getInt(_nightModeThresholdName, (int) Globals.MIN_READING_DAY_F);		// historical default
	}
	public void setNMThreshold(int val)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putInt(_nightModeThresholdName, val);
		
		commitAndLog(e);
	}
	
	public Boolean getSmoothApplyBrightness()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_smoothApplyBrightnessName, false);
	}
	public void setSmoothApplyBrightness(boolean bSmooth)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_smoothApplyBrightnessName, bSmooth);
		
		commitAndLog(e);
	}
	
	public Boolean getLowNightmodeValues()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_lowNightmodeValuesName, false);
	}
	public void setLowNightmodeValues(boolean bLow)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_lowNightmodeValuesName, bLow);
		
		commitAndLog(e);
	}
	
	public Boolean getManualNight()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_manualNightOnName, false);
	}
	public void setManualNight(boolean bNight)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_manualNightOnName, bNight);
		
		commitAndLog(e);
	}
	
	public Boolean getNightfallDelay()
	{
		SharedPreferences sp = getSP();
		return sp.getBoolean(_nightFallDelayName, false);
	}
	public void setNightfallDelay(boolean bNightFallDelay)
	{
		SharedPreferences.Editor e = getSP().edit();
		
		e.putBoolean(_nightFallDelayName, bNightFallDelay);
		
		commitAndLog(e);
	}
}
