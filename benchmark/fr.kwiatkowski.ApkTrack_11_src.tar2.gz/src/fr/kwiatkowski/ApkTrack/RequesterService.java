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

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This receiver collects the results of version checks.
 * Depending on the result, it may decide to perform a get on another website.
 * Ultimately, if an update is detected, an Intent is sent to the Activity or the Service to notify
 * the user.
 *
 * It expects to receive a serialized InstalledApp object as the "targetApp" parameter.
 * An additional string parameter, "source", indicates the origin of the request.
 */
public class RequesterService extends IntentService
{
    public static final String APP_CHECKED = "fr.kwiatkowski.apktrack.updateservice.action.APP_CHECKED";
    public static final String TARGET_APP_PARAMETER = "targetApp";
    public static final String UPDATE_RESULT_PARAMETER = "updateResult";
    public static final String SOURCE_PARAMETER = "source";

    // Do not flood update servers. 1 request every 2 seconds max.
    public static final int REQUEST_DELAY = 2000;

    public RequesterService() {
        super("RequesterService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        InstalledApp app = intent.getParcelableExtra(TARGET_APP_PARAMETER);
        String request_source = intent.getStringExtra(SOURCE_PARAMETER);
        if (app == null || request_source == null)
        {
            Log.v(MainActivity.TAG, "RequesterService was invoked with no targetApp and/or source argument!");
            return;
        }

        // Return if the user disabled background checks.
        if (request_source.equals(ScheduledVersionCheckService.SERVICE_SOURCE) &&
            !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_PREF_BACKGROUND_CHECKS, false))
        {
            Log.v(MainActivity.TAG, "Rejecting version check requested by the service due to user preferences.");
            return;
        }

        // Get preferred update source from the app, otherwise default to the applicable ones.
        UpdateSource updatesource = app.getUpdateSource();
        if (updatesource == null)
        {
            updatesource = UpdateSource.getSource(app, this);
            if (updatesource != null) {
                Log.v(MainActivity.TAG, "No specified update source. Defaulting to: " + updatesource.getName());
            }
            else {
                Log.v(MainActivity.TAG, "Could not find a suitable update source.");
            }
        }
        else {
            Log.v(MainActivity.TAG, "Stored update source for this app: " + updatesource.getName());
        }

        VersionGetResult res = null;
        while (updatesource != null)
        {
            res = new VersionGetTask(app, getApplicationContext(), updatesource).get();
            Log.v(MainActivity.TAG, updatesource.getName() + " check returned: " + res.getStatus());
            if (res.getStatus() == VersionGetResult.Status.ERROR && app.getUpdateSource() == null) {
                updatesource = UpdateSource.getNextSource(app, updatesource, this);
            }
            else
            {
                // If no UpdateSource was specified for the application, set the working one as default.
                if ((res.getStatus() == VersionGetResult.Status.SUCCESS || res.getStatus() == VersionGetResult.Status.UPDATED)
                    && app.getUpdateSource() == null)
                {
                    app.setUpdateSource(updatesource);
                    AppPersistence.getInstance(this).updateApp(app);
                }
                break;
            }
        }

        if (res == null) {
            res = new VersionGetResult(VersionGetResult.Status.ERROR, getString(R.string.no_data_found));
        }

        // Notify the activity or the service.
        Intent i = new Intent(APP_CHECKED);
        i.putExtra(TARGET_APP_PARAMETER, app);
        i.putExtra(UPDATE_RESULT_PARAMETER, res);
        i.putExtra(SOURCE_PARAMETER, request_source); // Send back the source of the request
        sendOrderedBroadcast(i, null);

        // Sleep after the result has been sent. Do not flood the target website.
        try {
            Thread.sleep(REQUEST_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
