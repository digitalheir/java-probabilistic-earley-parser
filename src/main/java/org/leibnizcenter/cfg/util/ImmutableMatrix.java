package org.leibnizcenter.cfg.util;

import java.util.Collection;
import java.util.Vector;

/**
 * Immutable matrix
 * Created by maarten on 21-4-16.
 */
public class ImmutableMatrix<T> {
    private final Vector<Vector<T>> list;
    private final int rowCount;
    private final int columnCount;

    /**
     * initializes n by m matrix, with null at every cell.
     *
     * @param width  Number of cells per row
     * @param height Number of rows in the matrix
     */
    @SuppressWarnings("unused")
    public ImmutableMatrix(int width, int height) {
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

    public ImmutableMatrix(MutableMatrix<T> m) {
        Vector<Vector<T>> list = new Vector<>(m.getRowCount());
        list.setSize(m.getRowCount());
        for (int i = 0; i < m.getRowCount(); i++) {
            Vector<T> row = new Vector<>(m.getColumnCount());
            row.setSize(m.getColumnCount());
            for (int j = 0; j < m.getColumnCount(); j++) {
                row.set(j, m.get(i, j));
            }
            list.set(i, row);
        }
        columnCount = m.getColumnCount();
        rowCount = m.getRowCount();
        this.list = list;
    }

    public static <T> Builder<T> builder(ImmutableMatrix<T> m) {
        return new Builder<>(m);
    }

    public T get(int row, int column) {
        return list.get(row).get(column);
    }

    public ImmutableMatrix<T> set(int row, int column, T item) {
        return builder(this).set(row, column, item).build();
    }

    /**
     * @return number of non-null items
     */
    public int size() {
        return (int) list.stream().flatMap(Collection::stream).filter(r -> r != null).count();
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public static class Builder<T> {
        MutableMatrix<T> m;

        @SuppressWarnings("unused")
        public Builder(int width, int height) {
            m = new MutableMatrix<>(width, height);
        }

        public Builder(ImmutableMatrix<T> m) {
            this.m = new MutableMatrix<>(m);
        }

        public Builder(MutableMatrix<T> scoreMap) {
            this.m = scoreMap;
        }

        public Builder<T> set(int w, int h, T element) {
            this.m.set(w, h, element);
            return this;
        }

        public ImmutableMatrix<T> build() {
            return new ImmutableMatrix<>(m);
        }

        public T get(int i, int j) {
            return m.get(i, j);
        }
    }

}
