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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.util.Arrays;

public class UpdateSourceChooser
{
    private String selected;

    /**
     * Create a chooser dialog that allows the user to choose his update source.
     * @param app The target application.
     */
    public void createSourceChooserDialog(final InstalledApp app, final Activity activity)
    {
        final String[] sources = UpdateSource.getSources(app, activity.getApplicationContext());
        int checked_item = app.getUpdateSource() == null ? -1 : Arrays.asList(sources).indexOf(app.getUpdateSource().getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.available_update_sources);
        builder.setSingleChoiceItems(sources, checked_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected = sources[which];
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                app.setUpdateSource(UpdateSource.getSource(selected, activity.getApplicationContext()));
                Log.v(MainActivity.TAG, app.getDisplayName() + "'s update source set to " + selected);
                AppPersistence.getInstance(activity.getApplicationContext()).updateApp(app);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {} // Cancel: don't do anything.
        });

        builder.show();
    }
}
