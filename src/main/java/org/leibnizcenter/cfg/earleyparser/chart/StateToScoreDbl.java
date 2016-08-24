package org.leibnizcenter.cfg.earleyparser.chart;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Maarten on 23-8-2016.
 */
public class StateToScoreDbl {
    private final Map<
            Rule,
                         /*index*/
            TIntObjectMap<
                                 /*rule start*/
                    TIntObjectMap<
                                         /*dot position to score*/
                            TIntDoubleMap
                            >
                    >
            > states;
    private DblSemiring semiring;

    public StateToScoreDbl(int capacity, DblSemiring semiring) {
        states = new HashMap<>(capacity);
        this.semiring = semiring;
    }

    public double getScore(Rule rule, int index, int ruleStart, int dot) {
        return getDotToScore(rule, index, ruleStart).get(dot);
    }

    public TIntDoubleMap getDotToScore(Rule rule, int index, int ruleStart) {
        if (!states.containsKey(rule)) states.put(rule, new TIntObjectHashMap<>(30));
        TIntObjectMap<TIntObjectMap<TIntDoubleMap>> iToRest = states.get(rule);

        if (!iToRest.containsKey(index))
            iToRest.put(index, new TIntObjectHashMap<>(50));
        TIntObjectMap<TIntDoubleMap> ruleStartToDotToState = iToRest.get(index);

        if (!ruleStartToDotToState.containsKey(ruleStart))
            ruleStartToDotToState.put(ruleStart, new TIntDoubleHashMap(10, 0.5F, -1, semiring.zero()));
        return ruleStartToDotToState.get(ruleStart);
    }

    public void add(Rule rule, int index, int ruleStart, int dotPosition, double addValue) {
        final double result = semiring.plus(
                getScore(rule, index, ruleStart, dotPosition),
                addValue
        );
        System.out.println(index + ": (" + ruleStart + ")" + rule.toString(dotPosition) + " += " + semiring.toProbability(addValue) + " = " + semiring.toProbability(result));
        setScore(rule, index, ruleStart, dotPosition, result);
    }

    private void setScore(Rule rule, int index, int ruleStart, int dotPosition, double plus) {
        getDotToScore(rule, index, ruleStart).put(dotPosition, plus);
    }

    public Map<Rule, TIntObjectMap<TIntObjectMap<TIntDoubleMap>>> getStates() {
        return states;
    }

    public double getScore(State state) {
        return getScore(state.getRule(), state.getPosition(), state.getRuleStartPosition(), state.getRuleDotPosition());
    }
}
