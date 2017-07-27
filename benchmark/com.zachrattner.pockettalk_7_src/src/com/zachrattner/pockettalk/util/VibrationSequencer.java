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
import java.util.Timer;

import android.util.Log;

/* Represents a single character in Morse code. Handles turning the vibrator on
 * and off at the appropriate times to output the appropriate character. */
public class VibrationSequencer
{
	/* Log tag, for debugging */
	private static final String TAG = VibrationSequencer.class.getCanonicalName();
	
	/* Initial delay, in ms. Useful for avoiding interference with the
	 * notification vibration. */
	private static final int DELAY = 3000;
	
	/* List of timer tasks, each representing an interval of time where the
	 * vibrator is on. */
	private ArrayList<VibrationTimerTask> m_Sequence;
	
	/* The timer used to schedule vibration */
	private Timer m_Timer;
	
	/* The index in the current vibration array */
	private int m_Index;
	
	/* Initialize member data. */
	public VibrationSequencer()
	{
		m_Timer    = new Timer();
		m_Index    = 0;
		m_Sequence = new ArrayList<VibrationTimerTask>();
	}
	
	/* Mutator for Sequence array */
	public void setSequence(ArrayList<VibrationTimerTask> Sequence)
	{
		m_Sequence = Sequence;
	}

	/* Accessor for Sequence array */
	public ArrayList<VibrationTimerTask> sequence()
	{
		return m_Sequence;
	}
	
	/* Schedule each vibration in the character. */
	public void start()
	{
		int Sum = DELAY;
		
		for (m_Index = 0; m_Index < m_Sequence.size(); m_Index++)
		{
			VibrationTimerTask Task = m_Sequence.get(m_Index);
			
			if (Task == null)
			{
				Log.w(TAG, "No VibrationTimerTask at index " + Integer.toString(m_Index));
				continue;
			}
			
			if (Task.state().on())
			{
				m_Timer.schedule(Task, Sum);
			}
			
			Sum += Task.state().duration();
		}			
	}
	
	/* Prematurely halt vibration. */
	public void cancel()
	{
		m_Timer.cancel();
		m_Timer = new Timer();
	}
}
