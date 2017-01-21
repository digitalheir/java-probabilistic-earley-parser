package org.leibnizcenter.cfg.earleyparser.chart.statesets;

import org.leibnizcenter.cfg.earleyparser.chart.state.State;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class ViterbiScores {
    private final Map<State, State.ViterbiScore> viterbiScores = new HashMap<>(500);

    /**
     * @param v viterbi score
     */
    public void put(State.ViterbiScore v) {
        put(v.getResultingState(), v);
    }


    /**
     * @param resultingState The resulting state from the transition
     * @param v              viterbi score
     */
    public void put(State resultingState, State.ViterbiScore v) {
        viterbiScores.put(resultingState, v);
    }

    public State.ViterbiScore get(State s) {
        return viterbiScores.get(s);
    }


}
