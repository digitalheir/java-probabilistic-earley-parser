package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;

import java.util.HashMap;
import java.util.Map;

/**
 */
class DeferredStateScoreComputations {
    final Map<State, DeferredValue> states;
    private final Resolvable ZERO;
    private final ExpressionSemiring semiring;

    DeferredStateScoreComputations(ExpressionSemiring semiring) {
        this.states = new HashMap<>();
        this.semiring = semiring;
        this.ZERO = semiring.ZERO_EXPRESSION;
    }

    DeferredValue getOrCreate(State state,
                              Resolvable default_) {
        if (this.states.containsKey(state)) {
            return this.states.get(state);
        } else {
            final DeferredValue deferredValue = new DeferredValue(default_);
            this.states.put(state, deferredValue);
            return deferredValue;
        }
    }

    public void plus(State s, Resolvable addValue) {
        DeferredValue current = this.getOrCreate(
                s,
                this.ZERO
        );
        current.setExpression(semiring.new Plus(addValue, current.getExpression()));
        this.states.put(s, current);
    }

}
