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

package com.zachrattner.pockettalk.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.zachrattner.pockettalk.R;
import com.zachrattner.pockettalk.util.SpeechActivity;

/* Defines the main GUI where the user can set preferences */
public class Main extends SpeechActivity
{
	/* List of human-readable speeds for display in the Morse code duration 
	 * spinner */
	private static final String[] SPEEDS = {"Slow", "Medium", "Fast"};
	
	/* Duration of shortest dot, in ms. Also, the increment added to the
	 * current duration to get the next duration. For example, at 60 ms, the 
	 * speeds {"Slow", "Medium", "Fast"} would map to {180, 120, 60}. */
	private static final int DURATION_PER_STEP = 60;
	
	/* The index in the SPEEDS array to default to if none is specified */
	private static final int DEFAULT_SPEED_INDEX = ((SPEEDS.length + 1) / 2);

	/* The default dot duration, in ms */
	public  static final int DEFAULT_DURATION = (DEFAULT_SPEED_INDEX * DURATION_PER_STEP);

	/* Application context */
	private Context m_Context;
	
	/* Speed selection GUI element */
	private Spinner m_Speed;
	
	/* Vibrate enable GUI element */
	private CheckBox m_VibrateEnable;
	
	/* Sender prepend GUI element */
	private CheckBox m_Sender;
	
	/* Read aloud enable GUI element */
	private CheckBox m_Read;
	
	/* Layout containing the speed spinner and its label */
	private LinearLayout m_VibrateSpeedWrapper;
	
	/* Current dot duration */
	private int  m_Duration;
	
	/* Current vibrate preference */
	private boolean m_Vibrate;
	
	/* Current "prepend sender" preference */
	private boolean m_IncludeSender;
	
	/* Current "read aloud" preference */
	private boolean m_ReadMessages;
	
	/* Indicator that this is the first time the speed spinner was selected,
	 * to avoid displaying a notification when the default is set. */
	private boolean m_FirstPass;
	
	/* The mapping from speed strings in the spinner to dot durations in ms. */
	private static final Map<String, Integer> SPEED_MAP;
	static
	{
		Map<String, Integer> SpeedMap = new HashMap<String, Integer>();
		
		for (int i = 0; i < SPEEDS.length; i++)
		{
			SpeedMap.put(SPEEDS[i], DURATION_PER_STEP * (SPEEDS.length - i));
		}
		
		SPEED_MAP = Collections.unmodifiableMap(SpeedMap);
	}
	
	/* Called when application is launched */
	@Override
	public void onCreate(Bundle State)
	{
		/* Initialize GUI */
		super.onCreate(State);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		/* Initialize member data */
		m_Duration      = DEFAULT_DURATION;
		m_Vibrate       = m_Preferences.vibrate();
		m_IncludeSender = m_Preferences.sender();
		m_ReadMessages  = m_Preferences.speak();
		m_Context       = getApplicationContext();		
		m_FirstPass     = true;
		
		/* Locate GUI elements */
		m_Speed               = (Spinner)  findViewById(R.id.Speed);
		m_VibrateEnable       = (CheckBox) findViewById(R.id.Vibrate);
		m_Sender              = (CheckBox) findViewById(R.id.Sender);
		m_Read                = (CheckBox) findViewById(R.id.Read);
		m_VibrateSpeedWrapper = (LinearLayout) findViewById(R.id.VibrateSpeedWrapper);
		
		/* Initialize the speed selector GUI widget */
		ArrayAdapter<String> Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SPEEDS);
		Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_Speed.setAdapter(Adapter);
		
		/* Handle the selection of a new speed */
		m_Speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
		    public void onItemSelected(AdapterView<?> Parent, View NewView, int Position, long ID)
		    {
		        String Value = Parent.getItemAtPosition(Position).toString();
		        m_Duration = SPEED_MAP.get(Value);
				
				if (m_Duration <= 0)
				{
					m_Duration = DEFAULT_DURATION;
				}
				
				m_Preferences.setDuration(m_Duration);
				
				if (m_FirstPass)
				{
					m_FirstPass = false;
				}
				else
				{
					showDuration();
				}
		    }
		    
