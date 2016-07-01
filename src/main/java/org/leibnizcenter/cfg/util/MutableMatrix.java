package org.leibnizcenter.cfg.util;

import java.util.Collection;
import java.util.Vector;

/**
 * Mutable matrix
 * Created by maarten on 21-4-16.
 */
public class MutableMatrix<T> {
    private final Vector<Vector<T>> list;
    private final int rowCount;
    private final int columnCount;

    /**
     * initializes n by m matrix, with null at every cell.
     *
     * @param width  Number of cells per row
     * @param height Number of rows in the matrix
     */
    public MutableMatrix(int width, int height) {
        list = new Vector<>(height);
        list.setSize(height);
        for (int i = 0; i < height; i++) {
            Vector<T> row = new Vector<>(width);
            row.setSize(width);
            list.set(i, row);
        }

        rowCount = height;
        columnCount = width;
    }

    /**
     * Creates a mutable copy of given matrix
     *
     * @param m immutable matrix
     */
    public MutableMatrix(ImmutableMatrix<T> m) {
        int rowCount = m.getRowCount();
        int columnCount = m.getColumnCount();
        Vector<Vector<T>> rows = new Vector<>(columnCount);
        rows.setSize(rowCount);
        for (int i = 0; i < rowCount; i++) {
            Vector<T> row = new Vector<>(columnCount);
            row.setSize(columnCount);
            for (int j = 0; j < columnCount; j++) row.set(j, m.get(i, j));
            rows.set(i, row);
        }
        this.columnCount = m.getColumnCount();
        this.rowCount = m.getRowCount();
        this.list = rows;
    }

    public T get(int row, int column) {
        return list.get(row).get(column);
    }

    public T set(int row, int column, T item) {
        return list.get(row).set(column, item);
    }

    /**
     * @return number of non-null items
     */
    public int size() {
        return (int) list.stream().flatMap(Collection::stream).filter(r -> r != null).count();
    }

    public Vector<Vector<T>> getRows() {
        return list;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }
}
