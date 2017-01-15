package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;

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
