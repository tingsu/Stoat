/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2011-2014  Daniel Kraft <d@domob.eu>

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

import android.os.Bundle;
import android.os.Vibrator;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

/**
 * Main activity for the "classic" version of Angulo.
 */
public class AnguloClassic extends AnguloBase
{

  private Vibrator vibrator;

  private Button toggleFreeze;
  private TextView gravityDegree, magneticDegree, combinedDegree;
  private TextView gravityPercent, magneticPercent, combinedPercent;

  private State state;

  /* Utility class encapsulating the application state.  This is used
     for easy storing and retrieving.  */
  private static final class State
  {

    /* Stored reference values, if any.  */
    public Vector refMagn;
    public Vector refGrav;

    /* Last sensor values reported.  */
    public Vector lastMagn;
    public Vector lastGrav;

    /* Number of times we're 'frozen' (may be up to twice, because of onStop
       and explicit manual freeze!).  */
    public byte frozen;

    public State ()
    {
      refMagn = null;
      refGrav = null;
      lastMagn = null;
      lastGrav = null;

      /* We start frozen, since there will follow a onResume event!  */
      frozen = 1;
    }

  }

  /**
   * Construct it with the proper help dialog IDs.
   */
  public AnguloClassic ()
  {
    super (R.layout.help_classic, R.id.help_classic_link);
  }

  /* Initialize some variables and find components.  */
  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate (savedInstanceState);

    /* Create state or restore saved one.  */
    if (getLastNonConfigurationInstance () != null)
      state = (State) getLastNonConfigurationInstance ();
    else
      state = new State ();

    setContentView (R.layout.classic);

    vibrator = (Vibrator) getSystemService (VIBRATOR_SERVICE);

    gravityDegree = (TextView) findViewById (R.id.gravityDegree);
    magneticDegree = (TextView) findViewById (R.id.magneticDegree);
    combinedDegree = (TextView) findViewById (R.id.combinedDegree);
    gravityPercent = (TextView) findViewById (R.id.gravityPercent);
    magneticPercent = (TextView) findViewById (R.id.magneticPercent);
    combinedPercent = (TextView) findViewById (R.id.combinedPercent);

    Button setRef = (Button) findViewById (R.id.buttonSetReference);
    setRef.setOnClickListener (new View.OnClickListener ()
      {
        public void onClick (View v)
        {
          doSetReference ();
        }
      });
    Button setLevel = (Button) findViewById (R.id.buttonLevel);
    setLevel.setOnClickListener (new View.OnClickListener ()
      {
        public void onClick (View v)
        {
          doSetLevel ();
        }
      });

    toggleFreeze = (Button) findViewById (R.id.buttonFreeze);
    View.OnClickListener freezeOnClick = new View.OnClickListener ()
      {
        public void onClick (View v)
        {
          doFreezeUnfreeze ();
        }
      };
    toggleFreeze.setOnClickListener (freezeOnClick);

    /* Also allow UI actions on the big degree display.  This is (hopefully)
       useful when interacting while holding the device away, when it's
       not easy to find the right button.  */
    combinedDegree.setOnClickListener (freezeOnClick);
    combinedDegree.setOnLongClickListener (new View.OnLongClickListener ()
      {
        public boolean onLongClick (View v)
        {
          doSetReference ();
          return true;
        }
      });

