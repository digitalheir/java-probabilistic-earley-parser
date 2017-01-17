package org.leibnizcenter.cfg.earleyparser.chart;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

/**
 */
public class ForwardScores {
    private final DblSemiring semiring;
    private final TObjectDoubleHashMap<State> forwardScores;

    ForwardScores(DblSemiring semiring) {
        this.semiring = semiring;
        this.forwardScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
    }

        /**
         * Default zero
         *
         * @param s state
         * @return forward score so far
         */
    public double get(State s) {
        return forwardScores.get(s);
    }

    public void put(State state, double score) {
        forwardScores.put(state, score);
    }

    public void add(State state, double increment) {
        final double newForwardScore = semiring.plus(forwardScores.get(state)/*default zero*/, increment);
        put(state, newForwardScore);
    }
}
