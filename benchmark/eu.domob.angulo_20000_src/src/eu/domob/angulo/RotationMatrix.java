/*
    Angulo.  Measure angles and slopes with Android!
    Copyright (C) 2011  Daniel Kraft <d@domob.eu>

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

/* Routines especially for rotation matrices.  */
public class RotationMatrix extends Matrix
{

  /* Just call through to parent constructor.  To find the matrix for two
     pairs of vectors, use the static routine below.  */
  public RotationMatrix (Vector[] cols)
  {
    super (cols);
  }

  /* Create a RotationMatrix from an ordinary matrix.  */
  public RotationMatrix (Matrix m)
  {
    super (m.values);
  }

  /* Get angle of rotation from the matrix.  */
  public float getAngle ()
  {
    final float trace = get (0, 0) + get (1, 1) + get (2, 2);
    final float cosine = trace - 1.0f;
    final Vector v = new Vector (get (2, 1) - get (1, 2),
                                 get (0, 2) - get (2, 0),
                                 get (1, 0) - get (0, 1));
    final float sine = v.norm ();

    return (float) Math.atan2 (sine, cosine);
  }

  /* From a pair of vectors, find an orthonormal basis and return the
     matrix corresponding to it.  */
  private static RotationMatrix buildONB (Vector a, Vector b)
  {
    Vector[] cols = new Vector[3];

    cols[0] = new Vector (a);
    cols[0].normalize ();

    final float inner = Vector.innerProduct (cols[0], b);
    cols[1] = Vector.linearCombination (b, -inner, cols[0]);
    cols[1].normalize ();

    cols[2] = Vector.crossProduct (cols[0], cols[1]);
    return new RotationMatrix (cols);
  }

  /* Find rotation matrix for two pairs of vectors.  */
  public static RotationMatrix findRotation (Vector a, Vector ap,
                                             Vector b, Vector bp)
  {
    final RotationMatrix B = buildONB (a, b);
    final RotationMatrix Bp = buildONB (ap, bp);

    return new RotationMatrix (Matrix.product (Bp, B.transpose ()));
  }

  /* Given two pairs of vectors, find the rotation angle including sign
     from our "rule-of-thumb".  */
  public static float getRotationAngle (Vector a, Vector ap,
                                        Vector b, Vector bp)
  {
    /* Perform angle estimation twice with a and b flipped positions.  Then
       take the mean value.  This ensures that the angle is symmetric and
       hopefully more stable, because the actually performed calculation is
       not completely symmetric.  */
    RotationMatrix R = findRotation (a, ap, b, bp);
    final float angle1 = R.getAngle ();
    R = findRotation (b, bp, a, ap);
    final float angle2 = R.getAngle ();

    final float angle = (angle1 + angle2) / 2.0f;

    final Vector crossA = Vector.crossProduct (a, ap);
    final Vector crossB = Vector.crossProduct (b, bp);

    return angle * Vector.angleSign (crossA, crossB);
  }

}
