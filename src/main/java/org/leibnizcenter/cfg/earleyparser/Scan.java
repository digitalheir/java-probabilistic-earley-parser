package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.ForwardScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.InnerScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;

/**
 * Scan phase of Earley
 * <p>
 * Created by maarten on 31/10/16.
 */
public final class Scan {
    public static <T> Delta<T> getScanDelta(
            ExpressionSemiring semiring,
            ScanProbability<T> scanProbability,
            TokenWithCategories<T> token,
            StateSets<T> stateSets,
            int position,
            State preScanState
    ) {
        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(position, token);

        final double previousForward = stateSets.forwardScores.get(preScanState);
        final double previousInner = stateSets.innerScores.get(preScanState);

        final double postScanForward = calculateForwardScore(scanProb, semiring, previousForward);
        final double postScanInner = calculateInnerScore(scanProb, semiring, previousInner);

        return new Delta<>(
                token.token,
                preScanState,
                postScanForward,
                postScanInner,
                /* Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>. Note that this state is unique for each preScanState */
                preScanState.rule, position + 1, preScanState.ruleStartPosition, preScanState.advanceDot()
        );
    }

    /**
     * Function to compute the forward score for the new state after scanning the given token.
     *
     * @param scanProbability           The probability of scanning this particular token
     * @param sr                        The semiring to calculate with
     * @param previousStateForwardScore The previous forward score
     * @return Computed forward score for the new state
     */
    static double calculateForwardScore(double scanProbability, DblSemiring sr, double previousStateForwardScore) {
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

    static double calculateInnerScore(double scanProbability, DblSemiring sr, double previousInner) {
        return Double.isNaN(scanProbability) ? previousInner : sr.times(previousInner, scanProbability);
    }

    public static class Delta<T> {
        public final State preScanState;
        public final double postScanForward;
        public final double postScanInner;

        public final State nextState;
        public final Token<T> token;

        public Delta(
                Token<T> token, State preScanState,
                double postScanForward,
                double postScanInner,

                Rule nextRule,
                int nextPosition,
                int nextRuleStart,
                int nextDot
        ) {
            this.preScanState = preScanState;
            this.postScanForward = postScanForward;
            this.postScanInner = postScanInner;
            this.token = token;
            this.nextState = State.create(nextPosition, nextRuleStart, nextDot, nextRule); // todo instantiation is not necessary
        }
    }
}
