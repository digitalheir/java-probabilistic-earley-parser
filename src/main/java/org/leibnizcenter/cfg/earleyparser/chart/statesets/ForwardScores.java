package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 */
public class ForwardScores {
    private final DblSemiring semiring;
    private final StateToDoubleMap forwardScores;

    ForwardScores(DblSemiring semiring) {
        this.semiring = semiring;
        this.forwardScores = new StateToDoubleMap(500, 0.5F, semiring.zero());
    }

        /**
         * Default zero. Runs in O(1).
         *
         * @param s state
         * @return forward score so far
         */
    public double get(State s) {
        return forwardScores.get(s);
    }

    /**
     * Runs in O(1).
     */
    public void put(State state, double score) {
        forwardScores.put(state, score);
    }

    /**
     * Runs in O(1).
     */
    public void add(State state, double increment) {
        final double newForwardScore = semiring.plus(forwardScores.get(state)/*default zero*/, increment);
        put(state, newForwardScore);
    }
}
