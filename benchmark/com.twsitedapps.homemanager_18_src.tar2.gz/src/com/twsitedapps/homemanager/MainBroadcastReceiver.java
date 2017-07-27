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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*****************************************************************************
 * MainBroadcastReceiver - (Beta) - This is a place holder for future work.
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class MainBroadcastReceiver extends BroadcastReceiver
{
    private final static String DEBUG_TAG = MainBroadcastReceiver.class.getSimpleName();

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override public void onReceive( final Context context,
                                     Intent intent )
    {
        // Future work
//        try
//        {
//            // Get home applications
//            if ( HomeManagerActivity.listAppInfo != null )
//            {
//                if ( HomeManagerActivity.listAppInfo.isEmpty() )
//                {
//                    // Set this so we know the installed Home App list has been built
//                    HomeManagerActivity.isFinishedBuildingList = false;
//                    
//                    new GetAppCacheTask( context, HomeManagerActivity.listAppInfo, null, HomeManagerActivity.isFinishedBuildingList ).execute();
//                }
//            }
//            else
//            {
//                // Set this so we know the installed Home App list has been built
//                HomeManagerActivity.isFinishedBuildingList = false;
//                
//                // Create the cached ArrayList of home applications
//                HomeManagerActivity.listAppInfo = new ArrayList<AppInfo>();
//                new GetAppCacheTask( context, HomeManagerActivity.listAppInfo, null, HomeManagerActivity.isFinishedBuildingList ).execute();
//            }
//        }
//        catch( IllegalStateException e )
//        {
//            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onResume : IllegalStateException" );
//            e.printStackTrace();
//        }
//        catch( Exception e )
//        {
//            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "onResume : Exception" );
//            e.printStackTrace();
//        }
        
        // Future work
//        if ( intent.getAction().toString().equals( "android.intent.action.BOOT_COMPLETED" ) )
//        {
//            // Do something we we decided to implement this feature
//        }
        
//        if ( intent.getAction().toString().equals( "action" ) )
//        {
//            
//        }

    } // End onReceive
} // End class MainBroadcastReceiver
// EOF