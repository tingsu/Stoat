/**
 *   Copyright 2012 Francesco Balducci
 *
 *   This file is part of FakeDawn.
 *
 *   FakeDawn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   FakeDawn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.balau.fakedawn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author francesco
 *
 */
public class InstallationReceiver extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
			if (intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
				Log.d("FakeDawn", "Package Replaced.");
				oldVersionCleanup(context);
				Intent startService = new Intent(context, Alarm.class);			
				context.startService(startService);
				Log.d("FakeDawn", "Alarm started.");
			}
		}
	}

	private  void oldVersionCleanup(Context context) {
		SharedPreferences pref = context.getSharedPreferences("main", Context.MODE_PRIVATE);
		String firstTimeVersion = pref.getString("first_time_version", "");
		if(firstTimeVersion.compareTo("1.0") == 0)
		{
			//We changed setRepeating intent from 1.0 to subsequent versions, so we need to cancel old alarms
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			Intent openDawn = new Intent(context, Dawn.class);
			openDawn.setFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK|
					Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|
					Intent.FLAG_FROM_BACKGROUND);
			PendingIntent openDawnPendingIntent = PendingIntent.getActivity(
					context, 
					0, 
					openDawn,
					0);
			am.cancel(openDawnPendingIntent);
			Log.d("FakeDawn", "1.0 alarms canceled.");
		}
	}

}
