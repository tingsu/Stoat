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

import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/*****************************************************************************
 * Util - Utility class with common static methods
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0.1.8
 */
public class Util
{
    private final static String DEBUG_TAG = HomeManagerActivity.class.getSimpleName();
    
    // Used for creating the intent for InstalledAppDetails
    private static final String      SCHEME                   = "package";
    private static final String      APP_PKG_NAME_21          = "com.android.settings.ApplicationPkgName";
    private static final String      APP_PKG_NAME_22          = "pkg";
    private static final String      APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String      APP_DETAILS_CLASS_NAME   = "com.android.settings.InstalledAppDetails";

    // No way to instantiate
    private Util(){};

    /*****************************************************************************
     * isCallable - Check to make sure Activity to start is callable
     * set by the end user <br>
     * 
     * @param a - Activity - The activity calling this method
     * @param intent - Intent - The intent that starts the application
     * 
     * @return boolean - Application can be started (true) or not (false)
     */
    public static boolean isCallable( final Context context, final Intent intent )
    {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY );
        return list.size() > 0;
    } // End isCallable

    
    /*****************************************************************************
     * getRunningProcess - Get a list of running processes
     * 
     * @param - context - Context
     * 
     * @return HashMap<String, Integer> - Package-Name, PID (Process ID)
     */
    public static HashMap<String, Integer> getRunningProcess( Context context )
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
        android.app.ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo( memoryInfo );

        List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        
        HashMap<String, Integer> pidMap = new HashMap<String, Integer>();
        for ( RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses )
        {
            pidMap.put( runningAppProcessInfo.processName, runningAppProcessInfo.pid );
            
            for( String pkg : runningAppProcessInfo.pkgList )
            {
                pidMap.put( pkg, runningAppProcessInfo.pid );
            }
        }

        return pidMap;
    } // End getRunningProcess
    
    
    /*****************************************************************************
     * getPkgMemory - Get the package's memory given it's PID
     * 
     * @param pid - int - The application's PID
     * @param context - Context
     * 
     * @return int - The memory as an int
     */
    @TargetApi ( 5 ) public static int getPkgMemory( final int pid, final Context context )
    {
        int total = 0;
        
        ActivityManager activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
        android.app.ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo( memoryInfo );
        
        int pids[] = new int[1];
        pids[0] = pid;
        android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo( pids );
        total = ( memoryInfoArray[0].getTotalSharedDirty() + 
                  memoryInfoArray[0].getTotalPss() +
                  memoryInfoArray[0].getTotalPrivateDirty()) / 1024;
        
        return total;
    } // End getPkgMemory
    
    

    /*****************************************************************************
     * makeIntentInstalledAppDetails - Get app's Android settings 
     * 
     * @param packageName - String - Package name of the app
     * 
     * @return Intent - The intent to get app details
     */
    public static Intent makeIntentInstalledAppDetails( final String packageName )
    {
        Intent intent = new Intent();

        // Build intent for API version greater than 9
        if( Build.VERSION.SDK_INT >= 9 )
        {
            intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
            Uri uri = Uri.fromParts( SCHEME, packageName, null );
            intent.setData( uri );
        }
        else
        // Build intent for API version 8 and below
        {
            final String appPkgName = ( Build.VERSION.SDK_INT == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21 );
            intent.setAction( Intent.ACTION_VIEW );
            intent.setClassName( APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME );
            intent.putExtra( appPkgName, packageName );
        }

        return intent;
    } // End makeIntentInstalledAppDetails
    
    
    /*****************************************************************************
     * isPackagePreferred - Check to see if the Package is a Preferred Activity
     * set by the end user <br>
     * 
     * @param activity - Activity - The activity calling this method
     * @param packageName - String - Package name of the app
     * 
     * @return boolean - Package is set as the default
     */
    public static boolean isPackagePreferred( final Activity activity, final String packageName )
    {
        boolean isPreferredPackage = false;

        try
        {
            final Intent intent = new Intent( Intent.ACTION_MAIN );
            intent.addCategory( Intent.CATEGORY_HOME );
            final ResolveInfo res = activity.getPackageManager().resolveActivity( intent, PackageManager.MATCH_DEFAULT_ONLY );
            String pkgName = res.activityInfo.packageName;
            if( res.activityInfo == null )
            {
                // should not happen. A home is always installed, isn't it?
            }
            else if( pkgName.equals( "android" ) )
            {
                // No default selected
            }
            else
            {
                // res.activityInfo.packageName and res.activityInfo.name gives you
                // the default app
                if( packageName.equals( pkgName ) )
                {
                    isPreferredPackage = true;
                }
            }
        }
        catch( IllegalStateException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "isPackagePreferred : IllegalStateException" );
            e.printStackTrace();
        }
        catch( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "isPackagePreferred : Exception" );
            e.printStackTrace();
        }

        return isPreferredPackage;
    } // End isPackagePreferred
    
    
    /*****************************************************************************
     * showNotification - Show the home app quick select notification <br>
     * 
     * @param activity - Activity - The activity calling this method
     * 
     */
    public static void showNotification( final Activity activity )
    {
        // Setup the notification
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService( Context.NOTIFICATION_SERVICE );
        
        // Setup the SMS Notification
        Notification quickSelectNotification;
        
        // Call intent
//        PendingIntent pIntent = PendingIntent.getActivity( thisActivity, 0, thisActivity.getIntent(), 0 );
        Intent getQuickSelectIntent = new Intent( StaticConfig.QUICK_SELET_INTENT );
        PendingIntent pQuickSelect = PendingIntent.getActivity( activity, 0, getQuickSelectIntent, 0 );
        
//        // Notification builder is on available in API 11 and above
//        if ( Build.VERSION.SDK_INT >=  Build.VERSION_CODES.HONEYCOMB )
//        {
//            // Build the notification for API level 11 and above
//            quickSelectNotification = new Notification.Builder( thisActivity )
//            .setContentTitle( activity.getResources().getString( R.string.quickSelect ) )
//            .setContentText( activity.getResources().getString( R.string.selectHomeApp ) )
//            .setSmallIcon( R.drawable.icon )
//            .setContentIntent( pQuickSelect )
//            .setAutoCancel( false )
//            .addAction( R.drawable.icon, "Select", pQuickSelect ).build();
//            
//            notificationManager.notify( 1, quickSelectNotification );
//        }
//        else
//        {
            // Anything below API 11 will need to use the deprecated method
            
            // Create a SMS notification that a known user has texted
            quickSelectNotification = new Notification( R.drawable.icon, "THM", System.currentTimeMillis() );
            quickSelectNotification.flags = Notification.FLAG_NO_CLEAR;
            quickSelectNotification.setLatestEventInfo( activity,
                                                        activity.getResources().getString( R.string.quickSelect ),
                                                        activity.getResources().getString( R.string.selectHomeApp ),
                                                        pQuickSelect );

            notificationManager.notify( 1, quickSelectNotification );
//        }
    }
}