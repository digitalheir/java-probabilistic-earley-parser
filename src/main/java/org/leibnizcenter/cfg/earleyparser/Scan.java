package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseCallbacks;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.ForwardScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.InnerScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Scan phase of Earley
 * <p>
 * Created by maarten on 31/10/16.
 */
public final class Scan<T> {

    private final StateSets<T> stateSets;
    private final DblSemiring sr;

    Scan(StateSets<T> stateSets) {
        this.stateSets = stateSets;
        this.sr = stateSets.grammar.semiring;
    }

    /**
     * Function to calculate the new inner score from given values
     *
     * @param scanProbability The probability of scanning this particular token
     * @param sr              The semiring to calculate with
     * @param previousInner   The previous inner score
     * @return The inner score for the new state
     */

    private static double calculateInnerScore(double scanProbability, DblSemiring sr, double previousInner) {
        return Double.isNaN(scanProbability) ? previousInner : sr.times(previousInner, scanProbability);
    }

    /**
     * Function to compute the forward score for the new state after scanning the given token.
     *
     * @param scanProbability           The probability of scanning this particular token
     * @param sr                        The semiring to calculate with
     * @param previousStateForwardScore The previous forward score
     * @return Computed forward score for the new state
     */
    private static double calculateForwardScore(double scanProbability, DblSemiring sr, double previousStateForwardScore) {
        return Double.isNaN(scanProbability) ? previousStateForwardScore : sr.times(previousStateForwardScore, scanProbability);
    }

    void scan(ParseCallbacks<T> callbacks, Chart<T> chart, int i, TokenWithCategories<T> token) {
        final ScanProbability<T> scanProbability = callbacks != null ? callbacks.scanProbability : null;
        if (callbacks != null) callbacks.beforeScan(i, token, chart);

        scan(
                i,
                token,
                scanProbability
        );

        if (callbacks != null) callbacks.onScan(i, token, chart);
    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param tokenPosition       The start index of the scan.
     * @param tokenWithCategories The token that was scanned.
     * @param scanProbability     Function that provides the probability of scanning the given token at this position. Might be null for a probability of 1.0.
     */
    @SuppressWarnings("WeakerAccess")
    public void scan(
            final int tokenPosition,
            final TokenWithCategories<T> tokenWithCategories,
            final ScanProbability<T> scanProbability
    ) {
        if (tokenWithCategories == null)
            throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");

        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition, tokenWithCategories);
        final Token<T> token = tokenWithCategories.getToken();
        final ForwardScores forwardScores = stateSets.forwardScores;
        final InnerScores innerScores = stateSets.innerScores;
        final int nextPosition = tokenPosition + 1;

//        StateToXMap<State> checkNoNewStatesAreDoubles = new StateToXMap<>(10 + 100);

        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        tokenWithCategories.getCategories().stream()
                .parallel()
                .flatMap((final Terminal<T> terminalType) -> {
                    final Set<State> statesActiveOnTerminals = stateSets.activeStates.getActiveOn(tokenPosition, terminalType);
                    return statesActiveOnTerminals == null
                            ? Stream.empty()
                            : statesActiveOnTerminals.stream();
                })
                .parallel() // Parallellize for performance: everything we do in map does not mutate state
                .map(preScanState -> new Delta<>(
                        token,
                        preScanState,
                        calculateForwardScore(scanProb, sr, forwardScores.get(preScanState)),
                        calculateInnerScore(scanProb, sr, innerScores.get(preScanState)),
                        /* Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>. Note that this state is unique for each preScanState */
                        preScanState.rule, nextPosition, preScanState.ruleStartPosition, preScanState.advanceDot()
                ))

                // After we have calculated everything, we mutate the chart
                .sequential()
                .forEach(stateSets::createStateAndSetScores);
    }

    public static class Delta<T> {
        public final State preScanState;
        public final double postScanForward;
        public final double postScanInner;

        public final State nextState;
        public final Token<T> token;

        Delta(
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
            this.nextState = State.create(nextPosition, nextRuleStart, nextDot, nextRule);
        }
    }
}
