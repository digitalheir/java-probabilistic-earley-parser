package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.Atom;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.AtomMap;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ForwardScores {
    private final DblSemiring semiring;
    private final StateToDoubleMap forwardScores;
    private final Map<State,Atom> forwardScoresAtom=new HashMap<>(500);
    private final AtomMap atoms;

    ForwardScores(DblSemiring semiring,AtomMap atoms) {
        this.semiring=semiring;
        this.atoms=atoms;
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
        forwardScoresAtom.put(state, atoms.getAtom(score));
    }

    /**
     * Runs in O(1).
     */
    public void add(State state, double increment) {
        final double newForwardScore = semiring.plus(forwardScores.get(state)/*default zero*/, increment);
        put(state, newForwardScore);
    }


    /**
     * Default zero
     *
     * @param state State for which to get inner score
     * @return inner score so far
     */
    public Atom getAtom(State state) {
        return forwardScoresAtom.get(state);
    }
}
