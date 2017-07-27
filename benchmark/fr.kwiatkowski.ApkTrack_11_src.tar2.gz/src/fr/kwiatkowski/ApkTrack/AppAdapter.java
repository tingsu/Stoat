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
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AppAdapter extends BaseAdapter
{
    private List<InstalledApp> data;
    private ColorStateList default_color = null;
    private boolean show_system = false;

    private int user_app_count = 0;

    public AppAdapter(List<InstalledApp> objects)
    {
        super();
        this.data = objects;

        // Move system apps to a different list, since they are not displayed by default.
        hideSystemApps();
    }

    @Override
    public int getCount()
    {
        if (show_system) {
            return data.size();
        }
        else {
            return user_app_count;
        }
    }

    @Override
    public InstalledApp getItem(int i)
    {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public boolean isShowSystem() {
        return show_system;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Context ctx = parent.getContext();

        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.list_item, parent, false);
        }

        if (position >= data.size()) {
            return LayoutInflater.from(ctx).inflate(R.layout.list_item, parent, false);
        }

        InstalledApp app = data.get(position);

        View app_info = convertView.findViewById(R.id.app_info);
        TextView name = (TextView) app_info.findViewById(R.id.name);
        TextView version = (TextView) app_info.findViewById(R.id.version);
        TextView date = (TextView) app_info.findViewById(R.id.date);
        ImageView action_icon = (ImageView) convertView.findViewById(R.id.action_icon);

        if (default_color == null) {
            default_color = name.getTextColors();
        }

        // Set application name
        String appname = app.getDisplayName();
        if (appname != null) {
            name.setText(appname);
        }
        else {
            name.setText(app.getPackageName());
        }

        // Display the loader if we're currently checking for updates for that application
        if (app.isCurrentlyChecking())
        {
            action_icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_popup_sync));
            action_icon.setVisibility(View.VISIBLE);
            ((Animatable) action_icon.getDrawable()).start();
            if (action_icon.hasOnClickListeners()) {
                action_icon.setOnClickListener(null);
            }
        }
        // Show a search icon if the app can be updated, and attach a click listener to it.
        else if (app.isUpdateAvailable() && !app.isLastCheckFatalError())
        {
            action_icon.setVisibility(View.VISIBLE);
            if (app.getUpdateSource() != null && app.getUpdateSource().getDownloadUrl() != null) {
                action_icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_download));
            }
            else {
                action_icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_btn_search));
            }
            final int pos_copy = position;
            action_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    InstalledApp app = data.get(pos_copy);
                    Uri uri;
                    if (app.getUpdateSource() != null && app.getUpdateSource().getDownloadUrl() != null)
                    {
                        Log.v(MainActivity.TAG, "Using update source's download url: " + String.format(app.getUpdateSource().getDownloadUrl(), app.getVersion(), app.getPackageName()));
                        uri = Uri.parse(String.format(app.getUpdateSource().getDownloadUrl(), app.getLatestVersion(), app.getPackageName()));
                    }
                    else
                    {
                        uri = Uri.parse(String.format(PreferenceManager.getDefaultSharedPreferences(ctx).getString(SettingsActivity.KEY_PREF_SEARCH_ENGINE,
                                        ctx.getString(R.string.search_engine_default)),
                                app.getDisplayName(), app.getLatestVersion(), app.getPackageName()));
                    }

                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    ctx.startActivity(i);
                }
            });
        }
        else
        {
            action_icon.setVisibility(View.INVISIBLE);
            action_icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_popup_sync));
            if (action_icon.hasOnClickListeners()) {
                action_icon.setOnClickListener(null);
            }
        }

        // Set version. Check whether the application is up to date.
        String latest_version = app.getLatestVersion();
        if (app.isLastCheckFatalError())
        {
            version.setText(app.getVersion() + " (" + latest_version + ")");
            version.setTextColor(Color.GRAY);
        }
        else if (latest_version != null)
        {
            if (!app.isUpdateAvailable())
            {
                // App is more recent than the latest version found
                if (app.getVersion() != null && !app.getVersion().equals(latest_version)) {
                    version.setText(String.format("%s (> %s)", app.getVersion(), latest_version));
                }
                else {
                    version.setText(app.getVersion());
                }
                version.setTextColor(Color.GREEN);
            }
            else
            {
                version.setText(String.format("%s (%s %s)",
                        app.getVersion(),
                        ctx.getResources().getString(R.string.current),
                        latest_version));
                version.setTextColor(Color.RED);
            }
        }
        else {
            version.setText(app.getVersion());
            version.setTextColor(default_color);
        }

        // Set last get date and update source
        String update_source = null;
        if (app.getUpdateSource() != null) {
            update_source = app.getUpdateSource().getName();
        }
        String last_check_date = app.getLastCheckDate();
        if (last_check_date == null)
        {
            date.setText(String.format(update_source == null ? "%s %s." : "[" + update_source + "] %s %s.",
                    ctx.getResources().getString(R.string.last_check),
                    ctx.getResources().getString(R.string.never)));
            date.setTextColor(Color.GRAY);
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat();
            date.setText(String.format(update_source == null ? "%s %s." : "[" + update_source + "] %s %s.",
                    ctx.getResources().getString(R.string.last_check),
                    sdf.format(new Date(Long.parseLong(last_check_date) * 1000))));
            date.setTextColor(default_color);
        }

        if (app.getIcon() != null)
        {
            ImageView i = (ImageView) convertView.findViewById(R.id.img);
            i.setImageDrawable(app.getIcon());
        }

        return convertView;
    }

    /**
     * When system apps are displayed, the object list is reordered to put all the user applications at the
     * beginning. The number of user apps is calculated in order to work in this sublist only.
     */
    public void hideSystemApps()
    {
        show_system = false;
        user_app_count = 0;
        for (InstalledApp app : data)
        {
            if (app.isSystemApp()) {
                break; // Ordered list: we can stop counting as soon as we hit a system app.
            }
            ++user_app_count;
        }
    }

    public void showSystemApps() {
        show_system = true;
    }

}