    /* Finally update display.  This seems to be necessary only when we are
       reloaded in frozen state, but it can't hurt.  */
    updateDisplays ();
  }

  /* Save transient state if there is any.  */
  @Override
  public Object onRetainNonConfigurationInstance () 
  {
    if (state != null)
      return state;

    return super.onRetainNonConfigurationInstance ();
  }

  /* Register the sensor listeners when we are active.  */
  @Override
  public void onResume ()
  {
    super.onResume ();
    unfreeze ();
  }
  private void unfreeze ()
  {
    assert (state.frozen > 0);
    --state.frozen;

    setFreezeLabel ();
  }

  /* Unregister sensor listeners to save battery.  */
  @Override
  public void onStop ()
  {
    super.onStop ();
    freeze ();
  }
  private void freeze ()
  {
    ++state.frozen;
    setFreezeLabel ();
  }

  @Override
  protected void newDirectionValue (int type, Vector val)
  {
    super.newDirectionValue (type, val);

    assert (state.frozen >= 0);
    if (state.frozen > 0)
      return;
      
    switch (type)
      {
      case Sensor.TYPE_ACCELEROMETER:
        state.lastGrav = val;
        updateDisplays ();
        break;

      case Sensor.TYPE_MAGNETIC_FIELD:
        state.lastMagn = val;
        updateDisplays ();
        break;

      default:
        assert (false);
        break;
      }
  }

  /* Update all displays from the current state.  */
  private void updateDisplays ()
  {
    setDisplays (state.refGrav, state.lastGrav, gravityDegree, gravityPercent);
    setDisplays (state.refMagn, state.lastMagn,
                 magneticDegree, magneticPercent);
    updateCombined ();
  }

  /* Update display of the 'combined' value.  Combined takes either gravity
     or magnetic field if one of them is available, or uses both of them
     if both are.  That way, we should get an accurate measurement even if
     we're rotating around one of those vectors as axis.  */
  private void updateCombined ()
  {
    final boolean hasGrav = (state.refGrav != null && state.lastGrav != null);
    final boolean hasMagn = (state.refMagn != null && state.lastMagn != null);

    if (hasGrav && !hasMagn)
      setDisplays (state.refGrav, state.lastGrav,
                   combinedDegree, combinedPercent);
    else if (hasMagn && !hasGrav)
      setDisplays (state.refMagn, state.lastMagn,
                   combinedDegree, combinedPercent);
    else if (hasGrav && hasMagn)
      {
        final float res
          = RotationMatrix.getRotationAngle (state.refGrav, state.lastGrav,
                                             state.refMagn, state.lastMagn);
        setDisplays (res, combinedDegree, combinedPercent);
      }
    else
      setDisplays (null, null, combinedDegree, combinedPercent);
  }

  /* For reference and measured value (either gravity or magnetic), set the
     display widgets accordingly.  */
  private void setDisplays (Vector ref, Vector now, TextView deg, TextView per)
  {
    if (ref == null)
      setDisplays (null, deg, per);
    else
      {
        assert (now != null);
        final float rad = Vector.angle (ref, now);
        setDisplays (rad, deg, per);
      }
  }
  private void setDisplays (Float rad, TextView deg, TextView per)
  {
    if (rad == null)
      {
        deg.setText ("-");
        per.setText ("");
        return;
      }

    final int degVal = Math.round ((float) Math.toDegrees (rad));
    final int perVal = Math.round (100.0f * (float) Math.tan (rad));

    deg.setText (degVal + "°");
    if (Math.abs (degVal) <= 45)
      per.setText (perVal + "%");
    else
      per.setText ("-");
  }

  /* Handle the 'set reference' UI action.  */
  private void doSetReference ()
  {
    state.refMagn = state.lastMagn;
    state.refGrav = state.lastGrav;
    vibrator.vibrate (500);
  }

  /* Handle the 'set reference to level' UI action.  */
  private void doSetLevel ()
  {
    state.refMagn = null;
    state.refGrav = state.lastGrav.snatchToAxis ();
    vibrator.vibrate (500);
  }

  /* Handle the freeze/unfreeze UI action.  */
  private void doFreezeUnfreeze ()
  {
    if (state.frozen > 0)
      {
        unfreeze ();
        vibrator.vibrate (new long[] {0, 25, 100, 25}, -1);
      }
    else
      {
        freeze ();
        vibrator.vibrate (25);
      }
  }

  /* Update the freeze-button's label according to current state.  */
  private void setFreezeLabel ()
  {
    if (state.frozen > 0)
      toggleFreeze.setText (R.string.unfreeze);
    else
      toggleFreeze.setText (R.string.freeze);
  }

}
