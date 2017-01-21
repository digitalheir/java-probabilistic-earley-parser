package org.leibnizcenter.cfg.earleyparser.callbacks;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Optional code runned before/after parse phase
 *
 * Created by maarten on 16/01/17.
 */
@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface ParseCallback<T> {
    void on(int position, TokenWithCategories<T> token, Chart chart);
}
