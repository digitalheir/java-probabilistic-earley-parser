package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Scan phase of Earley
 * <p>
 * Created by maarten on 31/10/16.
 */
public final class Scan {

    public static <T> double getScanProb(final ScanProbability<T> scanProbability, final TokenWithCategories<T> token, final int position) {
        return scanProbability == null ? Double.NaN : scanProbability.getProbability(position, token);
    }

    /**
     * Function to compute the forward score for the new state after scanning the given token.
     *
     * @param scanProbability           The probability of scanning this particular token
     * @param sr                        The semiring to calculate with
     * @param previousStateForwardScore The previous forward score
     * @return Computed forward score for the new state
     */
    public static double calculateForwardScore(final double scanProbability, final DblSemiring sr, final double previousStateForwardScore) {
        return Double.isNaN(scanProbability) ? previousStateForwardScore : sr.times(previousStateForwardScore, scanProbability);
    }

    /**
     * Function to calculate the new inner score from given values
     *
     * @param scanProbability The probability of scanning this particular token
     * @param sr              The semiring to calculate with
     * @param previousInner   The previous inner score
     * @return The inner score for the new state
     */

    public static double calculateInnerScore(final double scanProbability, final DblSemiring sr, final double previousInner) {
        return Double.isNaN(scanProbability) ? previousInner : sr.times(previousInner, scanProbability);
    }

    @Deprecated
    public static class Delta<T> {
        public final State preScanState;
        public final double postScanForward;
        public final double postScanInner;
        public final State nextState;
        public final Token<T> token;

        public Delta(
                final Token<T> token, final State preScanState,
                final double postScanForward,
                final double postScanInner,
                final Rule nextRule,
                final int nextPosition,
                final int nextRuleStart,
                final int nextDot
        ) {
            this.preScanState = preScanState;
            this.postScanForward = postScanForward;
            this.postScanInner = postScanInner;
            this.token = token;
            this.nextState = State.create(nextPosition, nextRuleStart, nextDot, nextRule);
        }
    }
}
