package org.leibnizcenter.cfg.earleyparser.parse.callbacks;

import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Optional code runned after scanning phase
 * <p>
 * Created by maarten on 16/01/17.
 */
@FunctionalInterface
public interface OnPostScan {
    <T> void onScan(int position, TokenWithCategories<T> token, Chart chart);
}
