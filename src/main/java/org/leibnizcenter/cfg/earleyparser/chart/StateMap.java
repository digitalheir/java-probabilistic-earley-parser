package org.leibnizcenter.cfg.earleyparser.chart;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * A map for states to expression values
 *
 * Created by Maarten on 30/10/2016.
 */
public class StateMap {
    public final Map<
            Rule,
                         /*index*/
            TIntObjectMap<
                                 /*rule start*/
                    TIntObjectMap<
                                         /*dot position*/
                            TIntObjectMap<
                                    /* score */
                                    ExpressionSemiring.Value
                                    >
                            >
                    >
            > states;
    private int size;

    public StateMap(int capacity) {
        states = new HashMap<>(capacity);
    }

    public boolean containsKey(Rule key) {
        return states.containsKey(key);
    }

    public void put(Rule key, TIntObjectMap<TIntObjectMap<TIntObjectMap<ExpressionSemiring.Value>>> value) {
        states.put(key, value);
    }

    public TIntObjectMap<TIntObjectMap<TIntObjectMap<ExpressionSemiring.Value>>> get(Rule key) {
        return states.get(key);
    }

    public void add(Rule rule, int position, int ruleStart, int dotPosition, ExpressionSemiring.Value value) {
        getDotPositionToScore(rule, position, ruleStart).put(dotPosition, value);
        size++;
    }

    public TIntObjectMap<ExpressionSemiring.Value> getDotPositionToScore(Rule rule, int index, int ruleStart) {
        if (!states.containsKey(rule)) states.put(rule, new TIntObjectHashMap<>(30));
        TIntObjectMap<TIntObjectMap<TIntObjectMap<ExpressionSemiring.Value>>> iToRest = states.get(rule);

        if (!iToRest.containsKey(index))
            iToRest.put(index, new TIntObjectHashMap<>(50));
        TIntObjectMap<TIntObjectMap<ExpressionSemiring.Value>> ruleStartToDotToState = iToRest.get(index);

        if (!ruleStartToDotToState.containsKey(ruleStart))
            ruleStartToDotToState.put(ruleStart, new TIntObjectHashMap<>(10, 0.5F, -1));
        return ruleStartToDotToState.get(ruleStart);
    }

    public int size() {
        return size;
    }

    public void forEach(StateHandler h) {
        states.forEach((rule, tIntObjectMapTIntObjectMap) ->
                tIntObjectMapTIntObjectMap.forEachEntry((position, tIntDoubleMapTIntObjectMap) -> {
                    tIntDoubleMapTIntObjectMap.forEachEntry((ruleStart, tIntDoubleMap) -> {
                        tIntDoubleMap.forEachEntry((dot, score) -> {
                            h.consume(position, ruleStart, dot, rule, score);
                            return true;
                        });
                        return true;
                    });
                    return true;
                }));
    }

    @FunctionalInterface
    public interface StateHandler {
        void consume(int position, int ruleStart, int dot, Rule rule, ExpressionSemiring.Value score);
    }
}
