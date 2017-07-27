/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2014  Daniel Kraft <d@domob.eu>

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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.util.Log;

/**
 * Handle sensor events and measure the time it takes for the device to drop.
 * This is then used to calculate the dropping height.
 */
public class DropMeasure implements SensorEventListener
{

  /** Sensor rate to use.  */
  private static final int RATE = SensorManager.SENSOR_DELAY_FASTEST;

  /** Treshold for accelerometer reading to consider as falling.  */
  private static final float TRESHOLD = 0.1f * SensorManager.GRAVITY_EARTH;

  /** Enum with possible status values.  */
  private static enum StatusValues
  {
    /** Not running.  */
    STOPPED,

    /** Waiting for first time in free fall.  */
    WAITING_FOR_DROP,

    /** Dropping.  */
    DROPPING,

    /** Measurement done.  */
    DONE;
  }

  /** Sensor manager used.  */
  private SensorManager sensorManager;

  /** SetHeight object to report back measured values.  */
  private SetHeight parent;

  /** Status.  */
  private StatusValues status;

  /** Time when we first measured dropping.  */
  private double dropStart;

  /**
   * Construct it.
   * @param sm Sensor manager to use.
   * @param p Parent object to use for reporting back results.
   */
  public DropMeasure (SensorManager sm, SetHeight p)
  {
    sensorManager = sm;
    parent = p;
    status = StatusValues.STOPPED;
  }

  /**
   * Start listening.
   */
  public void start ()
  {
    Sensor sens = sensorManager.getDefaultSensor (Sensor.TYPE_ACCELEROMETER);
    sensorManager.registerListener (this, sens, RATE);

    assert (status == StatusValues.STOPPED);
    status = StatusValues.WAITING_FOR_DROP;

    Log.d (AnguloBase.TAG, "Starting drop time measurement.");
  }

  /**
   * Stop listening.
   */
  public void stop ()
  {
    Sensor sens = sensorManager.getDefaultSensor (Sensor.TYPE_ACCELEROMETER);
    sensorManager.unregisterListener (this, sens);

    assert (status != StatusValues.STOPPED);
    status = StatusValues.STOPPED;

    Log.d (AnguloBase.TAG, "Stopping drop time measurement.");
  }

  /**
   * New sensor value available.
   * @param evt SensorEvent object.
   */
  @Override
  public void onSensorChanged (SensorEvent evt)
  {
    assert (evt.sensor.getType () == Sensor.TYPE_ACCELEROMETER);

    final Vector v = new Vector (evt.values);
    final boolean falling = (v.norm () < TRESHOLD);
    final double curTime = System.currentTimeMillis () / 1000.0;

    switch (status)
      {
      case WAITING_FOR_DROP:
        if (falling)
          {
            status = StatusValues.DROPPING;
            dropStart = curTime;
            Log.i (AnguloBase.TAG, "Free fall detected.");
          }
        break;

      case DROPPING:
        if (!falling)
          {
            status = StatusValues.DONE;

            final double dt = curTime - dropStart;
            assert (dt > 0.0);
            final double height = 0.5 * SensorManager.GRAVITY_EARTH * dt * dt;
            Log.i (AnguloBase.TAG,
                   String.format ("Free fall finished after %.2f s, gives"
                                  + " height %.2f m.", dt, height));

            parent.reportDropResult (height);
          }
        break;

      case DONE:
        /* Ignore.  */
        break;

      default:
        assert (false);
        break;
      }
  }

  /**
   * Sensor accuracy change.  Ignored.
   * @param sens Sensor changed.
   * @param accuracy New accuracy value.
   */
  @Override
  public void onAccuracyChanged (Sensor sens, int accuracy)
  {
    // Ignore.
  }

}
