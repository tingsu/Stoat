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
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple class representing an update source.
 * An update source is a website that can be scraped to get update information, or even APKs.
 */
public class UpdateSource implements Serializable
{
    private static ArrayList<UpdateSource> _SOURCES = null;

    /**
     * Reads the update sources from the JSON asset file.
     * @param ctx The context of the application.
     * @return A list of available update sources.
     */
    public static ArrayList<UpdateSource> getUpdateSources(Context ctx)
    {
        if (_SOURCES != null) {
            return _SOURCES;
        }

        _SOURCES = new ArrayList<UpdateSource>();
        Log.v(MainActivity.TAG, "Reading update sources...");
        try {
            InputStream is = ctx.getAssets().open("sources.json");

            StringBuilder buffer = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String s;
            while ((s = br.readLine()) != null) {
                buffer.append(s);
            }

            JSONArray sources = new JSONArray(buffer.toString());
            for (int i = 0 ; i < sources.length() ; ++i)
            {
                String name = sources.getJSONObject(i).getString("name");
                Log.v(MainActivity.TAG, "Reading " + name);
                String version_check_url = sources.getJSONObject(i).getString("version_check_url");
                String version_check_regexp = sources.getJSONObject(i).getString("version_check_regexp");
                String download_url = sources.getJSONObject(i).optString("download_url", null);
                String applicable_packages = sources.getJSONObject(i).optString("applicable_packages", ".*");
                _SOURCES.add(new UpdateSource(name, version_check_url, version_check_regexp, download_url, applicable_packages));
            }
        }
        catch (IOException e) {
            Log.v(MainActivity.TAG, "Could not open sources.json!", e);
        } catch (JSONException e) {
            Log.v(MainActivity.TAG, "sources.json seems to be malformed!", e);
        }

        return _SOURCES;
    }

    /**
     * Returns the first applicable UpdateSource for a given app
     * @param app The application we want to check
     * @param ctx The context of  the application
     * @return An update source that can be used for this application, or null if no source could be found.
     */
    public static UpdateSource getSource(InstalledApp app, Context ctx)
    {
        for (UpdateSource s : getUpdateSources(ctx))
        {
            if (s.isApplicable(app)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Returns the update source matching a specific name.
     * Generally used with a source name stored in the apps table.
     *
     * @param name The name of the source to retrieve.
     * @param ctx The context of the application
     * @return The requested source, or null if it doesn't exist.
     */
    public static UpdateSource getSource(String name, Context ctx)
    {
        for (UpdateSource s : getUpdateSources(ctx))
        {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }


    /**
     * Returns the next applicable UpdateSource for an app, after a given one.
     * The UpdateSources are returned in the order given in the JSON asset file.
     *
     * @param app The application whose version we want to check.
     * @param source The latest source used.
     * @param ctx The context of  the application
     * @return The next applicable UpdateSource, or null if no source could be found.
     */
    public static UpdateSource getNextSource(InstalledApp app, UpdateSource source, Context ctx)
    {
        // Don't access _SOURCES directly at first, it may not have been initialized.
        int index = getUpdateSources(ctx).indexOf(source);
        if (index == -1) {
            return null;
        }
        for (int i = ++index ; i < _SOURCES.size() ; ++i)
        {
            if (_SOURCES.get(i).isApplicable(app)) {
                return _SOURCES.get(i);
            }
        }
        return null;
    }

    /**
     * Returns the name of all the update sources available for a given package.
     * @param app The app we want to check against.
     * @param ctx The context of the application
     * @return A list of names for UpdateSources that can be used to check the application's version.
     */
    public static String[] getSources(InstalledApp app, Context ctx)
    {
        ArrayList<String> res = new ArrayList<String>();
        for (UpdateSource s : getUpdateSources(ctx))
        {
            if (s.isApplicable(app)) {
                res.add(s.getName());
            }
        }
        String[] retval = new String[res.size()];
        res.toArray(retval);
        return retval;
    }

    private String name;
    private String version_check_url;
    private String version_check_regexp;
    private String download_url;
    private String applicable_packages;

    public UpdateSource(String name,
                        String version_check_url,
                        String version_check_regexp,
                        String download_url,
                        String applicable_packages)
    {
        this.name = name;
        this.version_check_url = version_check_url;
        this.version_check_regexp = version_check_regexp;
        this.download_url = download_url;
        this.applicable_packages = applicable_packages;
    }

    public UpdateSource(String name,
                        String version_check_url,
                        String version_check_regexp,
                        String download_url)
    {
        this.name = name;
        this.version_check_url = version_check_url;
        this.version_check_regexp = version_check_regexp;
        this.download_url = download_url;
        this.applicable_packages = ".*";
    }

    public UpdateSource(String name,
                        String version_check_url,
                        String version_check_regexp)
    {
        this.name = name;
        this.version_check_url = version_check_url;
        this.version_check_regexp = version_check_regexp;
        this.download_url = null;
        this.applicable_packages = ".*";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersionCheckUrl() {
        return version_check_url;
    }

    public String getVersionCheckRegexp() {
        return version_check_regexp;
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public String getApplicablePackages() {
        return applicable_packages;
    }

    /**
     * Checks whether the UpdateSource is applicable for a given application.
     * The package name of the given app is checked against the "applicable_packages" regular expression
     * of the update source.
     * @param app The application to check.
     * @return Whether the UpdateSource is valid for a given application.
     */
    public boolean isApplicable(InstalledApp app)
    {
        // Verify that the update source supports given application by matching the package name.
        Pattern p = Pattern.compile(applicable_packages);
        Matcher m = p.matcher(app.getPackageName());
        return m.find();
    }

}
