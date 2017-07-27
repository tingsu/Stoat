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

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*****************************************************************************
 * GetHomeArrayAdapter - ArrayAdapter<AppInfo> - Adapter to show home apps 
 * currently installed within a listview. 
 * 
 * @author Russell T Mackler
 * @version 1.0.1.8
 * @since 1.0
 */
public class HomeManagerArrayAdapter extends ArrayAdapter<AppInfo>
{
    private static final String            DEBUG_TAG = HomeManagerArrayAdapter.class.getSimpleName();;

    // The activity using this adapter
    private final Activity                 context;

    // The ArrayList of home apps
    private final ArrayList<AppInfo>       listAppInfo;

    /*****************************************************************************
     * GetHomeArrayAdapter - ArrayAdapter<AppInfo> - Adapter to show home apps 
     * currently installed within a listview. 
     * 
     */
    public HomeManagerArrayAdapter( Activity context, ArrayList<AppInfo> listAppInfo )
    {
        super( context, R.layout.homemanagerrow, listAppInfo );
        this.context = context;
        this.listAppInfo = listAppInfo;
    }


    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getCount()
     */
    @Override public int getCount()
    {
        return this.listAppInfo.size(); // Set via getCacheImages method
    }


    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getItem(int)
     */
    @Override public AppInfo getItem( int position )
    {
        return this.listAppInfo.get( position );
    }


    /*****************************************************************************
     * ViewHolder - Used for UI performance
     *            
     */
    static class ViewHolder
    {
        // The icon associated with the application
        public ImageView iconDrawable;

        // The name of the application
        public TextView  txt_apptag;
        
        // The name of the application
        public TextView  txt_appName;

        // The version of the application
        public TextView  txt_versiontag;
        
        // The version of the application
        public TextView  txt_versionName;
        
        // The memory of the application
        public TextView txt_memoryName;
        
        // The permission tag
        public TextView txt_permissiontag;
        
        // If the App has detected permissions
        public TextView txt_permission;
        
        // The Row RelativeLayout
        public LinearLayout ll;
        
        public TextView txt_default;
    } // End ViewHolder


