/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2013-2014  Daniel Kraft <d@domob.eu>

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

import android.app.Activity;
import android.app.Dialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.text.method.LinkMovementMethod;

import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;

/**
 * Base class for Angulo activities.  This class is the base used for both
 * the "classic" and "tri-angulo" activities, that handles common stuff like
 * tracking device movement and displaying the menus.
 */
public abstract class AnguloBase extends Activity implements SensorEventListener
{

  /* The list of sensors that will be listened to.  */
  private static final int sensorTypes[] = {Sensor.TYPE_ACCELEROMETER,
                                            Sensor.TYPE_MAGNETIC_FIELD};

  /** ID for help dialog.  */
  private static final int DIALOG_HELP = 0;
  /** ID for about dialog.  */
  private static final int DIALOG_ABOUT = 1;

  /** Log tag.  */
  public static final String TAG = "Angulo";

  /** Resource ID for help dialog layout.  */
  private final int helpLayout;
  /** Resource ID for help dialog link element.  */
  private final int helpLink;

  /** Preferences object used.  */
  protected SharedPreferences pref;

  /** Sensor manager used.  */
  private SensorManager sensorManager;

  /** Smoother for magnetic values.  */
  private Smoother smoothMagn;
  /** Smoother for accelerometer data.  */
  private Smoother smoothGrav;

  /**
   * Construct it from a sub class, handing in the layout IDs to use
   * for the help dialog.
   * @param layout Help dialog layout ID.
   * @param link Link element ID in the help dialog.
   */
  protected AnguloBase (int layout, int link)
  {
    super ();
    helpLayout = layout;
    helpLink = link;
  }

  /**
   * Create the activity with initialisation of things.
   * @param savedInstanceState Saved data (if any).
   */
  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    PreferenceManager.setDefaultValues (this, R.xml.preferences, false);
    pref = PreferenceManager.getDefaultSharedPreferences (this);

    sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE);

    /* Layout is not initialised, this is left to the child classes.  */
  }

  /**
   * We resume the activity.  Set up sensor listeners.
   */
  @Override
  public void onResume ()
  {
    super.onResume ();

    /* Set screen orientation.  This is done here so that preference changes
       take immediate effect when the main activity is shown again.  */
    setOrientation ();

    /* Initialise smoothers.  */
    smoothMagn = new Smoother (pref);
    smoothGrav = new Smoother (pref);

    /* Register sensor listeners.  */

    final String rate = pref.getString ("rate", null);
    int sensorRate;
    if (rate.equals ("fastest"))
      sensorRate = SensorManager.SENSOR_DELAY_FASTEST;
    else if (rate.equals ("game"))
      sensorRate = SensorManager.SENSOR_DELAY_GAME;
    else if (rate.equals ("ui"))
      sensorRate = SensorManager.SENSOR_DELAY_UI;
    else
      {
        assert (rate.equals ("normal"));
        sensorRate = SensorManager.SENSOR_DELAY_NORMAL;
      }

    for (int type : sensorTypes)
      {
        Sensor sens = sensorManager.getDefaultSensor (type);
        sensorManager.registerListener (this, sens, sensorRate);
      }
  }

  /**
   * Pause activity.  Unregister sensor listeners.
   */
  @Override
  public void onStop ()
  {
    super.onStop ();

    for (int type : sensorTypes)
      {
        Sensor sens = sensorManager.getDefaultSensor (type);
        sensorManager.unregisterListener (this, sens);
      }

    smoothMagn = null;
    smoothGrav = null;
  }

  /**
   * New sensor value available.
   * @param evt SensorEvent object.
   */
  @Override
  public void onSensorChanged (SensorEvent evt)
  {
    final int type = evt.sensor.getType ();
    switch (type)
      {
      case Sensor.TYPE_ACCELEROMETER:
        if (smoothGrav != null)
          {
            smoothGrav.add (new Vector (evt.values));
            newDirectionValue (type, smoothGrav.get ());
          }
        break;

      case Sensor.TYPE_MAGNETIC_FIELD:
        if (smoothMagn != null)
          {
            smoothMagn.add (new Vector (evt.values));
            newDirectionValue (type, smoothMagn.get ());
          }
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

  /**
   * Method called when a fresh sensor value is available.  This can be
   * overwritten by childs in order to update their state.
   * @param type Sensor type.
   * @param val New direction vector.
   */
  protected void newDirectionValue (int type, Vector val)
  {
    //Log.v (TAG, String.format ("New sensor measurement for type %d.", type));

    // Nothing else done.
  }

  /**
   * Create the options menu.
   * @param menu Inflate menu there.
   * @return True since we always succeed.
   */
  @Override
  public boolean onCreateOptionsMenu (Menu menu)
  {
    MenuInflater inflater = getMenuInflater ();
    inflater.inflate (R.menu.main, menu);
    return true;
  }

  /**
   * Perform action on click in menu.
   * @param itm Selected item.
   * @return True iff event was processed.
   */
  @Override
  public boolean onOptionsItemSelected (MenuItem itm)
  {
    switch (itm.getItemId ())
      {
      case R.id.help:
        showDialog (DIALOG_HELP);
        return true;

      case R.id.about:
        showDialog (DIALOG_ABOUT);
        return true;

      case R.id.preferences:
        startActivity (new Intent (this, Preferences.class));
        return true;

      default:
        return super.onOptionsItemSelected (itm);
      }
  }

  /**
   * Create a dialog.
   * @param id Dialog to create.
   * @return The created dialog.
   */
  @Override
  public Dialog onCreateDialog (int id)
  {
    Dialog dlg = new Dialog (this);

    switch (id)
      {
      case DIALOG_HELP:
        dlg.setContentView (helpLayout);

        TextView tv = (TextView) dlg.findViewById (helpLink);
        tv.setMovementMethod (LinkMovementMethod.getInstance ());

        dlg.setTitle (R.string.help_title);
        break;

      case DIALOG_ABOUT:
        dlg.setContentView (R.layout.about);

        tv = (TextView) dlg.findViewById (R.id.about_version);
        final String aboutVersion = getString (R.string.about_version);
        final String appName = getString (R.string.app_name);
        final String appVersion = getString (R.string.app_version);
        tv.setText (String.format (aboutVersion, appName, appVersion));

        tv = (TextView) dlg.findViewById (R.id.about_link1);
        tv.setMovementMethod (LinkMovementMethod.getInstance ());
        tv = (TextView) dlg.findViewById (R.id.about_link2);
        tv.setMovementMethod (LinkMovementMethod.getInstance ());

        dlg.setTitle (R.string.about_title);
        break;

      default:
        assert (false);
      }

    return dlg;
  }

  /**
   * Interpret the screen orientation preference and set it accordingly.
   */
  private void setOrientation ()
  {
    final String orientation = pref.getString ("orientation", null);

    int req;
    if (orientation.equals ("sensor"))
      req = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
    else if (orientation.equals ("portrait"))
      req = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    else
      {
        assert (orientation.equals ("landscape"));
        req = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
      }

    setRequestedOrientation (req);
  }

}
