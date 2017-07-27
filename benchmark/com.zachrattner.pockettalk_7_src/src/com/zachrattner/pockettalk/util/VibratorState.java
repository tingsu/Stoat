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

/* Represents a state of being of the hardware vibrator */
public class VibratorState
{
	/* Whether the vibrator is vibrating (true) or not (false) */
	private boolean m_On;
	
	/* The duration of this state, in ms */
	private int m_Duration;
	
	/* Initialize member data */
	public VibratorState()
	{
		m_On = false;
		m_Duration = 0;
	}
	
	/* Mutator for On member */
	public void setOn(boolean On)
	{
		m_On = On;
	}
	
	/* Mutator for Duration member */
	public void setDuration(int Duration)
	{
		m_Duration = Duration;
	}
	
	/* Accessor for On member */
	public boolean on()
	{
		return m_On;
	}
	
	/* Accessor for Duration member */
	public int duration()
	{
		return m_Duration;
	}
}
