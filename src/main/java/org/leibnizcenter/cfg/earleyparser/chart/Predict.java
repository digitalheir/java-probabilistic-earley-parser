package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;

import java.util.Collection;
import java.util.HashSet;

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
        final DblSemiring sr = grammar.getSemiring();

        final Collection<State> statesToPredictOn = stateSets.getStatesActiveOnNonTerminals(index);
        final Collection<State.StateWithScore> newStates = new HashSet<>(20);

        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...
        statesToPredictOn.forEach(statePredecessor -> {
            final Category Z = statePredecessor.getActiveCategory();
            double prevForward = stateSets.forwardScores.get(statePredecessor);

            // For all productions Y → v such that R(Z =*L> Y) is nonzero
            grammar.getLeftStarCorners()
                    .getNonZeroScores(Z).stream()
                    .flatMap(Y -> grammar.getRules(Y).stream()) // ?
                    // we predict state <code>i: Y<sub>i</sub> → ·v</code>
                    .forEach(Y_to_v -> {
                        final Category Y = Y_to_v.getLeft();


                        // γ' = P(Y → v)
                        final double Y_to_vProbability = Y_to_v.getScore();

                        // α' = α * R(Z =*L> Y) * P(Y → v)
                        final double fw = sr.times(prevForward, grammar.getLeftStarScore(Z, Y), Y_to_vProbability);


                        if (stateSets.contains(Y_to_v,index, index, 0)) {
                            final State predicted = stateSets.get(index, index, 0, Y_to_v); // We might want to increment the probability of an existing state
                            final double innerScore = stateSets.innerScores.get(predicted);

                            if (!(Y_to_vProbability == innerScore || sr.zero() == innerScore))
                                throw new IssueRequest(Y_to_vProbability + " != " + innerScore);

                            stateSets.forwardScores.add(predicted, fw);
                            stateSets.innerScores.put(predicted, Y_to_vProbability);
                            stateSets.viterbiScores.put(new State.ViterbiScore(Y_to_vProbability, statePredecessor, predicted, grammar.getSemiring()));
                        } else {
                            final State predicted2 = State.create(index, index, 0, Y_to_v);
                            stateSets.viterbiScores.put(new State.ViterbiScore(Y_to_vProbability, statePredecessor, predicted2, grammar.getSemiring()));
                            newStates.add(new State.StateWithScore(predicted2, fw, Y_to_vProbability, null));
                        }
                    });
        });

        newStates.forEach(ss -> {
            final State state = ss.getState();
            stateSets.addIfNew(state);
            stateSets.forwardScores.add(state, ss.getForwardScore());
            stateSets.innerScores.put(state, ss.getInnerScore());
        });
    }
}
