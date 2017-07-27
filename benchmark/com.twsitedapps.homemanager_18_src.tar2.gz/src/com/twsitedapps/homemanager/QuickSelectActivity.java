/*****************************************************************************
 * Copyright 2011 Twisted Apps LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.twsitedapps.homemanager;

import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


/*****************************************************************************
 * QuickSelectActivity - Activity - display the installed home apps via a
 * listview after selecting a notification. 
 * 
 * @author Russell T Mackler
 * @version 1.0
 * @since 1.0.1.8
 */
public class QuickSelectActivity extends Activity
{
    private final static String      DEBUG_TAG                = QuickSelectActivity.class.getSimpleName();

    // This activity
    private Activity                 thisActivity             = null;
    
    // The Array Adapter for this ListView
    private QuickSelectArrayAdapter  quickSelectArrayAdapter;
    
    // The cached list of installed home applications
    public static ArrayList<AppInfo> listAppInfo;

    // Set to true when the Async Task is finished building the installed home app list
    public static boolean            isFinishedBuildingList   = false;


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override public void onCreate( Bundle savedInstanceState )
    {
        try
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.quickselectactivity );
    
            thisActivity = this;
    
            // Get the preferences for this app
            Preferences.getPrefs( thisActivity );
    
            // Create the cached ArrayList of home applications
            listAppInfo = new ArrayList<AppInfo>();
    
            // Set the ListView
            ListView mainListView = (ListView) findViewById( R.id.quickselect_listview );
    
            // Create the Array Adapter
            quickSelectArrayAdapter = new QuickSelectArrayAdapter( thisActivity, listAppInfo );
    
            // Set the adapter for the ListView
            mainListView.setAdapter( quickSelectArrayAdapter );
    
            // ListView LongClick Logic
            mainListView.setLongClickable( true );
            mainListView.setOnItemLongClickListener( new OnItemLongClickListener() 
            {
                public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id )
                {
                    // Get the name of the current selected home app
                    String name = listAppInfo.get( position ).getappName();
    
                    // Get the package name of the current selected home app 
                    String pkgname = listAppInfo.get( position ).getpackageName();
    
                    // Make the right Intent to start InstalledAppDetails
                    Intent it = Util.makeIntentInstalledAppDetails( pkgname );
    
                    // Make sure the selected application is Callable
                    if( Util.isCallable( thisActivity, it ) )
                    {
                        // Start the selected application
                        startActivity( it );
                    }
                    else
                    {
                        // Display user feedback if the home app is not callable
                        Toast.makeText( getApplicationContext(), getResources().getString(R.string.installed) + name + getResources().getString( R.string.not_callable ), Toast.LENGTH_SHORT ).show();
                    }
    
                    return ( true );
                }
            } );
    
            // Launch a listed application if selected
            mainListView.setOnItemClickListener( new OnItemClickListener() 
            {
                public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                {
                    try
                    {
                        // Get the name of the current selected home app
                        String name = listAppInfo.get( position ).getappName();
    
                        // Get the package name of the current selected home app
                        String packagename = listAppInfo.get( position ).getpackageName();
    
                        // Build the intent to launch
                        Intent AppIntent = new Intent( Intent.ACTION_MAIN );
                        AppIntent.addCategory( Intent.CATEGORY_HOME );
                        AppIntent.setPackage( packagename );
                        AppIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
    
                        // Make sure the selected application is Callable
                        if( Util.isCallable( thisActivity, AppIntent ) )
                        {                           
                            // Start the selected application
                            startActivity( AppIntent );
                        }
                        else
                        {
                            // Display user feedback if the home app is not callable
                            Toast.makeText( getApplicationContext(), getResources().getString(R.string.installed) + name + getResources().getString( R.string.not_callable ), Toast.LENGTH_SHORT ).show();
                        }
                    }
                    catch( NullPointerException e )
                    {
                        Log.e( DEBUG_TAG, "Launch App : NullPointerException" );
                        e.printStackTrace();
                    }
                    catch( SecurityException e )
                    {
                        Toast.makeText( getApplicationContext(), getResources().getString( R.string.securityException ), Toast.LENGTH_SHORT ).show();
                        Log.e( DEBUG_TAG, "Launch App : SecurityException" );
                        e.printStackTrace();
                    }
    
                } // End onItemClick
            } ); // End mainListView.setOnItemClickListener
    
            // Clear Notification
            Button btnClearNotification = (Button) findViewById( R.id.btnClearNotification );
            btnClearNotification.setOnClickListener( new View.OnClickListener() 
            {
                public void onClick( View v )
                {
                    // Cancel the notification
                    NotificationManager notificationManager = (NotificationManager) thisActivity.getSystemService( Context.NOTIFICATION_SERVICE );
                    notificationManager.cancelAll();
                }
            } );
            
            // Disable Notification
            Button btnDisableNotification = (Button) findViewById( R.id.btnDisableNotification );
            btnDisableNotification.setOnClickListener( new View.OnClickListener() 
            {
                public void onClick( View v )
                {
                    // Store the digest so we can check against it later
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( thisActivity ).edit();
                    editor.putBoolean( StaticConfig.NOTIFICATION_KEY, false );
                    editor.commit();
                }
            } );
        }
        catch ( NullPointerException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : NullPointerException" );
            e.printStackTrace();
        }
        catch ( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : Exception" );
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

        try
        {
            // Set the default language if the user changes it
            AppLocale.getInstance( thisActivity ).setDefaultLocale();
            
            // Get the preferences for this app
            Preferences.getPrefs( thisActivity );

            // Set this so we know the installed Home App list has been built
            isFinishedBuildingList = false;

            // Get cached images (First image within each directory within the
            // ListView)
            new GetAppCacheTask( thisActivity.getApplicationContext(), listAppInfo, quickSelectArrayAdapter, isFinishedBuildingList ).execute();
        }
        catch( IllegalStateException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onResume : IllegalStateException" );
            e.printStackTrace();
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onResume : Exception" );
            e.printStackTrace();
        }
    } // End onResume

} // End QuickSelectActivity
// EOF