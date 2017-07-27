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

import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;


/*****************************************************************************
 * AppInfo - Container - Typical information stored for an installed Home app.
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class AppInfo implements Comparable<AppInfo>
{
    private static final String DEBUG_TAG                = AppInfo.class.getSimpleName();

    // Application metadata
    private String              appName                  = null;  // Home App Name
    private String              versionName              = null;  // Home App Version
    private String              packageName              = null;  // Home App package name
    private String              appMemory                = null;  // Home App snap shot memory
    private Drawable            iconDrawable             = null;  // Home App Icon
    private boolean             isDefault                = false; // If this home app is the default
    private boolean             startAtBoot              = false; // If this home app starts upon reboot
    private boolean             WiFi                     = false; // If this home app has full network access
    private boolean             contacts                 = false; // If this home app can read contacts
    private boolean             sms                      = false; // If this home app can read/receive SMS


    // ///////////////////////////////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////////////////////////////

    /*****************************************************************************
     * Image - Empty Constructor
     */
    AppInfo()
    {
        // Empty constructor
    }


    /*****************************************************************************
     * Image - Full Constructor
     * 
     * @param appName
     *            - String = The name of the Application
     * @param versionName
     *            - String = The version of the Application
     * @param packageName
     *            - String = The package name
     * @param iconDrawable
     *            - Drawable = The icon of the Application
     * @param isDefault
     *            - boolean = This app is the default
     * @param startAtBoot
     *            - boolean = This app will start upon reboot
     * @param WiFi
     *            - boolean = This app has wifi/network permission
     * @param contacts
     *            - boolean = This app has read contact permission
     * @param sms
     *            - boolean = This app has read sms permission
     */
    AppInfo( final String   appName,
             final String   versionName,
             final String   packageName,
             final String   appMemory,
             final Drawable iconDrawable,
             final boolean  isDefault,
             final boolean  startAtBoot,
             final boolean  WiFi,
             final boolean  contacts,
             final boolean  sms )
    {
        this.appName      = appName;
        this.versionName  = versionName;
        this.packageName  = packageName;
        this.appMemory    = appMemory;
        this.iconDrawable = iconDrawable;
        this.isDefault    = isDefault;
        this.startAtBoot  = startAtBoot;
        this.WiFi         = WiFi;
        this.contacts     = contacts;
        this.sms          = sms;
    }


    // ///////////////////////////////////////////////////////////////////
    // Public methods
    // ///////////////////////////////////////////////////////////////////

    /*****************************************************************************
     * compareTo - Used for comparing Installed Home Apps
     * 
     * @param another
     *            - AppInfo - This should be an AppInfo object only
     */
    @Override public int compareTo( AppInfo another )
    {
        AppInfo tmpAppInfo = another;
        
        int dataCmp = 0;
        
        try
        {
            dataCmp = new Boolean( this.getIsDefault()).compareTo( new Boolean( tmpAppInfo.getIsDefault()));
        }
        catch ( NullPointerException e )
        {
            Log.e( DEBUG_TAG, "NullPointerException : compareTo" );
            e.printStackTrace();
        }
            
        return ( dataCmp );
    }
    
    /*****************************************************************************
     * DEFAULT_ORDER - Used with Collections for sorting Home apps based Home Default
     * Example: Collections.sort( ArrayList, AppInfo.DEFAULT_ORDER );
     */
    static final Comparator<AppInfo> DEFAULT_ORDER = new Comparator<AppInfo>()
    {
          public int compare( AppInfo e1, AppInfo e2 )
          {             
              int dataCmp = 0;
              
              try
              {
                  dataCmp = new Boolean( e2.getIsDefault()).compareTo( new Boolean( e1.getIsDefault()));
              }
              catch ( NullPointerException e )
              {
                  Log.e( DEBUG_TAG, "NullPointerException : DEFAULT_ORDER" );
                  e.printStackTrace();
              }
                  
              return ( dataCmp );
          }
    };
    
    /*****************************************************************************
     * NAME_ORDER_DECEND - Used with Collections for sorting Home apps based on Home app name
     * Example: Collections.sort( ArrayList, AppInfo.NAME_ORDER );
     */
    static final Comparator<AppInfo> NAME_ORDER_DECEND = new Comparator<AppInfo>()
    {
          public int compare( AppInfo e1, AppInfo e2 )
          {             
              int dataCmp = 0;
              
              try
              {
                  dataCmp = e2.getappName().toLowerCase().compareTo( e1.getappName().toLowerCase() );
              }
              catch ( NullPointerException e )
              {
                  Log.e( DEBUG_TAG, "NullPointerException : NAME_ORDER_DECEND" );
                  e.printStackTrace();
              }
                  
              return ( dataCmp );
          }
    };
    
    /*****************************************************************************
     * NAME_ORDER - Used with Collections for sorting Home apps based on Home app name
     * Example: Collections.sort( ArrayList, AppInfo.NAME_ORDER );
     */
    static final Comparator<AppInfo> NAME_ORDER = new Comparator<AppInfo>()
    {
          public int compare( AppInfo e1, AppInfo e2 )
          {             
              int dataCmp = 0;
              
              try 
              {
                  dataCmp = e1.getappName().toLowerCase().compareTo( e2.getappName().toLowerCase() );
              }
              catch ( NullPointerException e )
              {
                  Log.e( DEBUG_TAG, "NullPointerException : NAME_ORDER" );
                  e.printStackTrace();
              }
                  
              return ( dataCmp );
          }
    };

    /*****************************************************************************
     * MEMORY_ORDER - Used with Collections for sorting Home apps based on Home app memory
     * Example: Collections.sort( ArrayList, AppInfo.MEMORY_ORDER );
     */
    static final Comparator<AppInfo> MEMORY_ORDER = new Comparator<AppInfo>()
    {
          public int compare( AppInfo e1, AppInfo e2 )
          {             
              int dataCmp = 0;
              
              try 
              {
                  dataCmp = e2.getappMemory().toLowerCase().compareTo( e1.getappMemory().toLowerCase() );
              }
              catch ( NullPointerException e )
              {
                  Log.e( DEBUG_TAG, "NullPointerException : MEMORY_ORDER" );
                  e.printStackTrace();
              }
                  
              return ( dataCmp );
          }
    };

    /*****************************************************************************
     * setappName - The name of the Application
     * 
     * @param appName
     *            - String - The name of the Application
     */
    public void setappName( final String appName )
    {
        this.appName = appName;
    }


    /*****************************************************************************
     * setversionName - The version of the Application
     * 
     * @param versionName - String - The version of the Application
     */
    public void setversionName( final String versionName )
    {
        this.versionName = versionName;
    }
    
    
    /*****************************************************************************
     * setpackageName - The package name
     * 
     * @param packageName - String - The package name
     */
    public void setpackageName( final String packageName )
    {
        this.packageName = packageName;
    }
    
    /*****************************************************************************
     * setappMemory - The package name
     * 
     * @param appMemory - String - The App memory
     */
    public void setappMemory( final String appMemory )
    {
        this.appMemory = appMemory;
    }
    
    /*****************************************************************************
     * setstartAtBoot - The app has the permission to start at reboot
     * 
     * @param startAtBoot - boolean - true or false
     */
    public void setstartAtBoot( final boolean startAtBoot )
    {
        this.startAtBoot = startAtBoot;
    }
    
    /*****************************************************************************
     * setWiFi - The app has the permission to use the network
     * 
     * @param WiFi - boolean - true or false
     */
    public void setWiFi( final boolean WiFi )
    {
        this.WiFi = WiFi;
    }
    
    /*****************************************************************************
     * setContacts - The app has the permission to read contacts
     * 
     * @param contacts - boolean - true or false
     */
    public void setContacts( final boolean contacts )
    {
        this.contacts = contacts;
    }
    
    /*****************************************************************************
     * setSMS - The app has the permission to read SMS
     * 
     * @param sms - boolean - true or false
     */
    public void setSMS( final boolean sms )
    {
        this.sms = sms;
    }

    // -----------------------------------------------------------------------------

    /*****************************************************************************
     * getappName - The name of the Application
     * 
     * @return String - The name of the Application
     */
    public String getappName()
    {
        return ( this.appName );
    }

    
    /*****************************************************************************
     * getversionName - The version of the Application
     * 
     * @return String - The version of the Application
     */
    public String getversionName()
    {
        return ( this.versionName );
    }
    
    
    /*****************************************************************************
     * public getpackageName - The package name
     * 
     * @return String - The package name
     */
    public String getpackageName()
    {
        return ( this.packageName );
    }
    
    /*****************************************************************************
     * public getappMemory - The app memory
     * 
     * @return String - The app memory
     */
    public String getappMemory()
    {
        return ( this.appMemory );
    }

    /*****************************************************************************
     * public getIsDefault - If this is the default application
     * 
     * @return String - If this is the default application
     */
    public boolean getIsDefault()
    {       
        return ( this.isDefault );
    }

    /*****************************************************************************
     * public geticonDrawable - The icon of the Application
     * 
     * @return iconDrawable - Drawable - The icon of the Application
     */
    public Drawable geticonDrawable()
    { 
        return ( this.iconDrawable );
    }
    
    /*****************************************************************************
     * getstartAtBoot - The app has the permission to start at reboot
     * 
     * @param startAtBoot - boolean - true or false
     */
    public boolean getstartAtBoot()
    {
        return ( this.startAtBoot );
    }
    
    /*****************************************************************************
     * getWiFi - The app has the permission to use the network
     * 
     * @param WiFi - boolean - true or false
     */
    public boolean getWiFi()
    {
        return ( this.WiFi );
    }
    
    /*****************************************************************************
     * getContacts - The app has the permission to read contacts
     * 
     * @param contacts - boolean - true or false
     */
    public boolean getContacts()
    {
        return ( this.contacts );
    }
    
    /*****************************************************************************
     * getSMS - The app has the permission to read SMS
     * 
     * @param sms - boolean - true or false
     */
    public boolean getSMS()
    {
        return ( this.sms );
    }  
    
    /*****************************************************************************
     * public isValid - Check if this AppInfo is valid or not
     * 
     * @param a - The currently accessible Activity
     * 
     * @return boolean - true if the AppInfo is valid, otherwise false
     */
    public boolean isValid()
    {
        // Set to true if Image is valid
        boolean valid = false;

        if( this.appName != null && this.versionName != null && this.packageName != null )
        {
            valid = true;
        }

        return valid;
    } // End isValid


    /*****************************************************************************
     * public invalidate - Invalidate the AppInfo object - this sets all fields to
     * null
     */
    public void invalidate()
    {
        // Set all Dir metadata to null or zero
        this.appName      = null;
        this.versionName  = null;
        this.packageName  = null;
        this.appMemory    = null;
        this.iconDrawable = null;
        this.isDefault    = false;
        this.startAtBoot  = false;
        this.WiFi         = false;
        this.contacts     = false;
        this.sms          = false;
    } // End invaliiconDrawable
    
    /*****************************************************************************
     * getHomeApp - Get the current default Home application
     * 
     * @param context - Context - The current Activity's context calling this method
     * 
     * @return ResolveInfo - The ResolveInfo associated with the default home app
     * 
     */
    public static ResolveInfo getHomeApp( Context context )
    {
        ResolveInfo res = null;
        
        try
        {
            // To get the default Home app we need to search for ACTION_MAIN
            Intent intent = new Intent( Intent.ACTION_MAIN );
            
            // To get the default Home app we need to search for Category CATEGORY_HOME
            intent.addCategory( Intent.CATEGORY_HOME );
            
            // Get the default Home app
            res = context.getPackageManager().resolveActivity( intent, PackageManager.MATCH_DEFAULT_ONLY );
            
            // Get the packet name (Helps with viewing when debugging)
            String pkgName = res.activityInfo.packageName;
            
            // If we couldn't get any activity information ? (Should never happen)
            if( res.activityInfo == null )
            {
                // should not happen. A home is always installed, isn't it?
                res = null;
            }
            if( pkgName.equals( "android" ) )
            {
                // No default selected
                res = null;
            }
        }
        catch( NullPointerException e )
        {
            res = null;
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "getHomeApp : NullPointerException" );
            e.printStackTrace();
        }
        catch( Exception e )
        {
            res = null;
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "getHomeApp : Exception" );
            e.printStackTrace();
        }

        return res;
    } // End isPackagePreferred
    
} // End AppInfo
// EOF