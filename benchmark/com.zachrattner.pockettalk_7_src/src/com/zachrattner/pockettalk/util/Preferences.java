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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.zachrattner.pockettalk.gui.Main;

/* Provides an interface to the application's preferences, which persist even
 * after the application is exited. */
public class Preferences
{
	/* Log tag, for debugging */
	private static final String TAG  = Preferences.class.getCanonicalName();
	
	/* Unique preferences identifier */
	private static final String ID   = "com.zachrattner.pockettalk";

	/* Key for the "should speak messages" flag */
	private static final String SPEAK_KEY = "Speak";
	
	/* Key for the "should prepend sender's name" flag */
	private static final String SENDER_KEY = "Sender";
	
	/* Key for the "should vibrate messages" flag */
	private static final String VIBRATE_KEY = "Vibrate";
	
	/* Key for the "dot duration" integer, in ms */
	private static final String DURATION_KEY = "Duration";
	
	/* Key for the string to be spoken */
	private static final String SPEECH_TEXT_KEY = "SpeechText";

	/* Preference reader */
	private SharedPreferences m_Preferences;
	
	/* Preference writer */
	private SharedPreferences.Editor m_Editor;
	
	/* Initialize a preference object's member data. */
	public Preferences(Context AppContext)
	{
		/* Nothing can be done if no context was provided. */
		if (AppContext == null)
		{
			return;
		}
		
		m_Preferences = AppContext.getSharedPreferences(ID, Context.MODE_PRIVATE);
		m_Editor      = m_Preferences.edit();
	}
	
	/* Mutator for the Sender flag */
	public void setSender(boolean Sender)
	{
		Log.d(TAG, "Setting sender to " + Boolean.toString(Sender));
		m_Editor.putBoolean(SENDER_KEY, Sender);
		m_Editor.commit();
	}
	
	/* Mutator for the Speak flag */
	public void setSpeak(boolean Speak)
	{
		Log.d(TAG, "Setting speak to " + Boolean.toString(Speak));
		m_Editor.putBoolean(SPEAK_KEY, Speak);
		m_Editor.commit();
	}

	/* Mutator for the Vibrate flag */
	public void setVibrate(boolean Vibrate)
	{
		Log.d(TAG, "Setting vibrate to " + Boolean.toString(Vibrate));
		m_Editor.putBoolean(VIBRATE_KEY, Vibrate);
		m_Editor.commit();		
	}
	
	/* Mutator for the Duration integer */
	public void setDuration(int Duration)
	{
		Log.d(TAG, "Setting duration to " + Integer.toString(Duration));
		m_Editor.putInt(DURATION_KEY, Duration);
		m_Editor.commit();
	}
	
	/* Mutator for the speech text */
	public void setSpeechText(String Text)
	{
		Log.d(TAG, "Setting speech text to " + Text);
		m_Editor.putString(SPEECH_TEXT_KEY, Text);
		m_Editor.commit();
	}

	/* Sets the speech text to an empty string */
	public void clearSpeechText()
	{
		Log.d(TAG, "Clearing speech text");
		m_Editor.putString(SPEECH_TEXT_KEY, "");
		m_Editor.commit();
	}
	
	/* Accessor for the Sender flag */
	public boolean sender()
	{
		return m_Preferences.getBoolean(SENDER_KEY, true);
	}
	
	/* Accessor for the Speak flag */
	public boolean speak()
	{
		return m_Preferences.getBoolean(SPEAK_KEY, true);
	}
	
	/* Accessor for the Vibrate flag */
	public boolean vibrate()
	{
		return m_Preferences.getBoolean(VIBRATE_KEY, false);
	}
	
	/* Accessor for the Duration integer */
	public int duration()
	{
		return m_Preferences.getInt(DURATION_KEY, Main.DEFAULT_DURATION);
	}
	
	/* Accessor for the string to speak */
	public String speechText()
	{
		return m_Preferences.getString(SPEECH_TEXT_KEY, "");
	}
}
