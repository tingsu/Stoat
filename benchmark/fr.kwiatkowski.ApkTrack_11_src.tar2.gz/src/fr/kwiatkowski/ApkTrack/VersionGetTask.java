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

import android.content.Context;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The role of this asynchronous task is to request the Play Store page or AppBrain for a given app, and to
 * use a regular expression to get its latest advertised version (when displayed).
 */
public class VersionGetTask
{
    private InstalledApp app;
    private Context ctx;
    private UpdateSource source;
    private static String appbrain_vid_cookie;

    /**
     * Pattern used to detect apps that are no longer available from AppBrain.
     */
    private static Pattern appbrain_no_longer_available;
    private static Pattern fdroid_not_found;

    /**
     * Regexp used to get if a string is a version number, or an error string.
     * For instance, Google Play may return "Version varies depending on the device" and
     * we have to recognize this as an error.
     */
    private static Pattern check_version_pattern;

    private static String nexus_5_user_agent = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";

    static {
        appbrain_no_longer_available = Pattern.compile("This app is unfortunately no longer available on the Android market.|Oops! This page does not exist anymore...");
        fdroid_not_found = Pattern.compile("<p>Application not found</p>");
        check_version_pattern = Pattern.compile("^([^ ]| \\()*$");
    }

    /**
     * The role of this task is to request a web page for a given app, and to
     * use a regular expression to get its latest advertised version (when displayed).
     *
     * This constructor defaults the requested page to the last one working for the app, or the Google Play Store.
     *
     * @param app The application whose version we wish to get.
     * @param context The context of the application so resources can be accessed, etc.
     */
    public VersionGetTask(InstalledApp app, Context context)
    {
        super();
        this.app = app;
        this.ctx = context;
        source = app.getUpdateSource();
        if (source == null) {
            source = UpdateSource.getSource(app, ctx);
        }
    }

    public VersionGetTask(InstalledApp app, Context context, UpdateSource source)
    {
        super();
        this.app = app;
        this.ctx = context;
        this.source = source;
    }

    /**
     * This method performs the task in a synchronous manner.
     * Use @see <code>execute</code> instead if called from the UI thread.
     */
    public VersionGetResult get()
    {
        if (source == null)
        {
            Log.e(MainActivity.TAG, "ERROR: Tried to perform a version check with a null UpdateSource!");
            return new VersionGetResult(VersionGetResult.Status.ERROR, "Error", true);
        }
        VersionGetResult res = get_page(source.getVersionCheckUrl());
        process_result(res);
        return res;
    }

