package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.ForwardScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.InnerScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
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
@SuppressWarnings("WeakerAccess")
public final class Scan {
    /**
     * Don't instantiate
     */
    private Scan() {
        throw new Error();
    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param tokenPosition       The start index of the scan.
     * @param tokenWithCategories The token that was scanned.
     * @param scanProbability     Function that provides the probability of scanning the given token at this position. Might be null for a probability of 1.0.
     */
    public static <T> void scan(
            final int tokenPosition,
            TokenWithCategories<T> tokenWithCategories,
            ScanProbability<T> scanProbability,
            DblSemiring sr,
            StateSets stateSets
    ) {
        if (tokenWithCategories == null)
            throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");

        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition, tokenWithCategories);
        final Token<T> token = tokenWithCategories.getToken();
        final ForwardScores forwardScores = stateSets.forwardScores;
        final InnerScores innerScores = stateSets.innerScores;

//        StateToXMap<State> checkNoNewStatesAreDoubles = new StateToXMap<>(10 + 100);

        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        tokenWithCategories.getCategories().stream()
                .flatMap(terminalType -> {
                    final Set<State> statesActiveOnTerminals = stateSets.activeStates.getActiveOn(tokenPosition, terminalType);
                    return statesActiveOnTerminals == null
                            ? Stream.empty()
                            : statesActiveOnTerminals.stream();
                })
                .parallel() // Parallellize for performance: everything we do in map does not mutate state
                .map(preScanState -> {
                    if (!((Terminal<T>) preScanState.getActiveCategory())
                            .hasCategory(token)) throw new IssueRequest("This is a bug.");
                    // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                    final double preScanForward = forwardScores.get(preScanState);
                    final double preScanInner = innerScores.get(preScanState);
                    // Note that this state is unique for each preScanState
                    final int nextPosition = tokenPosition + 1;
                    final int nextRuleStart = preScanState.ruleStartPosition;
                    final int nextDot = preScanState.advanceDot();
                    final Rule nextRule = preScanState.rule;

                    // Get forward/inner score
                    final double postScanForward = calculateForwardScore(scanProb, sr, preScanForward);
                    final double postScanInner = calculateInnerScore(scanProb, sr, preScanInner);


                    return new Delta(
                            preScanState,
                            postScanForward,
                            postScanInner,
                            nextRule, nextPosition, nextRuleStart, nextDot
                    );
                })

                // After we have calculated everything, we may mutate the chart

                .parallel()//.sequential() // TODO does this need to be sequential actually?

                .forEach(score -> {
                    final int position = score.nextPosition;
                    final int ruleStart = score.nextRuleStart;
                    final int dot = score.nextDot;
                    final Rule rule = score.nextRule;

                    final State postScanState = stateSets.getOrCreate(position, ruleStart, dot, rule, token);

//                    if (checkNoNewStatesAreDoubles.contains(rule, position, ruleStart, dot))
//                        throw new IssueRequest("Tried to scan same state twice. This is a bug.");
//                    else checkNoNewStatesAreDoubles.put(postScanState, postScanState);

                    // Set forward score
                    forwardScores.put(
                            postScanState,
                            score.postScanForward
                    );
                    // Set inner score
                    innerScores.put(
                            postScanState,
                            score.postScanInner
                    );
                    // Set Viterbi score
                    stateSets.viterbiScores.put(
                            new State.ViterbiScore(score.postScanInner, score.preScanState, postScanState, sr)
                    );
                });
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
        if (Double.isNaN(scanProbability)) return previousInner;
        else return sr.times(previousInner, scanProbability);
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
        if (Double.isNaN(scanProbability)) return previousStateForwardScore;
        else return sr.times(previousStateForwardScore, scanProbability);
    }

    private static class Delta {

        private final State preScanState;
        private final double postScanForward;
        private final double postScanInner;

        private final Rule nextRule;
        private final int nextPosition;
        private final int nextRuleStart;
        private final int nextDot;

        public Delta(
                State preScanState,
                double postScanForward,
                double postScanInner,
                Rule nextRule,
                int nextPosition,
                int nextRuleStart,
                int nextDot) {
            this.preScanState = preScanState;
            this.postScanForward = postScanForward;
            this.postScanInner = postScanInner;

            this.nextRule = nextRule;
            this.nextPosition = nextPosition;
            this.nextRuleStart = nextRuleStart;
            this.nextDot = nextDot;
        }
    }
}
