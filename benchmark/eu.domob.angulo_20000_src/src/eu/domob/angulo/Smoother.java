/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2012  Daniel Kraft <d@domob.eu>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.domob.angulo;

import android.content.SharedPreferences;

/**
 * Keep track of a running average to smooth Vector values.
 */
public class Smoother
{

  /** Store the vectors we have here.  */
  private Vector[] buffer;

  /**
   * Where to store the next element.  This may either be a for now
   * empty slot, or an occupied one which should be replaced as
   * next element.
   */
  private int nextIndex;

  /**
   * Construct it, take the size we want from the peferences.
   * @param pref Preference object.
   */
  public Smoother (SharedPreferences pref)
  {
    final String val = pref.getString ("smoothing", null);
    int size;
    if (val.equals ("none"))
      size = 1;
    else if (val.equals ("little"))
      size = 10;
    else
      {
        assert (val.equals ("much"));
        size = 100;
      }

    buffer = new Vector[size];
    for (int i = 0; i < buffer.length; ++i)
      buffer[i] = null;
    nextIndex = 0;
  }

  /**
   * Put in a new measurement.
   * @param val New measurement value.
   */
  public void add (Vector val)
  {
    buffer[nextIndex] = val;
    nextIndex = ((nextIndex + 1) % buffer.length);
  }

  /**
   * Get smoothed value.
   * @return Smoothed value.
   * @throws RuntimeException If no values are there yet.
   */
  public Vector get ()
  {
    int cnt = 0;
    Vector sum = new Vector (0.0f, 0.0f, 0.0f);
    for (Vector v : buffer)
      if (v != null)
        {
          ++cnt;
          sum.add (v);
        }

    if (cnt == 0)
      throw new RuntimeException ("No data yet added to Smoother!");
    assert (cnt > 0);
    sum.scale (1.0f / cnt);

    return sum;
  }

}
