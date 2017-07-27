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
import java.util.Collections;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


/*****************************************************************************
 * HomeManagerActivity - Activity - display the installed home apps via a
 * listview. 
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class HomeManagerActivity extends Activity
{
    private final static String      DEBUG_TAG                = HomeManagerActivity.class.getSimpleName();

    // This activity
    private Activity                 thisActivity             = null;
    
    // The Array Adapter for this ListView
    private HomeManagerArrayAdapter  homeManagerArrayAdapter;
    
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
            setContentView( R.layout.homemanageractivity );
    
            thisActivity = this;
    
            // Get the preferences for this app
            Preferences.getPrefs( thisActivity );
    
            // Create the cached ArrayList of home applications
            listAppInfo = new ArrayList<AppInfo>();
    
            // Set the ListView
            ListView mainListView = (ListView) findViewById( R.id.main_listview );
            
            // Set the background color to black
            mainListView.setBackgroundColor( Color.BLACK );
    
            // Create the Array Adapter
            homeManagerArrayAdapter = new HomeManagerArrayAdapter( thisActivity, listAppInfo );
    
            // Set the adapter for the ListView
            mainListView.setAdapter( homeManagerArrayAdapter );
    
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
                    
                    Log.i( DEBUG_TAG, "Package Name = [" + pkgname + "]" );
    
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
    
            // Clear Default logic
            Button btnClearDefault = (Button) findViewById( R.id.btnClearDefault );
            btnClearDefault.setOnClickListener( new View.OnClickListener() 
            {
                public void onClick( View v )
                {
                    ResolveInfo res = AppInfo.getHomeApp( thisActivity );
    
                    if( res != null )
                    {
                        // Get the name of the current selected home app
                        String name = res.activityInfo.name;
    
                        // Get the package name of the current selected home app
                        String pkgname = res.activityInfo.packageName;
    
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
                    }
                    else
                    {
                        Toast.makeText( getApplicationContext(), getResources().getString(R.string.noDefaultSet), Toast.LENGTH_SHORT ).show();
                    }
                }
            } );
    
            // Set Default logic
            Button btnSetDefault = (Button) findViewById( R.id.btnSetDefault );
            btnSetDefault.setOnClickListener( new View.OnClickListener() 
            {
                public void onClick( View v )
                {
                    if( AppInfo.getHomeApp( thisActivity ) == null )
                    {
                        // Used to have the user select the default app...
                        Intent selector = new Intent( Intent.ACTION_MAIN );
                        selector.addCategory( Intent.CATEGORY_HOME );
                        selector.setComponent( new ComponentName( "android", "com.android.internal.app.ResolverActivity" ) );
                        startActivity( selector );
                    }
                    else
                    {
                        // Display user feedback
                        Toast.makeText( getApplicationContext(), getResources().getString( R.string.default_set ), Toast.LENGTH_SHORT ).show();
                    }
                }
            } );
    
            // Get Home Apps
            Button btnGetHomeApps = (Button) findViewById( R.id.btnGetHomeApps );
            btnGetHomeApps.setOnClickListener( new View.OnClickListener() 
            {
                public void onClick( View v )
                {
                    // Only allow the the Get home activity if installed home list
                    // is built
                    if( isFinishedBuildingList )
                    {
                        // Inform User that all home apps listed are on their market
                        Toast.makeText( getApplicationContext(), getResources().getString( R.string.homeapp_info ), Toast.LENGTH_SHORT ).show();
                        
                        try
                        {
                            // Start the Market App Activity
                            Intent getHomeIntent = new Intent( StaticConfig.GETHOME_INTENT );
                            
                            if( Util.isCallable( thisActivity, getHomeIntent ) )
                            { 
                                // Start Process Image activity
                                startActivity( getHomeIntent );
                            }
                            else
                            {
                                // Display user feedback if the home app is not callable
                                Toast.makeText( getApplicationContext(), getResources().getString( R.string.homeAppNotCallable ), Toast.LENGTH_SHORT ).show();
                            }
                        }
                        catch ( ActivityNotFoundException e )
                        {
                            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : ActivityNotFoundException" );
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        // Display user feedback
                        Toast.makeText( getApplicationContext(), getResources().getString( R.string.please_wait ), Toast.LENGTH_SHORT ).show();
                    }
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
            
            // Detect that the user has changed the language within preferences
            if ( AppLocale.getInstance( thisActivity ).languageChanged() )
            {
                // Immediately set the indicator that the language change was detected 
                AppLocale.getInstance( thisActivity ).setLanguageChanged( false );
                
                // Restart this activity based on the new locale's language selection
                Intent intent = getIntent();
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                finish();
                startActivity( intent );
            }
            
            // Get the preferences for this app
            Preferences.getPrefs( thisActivity );
            
            // Check if the Notification should be shown
            if ( PreferenceManager.getDefaultSharedPreferences( thisActivity ).getBoolean( StaticConfig.NOTIFICATION_KEY, true ) )
            {
                // Enable quick selecting of home apps from the notification bar
                Util.showNotification( thisActivity );
            }
            else
            {
                // Cancel the notification
                NotificationManager notificationManager = (NotificationManager) thisActivity.getSystemService( Context.NOTIFICATION_SERVICE );
                notificationManager.cancelAll();
            }

            // Set this so we know the installed Home App list has been built
            isFinishedBuildingList = false;

            // Get cached images (First image within each directory within the
            // ListView)
            new GetAppCacheTask( thisActivity.getApplicationContext(), listAppInfo, homeManagerArrayAdapter, isFinishedBuildingList ).execute();
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


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override public boolean onCreateOptionsMenu( final Menu menu )
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu, menu );
        return true;
    } // End onCreateOptionsMenu


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override public boolean onOptionsItemSelected( final MenuItem item )
    {
        boolean return_value = false;

        // Handle item selection
        switch( item.getItemId() )
        {
        case R.id.sortName:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppInfo.NAME_ORDER );

            // Refresh the ListView
            homeManagerArrayAdapter.notifyDataSetChanged();

            return_value = true;
            break;
        }
        case R.id.sortNameDec:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppInfo.NAME_ORDER_DECEND );

            // Refresh the ListView
            homeManagerArrayAdapter.notifyDataSetChanged();
            return_value = true;
            break;
        }
        case R.id.sortDefault:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppInfo.DEFAULT_ORDER );

            // Refresh the ListView
            homeManagerArrayAdapter.notifyDataSetChanged();

            return_value = true;
            break;
        }
        case R.id.sortMemory:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppInfo.MEMORY_ORDER );

            // Refresh the ListView
            homeManagerArrayAdapter.notifyDataSetChanged();

            return_value = true;
            break;
        }
        case R.id.preferences:
        {
            Intent preferencesIntent = new Intent();
            preferencesIntent.setAction( StaticConfig.PREFERENCES_INTENT );
            startActivity( preferencesIntent );

            return_value = true;
            break;
        }
        default:
            return_value = super.onOptionsItemSelected( item );
            break;
        } // End Switch

        return return_value;
    } // End onOptionsItemSelected
    
} // End HomeManagerActivity
// EOF