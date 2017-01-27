package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseCallbacks;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.TokenWithCategories;
import org.leibnizcenter.cfg.util.MapEntry;

import java.util.HashSet;
import java.util.Set;

/**
 * Predict phase
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class Predict<T> {

    private final StateSets<T> stateSets;
    private final Grammar<T> grammar;

    public Predict(StateSets<T> stateSets) {
        this.stateSets = stateSets;
        this.grammar = stateSets.grammar;
    }

    private static <T> Delta getNextStateAndScores(int index,
                                                   Grammar<T> grammar,
                                                   StateSets<T> stateSets,
                                                   MapEntry<State, Rule> statePredecessor_Y_to_v) {
        final State statePredecessor = statePredecessor_Y_to_v.getKey();
        final Rule Y_to_v = statePredecessor_Y_to_v.getValue();

        final Category Z = statePredecessor.getActiveCategory();
        final Category Y = Y_to_v.left;

        final double prevForward = stateSets.forwardScores.get(statePredecessor);

        // γ' = P(Y → value)
        final double Y_to_vProbability = Y_to_v.getScore();

        // α' = α * R(Z =*L> Y) * P(Y → value)
        final double fw = grammar.semiring.times(prevForward, grammar.semiring.times(grammar.getLeftStarScore(Z, Y), Y_to_vProbability));

        State state = State.create(index, index, 0, Y_to_v);
        boolean isNew = !stateSets.contains(state);
        return new Delta(isNew, state, Y_to_vProbability, fw, statePredecessor);
    }

    void predict(ParseCallbacks<T> callbacks, Chart<T> chart, int i, TokenWithCategories<T> token) {
        if (callbacks != null) callbacks.beforePredict(i, token, chart);

        predict(i);

        if (callbacks != null) callbacks.onPredict(i, token, chart);
    }

    /**
     * Makes predictions in the specified chart at the given index.
     * <p/>
     * For each state at position i, look at the the nonterminal at the dot position,
     * add a state that expands that nonterminal at position i, with the dot position at 0
     *
     * @param index The token index to make predictions at.
     */
    public void predict(int index) {
        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...
        final Set<State> activeOnNonTerminals = stateSets.activeStates.getActiveOnNonTerminals(index);
        if (activeOnNonTerminals != null)
            // Copy set to avoid concurrent modification
            new HashSet<>(activeOnNonTerminals).stream().parallel()

                    // For all productions Y → value such that R(Z =*L> Y) is nonzero
                    .flatMap(grammar::streamNonZeroLeftStarRulesWithPrecedingState).parallel()

                    // we predict state <code>i: Y<sub>i</sub> → ·value</code>
                    .map(statePredecessor_Y_to_v -> getNextStateAndScores(index, grammar, stateSets, statePredecessor_Y_to_v))

                    // Now that we've calculated the scores, add to chart...
                    .sequential()
                    .forEach(stateSets::setScores);
    }

    public static class Delta {
        public final double Y_to_vProbability;
        public final double fw;
        public final State statePredecessor;
        public final State predicted;
        public final boolean isNew;

        public Delta(boolean isNew, State state,
                     double y_to_vProbability,
                     double fw,
                     State statePredecessor) {
            this.isNew = isNew;
            this.predicted = state;
            this.Y_to_vProbability = y_to_vProbability;
            this.fw = fw;
            this.statePredecessor = statePredecessor;
        }
    }
}
