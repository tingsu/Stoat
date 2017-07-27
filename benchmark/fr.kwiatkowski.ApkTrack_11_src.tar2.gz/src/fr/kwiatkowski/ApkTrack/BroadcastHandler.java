/*
 * Copyright (c) 2015
 *
 * ApkTrack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ApkTrack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ApkTrack.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.kwiatkowski.ApkTrack;

import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;

public class BroadcastHandler extends BroadcastReceiver
{
    // This variable is checked by the Activity when it gains the focus to see if it should reload
    // its application list from the database.
    public enum reload_action { NONE, RELOAD, REFRESH };
    private static reload_action action_on_activity_focus_gain = reload_action.NONE;

    public static void setReloadAction(reload_action action) {
        action_on_activity_focus_gain = action;
    }

    public static reload_action getReloadAction() {
        return action_on_activity_focus_gain;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (RequesterService.APP_CHECKED.equals(intent.getAction())) {
            handle_app_checked(context, intent);
        }
        // An app has been upgraded, installed or removed. Tell the activity to refresh its list later.
        else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()) ||
                 Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction()) ||
                 Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) ||
                 Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction()))
        {
            Log.v(MainActivity.TAG, "Received " + intent.getAction() + " (" + intent.getDataString() + "). The activity will be informed.");
            setReloadAction(reload_action.REFRESH);
        }
        else {
            Log.v(MainActivity.TAG, "BroadcastHandler recieved an unhandled intent: " + intent.getAction());
        }
    }

    private void handle_app_checked(Context context, Intent intent)
    {
        InstalledApp app = intent.getParcelableExtra(RequesterService.TARGET_APP_PARAMETER);
        VersionGetResult res = (VersionGetResult) intent.getSerializableExtra(RequesterService.UPDATE_RESULT_PARAMETER);

        if (app == null || res == null)
        {
            Log.v(MainActivity.TAG, "Error: BroadcastHandler received an Intent with missing parameters! "
                    + "app=" + app + " / res=" + res);
            abortBroadcast();
            return;
        }

        Log.v(MainActivity.TAG, "BroadcastHandler received " + res.getStatus() + " for " + app.getDisplayName() + ".");

        if (res.getStatus() == VersionGetResult.Status.UPDATED)
        {
            // Get all updated apps
            ArrayList<InstalledApp> updated_apps = new ArrayList<InstalledApp>();
            for (InstalledApp ia : AppPersistence.getInstance(context).getStoredApps())
            {
                if (ia.isUpdateAvailable()) {
                    updated_apps.add(ia);
                }
            }

            Resources r = context.getResources();
            NotificationCompat.Builder b = new NotificationCompat.Builder(context);
            // Show a notification for updated apps
            if (updated_apps.size() == 0)
            {
                b.setContentTitle(r.getString(R.string.app_updated_notification, app.getDisplayName()))
                        .setContentText(r.getString(R.string.app_version_available, app.getLatestVersion()))
                        .setTicker(r.getString(R.string.app_can_be_updated, app.getDisplayName()))
                        .setSmallIcon(R.drawable.ic_menu_refresh)
                        .setAutoCancel(true); //TODO: Think about launching the search on user click.
            }
            else
            {
                b.setContentTitle(r.getString(R.string.apps_updated_notification))
                        .setContentText(r.getString(R.string.apps_updated_notification_summary, app.getDisplayName(), updated_apps.size()))
                        .setTicker(r.getString(R.string.apps_updated_notification))
                        .setSmallIcon(R.drawable.ic_menu_refresh)
                        .setAutoCancel(true);

                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                for (InstalledApp ia : updated_apps) {
                    style.addLine(r.getString(R.string.app_version_available_2, ia.getDisplayName(), ia.getVersion(), ia.getLatestVersion()));
                }
                style.setBigContentTitle(r.getString(R.string.apps_updated_notification));
                b.setStyle(style);
            }

            // Open ApkTrack when the notification is clicked.
            Intent i = new Intent();
            i.setClass(context, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
            b.setContentIntent(pi);

            NotificationManager mgr = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
            mgr.notify(1, b.build()); // Consolidate notifications.
            setReloadAction(reload_action.RELOAD);
        }
        else if (res.getStatus() == VersionGetResult.Status.ERROR && app.isLastCheckFatalError()) {
            setReloadAction(reload_action.RELOAD); // Refresh it there is a new fatal error.
        }
        else if (res.getStatus() == VersionGetResult.Status.SUCCESS) {
            setReloadAction(reload_action.RELOAD); // TODO: Find a way to set is_currently_checking to false if the source is the Activity.
        }

        abortBroadcast(); // Nobody's listening after this BroadcastReceiver.
    }
}
