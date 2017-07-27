/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2013  Daniel Kraft <d@domob.eu>

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

import android.content.Context;

import android.hardware.Camera;

import android.util.AttributeSet;
import android.util.Log;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Handle a camera preview that is displayed to a surface view.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{

  /** Camera used or null if not currently previewing.  */
  private Camera cam;
  /** Camera info for the active cam.  */
  private Camera.CameraInfo info;

  /** The activity to use for screen orientation.  */
  private Activity act;

  /**
   * Construct it.
   * @param c Our context.
   */
  public CameraPreview (Context c)
  {
    super (c);
    init ();
  }

  /**
   * Construct it.
   * @param c Our context.
   * @param attr Attributes.
   */
  public CameraPreview (Context c, AttributeSet attr)
  {
    super (c, attr);
    init ();
  }

  /**
   * Construct it.
   * @param c Our context.
   * @param attr Attributes.
   * @param ds DefStyle.
   */
  public CameraPreview (Context c, AttributeSet attr, int ds)
  {
    super (c, attr, ds);
    init ();
  }

  /**
   * Initialise the surface callback.
   */
  private void init ()
  {
    cam = null;
    act = null;
    info = null;

    final SurfaceHolder holder = getHolder ();
    holder.addCallback (this);
    holder.setType (SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  /**
   * Set the activity used.  This takes care of the correct screen orientation.
   * @param a The Activity to use for screen orientation.
   */
  public void setActivity (Activity a)
  {
    act = a;
    if (cam != null)
      {
        cam.stopPreview ();
        setCamOrientation ();
        cam.startPreview ();
      }
  }

  /**
   * Surface created.  Starts previewing.
   * @param holder Surface holder.
   */
  public void surfaceCreated (SurfaceHolder holder)
  {
    Log.i (AnguloBase.TAG, "Surface created, starting preview.");
    assert (cam == null);

    /* Find the first back-facing camera if there is any.  We don't just
       use Camera.open() because we need the index for getting the camera
       info and using it to set the display orientation.  */

    if (info == null)
      info = new Camera.CameraInfo ();

    int camIndex, camCnt;
    camCnt = Camera.getNumberOfCameras ();
    Log.i (AnguloBase.TAG, String.format ("Trying %d cameras...", camCnt));
    camIndex = -1;
    for (int i = 0; i < camCnt; ++i)
      {
        Camera.getCameraInfo (i, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
          {
            Log.i (AnguloBase.TAG,
                   String.format ("Found back-facing cam %d.", i));
            camIndex = i;
            break;
          }
      }
    
    if (camIndex != -1)
      {
        assert (info != null);

        cam = Camera.open (camIndex);
        try
          {
            cam.setPreviewDisplay (holder);
          }
        catch (IOException exc)
          {
            exc.printStackTrace ();
          }

        setCamOrientation ();
        cam.startPreview ();
      }
    else
      {
        Log.w (AnguloBase.TAG, "Found no back-facing camera.");
      }
  }

  /**
   * Surface destroyed.  Stop previewing.
   * @param holder Surface holder.
   */
  public void surfaceDestroyed (SurfaceHolder holder)
  {
    Log.i (AnguloBase.TAG, "Surface destroyed, stopping preview.");

    if (cam != null)
      {
        cam.stopPreview ();
        cam.release ();
        cam = null;
      }
  }

  /**
   * Surface changed, nothing to do.
   * @param holder Surface holder.
   * @param fmt Format.
   * @param w Width.
   * @param h Height.
   */
  public void surfaceChanged (SurfaceHolder holder, int fmt, int w, int h)
  {
    Log.i (AnguloBase.TAG, String.format ("Display size: %dx%d.", w, h));
    /* Nothing to do.  */
  }

  /**
   * Set proper display orientation for the camera preview.  This method
   * is based on the example in the API documentation for
   * Camera.setDisplayOrientation.
   */
  private void setCamOrientation ()
  {
    if (act == null || cam == null)
      return;

    assert (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK);
    final int rot = act.getWindowManager ().getDefaultDisplay ().getRotation ();
    int deg = 0;
    switch (rot)
      {
      case Surface.ROTATION_0:
        deg = 0;
        break;
      case Surface.ROTATION_90:
        deg = 90;
        break;
      case Surface.ROTATION_180:
        deg = 180;
        break;
      case Surface.ROTATION_270:
        deg = 270;
        break;
      default:
        assert (false);
      }

    final int val = (info.orientation - deg + 360) % 360;
    cam.setDisplayOrientation (val);
  }

}
