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

/* A very simple matrix class for my rotation matrix investigations.  */
public class Matrix
{

  protected float[][] values;

  public Matrix (float[][] v)
  {
    assert (v.length == 3);
    values = new float[3][3];
    for (int i = 0; i < 3; ++i)
      {
        assert (v[i].length == 3);
        for (int j = 0; j < 3; ++j)
          values[i][j] = v[i][j];
      }
  }

  /* Build the matrix from vectors making up its columns.  */
  public Matrix (Vector[] cols)
  {
    assert (cols.length == 3);
    values = new float[3][3];
    for (int i = 0; i < cols.length; ++i)
      {
        assert (cols[i].dim () == 3);
        for (int j = 0; j < cols[i].dim (); ++j)
          values[j][i] = cols[i].get (j);
      }
  }

  public final int dim (int ind)
  {
    if (ind == 0)
      return values.length;

    assert (ind == 1);
    return values[0].length;
  }

  public final float get (int r, int c)
  {
    return values[r][c];
  }

  public static Matrix product (Matrix a, Matrix b)
  {
    final int m = a.dim (0);
    final int n = a.dim (1);
    assert (n == b.dim (0));
    final int o = b.dim (1);

    float[][] res = new float[m][o];
    for (int i = 0; i < m; ++i)
      for (int j = 0; j < o; ++j)
        {
          res[i][j] = 0.0f;
          for (int k = 0; k < n; ++k)
            res[i][j] += a.values[i][k] * b.values[k][j];
        }

    return new Matrix (res);
  }

  /* Apply the matrix to a vector (i.e., matrix-vector-product).  */
  public final Vector apply (Vector v)
  {
    assert (v.dim () == dim (1));
    float[] res = new float[dim (0)];

    for (int i = 0; i < res.length; ++i)
      {
        res[i] = 0.0f;
        for (int j = 0; j < v.dim (); ++j)
          res[i] += values[i][j] * v.get (j);
      }

    return new Vector (res);
  }

  /* Transpose the matrix.  */
  public Matrix transpose ()
  {
    float[][] res = new float[dim (1)][dim (0)];
    for (int i = 0; i < values.length; ++i)
      for (int j = 0; j < values[i].length; ++j)
        res[j][i] = values[i][j];

    return new Matrix (res);
  }

}
