package org.leibnizcenter.cfg.algebra.matrix;

/**
 * Jama = Java Matrix class.
 * <p>
 * The Java Matrix Class provides the fundamental operations of numerical
 * linear algebra.  Various constructors create Matrices from two dimensional
 * arrays of double precision floating point numbers.  Various "gets" and
 * "sets" provide access to sub-matrices and matrix elements.  Several methods
 * implement basic matrix arithmetic, including matrix addition and
 * multiplication, matrix norms, and element-by-element array operations.
 * Methods for reading and printing matrices are also included.  All the
 * operations in this version of the Matrix Class involve real matrices.
 * Complex matrices may be handled in a future version.
 * <p>
 * Five fundamental matrix decompositions, which consist of pairs or triples
 * of matrices, permutation vectors, and the like, produce results in five
 * decomposition classes.  These decompositions are accessed by the Matrix
 * class to compute solutions of simultaneous linear equations, determinants,
 * inverses and other matrix functions.  The five decompositions are:
 * <P><UL>
 * <LI>Cholesky Decomposition of symmetric, positive definite matrices.
 * <LI>LU Decomposition of rectangular matrices.
 * <LI>QR Decomposition of rectangular matrices.
 * <LI>Singular Value Decomposition of rectangular matrices.
 * <LI>Eigenvalue Decomposition of both symmetric and nonsymmetric square matrices.
 * </UL>
 * <DL>
 * <DT><B>Example of use:</B></DT>
 * <p>
 * <DD>Solve a linear system A x = b and compute the residual norm, ||b - A x||.
 * <P><PRE>
 * double[][] vals = {{1.,2.,3},{4.,5.,6.},{7.,8.,10.}};
 * Matrix A = new Matrix(vals);
 * Matrix b = Matrix.random(3,1);
 * Matrix x = A.solve(b);
 * Matrix r = A.times(x).minus(b);
 * double rnorm = r.normInf();
 * </PRE></DD>
 * </DL>
 *
 * @author The MathWorks, Inc. and the National Institute of Standards and Technology.
 * @version 5 August 1998
 */

public class DblMatrix {
    /**
     * Array for internal storage of elements.
     *
     * @serial internal array storage.
     */
    private double[][] A;

    /**
     * Row and column dimensions.
     */
    private int m, n;

    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     */

    public DblMatrix(int m, int n) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
    }

    /**
     * Construct an m-by-n constant matrix.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @param s Fill the matrix with this scalar value.
     */

    @SuppressWarnings("unused")
    public DblMatrix(int m, int n, double s) {
        this.m = m;
        this.n = n;
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = s;
            }
        }
    }

    /**
     * Construct a matrix from a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @throws IllegalArgumentException All rows must have the same length
     */

    @SuppressWarnings("unused")
    public DblMatrix(double[][] A) {
        m = A.length;
        n = A[0].length;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        this.A = A;
    }

    /**
     * Construct a matrix quickly without checking arguments.
     *
     * @param A Two-dimensional array of doubles.
     * @param m Number of rows.
     * @param n Number of colums.
     */

    @SuppressWarnings("WeakerAccess")
    public DblMatrix(double[][] A, int m, int n) {
        this.A = A;
        this.m = m;
        this.n = n;
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     *
     * @param vals One-dimensional array of doubles, packed by columns (ala Fortran).
     * @param m    Number of rows.
     * @throws IllegalArgumentException Array length must be a multiple of m.
     */

    @SuppressWarnings("unused")
    public DblMatrix(double vals[], int m) {
        this.m = m;
        n = (m != 0 ? vals.length / m : 0);
        if (m * n != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        A = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = vals[i + j * m];
            }
        }
    }

    /**
     * Access the internal two-dimensional array.
     *
     * @return Pointer to the two-dimensional array of matrix elements.
     */

    double[][] getArray() {
        return A;
    }

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of matrix elements.
     */

    double[][] getArrayCopy() {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, C[i], 0, n);
        }
        return C;
    }

    /**
     * Get row dimension.
     *
     * @return m, the number of rows.
     */

    public int getRowDimension() {
        return m;
    }

    /**
     * Get column dimension.
     *
     * @return n, the number of columns.
     */

    public int getColumnDimension() {
        return n;
    }

    /**
     * Get a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @return A(i, j)
     */

    public double get(int i, int j) {
        return A[i][j];
    }

    /**
     * Get a submatrix.
     *
     * @param i1 Final row index
     * @param j1 Final column index
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */
    DblMatrix getMatrix(int i1, int j1) {
        DblMatrix X = new DblMatrix(i1 + 1, j1 + 1);
        double[][] B = X.getArray();
        try {
            for (int i = 0; i <= i1; i++) {
                System.arraycopy(A[i], 0, B[i], 0, j1 + 1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param r  Array of row indices.
     * @param j1 Final column index
     * @throws ArrayIndexOutOfBoundsException Submatrix indices
     */

    DblMatrix getMatrix(int[] r, int j1) {
        DblMatrix X = new DblMatrix(r.length, j1 + 1);
        double[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                System.arraycopy(A[r[i]], 0, B[i], 0, j1 + 1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Set a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @param s A(i,j).
     */

    public void set(int i, int j, double s) {
        A[i][j] = s;
    }

    /**
     * C = A + B
     *
     * @param B another matrix
     * @return A + B
     */

    public DblMatrix plus(DblMatrix B) {
        checkMatrixDimensions(B);
        DblMatrix X = new DblMatrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return X;
    }

    /**
     * Multiply a matrix by a scalar, C = s*A
     *
     * @param s scalar
     * @return s*A
     */

    public DblMatrix times(double s) {
        DblMatrix X = new DblMatrix(m, n);
        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = s * A[i][j];
            }
        }
        return X;
    }


    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param B another matrix
     * @return Matrix product, A * B
     * @throws IllegalArgumentException Matrix inner dimensions must agree.
     */

    public DblMatrix times(DblMatrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        DblMatrix X = new DblMatrix(m, B.n);
        double[][] C = X.getArray();
        double[] Bcolj = new double[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.A[k][j];
            }
            for (int i = 0; i < m; i++) {
                double[] Arowi = A[i];
                double s = 0;
                for (int k = 0; k < n; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                C[i][j] = s;
            }
        }
        return X;
    }

    /**
     * Solve A*X = B
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise
     */

    private DblMatrix solve(DblMatrix B) {
        return (m == n ? (new LUDecomposition(this)).solve(B) :
                (new QRDecomposition(this)).solve(B));
    }


    /**
     * Matrix inverse or pseudoinverse
     *
     * @return inverse(A) if A is square, pseudoinverse otherwise.
     */

    public DblMatrix inverse() {
        return solve(identity(m, m));
    }


    /**
     * Generate identity matrix
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */

    @SuppressWarnings("WeakerAccess")
    public static DblMatrix identity(int m, int n) {
        DblMatrix A = new DblMatrix(m, n);
        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                X[i][j] = (i == j ? 1.0 : 0.0);
            }
        }
        return A;
    }

    /**
     * Check if size(A) == size(B)
     **/

    private void checkMatrixDimensions(DblMatrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

}
