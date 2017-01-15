package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.expression.AddableValuesContainer;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;

import java.util.*;

/**
 * Complete stage
 *
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
    static <E> void completeNoViterbi(int position,
                                  Collection<State> states,
//                                   Set<State> completedStatesAlreadyHandled,
                                  AddableValuesContainer addForwardScores,
                                  AddableValuesContainer addInnerScores,
//                                   ScoreRefs computationsForward,
//                                   ScoreRefs computationsInner
                                  Grammar<E> grammar,
                                  StateSets<E> stateSets,
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

            ExpressionSemiring.Value unresolvedCompletedInner = addInnerScores.getOrCreate(
                    completedState,
                    stateSets.getInnerScore(completedState)
            );

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
            possiblyNewStates.forEach((index, ruleStart, dot, rule, ignored) -> {
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

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param i The index to make completions at.
     */
    public static <E> void completeNoViterbi(int i, Grammar<E> grammar, StateSets<E> stateSets) {
        final ExpressionSemiring semiring = grammar.getSemiring();
        final AddableValuesContainer addForwardScores = new AddableValuesContainer(50, semiring);
        final AddableValuesContainer addInnerScores = new AddableValuesContainer(50, semiring);
//        final ScoreRefs computationsInner = new ScoreRefs(1, semiring);
//        final ScoreRefs computationsForward = new ScoreRefs(1, semiring);

        completeNoViterbi(
                i,
                stateSets.getCompletedStatesThatAreNotUnitProductions(i),
                //new HashSet<>(),
                addForwardScores,
                addInnerScores,
//                computationsForward,
//                computationsInner,
                grammar, stateSets, semiring
        );

        // Resolve and set forward score
        addForwardScores.getStates().forEach((position, ruleStart, dot, rule, score) -> {
            final State state = stateSets.getOrCreate(position, ruleStart, dot, rule);
            stateSets.setForwardScore(state, score.resolve());
        });

        // Resolve and set inner score
        addInnerScores.getStates().forEach((position, ruleStart, dot, rule, score) -> {
            final State state = stateSets.getOrCreate(position, ruleStart, dot, rule);
            stateSets.setInnerScore(state, score.resolve());
        });
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need it to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). ~This method does not guarantee a left most parse.~
     *
     * @param completedState Completed state to calculate Viterbi score for
     * @param originPathTo Path to state
     * @param sr             Semiring to use for calculating
     *///TODO write tests
    public static <T> void setViterbiScores(final State completedState, final Set<State> originPathTo, final DblSemiring sr, StateSets<T> stateSets) {
        Collection<State> newStates = null; // init as null to avoid arraylist creation
        Collection<State> newCompletedStates = null; // init as null to avoid arraylist creation

        if (stateSets.getViterbiScore(completedState) == null)
            throw new IssueRequest("Expected Viterbi score to be set on completed state. This is a bug.");

        final double completedViterbi = stateSets.getViterbiScore(completedState).getScore();
        final NonTerminal Y = completedState.getRule().getLeft();
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        int completedPos = completedState.getPosition();
        for (State stateToAdvance : stateSets.getStatesActiveOnNonTerminal(Y, completedState.getRuleStartPosition(), completedPos)) {
            if (stateToAdvance.getPosition() > completedPos || stateToAdvance.getPosition() != completedState.getRuleStartPosition())
                throw new IssueRequest("Index failed. This is a bug.");
            int ruleStart = stateToAdvance.getRuleStartPosition();
            int nextDot = stateToAdvance.advanceDot();
            Rule rule = stateToAdvance.getRule();
            State resultingState = stateSets.get(completedPos, ruleStart, nextDot, rule);
            if (resultingState == null) {
                resultingState = State.create(completedPos, ruleStart, nextDot, rule);
                if (newStates == null) newStates = new HashSet<>(20);
                newStates.add(resultingState);
            }
            if (originPathTo.contains(resultingState)) {
                System.err.println("Already went past " + resultingState);
                return;
            }
            final State.ViterbiScore viterbiScore = stateSets.getViterbiScore(resultingState);
            final State.ViterbiScore prevViterbi = stateSets.getViterbiScore(stateToAdvance);
            if (prevViterbi == null) throw new Error("Expected viterbi to be set for " + stateToAdvance);
            final double prev = prevViterbi.getScore();
            final State.ViterbiScore newViterbiScore = new State.ViterbiScore(sr.times(completedViterbi, prev), completedState, resultingState, sr);

            if (viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0) {
                stateSets.setViterbiScore(newViterbiScore);
                if (resultingState.isCompleted()) {
                    if (newCompletedStates == null) newCompletedStates = new HashSet<>(20);
                    newCompletedStates.add(resultingState);
                }
            }

        }

        // Add new states to chart
        if (newStates != null) newStates.forEach(stateSets::add);

        // Recurse with new states that are completed
        if (newCompletedStates != null) newCompletedStates.forEach(resultingState -> {
            final Set<State> path = new HashSet<>(originPathTo);
            path.add(resultingState);
            setViterbiScores(resultingState, path, sr, stateSets);
        });
    }
}
