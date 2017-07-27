package fr.keuse.rightsalert.helper;

import android.Manifest;
import android.content.SharedPreferences;

public class Score {
	private static SharedPreferences preferences;
	
	private Score() {}
	
	public static void setPreferences(SharedPreferences p) {
		preferences = p;
	}
	
	public static int calculate(String[] permissions) {
		if(permissions == null)
			return 0;
		
		int score = 0;
		for(String permission : permissions) {
			if(permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION))
				score += Integer.parseInt(preferences.getString("score_accesscoarselocation", "22"));
			else if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION))
				score += Integer.parseInt(preferences.getString("score_accessfinelocation", "50"));
			else if(permission.equals(Manifest.permission.BATTERY_STATS))
				score += Integer.parseInt(preferences.getString("score_batterystats", "5"));
			else if(permission.equals(Manifest.permission.BLUETOOTH))
				score += Integer.parseInt(preferences.getString("score_bluetooth", "7"));
			else if(permission.equals(Manifest.permission.BLUETOOTH_ADMIN))
				score += Integer.parseInt(preferences.getString("score_bluetoothadmin", "13"));
			else if(permission.equals(Manifest.permission.BRICK))
				score += Integer.parseInt(preferences.getString("score_brick", "45"));
			else if(permission.equals(Manifest.permission.CALL_PHONE))
				score += Integer.parseInt(preferences.getString("score_callphone", "27"));
			else if(permission.equals(Manifest.permission.CALL_PRIVILEGED))
				score += Integer.parseInt(preferences.getString("score_callprivileged", "30"));
			else if(permission.equals(Manifest.permission.CAMERA))
				score += Integer.parseInt(preferences.getString("score_camera", "3"));
			else if(permission.equals(Manifest.permission.GET_ACCOUNTS))
				score += Integer.parseInt(preferences.getString("score_getaccounts", "6"));
			else if(permission.equals(Manifest.permission.INTERNET))
				score += Integer.parseInt(preferences.getString("score_internet", "10"));
			else if(permission.equals(Manifest.permission.MANAGE_ACCOUNTS))
				score += Integer.parseInt(preferences.getString("score_manageaccounts", "20"));
			else if(permission.equals(Manifest.permission.READ_CALENDAR))
				score += Integer.parseInt(preferences.getString("score_readcalendar", "11"));
			else if(permission.equals(Manifest.permission.READ_CONTACTS))
				score += Integer.parseInt(preferences.getString("score_readcontacts", "13"));
			else if(permission.equals(Manifest.permission.READ_HISTORY_BOOKMARKS))
				score += Integer.parseInt(preferences.getString("score_readhistorybookmarks", "9"));
			else if(permission.equals(Manifest.permission.READ_PHONE_STATE))
				score += Integer.parseInt(preferences.getString("score_readphonestate", "20"));
			else if(permission.equals(Manifest.permission.READ_SMS))
				score += Integer.parseInt(preferences.getString("score_readsms", "23"));
			else if(permission.equals(Manifest.permission.SEND_SMS))
				score += Integer.parseInt(preferences.getString("score_sendsms", "22"));
		}
		return score;
	}
	
	public static boolean isDangerous(int score) {
		return (score > Integer.parseInt(preferences.getString("score_alertthreshold", "40")));
	}
}
