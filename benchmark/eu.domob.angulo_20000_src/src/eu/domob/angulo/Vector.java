/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2011-2013  Daniel Kraft <d@domob.eu>

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

/* A 3D vector with basic routines for calculations.  */
public final class Vector
{

  private float[] values;

  public Vector (float[] v)
  {
    assert (v.length == 3);
    values = new float[3];
    for (int i = 0; i < 3; ++i)
      values[i] = v[i];
  }

  public Vector (float x, float y, float z)
  {
    values = new float[3];
    values[0] = x;
    values[1] = y;
    values[2] = z;
  }

  public Vector (Vector v)
  {
    this (v.values);
  }

  public int dim ()
  {
    return values.length;
  }

  public float get (int ind)
  {
    return values[ind];
  }

  public static float innerProduct (Vector a, Vector b)
  {
    assert (a.values.length == b.values.length);

    float res = 0.0f;
    for (int i = 0; i < a.values.length; ++i)
      res += a.values[i] * b.values[i];

    return res;
  }

  public static Vector crossProduct (Vector a, Vector b)
  {
    assert (a.values.length == 3 && b.values.length == 3);

    float[] res = new float[3];
    for (int i = 0; i < 3; ++i)
      {
        final int p = (i + 1) % 3;
        final int pp = (i + 2) % 3;
        res[i] = a.values[p] * b.values[pp] - a.values[pp] * b.values[p];
      }
    
    return new Vector (res);
  }

  public float norm ()
  {
    return (float) Math.sqrt (innerProduct (this, this));
  }

  public void normalize ()
  {
    final float n = norm ();
    scale (1.0f / n);
  }

  public void add (Vector v)
  {
    assert (v.values.length == values.length);
    for (int i = 0; i < values.length; ++i)
      values[i] += v.values[i];
  }

  public void scale (float f)
  {
    for (int i = 0; i < values.length; ++i)
      values[i] *= f;
  }

  /* "Snatch" this vector to one of the (six) coordinate semi-axes.  In other
     words, return the (signed) basis vector that is closest to this one
     in angle.  This can be used to find the "exact" reference point
     for a given "approximate" device orientation.  */
  public Vector snatchToAxis ()
  {
    assert (values.length > 0);

    int coord = 0;
    for (int i = 1; i < values.length; ++i)
      if (Math.abs (values[i]) > Math.abs (values[coord]))
        coord = i;

    return snatchToAxis (coord);
  }

  /* "Snatch" to the (given) coordinate axis.  */
  public Vector snatchToAxis (int coord)
  {
    float[] vals = new float[values.length];
    for (int i = 0; i < values.length; ++i)
      if (i == coord)
        vals[i] = values[i] / Math.abs (values[i]);
      else
        vals[i] = 0.0f;

    return new Vector (vals);
  }

  /**
   * Convert to string for debugging print-outs.
   * @return String representation of the vector.
   */
  @Override
  public String toString ()
  {
    StringBuffer res = new StringBuffer ();
    res.append ("(");
    for (int i = 0; i < values.length; ++i)
      {
        if (i > 0)
          res.append (", ");
        res.append (String.format ("%.3f", values[i]));
      }
    res.append (")");

    return res.toString ();
  }

  /* Calculate a + f b.  */
  public static Vector linearCombination (Vector a, float f, Vector b)
  {
    float[] v = new float[3];
    assert (a.dim () == v.length && b.dim () == v.length);
    for (int i = 0; i < v.length; ++i)
      v[i] = a.values[i] + f * b.values[i];

    return new Vector (v);
  }

  /* Calculate the angle between two vectors, in radiants.  This is the main
     routine used.  To get the sign right, we use the cross product between
     both vectors and then an empirical rule to find the sign from it.  */
  public static float angle (Vector a, Vector b)
  {
    final float normalization = a.norm () * b.norm ();
    final float cosine = innerProduct (a, b) / normalization;
    final Vector cross = crossProduct (a, b);
    final float sine = cross.norm () / normalization;

    float res = (float) Math.atan2 (sine, cosine);
    return res * angleSign (cross, null);
  }

  /* Find sign of angle by the "rule-of-thumb" from the cross-products.
     This routine is useful both for the method 'angle' above and for
     calculating the angle of a rotation matrix elsewhere.  */
  public static float angleSign (Vector p1, Vector p2)
  {
    /* Rule of thumb for sign of sine value: Sum up all components of the
       cross-product vector(s) and take the sign of this sum.  That way,
       if the vectors are flipped, the cross-products change sign and also
       the angle will be reported oppositely.  */
    float sum = 0.0f;
    for (int i = 0; i < p1.values.length; ++i)
      sum += p1.values[i];
    if (p2 != null)
      for (int i = 0; i < p2.values.length; ++i)
        sum += p2.values[i];

    return (sum > 0.0f ? -1.0f : 1.0f);
  }

}
