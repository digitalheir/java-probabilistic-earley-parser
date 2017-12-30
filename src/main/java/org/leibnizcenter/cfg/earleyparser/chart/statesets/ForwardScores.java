package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.Grammar;

public class ForwardScores {
    private final DblSemiring semiring;
    private final TObjectDoubleMap<State> forwardScores;
//    private final Map<State, Atom> forwardScoresAtom = new HashMap<>(500);
//    private final AtomFactory atoms;
//    private final Atom zeroA;

    ForwardScores(final Grammar grammar) {
        this.semiring = grammar.semiring;
//        this.atoms = grammar.atoms;
        final double zero = semiring.zero();
//        zeroA = atoms.getAtom(zero);
        this.forwardScores = new TObjectDoubleHashMap<>(500, 0.5F, zero);
    }


    /**
     * Default zero. Runs in O(1).
     *
     * @param s state
     * @return forward score so far
     */
    public double get(final State s) {
        return forwardScores.get(s);
    }

    /**
     * Runs in O(1).
     */
    public void put(final State state, final double score) {
        forwardScores.put(state, score);
//        forwardScoresAtom.put(state, atoms.getAtom(score));
    }

    /**
     * Runs in O(1).
     */
    public void increment(final State state, final double increment) {
        final double newForwardScore = semiring.plus(forwardScores.get(state)/*default zero*/, increment);
        put(state, newForwardScore);
    }


//    /**
//     * Default zero
//     *
//     * @param state State for which to get inner score
//     * @return inner score so far
//     */
//    public Atom getAtom(final State state) {
//        return forwardScoresAtom.getOrDefault(state, zeroA);
//    }
}
