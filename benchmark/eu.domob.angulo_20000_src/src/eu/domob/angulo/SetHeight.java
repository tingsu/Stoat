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
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.hardware.SensorManager;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

/**
 * Activity to configure the measurement height.
 */
public class SetHeight extends Activity
{

  /** Preference key to set.  */
  public static final String PREF_KEY = "user_height";

  /** Preferences object used.  */
  private SharedPreferences pref;

  /** Sensor manager object used.  */
  private SensorManager sensorManager;

  /** Text input field.  */
  private EditText input;

  /** Drop measurement instance used (if currently measuring).  */
  private DropMeasure drop;
  /** Drop dialog.  */
  private DroppingDialog dropDlg;

  /**
   * Initialise / restore from saved state.
   * @param savedInstanceState Saved state to restore from.
   */
  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    pref = PreferenceManager.getDefaultSharedPreferences (this);
    final String defHeight = getString (R.string.height_default);
    final String heightVal = pref.getString (PREF_KEY, defHeight);

    sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE);
    drop = null;
    dropDlg = null;

    setContentView (R.layout.set_height);

    input = (EditText) findViewById (R.id.height_entry);
    input.setText (heightVal);

    Button ok = (Button) findViewById (R.id.buttonOk);
    ok.setOnClickListener (new View.OnClickListener ()
      {
        public void onClick (View v)
        {
          SharedPreferences.Editor edit = pref.edit ();
          edit.putString (PREF_KEY, input.getText ().toString ());
          edit.apply ();

          finish ();
        }
      });

    Button cancel = (Button) findViewById (R.id.buttonCancel);
    cancel.setOnClickListener (new View.OnClickListener ()
      {
        public void onClick (View v)
        {
          finish ();
        }
      });

    Button drop = (Button) findViewById (R.id.buttonDrop);
    drop.setOnClickListener (new View.OnClickListener ()
      {
        public void onClick (View v)
        {
          startDropMeasurement ();
        }
      });
  }

  /**
   * Start drop time measurement.
   */
  private void startDropMeasurement ()
  {
    assert (drop == null && dropDlg == null);
    drop = new DropMeasure (sensorManager, this);
    dropDlg = new DroppingDialog (this);

    drop.start ();
    dropDlg.show ();
  }

  /**
   * Report a drop measurement result.
   * @param val Measured height value.
   */
  public void reportDropResult (double val)
  {
    drop.stop ();
    dropDlg.dismiss ();

    drop = null;
    dropDlg = null;

    input.setText (String.format (Locale.US, "%.2f", val));
  }

  /**
   * Dropping dialog cancelled.
   */
  public void reportDropCancel ()
  {
    drop.stop ();

    drop = null;
    dropDlg = null;
  }

  /* ************************************************************************ */
  /* DroppingDialog.  */

  /**
   * Progress dialog shown while drop time measurement is ongoing.
   */
  private static class DroppingDialog extends ProgressDialog
    implements DialogInterface.OnCancelListener
  {

    /** Parent SetHeight activity.  */
    private SetHeight parent;

    /**
     * Construct it given the main activity.
     * @param p Parent activity.
     */
    public DroppingDialog (SetHeight p)
    {
      super (p);

      parent = p;

      setCancelable (true);
      setProgressStyle (ProgressDialog.STYLE_SPINNER);
      setMessage (p.getString (R.string.height_dropping_msg));

      setOnCancelListener (this);
    }

    /**
     * Handle cancel event.
     * @param dlg Dialog cancelled.
     */
    @Override
    public void onCancel (DialogInterface dlg)
    {
      parent.reportDropCancel ();
    }

  }

}
