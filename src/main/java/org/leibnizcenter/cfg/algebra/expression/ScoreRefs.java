package org.leibnizcenter.cfg.algebra.expression;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring.Value;
import org.leibnizcenter.cfg.earleyparser.chart.StateMap;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;

/**
 * Contains references to deferred computations. Used in completion stage.
 * Created by Maarten on 23-8-2016.
 */
public class ScoreRefs {
    private final StateMap states;
    private final ExpressionSemiring semiring;

    ScoreRefs(int capacity, ExpressionSemiring semiring) {
        states = new StateMap(capacity);
        this.semiring = semiring;
    }

    Value getExpression(Rule rule, int index, int ruleStart, int dot) {
        return states.getDotPositionToScore(rule, index, ruleStart).get(dot);
    }

    void setScore(Rule rule, int index, int ruleStart, int dotPosition, Value set) {
        states.getDotPositionToScore(rule, index, ruleStart)
                .put(dotPosition, set);
    }

    public StateMap getStates() {
        return states;
    }

    private Value getExpression(State state) {
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
