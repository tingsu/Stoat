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

import java.util.TimerTask;

import android.os.Vibrator;
import android.util.Log;

/* Handles turning on the vibrator for a predetermined amount of time from a
 * timer's context. */
public class VibrationTimerTask extends TimerTask
{	
	/* Log tag, for debugging */
	private static final String TAG = VibrationTimerTask.class.getCanonicalName();
	
	/* The state of the vibrator represented by this task */
	private VibratorState m_State;
	
	/* An interface to the hardware vibrator */
	private Vibrator m_Vibrator;
	
	/* Initialize member data */
	public VibrationTimerTask(Vibrator NewVibrator)
	{
		m_Vibrator = NewVibrator;
		m_State    = new VibratorState();
	}
	
	/* Mutator for State member */
	public void setState(VibratorState State)
	{
		m_State = State;
	}
	
	/* Accessor for State member */
	public VibratorState state()
	{
		return m_State;
	}
	
	/* Called on timer expiry to perform vibration */
	@Override
	public void run()
	{	
		if (m_State == null)
		{
			Log.e(TAG, "Vibrator state is null");
			return;
		}
		
		if (m_State.on())
		{
			if (m_Vibrator == null)
			{
				Log.e(TAG, "Vibrator is null");
				return;
			}
			
			m_Vibrator.vibrate(m_State.duration());
		}
	}
}
