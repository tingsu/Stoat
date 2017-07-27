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
package com.twsitedapps.homemanager;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;


/***********************************************************************
 * AppLocale - Manage the locale of the app based on user selection
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0.1.8
 */
public class AppLocale
{
    private final static String DEBUG_TAG           = AppLocale.class.getSimpleName();

    // Used to change the language
    private Activity            activity            = null;

    // Used if AppLocale is used as a singleton
    private static AppLocale    appLocale           = null;

    // Value stored if locale is modified
    private Locale              locale              = null;

    // If the user has selected to change the language (tmp indicator)
    private boolean             languageChanged     = false;

    // Used for preferences
    public static final String  preferenceKey       = "listLanguage"; // language
    public static final String  originalKey         = "originalLanguage";
    public static final String  defaultInvalidValue = "none";


    /***********************************************************************
     * AppLocale - Manage the locale of the app based on user selection
     * 
     * @param activity
     *            - Activity - The activity to change the locale language
     * 
     */
    public AppLocale( Activity activity )
    {
        this.activity = activity;
    }


    /***********************************************************************
     * getLocale - Gets the current set locale for this app or null if it isn't
     * set.
     * 
     */
    public Locale getLocale()
    {
        return locale;
    }


    /***********************************************************************
     * languageChanged - If the language was just changed.
     * 
     */
    public boolean languageChanged()
    {
        return languageChanged;
    }


    /***********************************************************************
     * setLanguageChanged - Call this within the onSharedPreferenceChanged
     * method when the user selects a new language within preferences
     * 
     * @param languageChanged
     *            - boolean - true when the language is changed within
     *            preferences.
     * 
     */
    public void setLanguageChanged( final boolean languageChanged )
    {
        this.languageChanged = languageChanged;
    }


    /***********************************************************************
     * getInstance - Return a singleton instance of AppLocale
     * 
     * @param activity
     *            - Activity - The activity to change the locale language
     * 
     */
    public static AppLocale getInstance( Activity activity )
    {
        // If the singleton hasn't been declared
        if( appLocale == null )
        {
            // declare the singleton instance of a SecurePreferences object
            appLocale = new AppLocale( activity );
        }

        return appLocale;
    }


    /***********************************************************************
     * setDefaultLocale - Set the default language for the application
     * 
     */
    public void setDefaultLocale()
    {
        try
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( activity );
            String language = prefs.getString( preferenceKey, defaultInvalidValue );

            // Only modify the language if it isn't the invalid value
            if( !language.equalsIgnoreCase( defaultInvalidValue ) )
            {
                modifyLocale( language );
            }
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "setDefaultLocale : Exception" );
            e.printStackTrace();
        }
    }


    /***********************************************************************
     * modifyLocale - Modify the locale's language setting; which will be saved
     * within the application's preferences
     * 
     * @param language
     *            - String - The new language to be used
     * 
     */
    public void modifyLocale( final String language )
    {
        try
        {
            // Make sure the preference string passed in is valid
            if( language != null && !language.equalsIgnoreCase( "" ) )
            {
                // Store the original language
                storeOriginal();

                // Set the Locale
                locale = new Locale( language );

                // Store the modified local within preferences
                storeLocale( language );

                // Change the Apps default Locale Language setting
                Locale.setDefault( locale );

                // Update the configuration with the new language
                Configuration config = new android.content.res.Configuration();
                config.locale = locale;
                activity.getBaseContext().getResources().updateConfiguration( config, activity.getBaseContext().getResources().getDisplayMetrics() );
            }
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "modifyLocale : Exception" );
            e.printStackTrace();
        }
    }


    /***********************************************************************
     * clearLocale - Remove the user's language selection
     * 
     */
    public boolean clearLocale()
    {
        boolean stored = false;

        try
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( activity );
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString( preferenceKey, defaultInvalidValue );
            stored = editor.commit();

            // Reset to the original language
            String originalLanguage = PreferenceManager.getDefaultSharedPreferences( activity ).getString( originalKey, defaultInvalidValue );
            modifyLocale( originalLanguage );
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "clearLocale : Exception" );
            e.printStackTrace();
        }

        return stored;
    }


    /***********************************************************************
     * storeLocale - Store the locale's language setting within the
     * application's preferences
     * 
     * @param language
     *            - String - The new language to be used
     * 
     */
    private boolean storeLocale( final String language )
    {
        boolean stored = false;

        try
        {
            // Make sure language is valid
            if( language != null && !language.equals( "" ) )
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( activity );
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString( preferenceKey, language );
                stored = editor.commit();
            }
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "storeLocale : Exception" );
            e.printStackTrace();
        }

        return stored;
    }


    /***********************************************************************
     * storeOriginal - Store the locale's original language setting before user
     * modified the value
     * 
     */
    private boolean storeOriginal()
    {
        boolean stored = false;

        try
        {
            // Make sure the activity isn't null and only set if the default is
            // stored; "none"
            if( activity != null &&
                PreferenceManager.getDefaultSharedPreferences( activity ).getString( originalKey, defaultInvalidValue ).equals( defaultInvalidValue ) )
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( activity );
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString( originalKey, activity.getResources().getConfiguration().locale.getISO3Language() );
                stored = editor.commit();
            }
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "storeOriginal : Exception" );
            e.printStackTrace();
        }

        return stored;
    }
}