    /*
     * (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override public View getView( int position, View convertView, ViewGroup parent )
    {
        // ViewHolder will buffer the individual fields of the home manager row layout
        ViewHolder holder;

        // Recycle existing view if passed as parameter
        // This will save memory and time on Android
        // This only works if the base layout for all classes are the same
        View rowView = convertView;
        if( rowView == null )
        {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate( R.layout.homemanagerrow, null, true );
            holder = new ViewHolder();
            holder.txt_apptag = (TextView) rowView.findViewById( R.id.txt_apptag );
            holder.txt_appName = (TextView) rowView.findViewById( R.id.txt_app );
            holder.txt_versiontag = (TextView) rowView.findViewById( R.id.txt_versiontag );
            holder.txt_versionName = (TextView) rowView.findViewById( R.id.txt_version );
            holder.txt_memoryName = (TextView) rowView.findViewById( R.id.txt_memory );
            holder.txt_permissiontag = (TextView) rowView.findViewById( R.id.txt_permissiontag );
            holder.txt_permission = (TextView) rowView.findViewById( R.id.txt_permission );
            holder.iconDrawable = (ImageView) rowView.findViewById( R.id.appIconImageView );
            holder.ll = (LinearLayout) rowView.findViewById( R.id.mainlinearlayout );
            holder.txt_default = (TextView) rowView.findViewById( R.id.txt_default );
            rowView.setTag( holder );
        }
        else
        {
            holder = (ViewHolder) rowView.getTag();
        }

        // Only if the list of apps is not null display the contents
        if( listAppInfo != null )
        {
            try
            {
                // Display permissions
                boolean displayPermissions = false;
                String permissions = "";
                
                // Display app values set
                holder.txt_appName.setText( listAppInfo.get( position ).getappName().trim() );
                holder.txt_versionName.setText( listAppInfo.get( position ).getversionName() );
                holder.txt_memoryName.setText( listAppInfo.get( position ).getappMemory() );

                // Set the image if it was cached
                holder.iconDrawable.setImageDrawable( listAppInfo.get( position ).geticonDrawable() );
                
                // If the app is the default app
                if ( listAppInfo.get( position ).getIsDefault() )
                {
                    holder.txt_default.setText( context.getResources().getString( R.string.defaultValue ) );
                    holder.txt_default.setVisibility( View.VISIBLE );
                }
                else
                {
                    holder.txt_default.setVisibility( View.INVISIBLE );
                }
                
                // If the app has the permission to start at boot
                if ( listAppInfo.get( position ).getstartAtBoot() )
                {
                    permissions = context.getResources().getString( R.string.startAtBoot );
                    displayPermissions = true;
                }
                
                // If the app has the permission to make Internet connections
                if ( listAppInfo.get( position ).getWiFi() )
                {
                    // Add a comma if a permission already detected
                    if ( displayPermissions )
                    {
                        permissions += ", ";
                    }
                    
                    permissions += context.getResources().getString( R.string.wifi );
                    displayPermissions = true;
                }
                
                // If the app has the permission to read contacts
                if ( listAppInfo.get( position ).getContacts() )
                {
                    // Add a comma if a permission already detected
                    if ( displayPermissions )
                    {
                        permissions += ", ";
                    }
                    
                    permissions += context.getResources().getString( R.string.contacts );
                    displayPermissions = true;
                }
                
                // If the app has the permission to read contacts
                if ( listAppInfo.get( position ).getSMS() )
                {
                    // Add a comma if a permission already detected
                    if ( displayPermissions )
                    {
                        permissions += ", ";
                    }
                    
                    permissions += context.getResources().getString( R.string.sms );
                    displayPermissions = true;
                }
                
                // If a permission was found
                if ( displayPermissions )
                {
                    holder.txt_permission.setVisibility( View.VISIBLE );
                    holder.txt_permission.setText( permissions );
                    holder.txt_permissiontag.setVisibility( View.VISIBLE );
                }
                else
                {
                    holder.txt_permission.setVisibility( View.INVISIBLE );
                    holder.txt_permission.setText( "" );
                    holder.txt_permissiontag.setVisibility( View.INVISIBLE );
                }
                
                // If this is the default application
                if( listAppInfo.get( position ).getIsDefault() )
                {
                    if ( StaticConfig.theme == StaticConfig.BLACK )
                    {
                        holder.ll.setBackgroundColor( Color.WHITE );
                        holder.txt_apptag.setTextColor( Color.BLACK );
                        holder.txt_appName.setTextColor( Color.BLACK );
                        holder.txt_versiontag.setTextColor( Color.BLACK );
                        holder.txt_versionName.setTextColor( Color.BLACK );
                        holder.txt_permissiontag.setTextColor( Color.BLACK );
                        holder.txt_memoryName.setTextColor( Color.RED );
                        holder.txt_permission.setTextColor( Color.RED );
                    }
                    else if( StaticConfig.theme == StaticConfig.WHITE )
                    {
                        holder.ll.setBackgroundColor( Color.BLACK );
                        holder.txt_apptag.setTextColor( Color.WHITE );
                        holder.txt_appName.setTextColor( Color.WHITE );
                        holder.txt_versiontag.setTextColor( Color.WHITE );
                        holder.txt_versionName.setTextColor( Color.WHITE );
                        holder.txt_permissiontag.setTextColor( Color.WHITE );
                        holder.txt_memoryName.setTextColor( Color.YELLOW );
                        holder.txt_permission.setTextColor( Color.YELLOW );
                    }
                    else if( StaticConfig.theme == StaticConfig.GREY )
                    {
                        holder.ll.setBackgroundColor( Color.BLACK );
                        holder.txt_apptag.setTextColor( Color.DKGRAY );
                        holder.txt_appName.setTextColor( Color.DKGRAY );
                        holder.txt_versiontag.setTextColor( Color.DKGRAY );
                        holder.txt_versionName.setTextColor( Color.DKGRAY );
                        holder.txt_permissiontag.setTextColor( Color.DKGRAY );
                        holder.txt_memoryName.setTextColor( Color.YELLOW );
                        holder.txt_permission.setTextColor( Color.YELLOW );
                    }
                    else if( StaticConfig.theme == StaticConfig.CYAN )
                    {
                        holder.ll.setBackgroundColor( Color.BLACK );
                        holder.txt_apptag.setTextColor( Color.CYAN );
                        holder.txt_appName.setTextColor( Color.CYAN );
                        holder.txt_versiontag.setTextColor( Color.CYAN );
                        holder.txt_versionName.setTextColor( Color.CYAN );
                        holder.txt_permissiontag.setTextColor( Color.CYAN );
                        holder.txt_memoryName.setTextColor( Color.CYAN );
                        holder.txt_permission.setTextColor( Color.CYAN );
                    }
                    else if( StaticConfig.theme == StaticConfig.GREEN )
                    {
                        holder.ll.setBackgroundColor( Color.BLACK );
                        holder.txt_apptag.setTextColor( Color.GREEN );
                        holder.txt_appName.setTextColor( Color.GREEN );
                        holder.txt_versiontag.setTextColor( Color.GREEN );
                        holder.txt_versionName.setTextColor( Color.GREEN );
                        holder.txt_permissiontag.setTextColor( Color.GREEN );
                        holder.txt_memoryName.setTextColor( Color.YELLOW );
                        holder.txt_permission.setTextColor( Color.YELLOW );
                    }
                    else if( StaticConfig.theme == StaticConfig.MAGENTA )
                    {
                        holder.ll.setBackgroundColor( Color.BLACK );
                        holder.txt_apptag.setTextColor( Color.MAGENTA );
                        holder.txt_appName.setTextColor( Color.MAGENTA );
                        holder.txt_versiontag.setTextColor( Color.MAGENTA );
                        holder.txt_versionName.setTextColor( Color.MAGENTA );
                        holder.txt_permissiontag.setTextColor( Color.MAGENTA );
                        holder.txt_memoryName.setTextColor( Color.YELLOW );
                        holder.txt_permission.setTextColor( Color.YELLOW );
                    }
                }
                else
                {
                    if ( StaticConfig.theme == StaticConfig.BLACK )
                    {
                        holder.ll.setBackgroundColor( Color.BLACK );
                        holder.txt_apptag.setTextColor( Color.WHITE );
                        holder.txt_appName.setTextColor( Color.WHITE );
                        holder.txt_versiontag.setTextColor( Color.WHITE );
                        holder.txt_versionName.setTextColor( Color.WHITE );
                        holder.txt_permissiontag.setTextColor( Color.WHITE );
                        holder.txt_memoryName.setTextColor( Color.YELLOW );
                        holder.txt_permission.setTextColor( Color.YELLOW );
                    }
                    else if( StaticConfig.theme == StaticConfig.WHITE )
                    {
                        holder.ll.setBackgroundColor( Color.WHITE );
                        holder.txt_apptag.setTextColor( Color.BLACK );
                        holder.txt_appName.setTextColor( Color.BLACK );
                        holder.txt_versiontag.setTextColor( Color.BLACK );
                        holder.txt_versionName.setTextColor( Color.BLACK );
                        holder.txt_permissiontag.setTextColor( Color.BLACK );
                        holder.txt_memoryName.setTextColor( Color.RED );
                        holder.txt_permission.setTextColor( Color.RED );
                    }
                    else if( StaticConfig.theme == StaticConfig.GREY )
                    {
                        holder.ll.setBackgroundColor( Color.DKGRAY );
                        holder.txt_apptag.setTextColor( Color.BLACK );
                        holder.txt_appName.setTextColor( Color.BLACK );
                        holder.txt_versiontag.setTextColor( Color.BLACK );
                        holder.txt_versionName.setTextColor( Color.BLACK );
                        holder.txt_permissiontag.setTextColor( Color.BLACK );
                        holder.txt_memoryName.setTextColor( Color.CYAN );
                        holder.txt_permission.setTextColor( Color.CYAN );
                    }
                    else if( StaticConfig.theme == StaticConfig.CYAN )
                    {
                        holder.ll.setBackgroundColor( Color.CYAN );
                        holder.txt_apptag.setTextColor( Color.BLACK );
                        holder.txt_appName.setTextColor( Color.BLACK );
                        holder.txt_versiontag.setTextColor( Color.BLACK );
                        holder.txt_versionName.setTextColor( Color.BLACK );
                        holder.txt_permissiontag.setTextColor( Color.BLACK );
                        holder.txt_memoryName.setTextColor( Color.RED );
                        holder.txt_permission.setTextColor( Color.RED );
                    }
                    else if( StaticConfig.theme == StaticConfig.GREEN )
                    {
                        holder.ll.setBackgroundColor( Color.GREEN );
                        holder.txt_apptag.setTextColor( Color.BLACK );
                        holder.txt_appName.setTextColor( Color.BLACK );
                        holder.txt_versiontag.setTextColor( Color.BLACK );
                        holder.txt_versionName.setTextColor( Color.BLACK );
                        holder.txt_permissiontag.setTextColor( Color.BLACK );
                        holder.txt_memoryName.setTextColor( Color.BLUE );
                        holder.txt_permission.setTextColor( Color.BLUE );
                    }
                    else if( StaticConfig.theme == StaticConfig.MAGENTA )
                    {
                        holder.txt_default.setText( "" );
                        holder.ll.setBackgroundColor( Color.MAGENTA );
                        holder.txt_apptag.setTextColor( Color.BLACK );
                        holder.txt_appName.setTextColor( Color.BLACK );
                        holder.txt_versiontag.setTextColor( Color.BLACK );
                        holder.txt_versionName.setTextColor( Color.BLACK );
                        holder.txt_permissiontag.setTextColor( Color.BLACK );
                        holder.txt_memoryName.setTextColor( Color.WHITE );
                        holder.txt_permission.setTextColor( Color.WHITE );
                    }
                }
            }
            catch( OutOfMemoryError e )
            {
                Log.e( DEBUG_TAG, "getView : OutOfMemoryError : cacher image" );
                e.printStackTrace();
            }
            catch( IndexOutOfBoundsException e )
            {
                Log.e( DEBUG_TAG, "getView : IndexOutOfBoundsException : cacher image" );
                e.printStackTrace();
            }
            catch( NullPointerException e )
            {
                Log.e( DEBUG_TAG, "getView : NullPointerException : cacher image" );
                e.printStackTrace();
            }
        } // End if cacher != null

        return rowView;
    } // End getView
} // End HomeManagerArrayAdapter
// EOF