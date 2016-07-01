package org.leibnizcenter.cfg.earleyparser.parse;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maarten on 28-6-16.
 */
public class StatePredictor {
    private final Chart.StateSets stateSets;
    private final int index;
    private final Map<State, State.Score> statesToPredictOn;
    private final Grammar grammar;


    public StatePredictor(Grammar grammar, int index, Chart.StateSets stateSets) {
        this.stateSets = stateSets;
        this.index = index;
        this.grammar = grammar;
        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Yμ</code>, get Y...
        this.statesToPredictOn = stateSets.getStatesActiveOnNonTerminals(index);
    }

    private static <U> TObjectDoubleHashMap<U> appendForwardScores(TObjectDoubleHashMap<U> a, TObjectDoubleHashMap<U> b) {
        a.forEachEntry((rule, score) -> {
            b.put(rule, score + b.get(rule)/*defaults to 0.0*/);
            return true;
        });
        return b;
    }

    public void predict() {
        //TODO precompute left-corner recursion
        if (stateSets.containsStates(index)) { // any states at this index?

            // Given state <code>i: X<sub>k</sub> → λ·Zμ</code>
            for (Map.Entry<State, State.Score> statePredecessor : statesToPredictOn.entrySet()) {
                final Category Z = statePredecessor.getKey().getActiveCategory();
                double prevForward = statePredecessor.getValue().getForwardScore();

                // For all production Y → μ such that R(Z =*L> Y) is nonzero
                TObjectDoubleHashMap<Rule> forwardScores = grammar.getAllRules().stream()
                        .map(Yrule -> {
                            System.out.println(grammar.getLeftStarScore(Z, Yrule.getLeft()) > 0.0);
                            return Yrule;
                        })
                        .filter(Yrule -> grammar.getLeftStarScore(Z, Yrule.getLeft()) > 0.0)

                        // we predict <code>i: Y<sub>i</sub> → ·μ</code>
                        .reduce(new TObjectDoubleHashMap<>((int) Math.pow(grammar.size(), 2), 0.5F, 0.0),

                                //   with forward += previous_forward * R(Z =*L> Y) * P(Y → μ)
                                (forwardScores, Yrule) -> {
                                    double LstarProb = grammar.getLeftStarScore(Z, Yrule.getLeft());
                                    final double nextForward = prevForward
                                            * LstarProb
                                            * Yrule.getProbability();
                                    forwardScores.put(Yrule, forwardScores.get(Yrule) + nextForward);
                                    return forwardScores;
                                },

                                StatePredictor::appendForwardScores);

                // and inner: P(Y → μ)
                Map<State, State.Score> predictedStates = new HashMap<>(forwardScores.size());
                forwardScores.forEachEntry((rule, summedForwardScore) -> predictedStates.put(
                        new State(rule, index, index, 0),
                        new State.Score(summedForwardScore, rule.getProbability())
                        )
                );
            }
        }
    }
}