		    public void onNothingSelected(AdapterView<?> Parent)
		    {
		    	m_Duration = DEFAULT_DURATION;
		    	m_Preferences.setDuration(m_Duration);
		    	showDuration();
		    }
		    
		    private void showDuration()
		    {
		    	Toast.makeText
		    	(
		    		m_Context, 
		    		"The dot character will last " + Integer.toString(m_Duration) + " ms.", 
		    		Toast.LENGTH_SHORT
		    	).show();		    	
		    }
		});
		
		/* Handle toggling the vibrate enable checkbox */
		m_VibrateEnable.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View NewView)
			{
				m_Vibrate = m_VibrateEnable.isChecked();
				m_Preferences.setVibrate(m_Vibrate);
				
				if (m_Vibrate)
				{
					m_VibrateSpeedWrapper.setVisibility(View.VISIBLE);
					Toast.makeText
					(
						m_Context,
						"Text messages will be vibrated in Morse code when they are received.",
						Toast.LENGTH_SHORT
					).show();
				}
				else
				{
					m_VibrateSpeedWrapper.setVisibility(View.INVISIBLE);
					Toast.makeText
					(
						m_Context,
						"Text messages will not be vibrated in Morse code when they are received.", 
						Toast.LENGTH_SHORT
					).show();
				}
			}
		});
		
		/* Handle toggling the "prepend sender" checkbox */
		m_Sender.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View NewView)
			{
				m_IncludeSender = m_Sender.isChecked();
				m_Preferences.setSender(m_IncludeSender);
				
				if (m_IncludeSender)
				{
					Toast.makeText
					(
						m_Context, 
						"The sender's name will be included before the message text.", 
						Toast.LENGTH_SHORT
					).show();
				}
				else
				{
					Toast.makeText
					(
						m_Context, 
						"The sender's name will not be included before the message text.", 
						Toast.LENGTH_SHORT
					).show();
				}
			}
		});
		
		/* Handle toggling the "read aloud" checkbox */
		m_Read.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View NewView)
			{
				m_ReadMessages = m_Read.isChecked();
				m_Preferences.setSpeak(m_ReadMessages);
				
				if (m_ReadMessages)
				{
					Toast.makeText
					(
						m_Context,
						"Text messages will be read aloud when they are received.",
						Toast.LENGTH_SHORT
					).show();
				}
				else
				{
					Toast.makeText
					(
						m_Context, 
						"Text messages will not be read aloud when they are received.",
						Toast.LENGTH_SHORT
					).show();
				}
			}
		});
		
		/* Load the preferences and update the GUI to represent the saved
		 * state. */
		Integer Duration = m_Preferences.duration();
		int SpeedIndex   = DEFAULT_SPEED_INDEX;
		Object Speeds[]  = SPEED_MAP.values().toArray();
		
		for (int i = 0; i < Speeds.length; i++)
		{
			if (Duration == Speeds[i])
			{
				SpeedIndex = i;
				break;
			}
		}
		
		m_Speed.setSelection(SpeedIndex);
		m_VibrateEnable.setChecked(m_Preferences.vibrate());
		m_Sender.setChecked(m_Preferences.sender());
		m_Read.setChecked(m_Preferences.speak());
		
		/* Only show the vibration speed spinner if vibration is enabled. */
		if (m_VibrateEnable.isChecked())
		{
			m_VibrateSpeedWrapper.setVisibility(View.VISIBLE);
		}
	}
	
	/* Called when the activity was suspended, but not quit */
	@Override
	public void onResume()
	{
		super.onResume();
		
		/* Prevent the dot duration from being displayed when the application
		   is resumed. */
		m_FirstPass = true;
	}
}
