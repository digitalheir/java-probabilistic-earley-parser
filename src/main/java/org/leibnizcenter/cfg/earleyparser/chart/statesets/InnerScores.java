package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.Atom;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.grammar.AtomMap;

import java.util.HashMap;
import java.util.Map;

public class InnerScores {
    public final DblSemiring semiring;
    private final StateToDoubleMap innerScores;
    private final Map<State,Atom> innerScoresAtom=new HashMap<>(500);
    private final AtomMap atoms;

    InnerScores(DblSemiring semiring,AtomMap atoms) {
        this.semiring=semiring;
        this.atoms=atoms;
        this.innerScores = new StateToDoubleMap(500, 0.5F, semiring.zero());
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
