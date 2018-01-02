package org.leibnizcenter.cfg.earleyparser;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.Bug;
import org.leibnizcenter.cfg.grammar.Grammar;

import java.util.HashMap;
import java.util.Map;

public class DeferredStateScoreComputations {
    public final Map<State, ExpressionWrapper> states;
    public final TObjectDoubleMap<State> statesDouble = new TObjectDoubleHashMap<>(50, 0.5F, Double.NaN);
    private final ExpressionSemiring semiring;

    public DeferredStateScoreComputations(final Grammar grammar) {
        this.states = new HashMap<>();
        this.semiring = grammar.semiring;
    }

    public ExpressionWrapper getOrCreate(final State state,
                                         final double default_) {
        if (this.states.containsKey(state)) {
            return this.states.get(state);
        } else {
            final ExpressionWrapper expressionWrapper = new ExpressionWrapper(default_);
            this.states.put(state, expressionWrapper);
            return expressionWrapper;
        }
    }

    public ExpressionWrapper getOrNull(final State state) {
        return this.states.getOrDefault(state, null);
    }

    public void plus(final State s, final Resolvable addValue) {
        final ExpressionWrapper current = this.getOrCreate(s, this.semiring.zero());

        if (current.hasExpression())
            current.setExpression(semiring.plus(addValue, current.getExpression()));
        else if (addValue instanceof Atom && !current.hasExpression())
            current.setExpression(semiring.plus(((Atom) addValue).value, current.getLiteral()));
        else
            current.setExpression(semiring.plus(addValue, current.getLiteral()));

        this.states.put(s, current);
    }

    public void plusProductOf(final State s, final ExpressionSemiring semiring, final double t1, final ExpressionWrapper t2, final Resolvable t3) {
        final ExpressionWrapper current = this.getOrCreate(s, this.semiring.zero());
        final Resolvable addValue = semiring.times(t1, t2, t3);

        if (current.hasExpression())
            current.setExpression(this.semiring.plus(addValue, current.getExpression()));
//        else if (addValue instanceof Atom && !current.hasExpression()) {
//            throw new Error("Never happens");
//            current.setExpression(this.semiring.plus(((Atom) addValue).value, current.getLiteral()));
//        }
        else
            current.setExpression(this.semiring.plus(addValue, current.getLiteral()));

        this.states.put(s, current);
    }
}