    private void process_result(VersionGetResult result)
    {
        app.setCurrentlyChecking(false);

        if (result.getStatus() == VersionGetResult.Status.SUCCESS)
        {
            Matcher m = Pattern.compile(String.format(source.getVersionCheckRegexp(), app.getPackageName())).matcher(result.getMessage());

            if (m.find())
            {
                String version = m.group(1).trim();
                Log.v(MainActivity.TAG, "Version obtained: " + version);
                app.setLatestVersion(version);

                // Change the status to ERROR if this is not a version number.
                if (!check_version_pattern.matcher(version).matches())
                {
                    Log.v(MainActivity.TAG, "This is not recognized as a version number.");
                    app.setLatestVersion(ctx.getResources().getString(R.string.no_data_found));
                    result.setStatus(VersionGetResult.Status.ERROR);
                    return;
                }

                // Change the status to ERROR if AppBrain is messing with us.
                if ("1000000".equals(version)) {
                    Log.v(MainActivity.TAG, "Received a false version number from AppBrain. Discarding.");
                    app.setLatestVersion(ctx.getResources().getString(R.string.no_data_found));
                    result.setStatus(VersionGetResult.Status.ERROR);
                    return;
                }

                // Do not perform further auto checks if this is not a version number (i.e. "Varies with the device").
                app.setLastCheckFatalError(!check_version_pattern.matcher(version).matches());

                // Update the result object. This data is forwarded to the service during periodic updates.
                if (!app.isLastCheckFatalError() && app.isUpdateAvailable())
                {
                    result.setMessage(version);
                    result.setStatus(VersionGetResult.Status.UPDATED);
                }
            }
            else
            {
                // AppBrain may have pages for apps it doesn't have. Treat as a 404.
                if ("AppBrain".equals(source.getName()))
                {
                    m = appbrain_no_longer_available.matcher(result.getMessage());
                    if (m.find())
                    {
                        Log.v(MainActivity.TAG, "Application no longer available on AppBrain.");
                        result.setStatus(VersionGetResult.Status.ERROR);
                        return;
                    }
                }
                // F-Droid doesn't return a 404 for applications it doesn't have.
                else if ("F-Droid".equals(source.getName()))
                {
                    m = fdroid_not_found.matcher(result.getMessage());
                    if (m.find())
                    {
                        result.setStatus(VersionGetResult.Status.ERROR);
                        return;
                    }
                }
                // Play Store may not contain any version info for some apps.
                else if ("Play Store".equals(source.getName()))
                {
                    result.setStatus(VersionGetResult.Status.ERROR);
                    return;
                }

                Log.v(MainActivity.TAG, "Nothing matched by the regular expression.");
                Log.d(MainActivity.TAG, result.getMessage()); // Dump the page contents to debug the problem.
                Log.v(MainActivity.TAG, "Requested page: " + source.getName());
                app.setLastCheckFatalError(true);
            }
        }
        else
        {
            if (!result.isFatal())
            {
                // Error is not fatal, most likely network related.
                // Don't update the app, but try again later.
                return;
            }
            app.setLastCheckFatalError(true);
            app.setLatestVersion(result.getMessage());
        }

        app.setLastCheckDate(String.valueOf(System.currentTimeMillis() / 1000L));
        AppPersistence.getInstance(ctx).updateApp(app);
    }

    private VersionGetResult get_page(String url)
    {
        Log.v(MainActivity.TAG, "Requesting " + String.format(url, app.getPackageName()));
        InputStream conn = null;
        try
        {
            HttpURLConnection huc = (HttpURLConnection) new URL(String.format(url, app.getPackageName())).openConnection();

            String user_agent = System.getProperty("http.agent");
            if (user_agent == null) { // Some devices seem to return null here (see issue #8).
                user_agent = nexus_5_user_agent;
            }
            huc.setRequestProperty("User-Agent", user_agent);

            huc.setRequestMethod("GET");
            huc.setReadTimeout(15000); // Timeout : 15s
            huc.connect();
            conn = huc.getInputStream();
            return new VersionGetResult(VersionGetResult.Status.SUCCESS, Misc.readAll(conn, 2048));
        }
        catch (FileNotFoundException e)
        {
            // This error is fatal: do not look for updates automatically anymore.
            return new VersionGetResult(VersionGetResult.Status.ERROR, ctx.getResources().getString(R.string.no_data_found), true);
        }
        catch (UnknownHostException e) {
            return new VersionGetResult(VersionGetResult.Status.NETWORK_ERROR, ctx.getResources().getString(R.string.network_error));
        }
        catch (Exception e)
        {
            Log.e(MainActivity.TAG, String.format(url, app.getPackageName()) + " could not be retrieved! (" +
                    e.getMessage() + ")", e);

            return new VersionGetResult(VersionGetResult.Status.NETWORK_ERROR,
                    ctx.getResources().getString(R.string.generic_exception, e.getLocalizedMessage()));
        }
        finally
        {
            if (conn != null) {
                try {
                    conn.close();
                } catch (IOException ignored) {}
            }
        }
    }
}

class VersionGetResult implements Serializable
{
    enum Status { SUCCESS, ERROR, NETWORK_ERROR, UPDATED }

    private String message;
    private boolean fatal;
    private Status result;

    VersionGetResult(Status status, String message)
    {
        this.message = message;
        this.result = status;
        this.fatal = false;
    }

    VersionGetResult(Status status, String message, boolean fatal)
    {
        this.result = status;
        this.message = message;
        this.fatal = fatal;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFatal() {
        return fatal;
    }

    public Status getStatus() {
        return result;
    }

    public void setStatus(Status result) {
        this.result = result;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}