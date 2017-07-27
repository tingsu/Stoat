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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

/* Handles conversion from strings to vibration timing and controls the vibrator
 * via a timer. */
public class MorseCode
{
	/* Log tag for debugging */
	private static final String TAG = MorseCode.class.getCanonicalName();
	
	/* Application context */
	private Context m_Context;
	
	/* Interface to the hardware vibrator */
	private Vibrator m_Vibrator;
	
	/* Interface to application preferences */
	private Preferences m_Preferences;
	
	/* For sequencing vibrations and handling periods of no vibration */
	private VibrationSequencer m_Sequencer;
	
	/* Duration of a dot, in ms */
	private Integer m_Dot;
	
	/* Duration of a dash, in ms */
	private Integer m_Dash;
	
	/* Duration of a pause between symbols in a letter, in ms */
    private Integer m_IntraLetterDelay; 
    
    /* Duration of a pause between letters, in ms */
    private Integer m_InterLetterDelay; 
    
    /* Duration of a pause between words, in ms */
    private Integer m_InterWordDelay;    
    
    /* Mapping from characters to vibration duration sequences */
    private Map<Character, Integer[]> m_CharMap;
	
	public MorseCode(Context AppContext)
	{
		/* Initialize member data */
		m_Context     = AppContext;
		m_Preferences = new Preferences(m_Context);
		m_Vibrator    = null;
		m_Sequencer   = null;
		
		/* The preferences store the duration of a dot ("unit time"), so
		 * calculate all the possible characters based off of the dot's
		 * duration. */
		calculateTiming(m_Preferences.duration());
		
		/* If the vibrator failed to initialize, then notify the user. */
		if (!initVibrator())
		{
			Log.e(TAG, "No vibrator present");
			Toast.makeText(AppContext, "No vibrator present!", Toast.LENGTH_SHORT).show();
		}
	}
	
	/* Attempt to initialize the hardware vibrator. This will fail if the
	 * device doesn't have a vibrator, or the operating system can't initialize
	 * it for some reason. */
	public boolean initVibrator()
	{
		m_Vibrator = (Vibrator) m_Context.getSystemService(Context.VIBRATOR_SERVICE);
		return (m_Vibrator != null);
	}
	
	/* Determine the on/off sequence to vibrate the given string, and schedule
	 * a timer to perform the appropriate vibration. */
	public void processString(String ToVibrate)
	{
		/* The map contains only uppercase characters. Plus, Morse code doesn't
		 * differentiate between upper and lowercase letters. */
		ToVibrate = ToVibrate.toUpperCase();
		
		int Length = ToVibrate.length();
		ArrayList<VibrationTimerTask> Sequence = new ArrayList<VibrationTimerTask>();
				
		for (int i = 0; i < Length; i++)
		{
			Character Item          = ToVibrate.charAt(i);
			VibrationTimerTask Task = null;
			
			Log.d(TAG, "Handling '" + Item + "'...");
			
			/* Handle spaces */
			if (Item.equals(' '))
			{
				/* Replace the trailing inter-letter delay, if it's present. */
				if (Sequence.size() > 0)
				{
					Sequence.remove(Sequence.size() - 1);
				}
				
				Task = new VibrationTimerTask(m_Vibrator);
				Task.state().setOn(false);
				Task.state().setDuration(m_InterWordDelay);
				Sequence.add(Task);
				continue;
			}
			
			/* Handle non-space characters by consulting the map. */
			Integer[] Durations = m_CharMap.get(Item);
			if (Durations == null)
			{
				continue;
			}
			
			/* Build the on/off vibration sequence for the character. */
			for (int j = 0; j < Durations.length; j++)
			{
				/* Set the dot or the dash. */
				Task = new VibrationTimerTask(m_Vibrator);
				Task.state().setOn(true);
				Task.state().setDuration(Durations[j]);
				Sequence.add(Task);
				
				/* Append an intra-letter delay. */
				if (j < (Durations.length - 1))
				{
					Task = new VibrationTimerTask(m_Vibrator);
					Task.state().setOn(false);
					Task.state().setDuration(m_IntraLetterDelay);
					Sequence.add(Task);
				}
			}
			
			/* The letter is complete, so add an inter-letter delay. This delay
			 * will be removed if the next character turns out to be a space. */
			Task = new VibrationTimerTask(m_Vibrator);
			Task.state().setOn(false);
			Task.state().setDuration(m_InterLetterDelay);
			Sequence.add(Task);
		}
	
		/* Dump the entire vibration sequence. */
		for (int i = 0; i < Sequence.size(); i++)
		{
			String On = Sequence.get(i).state().on() ? "on" : "off";
			Log.d(TAG, On + ", " + Integer.toString(Sequence.get(i).state().duration()) + " ms");
		}
		
		/* Schedule the sequence on the hardware vibrator. */
		m_Sequencer = new VibrationSequencer();		
		m_Sequencer.setSequence(Sequence);
		m_Sequencer.start();
	}
	
