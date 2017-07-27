package fr.kwiatkowski.ApkTrack;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity
{
    public final static String KEY_PREF_SEARCH_ENGINE = "pref_search_engine";
    public final static String KEY_PREF_BACKGROUND_CHECKS = "pref_background_checks";
    public final static String KEY_PREF_WIFI_ONLY = "pref_wifi_only";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
