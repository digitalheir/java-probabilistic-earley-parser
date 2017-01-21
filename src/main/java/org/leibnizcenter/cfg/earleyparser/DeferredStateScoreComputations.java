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
        this.ZERO = semiring::zero;
    }

    private DeferredValue getOrCreate(State state,
                                      Resolvable default_) {
        if (this.states.containsKey(state)) {
            return this.states.get(state);
        } else {
            final DeferredValue deferredValue = new DeferredValue(default_);
            this.states.put(state, deferredValue);
            return deferredValue;
        }
    }

    DeferredValue getOrCreate(State state,
                              double default_) {
        if (this.states.containsKey(state)) {
            return this.states.get(state);
        } else {
            final DeferredValue deferredValue = new DeferredValue(new Atom(default_));
            this.states.put(state, deferredValue);
            return deferredValue;
        }
    }

    public void plus(State s, Resolvable addValue) {
        DeferredValue current = this.getOrCreate(
                s,
                this.ZERO
        );
        current.expression = new ExpressionSemiring.Plus(semiring, addValue, current.expression);
        this.states.put(s, current);
    }

}
