/******************************************************************************
 * This file is part of Pocket Talk, an Android application that reads text 
 * messages aloud, and vibrates them in International Morse Code.
 *
 * Pocket Talk was released in January 2012 by Zach Rattner 
 * (info@zachrattner.com).
 *
 * Pocket Talk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pocket Talk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pocket Talk.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.zachrattner.pockettalk.util;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import com.zachrattner.pockettalk.gui.Main;

/* Handles text messages as they are received and figures out what to do with
 * them. */
public class SMSReader extends BroadcastReceiver
{
	/* Log tag, for debugging */
	private static final String TAG = SMSReader.class.getCanonicalName();
	
	/* Application context */
	private Context m_Context;
	
	/* Wake lock for keeping the device from going to sleep while the vibrator
	 * is on. */
	private WakeLock m_WakeLock;
	
	/* Handle messages when they are received */
    @Override
    public void onReceive(Context AppContext, Intent AppIntent) 
    {
    	m_Context             = AppContext;
        Bundle Extras         = AppIntent.getExtras();        
        SmsMessage[] Messages = null;
        String Message        = null;
        
        /* Make sure there is a message to handle before continuing. */
        if ((AppContext == null) || (Extras == null))
        {
        	return;
        }
        
        /* Initialize the wake lock/ */
		PowerManager Manager = (PowerManager) AppContext.getSystemService(Context.POWER_SERVICE);
		m_WakeLock = Manager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
        
		/* Initialize member data */
        Preferences Prefs = new Preferences(AppContext);
        Object[] PDUs     = (Object[]) Extras.get("pdus");
        Messages          = new SmsMessage[PDUs.length];            

        /* Build the message. */
        Message = "";
        for (int i = 0; i < Messages.length; i++)
        { 
            Messages[i] = SmsMessage.createFromPdu((byte[]) PDUs[i]);
            
            if (Prefs.sender())
            {
            	String Sender = Messages[i].getOriginatingAddress(); 
            	if (Sender != null)
            	{
            		Sender   = getContactNameByNumber(Sender);
            		Message += (Sender + " says: ");
            	}
            }
            
            Message += Messages[i].getMessageBody().toString();
            Message += " ";        
        }
        
        /* Handle vibrating the message in Morse code. */
        if (Prefs.vibrate())
        {
        	Log.d(TAG, "Vibrating message...");
        	m_WakeLock.acquire();
	        MorseCode Writer = new MorseCode(AppContext);
	        Writer.processString(Message);
	        m_WakeLock.release();
        }
        
        /* Handle speaking the message. */
        if (Prefs.speak())
        {
        	Log.d(TAG, "Speaking message text...");
        	Prefs.setSpeechText(Message);
        	
        	/* Start the main activity to perform the speech */
        	Intent Speaker = new Intent(AppContext, Main.class);
        	Speaker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	AppContext.startActivity(Speaker);
        }
    }
    
    /* Given a phone number, return the name from the user's contact list. If
     * the number is not in the user's contact list, return the original
     * number. */
    public String getContactNameByNumber(String Number)
    {
    	/* Look up the number in the contacts. */
        Uri LookupURI = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(Number));
        String Name   = null;

        /* Resolve the URI and get a cursor pointing to the result. */
        ContentResolver Resolver = m_Context.getContentResolver();
        Cursor Result = Resolver.query(LookupURI, new String[]
        		                       {
        		                           BaseColumns._ID,
                					       ContactsContract.PhoneLookup.DISPLAY_NAME
                					   }, 
                					   null, null, null);

        try
        {
        	/* Check the cursor for validity before attempting to extract the
        	 * name. */
            if ((Result != null) && (Result.getCount() > 0))
            {
            	Result.moveToNext();
                Name = Result.getString(Result.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        }
        finally
        {
        	/* Close the cursor once it's done being used. */
            if (Result != null)
            {
            	Result.close();
            }
        }

        /* If no name was found, return the original number. */
        if (Name == null)
        {
            Log.w(TAG, "Could not resolve number: " + Number);
        	return Number;
        }
        
    	Log.d(TAG, "Resolved number " + Number + " to " + Name);
        return Name;
    }
}

