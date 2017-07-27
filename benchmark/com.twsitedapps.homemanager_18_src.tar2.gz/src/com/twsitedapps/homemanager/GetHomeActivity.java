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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/*****************************************************************************
 * GetHomeActivity - Activity - which shows all of the potential home apps
 * on the Android Play Store. 
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class GetHomeActivity extends Activity
{
    private final static String  DEBUG_TAG    = GetHomeActivity.class.getSimpleName();

    private Activity             thisActivity = null;
    private GetHomeArrayAdapter  getHomeArrayAdapter;
    private ArrayList<AppMarket> listAppInfo;


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override public void onCreate( Bundle savedInstanceState )
    {
        try
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.gethomeactivity );
    
            thisActivity = this;
    
            // Get the preferences for this app
            Preferences.getPrefs( thisActivity );
    
            // Create the container for Market apps
            listAppInfo = new ArrayList<AppMarket>();
            // Create the list of Market apps
            createAppMarketList();
    
            // View
            ListView mainListView = (ListView) findViewById( R.id.main_gethomelistview );
            mainListView.setBackgroundColor( Color.BLACK );
    
            // Adapter
            getHomeArrayAdapter = new GetHomeArrayAdapter( thisActivity, listAppInfo );
    
            // Set the adapter for the ListView
            mainListView.setAdapter( getHomeArrayAdapter );
    
            // ListView LongClick Logic
            mainListView.setLongClickable( false );
            mainListView.setOnItemLongClickListener( new OnItemLongClickListener() 
            {
                public boolean onItemLongClick( AdapterView<?> parent, View view, int position, long id )
                {
                    return ( true );
                }
            } );
    
            // Launch a listed application if selected
            mainListView.setOnItemClickListener( new OnItemClickListener() 
            {
                public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                {
                    // Get Home App from the market
                    try
                    {
                        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( listAppInfo.get( position ).getmarket() ) ) );
                    }
                    catch( ActivityNotFoundException e )
                    {
                        Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onCreate : ActivityNotFoundException" );
                        e.printStackTrace();
                        
                        Toast.makeText( getApplicationContext(), getResources().getString( R.string.market_failed ), Toast.LENGTH_SHORT ).show();
                    }
                } // End onItemClick
            } ); // End mainListView.setOnItemClickListener
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

            // Compare installed vs known Home Apps on market
            new GetAppCacheTask().execute();
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


    /*****************************************************************************
     * GetAppCacheTask - AsyncTask - Get app information. 
     * 
     * @author Russell T Mackler
     * @version 1.0
     * @since 1.0
     */
    class GetAppCacheTask extends AsyncTask<Void, int[], Integer>
    {

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override protected Integer doInBackground( Void... unused )
        {
            // Keep track of RuntimeException if Media is no longer mounted
            Integer isOK = 1;

            try
            {               
                // Loop through all activities and get their information
                for ( int i = 0; i < listAppInfo.size(); i++ )
                {
                    for( int j = 0; j < HomeManagerActivity.listAppInfo.size(); j++ )
                    {
                        if ( listAppInfo.get( i ).getappName().equals( HomeManagerActivity.listAppInfo.get( j ).getappName() ) )
                        {
                            listAppInfo.get( i ).setInstalled( true );
                            listAppInfo.get( i ).seticonDrawable( HomeManagerActivity.listAppInfo.get( j ).geticonDrawable() );
                            
                            int publish[] = { 1 };
                            publishProgress( publish );
                        }
                    }
                } // End for loop Get Home installed apps

            }
            catch( RuntimeException e )
            {
                Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "doInBackground : RuntimeException" );
                e.printStackTrace();

                // Set to 0 to exit this activity
                isOK = 0;
            }
            catch( OutOfMemoryError e )
            {
                Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "doInBackground : OutOfMemoryError" );
                e.printStackTrace();

                // Set to 0 to exit this activity
                isOK = 0;
            }

            return ( isOK );
        } // End doInBackground


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override protected void onProgressUpdate( int[]... publish )
        {
            if( listAppInfo != null )
            {
                getHomeArrayAdapter.notifyDataSetChanged();
            }
        } // End onProgressUpdate


        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override protected void onPostExecute( Integer isOK )
        {
            // Should we exit this activity due to an exception
            if( isOK == 0 )
            {
                finish();
            }

            Collections.sort( listAppInfo, AppMarket.NAME_ORDER );
            getHomeArrayAdapter.notifyDataSetChanged();
        } // End onPostExecute
    } // End GetAppCacheTask


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override public boolean onCreateOptionsMenu( final Menu menu )
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_market, menu );
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
            Collections.sort( listAppInfo, AppMarket.NAME_ORDER );

            // Refresh the ListView
            getHomeArrayAdapter.notifyDataSetChanged();

            return_value = true;
            break;
        }
        case R.id.sortNameDec:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppMarket.NAME_ORDER_DECEND );

            // Refresh the ListView
            getHomeArrayAdapter.notifyDataSetChanged();
            return_value = true;
            break;
        }
        case R.id.sortInstalled:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppMarket.INSTALLED_ORDER );

            // Refresh the ListView
            getHomeArrayAdapter.notifyDataSetChanged();

            return_value = true;
            break;
        }
        case R.id.sortNotInstalled:
        {
            // Sort Application by App Name
            Collections.sort( listAppInfo, AppMarket.NOTINSTALLED_ORDER );

            // Refresh the ListView
            getHomeArrayAdapter.notifyDataSetChanged();

            return_value = true;
            break;
        }
        default:
            return_value = super.onOptionsItemSelected( item );
            break;
        } // End Switch

        return return_value;
    } // End onOptionsItemSelected
    
    
    /*****************************************************************************
     * createAppMarketList - Creates an Array of AppMarket objects
     *
     */
    private void createAppMarketList()
    {
        try
        {
            // Create the array of Apps in the market (App Name <key>; App Pkg name <value>)
            HashMap<String, String> tmpHashMap = new HashMap<String, String>();
            tmpHashMap.put( StaticConfig.a360LauncherName, StaticConfig.a360Launcher );
            tmpHashMap.put( StaticConfig.a91PandaHomeName, StaticConfig.a91PandaHome );
            tmpHashMap.put( StaticConfig.AbodeName, StaticConfig.Abode );
            tmpHashMap.put( StaticConfig.ADWLauncherName, StaticConfig.ADWLauncher );
            tmpHashMap.put( StaticConfig.ApexLauncherName, StaticConfig.ApexLauncher );
            tmpHashMap.put( StaticConfig.aShellName, StaticConfig.aShell );
            tmpHashMap.put( StaticConfig.AtomLauncherName, StaticConfig.AtomLauncher );
            tmpHashMap.put( StaticConfig.BalancerLauncherName, StaticConfig.BalancerLauncher );
            tmpHashMap.put( StaticConfig.BuzzLauncherName, StaticConfig.BuzzLauncher );
            tmpHashMap.put( StaticConfig.ClaystoneLauncherName, StaticConfig.ClaystoneLauncher );
            tmpHashMap.put( StaticConfig.CrazyHomeLiteName, StaticConfig.CrazyHomeLite );
            tmpHashMap.put( StaticConfig.dxTopLiteName, StaticConfig.dxTopLite );
            tmpHashMap.put( StaticConfig.EspierLauncherName, StaticConfig.EspierLauncher );
            tmpHashMap.put( StaticConfig.EverythingHomeName, StaticConfig.EverythingHome );
            tmpHashMap.put( StaticConfig.EZLauncherName, StaticConfig.EZLauncher );
            tmpHashMap.put( StaticConfig.FastHomeName, StaticConfig.FastHome );
            tmpHashMap.put( StaticConfig.FinalLauncherName, StaticConfig.FinalLauncher );
            tmpHashMap.put( StaticConfig.GOLauncherEXName, StaticConfig.GOLauncherEX );
            tmpHashMap.put( StaticConfig.HiLauncherName, StaticConfig.HiLauncher );
            tmpHashMap.put( StaticConfig.HoloLauncherHDName, StaticConfig.HoloLauncherHD );
            tmpHashMap.put( StaticConfig.HomeName, StaticConfig.Home );
            tmpHashMap.put( StaticConfig.homescreen3DfreeversionName, StaticConfig.homescreen3Dfreeversion );
            tmpHashMap.put( StaticConfig.iHomeName, StaticConfig.iHome );
            tmpHashMap.put( StaticConfig.KitKatLauncherName, StaticConfig.KitKatLauncher );
            tmpHashMap.put( StaticConfig.Launcher360Name, StaticConfig.Launcher360 );
            tmpHashMap.put( StaticConfig.Launcher7Name, StaticConfig.Launcher7 );
            tmpHashMap.put( StaticConfig.Launcher8freeName, StaticConfig.Launcher8free );
            tmpHashMap.put( StaticConfig.launcher91Name, StaticConfig.launcher91 );
            tmpHashMap.put( StaticConfig.LauncherName, StaticConfig.Launcher );
            tmpHashMap.put( StaticConfig.LauncherProName, StaticConfig.LauncherPro );
            tmpHashMap.put( StaticConfig.LightningLauncherName, StaticConfig.LightningLauncher );
            tmpHashMap.put( StaticConfig.LiveHomeName, StaticConfig.LiveHome );
            tmpHashMap.put( StaticConfig.MetroUIName, StaticConfig.MetroUI );
            tmpHashMap.put( StaticConfig.MiHomeName, StaticConfig.MiHome );
            tmpHashMap.put( StaticConfig.MiniLauncherName, StaticConfig.MiniLauncher );
            tmpHashMap.put( StaticConfig.mooLauncherName, StaticConfig.mooLauncher );
            tmpHashMap.put( StaticConfig.MXHomeLauncherName, StaticConfig.MXHomeLauncher );
            tmpHashMap.put( StaticConfig.MyHomeliteName, StaticConfig.MyHomelite );
            tmpHashMap.put( StaticConfig.MyLauncherName, StaticConfig.MyLauncher );
            tmpHashMap.put( StaticConfig.NemusLauncherName, StaticConfig.NemusLauncher );
            tmpHashMap.put( StaticConfig.NovaLauncherName, StaticConfig.NovaLauncher );
            tmpHashMap.put( StaticConfig.QQlauncherName, StaticConfig.QQlauncher );
            tmpHashMap.put( StaticConfig.QuickLaunchHomeName, StaticConfig.QuickLaunchHome );
            tmpHashMap.put( StaticConfig.ReginaLauncherName, StaticConfig.ReginaLauncher );
            tmpHashMap.put( StaticConfig.SimpleHomeLiteName, StaticConfig.SimpleHomeLite );
            tmpHashMap.put( StaticConfig.SimpleHomeName, StaticConfig.SimpleHome );
            tmpHashMap.put( StaticConfig.SmartLauncherName, StaticConfig.SmartLauncher );
            tmpHashMap.put( StaticConfig.TagHomeName, StaticConfig.TagHome );
            tmpHashMap.put( StaticConfig.TrebuchetName, StaticConfig.Trebuchet );
            tmpHashMap.put( StaticConfig.ZeamLauncherName, StaticConfig.ZeamLauncher );
            
            for( String key : tmpHashMap.keySet()  )
            {
                AppMarket tmpAppMarket = new AppMarket( key, tmpHashMap.get( key ));
                listAppInfo.add( tmpAppMarket );
            }
        }
        catch( IllegalStateException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "createAppMarketList : IllegalStateException" );
            e.printStackTrace();
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "createAppMarketList : Exception" );
            e.printStackTrace();
        }
    }

} // End HomeManagerActivity