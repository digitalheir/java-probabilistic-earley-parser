package org.leibnizcenter.cfg.algebra.matrix;

/**
 * QR Decomposition.
 * <p>
 * For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
 * orthogonal matrix Q and an n-by-n upper triangular matrix R so that
 * A = Q*R.
 * <p>
 * The QR decompostion always exists, even if the matrix does not have
 * full rank, so the constructor will never fail.  The primary use of the
 * QR decomposition is in the least squares solution of non-square systems
 * of simultaneous linear equations.  This will fail if {@link #isFullRank}
 * returns false.
 */

public class QRDecomposition implements Decomposition {

/* ------------------------
   Class variables
 * ------------------------ */

    /**
     * Array for internal storage of decomposition.
     *
     * @serial internal array storage.
     */
    private final double[][] QR;

    /**
     * Row and column dimensions.
     */
    private final int m;
    private final int n;

    /**
     * Array for internal storage of diagonal of R.
     *
     * @serial diagonal of R.
     */
    private final double[] Rdiag;

    /**
     * QR Decomposition, computed by Householder reflections.
     * Structure to access R and the Householder vectors and compute Q.
     *
     * @param A Rectangular matrix
     */

    public QRDecomposition(final Matrix A) {
        // Initialize.
        QR = A.getArrayCopy();
        m = A.getRowDimension();
        n = A.getColumnDimension();
        Rdiag = new double[n];

        // Main loop.
        for (int k = 0; k < n; k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < m; i++) {
                nrm = hypot(nrm, QR[i][k]);
            }

            if (nrm != 0.0) {
                // Form k-th Householder vector.
                if (QR[k][k] < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < m; i++) {
                    QR[i][k] /= nrm;
                }
                QR[k][k] += 1.0;

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < n; j++) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k] * QR[i][j];
                    }
                    s = -s / QR[k][k];
                    for (int i = k; i < m; i++) {
                        QR[i][j] += s * QR[i][k];
                    }
                }
            }
            Rdiag[k] = -nrm;
        }
    }

    /**
     * sqrt(a^2 + b^2) without under/overflow.
     **/
    private static double hypot(final double a, final double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b / a;
            r = Math.abs(a) * Math.sqrt(1 + r * r);
        } else if (b != 0) {
            r = a / b;
            r = Math.abs(b) * Math.sqrt(1 + r * r);
        } else {
            r = 0.0;
        }
        return r;
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */

    public boolean isFullRank() {
        for (int j = 0; j < n; j++) if (Rdiag[j] == 0) return false;
        return true;
    }

    /**
     * Return the upper triangular factor
     *
     * @return R
     */

    public Matrix getR() {
        final Matrix X = new Matrix(n, n);
        final double[][] R = X.getArray();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < j) {
                    R[i][j] = QR[i][j];
                } else if (i == j) {
                    R[i][j] = Rdiag[i];
                } else {
                    R[i][j] = 0.0;
                }
            }
        }
        return X;
    }

    /**
     * Least squares solution of A*X = B
     *
     * @param B A Matrix with as many rows as A and any number of columns.
     * @return X that minimizes the two norm of Q*R*X-B.
     * @throws IllegalArgumentException Matrix row dimensions must agree.
     * @throws RuntimeException         Matrix is rank deficient.
     */

    public Matrix solve(final Matrix B) {
        if (B.getRowDimension() != m) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("Matrix is rank deficient.");
        }

        // Copy right hand side
        final int nx = B.getColumnDimension();
        final double[][] X = B.getArrayCopy();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {
            for (int j = 0; j < nx; j++) {
                double s = 0.0;
                for (int i = k; i < m; i++) {
                    s += QR[i][k] * X[i][j];
                }
                s = -s / QR[k][k];
                for (int i = k; i < m; i++) {
                    X[i][j] += s * QR[i][k];
                }
            }
        }
        // Solve R*X = Y;
        for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X[k][j] /= Rdiag[k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j] * QR[i][k];
                }
            }
        }
        return (new Matrix(X, n, nx).getMatrix(n - 1, nx - 1));
    }
}
