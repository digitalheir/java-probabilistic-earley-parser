package org.leibnizcenter.cfg.earleyparser.parse;

/**
 * Allows us to intervene on scan probabilities.
 * <p>
 * Created by Maarten on 31-7-2016.
 */
public interface ScanProbability {
    double getProbability(int index);
}
