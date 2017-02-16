package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.Atom;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.AtomFactory;

import java.util.HashMap;
import java.util.Map;

public class InnerScores {
    public final DblSemiring semiring;
    private final TObjectDoubleMap<State> innerScores;
    private final Map<State,Atom> innerScoresAtom=new HashMap<>(500);
    private final AtomFactory atoms;

    InnerScores(DblSemiring semiring, AtomFactory atoms) {
        this.semiring=semiring;
        this.atoms=atoms;
        this.innerScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
    }

    public void put(State s, double probability) {
        innerScores.put(s, probability);
        innerScoresAtom.put(s, atoms.getAtom(probability));
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

    /**
     * Default zero
     *
     * @param state State for which to get inner score
     * @return inner score so far
     */
    public Atom getAtom(State state) {
        return innerScoresAtom.get(state);
    }
}