	/* Prematurely cancel a vibration sequence. */
	public void cancel()
	{
		if (m_Sequencer != null)
		{
			m_Sequencer.cancel();
		}
	}

	/* Determine the timing for all possible characters from the duration of
	 * the dot, in ms. */
	private void calculateTiming(int Dot)
	{
		// Morse code characters
		m_Dot  = Dot;
		m_Dash = (3 * m_Dot);
		
		// Timing values
		m_IntraLetterDelay = m_Dot;
		m_InterLetterDelay = (3 * m_Dot);
		m_InterWordDelay   = (7 * m_Dot);
		
		// Alphabetic characters
		Integer A[] = {m_Dot, m_Dash};
		Integer B[] = {m_Dash, m_Dot, m_Dot, m_Dot};
		Integer C[] = {m_Dash, m_Dot, m_Dash, m_Dot};
		Integer D[] = {m_Dash, m_Dot, m_Dot};
		Integer E[] = {m_Dot};
		Integer F[] = {m_Dot, m_Dot, m_Dash, m_Dot};
		Integer G[] = {m_Dash, m_Dash, m_Dot};
		Integer H[] = {m_Dot, m_Dot, m_Dot, m_Dot};
		Integer I[] = {m_Dot, m_Dot};
		Integer J[] = {m_Dot, m_Dash, m_Dash, m_Dash};
		Integer K[] = {m_Dash, m_Dot, m_Dash};
		Integer L[] = {m_Dot, m_Dash, m_Dot, m_Dot};
		Integer M[] = {m_Dash, m_Dash};
		Integer N[] = {m_Dash, m_Dot};
		Integer O[] = {m_Dash, m_Dash, m_Dash};
		Integer P[] = {m_Dot, m_Dash, m_Dash, m_Dot};
		Integer Q[] = {m_Dash, m_Dash, m_Dot, m_Dash};
		Integer R[] = {m_Dot, m_Dash, m_Dot};
		Integer S[] = {m_Dot, m_Dot, m_Dot};
		Integer T[] = {m_Dash};
		Integer U[] = {m_Dot, m_Dot, m_Dash};
		Integer V[] = {m_Dot, m_Dot, m_Dot, m_Dash};
		Integer W[] = {m_Dot, m_Dash, m_Dash};
		Integer X[] = {m_Dash, m_Dot, m_Dot, m_Dash};
		Integer Y[] = {m_Dash, m_Dot, m_Dash, m_Dash};
		Integer Z[] = {m_Dash, m_Dash, m_Dot, m_Dot};

		// Numeric characters
		Integer Zero[]  = {m_Dash, m_Dash, m_Dash, m_Dash, m_Dash};
		Integer One[]   = {m_Dot, m_Dash, m_Dash, m_Dash, m_Dash};
		Integer Two[]   = {m_Dot, m_Dot, m_Dash, m_Dash, m_Dash};
		Integer Three[] = {m_Dot, m_Dot, m_Dot, m_Dash, m_Dash};
		Integer Four[]  = {m_Dot, m_Dot, m_Dot, m_Dot, m_Dash};
		Integer Five[]  = {m_Dot, m_Dot, m_Dot, m_Dot, m_Dot};
		Integer Six[]   = {m_Dash, m_Dot, m_Dot, m_Dot, m_Dot};
		Integer Seven[] = {m_Dash, m_Dash, m_Dot, m_Dot, m_Dot};
		Integer Eight[] = {m_Dash, m_Dash, m_Dash, m_Dot, m_Dot};
		Integer Nine[]  = {m_Dash, m_Dash, m_Dash, m_Dash, m_Dot};

		// Punctuation characters
		Integer Period[]      = {m_Dot, m_Dash, m_Dot, m_Dash, m_Dot, m_Dash};
		Integer Comma[]       = {m_Dash, m_Dash, m_Dot, m_Dot, m_Dash, m_Dash};
		Integer Colon[]       = {m_Dash, m_Dash, m_Dash, m_Dot, m_Dot, m_Dot};
		Integer Question[]    = {m_Dot, m_Dot, m_Dash, m_Dash, m_Dot, m_Dot};
		Integer Apostrophe[]  = {m_Dot, m_Dash, m_Dash, m_Dash, m_Dash, m_Dot};
		Integer Hyphen[]      = {m_Dash, m_Dot, m_Dot, m_Dot, m_Dot, m_Dash};
		Integer Slash[]       = {m_Dash, m_Dot, m_Dot, m_Dash, m_Dot};
		Integer Parenthesis[] = {m_Dash, m_Dot, m_Dash, m_Dash, m_Dot, m_Dash};
		Integer Quote[]       = {m_Dot, m_Dash, m_Dot, m_Dot, m_Dash, m_Dot};
		Integer At[]          = {m_Dot, m_Dash, m_Dash, m_Dot, m_Dash, m_Dot};
		Integer Equals[]      = {m_Dash, m_Dot, m_Dot, m_Dot, m_Dash};
		
		// Build a mapping from characters to Morse code symbol arrays
		m_CharMap = new HashMap<Character, Integer[]>();
			
		m_CharMap.put('A', A);
		m_CharMap.put('B', B);
		m_CharMap.put('C', C);
		m_CharMap.put('D', D);
		m_CharMap.put('E', E);
		m_CharMap.put('F', F);
		m_CharMap.put('G', G);
		m_CharMap.put('H', H);
		m_CharMap.put('I', I);
		m_CharMap.put('J', J);
		m_CharMap.put('K', K);
		m_CharMap.put('L', L);
		m_CharMap.put('M', M);
	    m_CharMap.put('N', N);
	    m_CharMap.put('O', O);
	    m_CharMap.put('P', P);
	    m_CharMap.put('Q', Q);
	    m_CharMap.put('R', R);
	    m_CharMap.put('S', S);
	    m_CharMap.put('T', T);
	    m_CharMap.put('U', U);
	    m_CharMap.put('V', V);
	    m_CharMap.put('W', W);
	    m_CharMap.put('X', X);
	    m_CharMap.put('Y', Y);
	    m_CharMap.put('Z', Z);
	    
	    m_CharMap.put('0', Zero);
	    m_CharMap.put('1', One);
	    m_CharMap.put('2', Two);
	    m_CharMap.put('3', Three);
	    m_CharMap.put('4', Four);
	    m_CharMap.put('5', Five);
	    m_CharMap.put('6', Six);
	    m_CharMap.put('7', Seven);
	    m_CharMap.put('8', Eight);
	    m_CharMap.put('9', Nine);
	    
	    /* There is no exclamation point in Morse code, so treat it as a 
	     * period. */
	    m_CharMap.put('!',  Period);
	    m_CharMap.put('.',  Period);
	    m_CharMap.put(',',  Comma);
	    m_CharMap.put(':',  Colon);
	    m_CharMap.put('?',  Question);
	    m_CharMap.put('\'', Apostrophe);
	    m_CharMap.put('-',  Hyphen);
	    m_CharMap.put('/',  Slash);
	    m_CharMap.put('(',  Parenthesis);
	    m_CharMap.put(')',  Parenthesis);
	    m_CharMap.put('"',  Quote);
	    m_CharMap.put('@',  At);
	    m_CharMap.put('=',  Equals);
	}
}
