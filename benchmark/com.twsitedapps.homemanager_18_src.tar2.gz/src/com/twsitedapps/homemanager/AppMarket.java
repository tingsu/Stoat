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

import android.graphics.drawable.Drawable;
import android.util.Log;


/*****************************************************************************
 * App Market - Container - Apps within the market.
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class AppMarket implements Comparable<AppMarket>
{
    private static final String DEBUG_TAG                = AppMarket.class.getSimpleName();

    // Application metadata
    private String              appName                  = null;
    private String              market                   = null;
    private Drawable            iconDrawable             = null;
    private boolean             installed                = false;


    // ///////////////////////////////////////////////////////////////////
    // Constructors
    // ///////////////////////////////////////////////////////////////////

    /*****************************************************************************
     * Image - Empty Constructor
     */
    AppMarket()
    {
        // Empty constructor
    }


    /*****************************************************************************
     * Image - Full Constructor
     * 
     * @param appName
     *            - String = The name of the Application
     * @param market
     *            - String = The market search package name
     * @param iconDrawable
     *            - Drawable = The icon of the Application
     * @param installed
     *            - boolean = If the Home App is installed or not?
     */
    AppMarket( final String   appName,
               final String   market,
               final Drawable iconDrawable,
               final boolean  installed  )
    {
        this.appName      = appName;
        this.market       = market;
        this.iconDrawable = iconDrawable;
        this.installed    = installed;
    }
    
    
    /*****************************************************************************
     * Image - Full Constructor
     * 
     * @param appName
     *            - String = The name of the Application
     * @param market
     *            - String = The market search package name
     */
    AppMarket( final String   appName,
               final String   market )
    {
        this.appName      = appName;
        this.market       = market;
    }


    // ///////////////////////////////////////////////////////////////////
    // Public methods
    // ///////////////////////////////////////////////////////////////////

    /*****************************************************************************
     * compareTo - Used for comparing Installed Home Apps
     * 
     * @param another
     *            - AppMarket - This should be an AppInfo object only
     */
    @Override public int compareTo( AppMarket another )
    {
        AppMarket tmpAppInfo = another;
        
        int dataCmp = 0;
        
        try
        {
            dataCmp = new Boolean( this.isInstalled()).compareTo( new Boolean( tmpAppInfo.isInstalled()));
        }
        catch ( NullPointerException e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "NullPointerException : compareTo" );
            e.printStackTrace();
        }
        catch ( Exception e )
        {
            Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "Exception : compareTo" );
            e.printStackTrace();
        }
            
        return ( dataCmp );
    }

    
    /*****************************************************************************
     * INSTALLED_ORDER - Used with Collections for sorting Home apps based Home Default
     * Example: Collections.sort( ArrayList, AppMarket.DEFAULT_ORDER );
     */
    static final Comparator<AppMarket> INSTALLED_ORDER = new Comparator<AppMarket>()
    {
          public int compare( AppMarket e1, AppMarket e2 )
          {             
              int dataCmp = 0;
              
              try
              {
                  dataCmp = new Boolean( e2.isInstalled()).compareTo( new Boolean( e1.isInstalled()));
              }
              catch ( NullPointerException e )
              {
                  Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "NullPointerException : INSTALLED_ORDER" );
                  e.printStackTrace();
              }
                  
              return ( dataCmp );
          }
    };
    
    /*****************************************************************************
     * NOTINSTALLED_ORDER - Used with Collections for sorting Home apps based Home Default
     * Example: Collections.sort( ArrayList, AppMarket.DEFAULT_ORDER );
     */
    static final Comparator<AppMarket> NOTINSTALLED_ORDER = new Comparator<AppMarket>()
    {
          public int compare( AppMarket e1, AppMarket e2 )
          {             
              int dataCmp = 0;
              
              try
              {
                  dataCmp = new Boolean( e1.isInstalled()).compareTo( new Boolean( e2.isInstalled()));
              }
              catch ( NullPointerException e )
              {
                  Log.e( DEBUG_TAG, StaticConfig.TWISTED_TAG + "NullPointerException : NOTINSTALLED_ORDER" );
                  e.printStackTrace();
              }
                  
              return ( dataCmp );
          }
    };
    
    /*****************************************************************************
     * NAME_ORDER_DECEND - Used with Collections for sorting Home apps based on Home app name
     * Example: Collections.sort( ArrayList, AppMarket.NAME_ORDER );
     */
    static final Comparator<AppMarket> NAME_ORDER_DECEND = new Comparator<AppMarket>()
    {
          public int compare( AppMarket e1, AppMarket e2 )
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
     * Example: Collections.sort( ArrayList, AppMarket.NAME_ORDER );
     */
    static final Comparator<AppMarket> NAME_ORDER = new Comparator<AppMarket>()
    {
          public int compare( AppMarket e1, AppMarket e2 )
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
     * setmarket - The search string for the App on the market
     * 
     * @param market - String - The search string for the App on the market
     */
    public void setmarket( final String market )
    {
        this.market = market;
    }
    
    
    /*****************************************************************************
     * seticonDrawable - Set the icon drawable for this Market App
     * 
     * @param drawable - Drawable - The icon drawable for this Market App
     */
    public void seticonDrawable( final Drawable drawable )
    {
        this.iconDrawable = drawable;
    }

    
    /*****************************************************************************
     * setInstalled - Set to true if this Market App is installed
     * 
     * @param installed - boolean - True if this Market App is installed
     */
    public void setInstalled( final boolean installed )
    {
        this.installed = installed;
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
     * getmarket - The search string for the App on the market
     * 
     * @return String - The search string for the App on the market
     */
    public String getmarket()
    {
        return ( this.market );
    }


    /*****************************************************************************
     * public isInstalled - If this app is Installed or not?
     * 
     * @return boolean - true if the app is installed; false otherwise
     */
    public boolean isInstalled()
    {
        
        return ( this.installed );
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
     * public isValid - Check if this AppMarket is valid or not
     * 
     * @param a - The currently accessible Activity
     * 
     * @return boolean - true if the directory is valid, otherwise false
     */
    public boolean isValid()
    {
        // Set to true if Image is valid
        boolean valid = false;

        if( this.appName != null && this.market != null )
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
        this.market       = null;
        this.iconDrawable = null;
        this.installed    = false; 
    } // End invaliiconDrawable

} // End AppMarket
// EOF