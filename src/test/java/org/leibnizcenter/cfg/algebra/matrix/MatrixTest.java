package org.leibnizcenter.cfg.algebra.matrix;

import org.junit.Assert;
import org.junit.Test;

public class MatrixTest {
    @Test
    public void inverse() throws Exception {
        // Determine inverse manually
        final double scalar = 1 / ((2.1 * 3.2) - (2.2 * 3.1));
        final double[][] m = (new double[][]{
                new double[]{scalar * 3.2, scalar * -2.2},
                new double[]{scalar * -3.1, scalar * 2.1}
        });

        // Calculate inverse through Matrix class
        final Matrix inverse = new Matrix(new double[][]{
                new double[]{2.1, 2.2},
                new double[]{3.1, 3.2}
        }).inverse();

        inverse.forEach((row, column, value) -> Assert.assertEquals(value, m[row][column], 0.000001));
    }

}