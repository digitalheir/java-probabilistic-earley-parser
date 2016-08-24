package org.leibnizcenter.cfg.algebra.expression;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring.Value;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains references to deferred computations. Used in completion stage.
 * Created by Maarten on 23-8-2016.
 */
public class ScoreRefs {
    private final Map<
            Rule,
                         /*index*/
            TIntObjectMap<
                                 /*rule start*/
                    TIntObjectMap<
                                         /*dot position*/
                            TIntObjectMap<
                                    /* score */
                                    Value
                                    >
                            >
                    >
            > states;
    private final ExpressionSemiring semiring;

    public ScoreRefs(int capacity, ExpressionSemiring semiring) {
        states = new HashMap<>(capacity);
        this.semiring = semiring;
    }

    public Value getExpression(Rule rule, int index, int ruleStart, int dot) {
        return getDotToScore(rule, index, ruleStart).get(dot);
    }

    public TIntObjectMap<Value> getDotToScore(Rule rule, int index, int ruleStart) {
        if (!states.containsKey(rule)) states.put(rule, new TIntObjectHashMap<>(30));
        TIntObjectMap<TIntObjectMap<TIntObjectMap<Value>>> iToRest = states.get(rule);

        if (!iToRest.containsKey(index))
            iToRest.put(index, new TIntObjectHashMap<>(50));
        TIntObjectMap<TIntObjectMap<Value>> ruleStartToDotToState = iToRest.get(index);

        if (!ruleStartToDotToState.containsKey(ruleStart))
            ruleStartToDotToState.put(ruleStart, new TIntObjectHashMap<>(10, 0.5F, -1));
        return ruleStartToDotToState.get(ruleStart);
    }

    void setScore(Rule rule, int index, int ruleStart, int dotPosition, Value set) {
        getDotToScore(rule, index, ruleStart)
                .put(dotPosition, set);
    }

    public Map<Rule, TIntObjectMap<TIntObjectMap<TIntObjectMap<Value>>>> getStates() {
        return states;
    }

    public Value getExpression(State state) {
        return getExpression(state.getRule(), state.getPosition(), state.getRuleStartPosition(), state.getRuleDotPosition());
    }

    public Value getOrCreate(State state, double defaultValue) {
        Value exp = getExpression(state);
        if (exp == null) {
            setScore(state, semiring.dbl(defaultValue));
            exp = getExpression(state);
            if (exp == null) throw new IssueRequest("expression should not be null");
            return exp;
        } else return exp;
    }

    private void setScore(State state, Value expression) {
        setScore(state.getRule(), state.getPosition(), state.getRuleStartPosition(), state.getRuleDotPosition(), expression);
    }
}
