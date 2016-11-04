package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.token.Token;

/**
 * Created by maarten on 31/10/16.
 */
public final class Scan {
    /**
     * Don't instantiate
     */
    private Scan() {
        throw new Error();
    }

    static <E> void scan(int tokenPosition, Token<E> token, ScanProbability scanProbability, Grammar grammar, StateSets stateSets) {
        if (token == null) throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");

        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition);
        final DblSemiring sr = grammar.getSemiring();
        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        // noinspection unchecked
        stateSets.getStatesActiveOnTerminals(tokenPosition).stream()//.parallel()
                // TODO can this be more efficient, ie have tokens make their category be explicit? (Do we want to maintain the possibility of such "fluid" categories?)
                //.sequential()
                .filter(state -> ((Terminal) state.getActiveCategory()).hasCategory(token))
                // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                .forEach(preScanState -> {
                    /*
                     * All these methods are synchronized
                     */
                            final double preScanForward = stateSets.getForwardScore(preScanState);
                            final double preScanInner = stateSets.getInnerScore(preScanState);
                            // Note that this state is unique for each preScanState
                            final State postScanState = stateSets.getOrCreate(
                                    tokenPosition + 1, preScanState.getRuleStartPosition(),
                                    preScanState.advanceDot(),
                                    preScanState.getRule(),
                                    token
                            );

                            // Set forward score //synchronized
                            stateSets.setForwardScore(
                                    postScanState,
                                    calculateForwardScore(scanProb, sr, preScanForward)
                            );

                            // Get inner score (no side effects)
                            final double postScanInner = calculateInnerScore(scanProb, sr, preScanInner);
                            // Set inner score //synchronized
                            stateSets.setInnerScore(
                                    postScanState,
                                    postScanInner
                            );

                            // Set Viterbi score//synchronized
                            stateSets.setViterbiScore(new State.ViterbiScore(postScanInner, preScanState, postScanState, sr));
                        }
                );
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
    static double calculateForwardScore(double scanProbability, DblSemiring sr, double previousStateForwardScore) {
        if (Double.isNaN(scanProbability)) return previousStateForwardScore;
        else return sr.times(previousStateForwardScore, scanProbability);
    }
}
