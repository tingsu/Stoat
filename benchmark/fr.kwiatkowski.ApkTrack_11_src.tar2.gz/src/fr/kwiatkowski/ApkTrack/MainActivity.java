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

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends ListActivity
{
    public static String TAG = "ApkTrack"; // Tag used for debug messages.
    public static String ACTIVITY_SOURCE = "activity"; // Tag used to identify the origin of a version check request.
    public static String CURRENT_SEARCH = null;

    private AppAdapter adapter;
    private AppPersistence persistence;
    private List<InstalledApp> installed_apps;
    private Comparator<InstalledApp> comparator = new UpdatedSystemComparator();

    // This receiver is notified when the RequesterService has performed a check.
    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            InstalledApp app = intent.getParcelableExtra(RequesterService.TARGET_APP_PARAMETER);
            // Restore the app's icon, which cannot be passed inside the Parcelable.
            persistence.restoreIcon(app);

            VersionGetResult res = (VersionGetResult) intent.getSerializableExtra(RequesterService.UPDATE_RESULT_PARAMETER);

            if (app == null || res == null)
            {
                Log.v(MainActivity.TAG, "Error: MainActivity's Broadcast receiver received an Intent with missing parameters! "
                        + "app=" + app + " / res=" + res);
                abortBroadcast(); // No need to try with the BroadcastHandler.
                return;
            }

            Log.v(MainActivity.TAG, "MainActivity received " + res.getStatus() + " for " + app.getDisplayName() + ".");

            // Network error: display a message to indicate the failure if the user initiated the request.
            // If the request was launched by the background service, don't confuse the user with an error message.
            if (res.getStatus() == VersionGetResult.Status.NETWORK_ERROR
                    && ACTIVITY_SOURCE.equals(intent.getStringExtra(RequesterService.SOURCE_PARAMETER)))
            {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.network_error),
                        Toast.LENGTH_SHORT).show();
            }

            // Find the application in the list.
            int index = installed_apps.indexOf(app);
            if (index != -1)
            {
                // Replace the old app with the new one in the displayed list. No need to sort again, this is a replacement.
                installed_apps.remove(index);
                installed_apps.add(index, app);
                app.setCurrentlyChecking(false);
                notifyAdapterInUIThread();
            }
            else {
                Log.v(MainActivity.TAG, "Received an APP_CHECKED intent for " + app.getDisplayName()
                        + ", but it's not in the list..?"); // Might happen if a user requests an update and
                                                            // uninstalls the app + refreshes before it completes.
            }

            abortBroadcast();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // New thread to load the data without hanging the UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                persistence = AppPersistence.getInstance(getApplicationContext());
                installed_apps = getInstalledAps();
                adapter = new AppAdapter(installed_apps);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.setListAdapter(adapter);
                    }
                });

                // Show a dialog when a user long-clicks on an application.
                getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        InstalledApp app = (InstalledApp) getListView().getItemAtPosition(position);
                        new UpdateSourceChooser().createSourceChooserDialog(app, MainActivity.this);
                        return true;
                    }
                });

                // Hide the spinner now
                final LinearLayout ll = (LinearLayout) findViewById(R.id.spinner);
                ll.post(new Runnable() {
                    @Override
                    public void run() {
                        ll.setVisibility(View.GONE);
                    }
                });
            }
        }).start();

        WakefulIntentService.scheduleAlarms(new PollReciever(), this);
    }

    /**
     * Set up the BroadcastReceiver on resume. Activity-or-Notification pattern.
     */
    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(RequesterService.APP_CHECKED);
        filter.setPriority(2);
        registerReceiver(receiver, filter);
    }

    /**
     * Disable the BroadcastReceiver on pause. Activity-or-Notification pattern.
     */
    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    /**
     * Handles the ACTION_SEARCH intent, caused by a user search.
     */
    public void onNewIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            new Thread() { // Move out of UI
                @Override
                public void run() {
                    // Show spinner
                    final LinearLayout ll = (LinearLayout) findViewById(R.id.spinner);
                    ll.post(new Runnable() {
                        @Override
                        public void run() {
                            ll.setVisibility(View.VISIBLE);
                        }
                    });

                    List<InstalledApp> new_list = persistence.getStoredApps(query);
                    if (new_list.size() == 0) // No results found. Do not empty the list.
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.search_no_result),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Hide the spinner.
                        ll.post(new Runnable() {
                            @Override
                            public void run() {
                                ll.setVisibility(View.GONE);
                            }
                        });

                        return;
                    }

                    CURRENT_SEARCH = query;
                    installed_apps.clear();
                    installed_apps.addAll(new_list);
                    Collections.sort(installed_apps, comparator);
                    if (!adapter.isShowSystem()) {
                        adapter.hideSystemApps();
                    }
                    Log.v(TAG, "Search " + query + ": " + installed_apps.size() + " apps matched.");

                    notifyAdapterInUIThread();
                    // Hide the spinner.
                    ll.post(new Runnable() {
                        @Override
                        public void run() {
                            ll.setVisibility(View.GONE);
                        }
                    });
                }
            }.start();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (!hasFocus || installed_apps == null || adapter == null) {
            return;
        }

        // Refreshes have bad interactions with partially populated application lists.
        // It is better to wait until the user is done with his search.
        if (CURRENT_SEARCH != null) {
            return;
        }

        // Nothing to do if the service hasn't detected any updates.
        if (BroadcastHandler.getReloadAction() == BroadcastHandler.reload_action.NONE) {
            return;
        }

        // When focus is gained, refresh the application list. It may have been changed by the
        // background service.
        // Keep a copy, because the main thread will reset the variable immediately.
        final BroadcastHandler.reload_action action = BroadcastHandler.getReloadAction();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // Show spinner
                final LinearLayout ll = (LinearLayout) findViewById(R.id.spinner);
                ll.post(new Runnable() {
                    @Override
                    public void run() {
                        ll.setVisibility(View.VISIBLE);
                    }
                });
                Log.d(TAG, "MainActivity.onWindowFocusChanged: action_on_activity_focus_gain = " + action);

                // Reload installed apps from the database
                if (action == BroadcastHandler.reload_action.RELOAD)
                {
                    installed_apps.clear();
                    installed_apps.addAll(getInstalledAps());
                }
                // Redetect installed apps
                else if (action == BroadcastHandler.reload_action.REFRESH) {
                    onRefreshAppsClicked();
                }

                notifyAdapterInUIThread();

                // Hide spinner
                ll.post(new Runnable() {
                    @Override
                    public void run() {
                        ll.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
        BroadcastHandler.setReloadAction(BroadcastHandler.reload_action.NONE);
    }

    /**
     * Retreives the list of applications installed on the device.
     * If no data is present in the database, the list is generated.
     */
    private List<InstalledApp> getInstalledAps()
    {
        List<InstalledApp> applist = persistence.getStoredApps();
        if (applist.size() == 0) {
            applist = persistence.refreshInstalledApps(true);
        }

        Collections.sort(applist, comparator);

        return applist;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.action_bar_menu, menu);
        SearchManager seaman = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        SearchView search_view = (SearchView) menu.findItem(R.id.search).getActionView();
        search_view.setSearchableInfo(seaman.getSearchableInfo(getComponentName()));
        search_view.setSubmitButtonEnabled(true);

        // onClose is apparently buggy, and I have to use this callback instead.
        // Source: https://stackoverflow.com/questions/13920960/searchview-oncloselistener-does-not-get-invoked
        search_view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v)
            {
                CURRENT_SEARCH = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final LinearLayout ll = (LinearLayout) findViewById(R.id.spinner);
                        ll.post(new Runnable() {
                            @Override
                            public void run() {
                                ll.setVisibility(View.VISIBLE);
                            }
                        });
                        installed_apps.clear();
                        installed_apps.addAll(persistence.getStoredApps());
                        Collections.sort(installed_apps, comparator);
                        if (!adapter.isShowSystem()) {
                            adapter.hideSystemApps();
                        }
                        notifyAdapterInUIThread();

                        ll.post(new Runnable() {
                            @Override
                            public void run() {
                                ll.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This function handles user input through the action bar.
     * Three buttons exist as of yet:
     * - Get the latest version for all installed apps
     * - Regenerate the list of installed applications
     * - Hide / show system applications
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.check_all_apps:
                for (InstalledApp ia : installed_apps) {
                    performVersionCheck(ia);
                }
                return true;

            case R.id.refresh_apps:
                if (CURRENT_SEARCH != null) { // Do not allow refreshes during a search.
                    return true;
                }
                item.setEnabled(false);

                // Do this in a separate thread, or the UI hangs.
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        onRefreshAppsClicked();
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run() {
                                item.setEnabled(true);
                            }
                        });
                    }
                }).start();
                return true;

            case R.id.show_system:
                if (!adapter.isShowSystem())
                {
                    if (comparator instanceof UpdatedSystemComparator) {
                        comparator = new UpdatedComparator();
                    }
                    else if (comparator instanceof SystemComparator) {
                        comparator = new AlphabeticalComparator();
                    }
                    Collections.sort(installed_apps, comparator);
                    adapter.showSystemApps();
                    item.setTitle(R.string.hide_system_apps);
                }
                else
                {
                    if (comparator instanceof AlphabeticalComparator) {
                        comparator = new SystemComparator();
                    }
                    else if (comparator instanceof UpdatedComparator) {
                        comparator = new UpdatedSystemComparator();
                    }
                    Collections.sort(installed_apps, comparator);
                    adapter.hideSystemApps();
                    item.setTitle(R.string.show_system_apps);
                }
                adapter.notifyDataSetChanged();
                return true;

            case R.id.sort_type:
                if (comparator instanceof UpdatedSystemComparator)
                {
                    item.setTitle(R.string.sort_type_updated);
                    comparator = new SystemComparator();
                }
                else if (comparator instanceof SystemComparator)
                {
                    item.setTitle(R.string.sort_type_alpha);
                    comparator = new UpdatedSystemComparator();
                }
                else if (comparator instanceof AlphabeticalComparator)
                {
                    item.setTitle(R.string.sort_type_alpha);
                    comparator = new UpdatedComparator();
                }
                else if (comparator instanceof UpdatedComparator)
                {
                    item.setTitle(R.string.sort_type_updated);
                    comparator = new AlphabeticalComparator();
                }
                Collections.sort(installed_apps, comparator);
                adapter.notifyDataSetChanged();
                return true;

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.search:
                //onSearchRequested();
                return true; // Handled by the SearchManager

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        InstalledApp app = (InstalledApp) getListView().getItemAtPosition(position);
        performVersionCheck(app);
    }

    private void performVersionCheck(InstalledApp app)
    {
        if (app != null && !app.isCurrentlyChecking())
        {
            // The loader icon will be displayed from here on
            app.setCurrentlyChecking(true);
            notifyAdapterInUIThread();
            Intent i = new Intent(this, RequesterService.class);
            i.putExtra(RequesterService.TARGET_APP_PARAMETER, app);
            i.putExtra(RequesterService.SOURCE_PARAMETER, ACTIVITY_SOURCE);
            startService(i);
        }
    }

    private void onRefreshAppsClicked()
    {
        final List<InstalledApp> new_list = persistence.refreshInstalledApps(false);

        // Remove the ones we already have. We wouldn't want duplicates
        final ArrayList<InstalledApp> uninstalled_apps = new ArrayList<InstalledApp>(installed_apps);
        uninstalled_apps.removeAll(new_list);

        // Check for updated apps
        int updated_count = 0;
        for (InstalledApp ai : new_list)
        {
            if (installed_apps.contains(ai) && ai.getVersion() != null &&
                    !ai.getVersion().equals(installed_apps.get(installed_apps.indexOf(ai)).getVersion()))
            {
                updated_count += 1;
                // The following lines may look strange, but it works because of the equality operation
                // override for InstalledApp: objects are matched on their package name alone.
                installed_apps.remove(ai); // Removes the app with the same package name as ai
                installed_apps.add(ai);    // Adds the new app
                Collections.sort(installed_apps, comparator);
            }
        }
        if (updated_count > 0) {
            notifyAdapterInUIThread();
        }

        // Keep the new applications
        new_list.removeAll(installed_apps);

        final int final_updated_count = updated_count;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources res = getResources();
                Toast t = Toast.makeText(getApplicationContext(),
                        res.getString(R.string.new_apps_detected, new_list.size()) +
                        res.getString(R.string.apps_updated, final_updated_count) +
                        res.getString(R.string.apps_deleted, uninstalled_apps.size()),
                        Toast.LENGTH_SHORT);
                t.show();
            }
        });

        // Remove uninstalled applications from the list
        if (uninstalled_apps.size() > 0)
        {
            installed_apps.removeAll(uninstalled_apps);
            for (InstalledApp app : uninstalled_apps) {
                persistence.removeFromDatabase(app);
            }
            notifyAdapterInUIThread();
        }

        // Add new applications
        if (new_list.size() > 0)
        {
            for (InstalledApp app : new_list)
            {
                // Save the newly detected applications in the database.
                persistence.insertApp(app);
                installed_apps.add(app);
            }

            Collections.sort(installed_apps, comparator);
            notifyAdapterInUIThread();
        }
    }

    private void notifyAdapterInUIThread()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}

