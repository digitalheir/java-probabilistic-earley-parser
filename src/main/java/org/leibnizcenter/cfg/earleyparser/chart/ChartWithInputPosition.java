package org.leibnizcenter.cfg.earleyparser.chart;

/**
 */
public class ChartWithInputPosition<T> {
    public final Chart<T> chart;
    public final int index;

    public ChartWithInputPosition(Chart<T> chart, int index) {
        this.chart = chart;
        this.index = index;
    }
}
