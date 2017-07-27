/*****************************************************************************
 *    Copyright 2011 Twisted Apps LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

/*****************************************************************************
 * AppPreferences - Preferences
 * 
 * @author Russell T Mackler
 * @version 1.0
 * @since 1.0
 */
package com.twsitedapps.homemanager;

import java.util.FormatFlagsConversionMismatchException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

/*****************************************************************************
 * Preferences - Used for preferences
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    private final static String DEBUG_TAG = Preferences.class.getSimpleName();
    
    private Activity thisActivity = null;
    
    /*
     * (non-Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        thisActivity = this;

        try
        {
            addPreferencesFromResource( R.xml.preferences );
    
            // Go to the Twisted home manager's blog version section
            Preference version = (Preference) findPreference( "version" );
            version.setOnPreferenceClickListener( new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick( Preference preference )
                {
                    try
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( StaticConfig.THM_URL ) ) );
                    }
                    catch( NullPointerException e )
                    {
                        Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "version.setOnPreferenceClickListener : NullPointerException" );
                        e.printStackTrace();
                    }

                    return true;
                }
            } );
            
            // Go to the Twisted home manager's blog
            Preference blog = (Preference) findPreference( "blog" );
            blog.setOnPreferenceClickListener( new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick( Preference preference )
                {
                    try
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( StaticConfig.THM_URL ) ) );
                    }
                    catch( NullPointerException e )
                    {
                        Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "blog.setOnPreferenceClickListener : NullPointerException" );
                        e.printStackTrace();
                    }
    
                    return true;
                }
            } );
            
            // Get the custom preference send support e-mail
            Preference supportEmail = (Preference) findPreference( "supportEmail" );
            supportEmail.setOnPreferenceClickListener( new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick( Preference preference )
                {
                    /* Create the Intent */
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    
                    /* Fill it with Data */
                    emailIntent.setType( "plain/text" );
                    emailIntent.putExtra( android.content.Intent.EXTRA_EMAIL, new String[]{"twistedapps@twistedapps.org"} );
                    
                    final String subject = "THM - email " + getResources().getString( R.string.version_num );
                    
                    emailIntent.putExtra( android.content.Intent.EXTRA_SUBJECT, subject );
                    emailIntent.putExtra( android.content.Intent.EXTRA_TEXT, "Hi, " );
    
                    /* Send it off to the Activity-Chooser */
                    startActivity( Intent.createChooser( emailIntent, "Send mail..." ) );
    
                    return true;
                }
            } );
    
            // Allow the user to rollback to an earlier release
            Preference rollback = (Preference) findPreference( "rollback" );
            rollback.setOnPreferenceClickListener( new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick( Preference preference )
                {
                    try
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( StaticConfig.ROLL_URL ) ) );
                    }
                    catch( NullPointerException e )
                    {
                        Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "rollback.setOnPreferenceClickListener : NullPointerException" );
                        e.printStackTrace();
                    }
    
                    return true;
                }
            } );
            
            // Allow the user to see open source project
            Preference github = (Preference) findPreference( "github" );
            github.setOnPreferenceClickListener( new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick( Preference preference )
                {
                    try
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( StaticConfig.GITHUB_URL ) ) );
                    }
                    catch( NullPointerException e )
                    {
                        Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "github.setOnPreferenceClickListener : NullPointerException" );
                        e.printStackTrace();
                    }
    
                    return true;
                }
            } );
            
            // Go to the Twisted home manager's Android Market location
            Preference market = (Preference) findPreference( "market" );
            market.setOnPreferenceClickListener( new OnPreferenceClickListener()
            {
                public boolean onPreferenceClick( Preference preference )
                {
                    try
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( StaticConfig.THM_MRK ) ) );
                    }
                    catch( NullPointerException e )
                    {
                        Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "market.setOnPreferenceClickListener : NullPointerException" );
                        e.printStackTrace();
                    }
    
                    return true;
                }
            } );
        }
        catch( FormatFlagsConversionMismatchException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : FormatFlagsConversionMismatchException" );
            e.printStackTrace();
        }
        catch( NullPointerException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : NullPointerException" );
            e.printStackTrace();
        }

    } // End onCreate
    
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override protected void onResume()
    {
        super.onResume();
        // Set the default language if the user changes it
        AppLocale.getInstance( thisActivity ).setDefaultLocale();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    }


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
    }
    
    
    /*
     * (non-Javadoc)
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
    @Override public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
    {
        try
        {
            // Detect when the user has changed the language option
            if ( key.equals( "listLanguage" ) )
            {
                // If there is a language change
                // Make sure to set a flag for the main activity to restart
                AppLocale.getInstance( thisActivity ).setLanguageChanged( true );
                
                // Check to see if we should clear the user's selected Locale language
                if ( sharedPreferences.getString(key, AppLocale.defaultInvalidValue ).equals( AppLocale.defaultInvalidValue ) )
                {
                    // Clear the user's select Locale and use device real locale
                    AppLocale.getInstance( thisActivity ).clearLocale();
                }
                
                // Preferences reloads, but language is updated?
                Intent intent = getIntent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        }
        catch( NullPointerException e )
        {
            e.printStackTrace();
        }
    }
    

    /*****************************************************************************
     * getPrefs - Get All of the current preferences
     * 
     * @param a - Activity - The current Activity calling this method
     *  
     */
    static public void getPrefs( final Activity a )
    {
        try
        {
            // Get the Preference Manager
            StaticConfig.preferences = PreferenceManager.getDefaultSharedPreferences( a );
               
            // Get Twisted home manager's theme 
            String theme = StaticConfig.preferences.getString( StaticConfig.THEME, Integer.toString( 0 ) );
            StaticConfig.theme = Integer.parseInt( theme );           
        }
        catch( FormatFlagsConversionMismatchException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : FormatFlagsConversionMismatchException" );
            e.printStackTrace();
        }
        catch( NullPointerException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : NullPointerException" );
            e.printStackTrace();
        }

    } // End getPrefs
} // End Class AppPreferences