package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

public class InnerScores {
    public final DblSemiring semiring;
    private final TObjectDoubleHashMap<State> innerScores;

    InnerScores(DblSemiring semiring) {
        this.semiring=semiring;
        this.innerScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
    }

    public void put(State s, double probability) {
        innerScores.put(s, probability);
    }

    /**
     * Default zero
     *
     * @param state State for which to get inner score
     * @return inner score so far
     */
    public double get(State state) {
        return innerScores.get(state);
    }
}
