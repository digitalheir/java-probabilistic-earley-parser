package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.Set;


/**
 * <p>
 * A chart produced by an  Earley parser.
 * <p/>
 * <p>
 * Charts contain sets of {@link State states} mapped to the string indices where
 * they originate. Since the state sets are {@link Set sets}, an state can only
 * be added at a given index once (as sets do not permit duplicate members).
 * State sets are not guaranteed to maintain states in their order of insertion.
 * </p>
 */
public class Chart<T> {
    public final StateSets<T> stateSets;
    public final Grammar<T> grammar;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart(Grammar<T> grammar) {
        this(grammar, new StateSets<>(grammar));
    }

    /**
     * Creates a new chart from the specified sorted map of indices mapped to
     * state sets.
     *
     * @param stateSets The map of integer-mapped state sets to use as this
     *                  chart's backing data structure.
     */
    private Chart(Grammar<T> grammar, StateSets<T> stateSets) {
        this.stateSets = stateSets;
        this.grammar = grammar;
    }

    /**
     * Counts the total number of states contained in this chart, at any
     * index.
     *
     * @return The total number of states contained.
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public int countStates() {
        return stateSets.countStates();
    }

    /**
     * Gets a string representation of this chart.
     */
    @Override
    public String toString() {
        return stateSets.toString();
    }

    void addState(State state, double forward, double inner) {
        stateSets.getOrCreate(state);
        stateSets.innerScores.put(state, inner);
        stateSets.forwardScores.put(state, forward);
        if (stateSets.viterbiScores.get(state) == null)
            stateSets.setViterbiScore(new State.ViterbiScore(grammar.semiring.one(), null, state, grammar.semiring));
    }

    @SuppressWarnings("unused")
    public Set<State> getStates(int index) {
        return stateSets.getStates(index);
    }

    public double getForwardScore(State s) {
        return stateSets.forwardScores.get(s);
    }

    @SuppressWarnings("unused")
    public double getInnerScore(State s) {
        return stateSets.innerScores.get(s);
    }

    public State.ViterbiScore getViterbiScore(State s) {
        return stateSets.viterbiScores.get(s);
    }

    public void addInitialState(Category goal) {
        ExpressionSemiring sr = grammar.semiring;
        addState(new State(Rule.create(sr, 1.0, Category.START, goal), 0),
                sr.one(),
                sr.one()
        );
    }
}
