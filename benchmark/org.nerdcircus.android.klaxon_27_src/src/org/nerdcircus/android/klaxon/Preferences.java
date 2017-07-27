/* 
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nerdcircus.android.klaxon;

import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Build;

import android.util.Log;

import org.nerdcircus.android.klaxon.Changelog;
import org.nerdcircus.android.klaxon.ReplyList;

public class Preferences extends PreferenceActivity {
    
    private static final Uri CHANGELOG_URI = Uri.parse("http://code.google.com/p/klaxon/wiki/ChangeLog");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference replylist = this.findPreference("edit_replies");
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClass(this, ReplyList.class);
        replylist.setIntent(i);

        // rig up the Changelog
        replylist = this.findPreference("changelog");
        i = new Intent(Intent.ACTION_MAIN);
        i.setClass(this, Changelog.class);
        replylist.setIntent(i);

        //disable the "Consume SMS" option if the build is too low
        //NB: there's no code to act on this, since the abortBroadcast() 
        // call will not break anything when called in < 1.6
        Log.d("BUILDVERSION", Build.VERSION.SDK);
        if(Integer.valueOf(Build.VERSION.SDK) <= Integer.valueOf(3)){
            CheckBoxPreference csp = (CheckBoxPreference) this.findPreference("consume_sms_message");
            csp.setChecked(false);
            csp.setEnabled(false);
        }
        
    }

}

