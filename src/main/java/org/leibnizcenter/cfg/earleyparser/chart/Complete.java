package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.expression.AddableValuesContainer;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by maarten on 31/10/16.
 */
public class Complete {
    /**
     * Don't instantiate
     */
    private Complete() {
        throw new Error();
    }

    /**
     * Completes states exhaustively and makes resolvable expressions for the forward and inner scores.
     * Note that these expressions can only be resolved to actual values after finishing completion, because they may depend on one another.
     *
     * @param position         State position
     * @param states           Completed states to use for deducing what states to proceed
     * @param addForwardScores Container / helper for adding to forward score expressions
     * @param addInnerScores   Container / helper for adding to inner score expressions
     *                         //     * @param completedStatesAlreadyHandled The completed states that we don't want to reiterate.
     *                         //     * @param computationsForward           Container for forward score expressions. Probably superfluous.
     *                         //     * @param computationsInner             Container for inner score expressions. Probably superfluous.
     */
    static void completeNoViterbi(int position,
                                  Collection<State> states,
//                                   Set<State> completedStatesAlreadyHandled,
                                  AddableValuesContainer addForwardScores,
                                  AddableValuesContainer addInnerScores,
//                                   ScoreRefs computationsForward,
//                                   ScoreRefs computationsInner
                                  Grammar grammar,
                                  StateSets stateSets,
                                  ExpressionSemiring semiring
    ) {
        StateMap possiblyNewStates = null;
        // For all states
        //      i: Y<sub>j</sub> → v·    [a",y"]
        //      j: X<sub>k</suv> → l·Zm  [a',y']
        //
        //  such that the R*(Z =*> Y) is nonzero
        //  and Y → v is not a unit production
        for (State completedState : states) {
            //completedStatesAlreadyHandled.add(completedState);
            final int j = completedState.getRuleStartPosition();
            final NonTerminal Y = completedState.getRule().getLeft();

            ExpressionSemiring.Value unresolvedCompletedInner = addInnerScores.getOrCreate(completedState, stateSets.getInnerScore(completedState));

            //noinspection Convert2streamapi
            for (State stateToAdvance : stateSets.getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(j, Y)) {
                if (j != stateToAdvance.getPosition()) throw new IssueRequest("Index failed. This is a bug.");
                // Make i: X_k → lZ·m
//                Value prevInner = computationsForward.getOrCreate(stateToAdvance, stateSets.getInnerScore(stateToAdvance));
                ExpressionSemiring.Value prevInner = addInnerScores.getOrCreate(stateToAdvance, stateSets.getInnerScore(stateToAdvance));
//                Value prevForward = computationsInner.getOrCreate(stateToAdvance, stateSets.getForwardScore(stateToAdvance));
                ExpressionSemiring.Value prevForward = addForwardScores.getOrCreate(stateToAdvance, stateSets.getForwardScore(stateToAdvance));

                final Category Z = stateToAdvance.getActiveCategory();

                ExpressionSemiring.Value unitStarScore = semiring.dbl(grammar.getUnitStarScore(Z, Y));
                ExpressionSemiring.Value fw = unitStarScore.times(prevForward).times(unresolvedCompletedInner);
                ExpressionSemiring.Value inner = unitStarScore.times(prevInner).times(unresolvedCompletedInner);

//                    if (completedState != null) {
//                    } else {
//                                    resultingState = stateSets.create(i,
//                                            stateToAdvance.getRuleStartPosition(),
//                                            stateToAdvance.advanceDot(),
//                                            stateToAdvance.getRule());
//                    }
                Rule newStateRule = stateToAdvance.getRule();
                int newStateDotPosition = stateToAdvance.advanceDot();
                int newStateRuleStart = stateToAdvance.getRuleStartPosition();
                addForwardScores.add(
                        newStateRule,
                        position,
                        newStateRuleStart,
                        newStateDotPosition,
                        fw
                );

                // If this is a new completed state that is no unit production, make a note of it it because we want to recursively call *complete* on these states
                if (
                        newStateRule.isPassive(newStateDotPosition)/*isCompleted*/
                                && !newStateRule.isUnitProduction()
                                && stateSets.get(position, newStateRuleStart, newStateDotPosition, newStateRule) == null) {
                    if (possiblyNewStates == null) possiblyNewStates = new StateMap(20);
                    possiblyNewStates.add(
                            newStateRule,
                            position,
                            newStateRuleStart,
                            newStateDotPosition,
                            fw
                    );
                }

                addInnerScores.add(
                        newStateRule,
                        position,
                        newStateRuleStart,
                        newStateDotPosition,
                        inner
                );
            }
        }

        if (possiblyNewStates != null) {
            List<State> newCompletedStates = new ArrayList<>(possiblyNewStates.size());
            possiblyNewStates.forEach((index, ruleStart, dot, rule, score) -> {
                boolean isnew = stateSets.get(index, ruleStart, dot, rule) == null;
                final State state = stateSets.getOrCreate(index, ruleStart, dot, rule);
                if (!isnew || !state.isCompleted() || state.rule.isUnitProduction())
                    throw new IssueRequest("Unexpected state found in possible new states. This is a bug.");
                //if (completedStatesAlreadyHandled==null||!completedStatesAlreadyHandled.contains(state))
                newCompletedStates.add(state);
            });
            //noinspection ConstantConditions
            if (newCompletedStates != null && newCompletedStates.size() > 0) completeNoViterbi(position,
                    newCompletedStates,
//                    completedStatesAlreadyHandled,
                    addForwardScores,
                    addInnerScores,
//                    computationsForward,
//                    computationsInner
                    grammar, stateSets, semiring
            );
//        }
        }
    }
}
