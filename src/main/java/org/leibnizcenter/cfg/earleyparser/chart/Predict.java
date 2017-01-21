package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.ForwardScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.InnerScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.ViterbiScores;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.util.MapEntry;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Predict phase
 * <p>
 * Created by maarten on 31/10/16.
 */
@SuppressWarnings("WeakerAccess")
public class Predict {


    /**
     * Don't instantiate
     */
    private Predict() {
        throw new Error();
    }

    /**
     * Makes predictions in the specified chart at the given index.
     * <p/>
     * For each state at position i, look at the the nonterminal at the dot position,
     * add a state that expands that nonterminal at position i, with the dot position at 0
     *
     * @param index The token index to make predictions at.
     */
    public static <T> void predict(int index, Grammar<T> grammar, StateSets<T> stateSets) {
        final ExpressionSemiring semiring = grammar.getSemiring();

        final ForwardScores forwardScores = stateSets.forwardScores;
        final InnerScores innerScores = stateSets.innerScores;
        final ViterbiScores viterbiScores = stateSets.viterbiScores;

        final Collection<State> statesToPredictOn = stateSets.activeStates.getActiveOnNonTerminals(index);


        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...

        Collection<State> newStates = statesToPredictOn.stream()
                .parallel()
                .flatMap(statePredecessor -> {
                    final Category Z = statePredecessor.getActiveCategory();

                    // For all productions Y → v such that R(Z =*L> Y) is nonzero
                    return grammar
                            .getNonZeroLeftStarRules(Z)
                            .stream()
                            .map(Y_to_v -> new MapEntry<>(statePredecessor, Y_to_v));
                })
                .parallel()
                .map(statePredecessor_Y_to_v -> {
                    // we predict state <code>i: Y<sub>i</sub> → ·v</code>

                    final State statePredecessor = statePredecessor_Y_to_v.getKey();
                    final Rule Y_to_v = statePredecessor_Y_to_v.getValue();

                    final Category Z = statePredecessor.getActiveCategory();
                    final Category Y = Y_to_v.getLeft();

                    final double prevForward = forwardScores.get(statePredecessor);

                    // γ' = P(Y → v)
                    final double Y_to_vProbability = Y_to_v.getScore();

                    // α' = α * R(Z =*L> Y) * P(Y → v)
                    final double fw = semiring.times(prevForward, grammar.getLeftStarScore(Z, Y), Y_to_vProbability);

                    return (stateSets.contains(Y_to_v, index, index, 0))
                            ? new IncrementExistingState(stateSets.get(index, index, 0, Y_to_v), Y_to_vProbability, fw, statePredecessor)
                            : new IncrementNewState(State.create(index, index, 0, Y_to_v), Y_to_vProbability, statePredecessor, fw);
                })

                .parallel()//.sequential() // Now that we've calculated the scores, add to chart... // TODO does this need to be sequential actually?

                .filter(delta -> {
                    if (delta instanceof IncrementNewState) {
                        // New state, pass outside this stream to avoid concurrent modification
                        IncrementNewState inc = (IncrementNewState) delta;
                        final State state = inc.newState;
                        viterbiScores.put(new State.ViterbiScore(inc.Y_to_vProbability, inc.statePredecessor, state, semiring));
                        forwardScores.add(state, inc.fw);
                        innerScores.put(state, inc.Y_to_vProbability);

                        return true;
                    } else if (delta instanceof IncrementExistingState) {
                        // Not new, add data to chart right away

                        IncrementExistingState inc = (IncrementExistingState) delta;
                        final double innerScore = innerScores.get(inc.predicted);

                        if (!(inc.Y_to_vProbability == innerScore || semiring.zero() == innerScore))
                            throw new IssueRequest(inc.Y_to_vProbability + " != " + innerScore);

                        forwardScores.add(
                                inc.predicted, inc.fw
                        );
                        innerScores.put(
                                inc.predicted, inc.Y_to_vProbability
                        );
                        viterbiScores.put(
                                new State.ViterbiScore(inc.Y_to_vProbability, inc.statePredecessor, inc.predicted, semiring)
                        );

                        return false;
                    } else throw new Error();
                })
                .map(delta -> delta.getState())
                .collect(Collectors.toList());

        // Process the new states outside of stream to avoid concurrent modification
        newStates.forEach(state -> stateSets.addIfNew(state));
    }

    private interface Delta {
        State getState();
    }

    private static class IncrementExistingState implements Delta {

        private final double Y_to_vProbability;
        private final double fw;
        private final State statePredecessor;
        private final State predicted;

        public IncrementExistingState(State state,
                                      double y_to_vProbability,
                                      double fw,
                                      State statePredecessor) {
            this.predicted = state;
            this.Y_to_vProbability = y_to_vProbability;
            this.fw = fw;
            this.statePredecessor = statePredecessor;
        }

        @Override
        public State getState() {
            throw new IssueRequest("This is a bug");
        }
    }

    private static class IncrementNewState implements Delta {
        public final double Y_to_vProbability;
        public final State statePredecessor;
        public final double fw;
        private final State newState;

        private IncrementNewState(State state, double y_to_vProbability, State statePredecessor, double fw) {
            Y_to_vProbability = y_to_vProbability;
            this.statePredecessor = statePredecessor;
            this.fw = fw;
            this.newState = state;
        }

        @Override
        public State getState() {
            return newState;
        }
    }
}
