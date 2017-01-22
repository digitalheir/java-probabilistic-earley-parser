package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.util.Triple;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Complete stage
 * <p>
 * Created by maarten on 31/10/16.
 */
public class Complete<T> {
    private final StateSets<T> stateSets;

    /**
     */
    Complete(StateSets<T> stateSets) {
        this.stateSets = stateSets;
    }

    private static boolean newViterbiIsBetter(State.ViterbiScore viterbiScore, State.ViterbiScore newViterbiScore) {
        return viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0;
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
    private static <E> void completeNoViterbi(final int position,
                                              final Collection<State> states,
                                              final DeferredStateScoreComputations addForwardScores,
                                              final DeferredStateScoreComputations addInnerScores,
                                              final StateSets<E> stateSets
    ) {
        if (states == null || states.size() <= 0) return;


        final DeferredStateScoreComputations newStates = new DeferredStateScoreComputations(stateSets.grammar.semiring);
        final Set<State> newCompletedStates = states.stream()
                // For all states
                //      i: Y<sub>j</sub> → v·    [a",y"]
                //      j: X<sub>k</suv> → l·Zm  [a',y']
                //
                //  such that the R*(Z =*> Y) is nonzero
                //  and Y → v is not a unit production

                // WARNING: shared mutated mutability
                .map(completedState -> new Triple(null, completedState, addInnerScores.getOrCreate(completedState, stateSets.innerScores.getAtom(completedState))))

                /* Safe to parallelize here */
                .parallel()
                .flatMap(stateSets.activeStates::streamAllStatesToAdvance)
                .parallel()
                .map(stateInformation -> completeNoViterbiForTriple(
                        position,
                        addInnerScores.getOrCreate(stateInformation.stateToAdvance, stateSets.innerScores.getAtom(stateInformation.stateToAdvance)),
                        addForwardScores.getOrCreate(stateInformation.stateToAdvance, stateSets.forwardScores.getAtom(stateInformation.stateToAdvance)),
                        stateSets,
                        stateInformation
                        )
                )

                /* */

                .sequential()

                /* WARNING: shared mutable state mutated*/

                .filter(delta -> {
                            addForwardScores.plus(delta.state, delta.addForward);
                            addInnerScores.plus(delta.state, delta.addInner);
                            return delta.newCompletedStateNoUnitProduction;
                        }
                )
                .map(newStates::addForward)
                .map(Delta::getState)

                /* */

                .collect(Collectors.toSet());
        // recurse
        if (newCompletedStates.size() > 0) {
            newCompletedStates.forEach(stateSets::getOrCreate);
            Complete.completeNoViterbi(
                    position,
                    newCompletedStates,
                    addForwardScores,
                    addInnerScores,
                    stateSets
            );
        }
    }

    private static <E> Delta completeNoViterbiForTriple(int position,
                                                        Resolvable prevInner,
                                                        Resolvable prevForward,
                                                        StateSets<E> stateSets,
                                                        Triple t) {
        final int j = t.completedState.ruleStartPosition;
        final NonTerminal Yl = t.completedState.rule.left;

        // Make i: X_k → lZ·m
        final Category Z = t.stateToAdvance.getActiveCategory();
        final Grammar<E> grammar = stateSets.grammar;
        final Resolvable unitStarScore = grammar.getUnitStarScore(Z, Yl);

        final ExpressionSemiring.Times fw = grammar.semiring.new Times(unitStarScore, prevForward, t.completedInner);
        final ExpressionSemiring.Times inner = grammar.semiring.new Times(unitStarScore, prevInner, t.completedInner);
        if (j != t.stateToAdvance.position) throw new IssueRequest("Index failed. This is a bug.");
        final State s = State.create(
                position,
                t.stateToAdvance.ruleStartPosition,
                t.stateToAdvance.advanceDot(),
                t.stateToAdvance.rule
        );
        return
                new Delta(
                        s,
                        inner,
                        fw,
                        // If this is a new completed state that is no unit production, make a note of it it because we want to recursively call *complete* on these states
                        ((s.rule.isPassive(s.ruleDotPosition)/*isCompleted*/
                                && !s.rule.isUnitProduction()
                                && !stateSets.contains(s)))

                );
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need it to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). ~This method does not guarantee a left most parse.~
     *
     * @param completedState Completed state to calculate Viterbi score for
     */
    @SuppressWarnings("WeakerAccess")
    public void computeViterbiScoresForCompletedState(
            final State completedState
    ) {
        if (stateSets.viterbiScores.get(completedState) == null)
            throw new IssueRequest("Expected Viterbi score to be set on completed state. This is a bug.");

        final double completedViterbi = stateSets.viterbiScores.get(completedState).getScore();
        final NonTerminal Yl = completedState.rule.left;
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        int completedPos = completedState.position;
        final Set<State> statesToAdvance = stateSets.activeStates.getStatesActiveOnNonTerminal(Yl, completedState.ruleStartPosition, completedPos);
        if (statesToAdvance != null && statesToAdvance.size() > 0) {
            Collection<ViterbiDelta> newStates = statesToAdvance.stream()
                    /* Safe to parallelize here */
                    .parallel()
                    .map((stateToAdvance) -> computeViterbiForState(completedState, completedViterbi, stateToAdvance))
                    .filter(d -> d != null)
                    .collect(Collectors.toSet());

            /* WARNING: shared mutable state mutated */
            newStates.forEach(stateSets::processDelta);

            // Recurse with new states that are completed
            newStates.stream()
                    .filter(ViterbiDelta::isNewCompletedState)
                    .map(d -> d.resultingState)
                    //recurse on newCompletedStates
                    .forEach(this::computeViterbiScoresForCompletedState);
        }

    }

    private ViterbiDelta computeViterbiForState(State completedState, double completedViterbi, State stateToAdvance) {
        final State resultingState = State.create(completedState.position, stateToAdvance.ruleStartPosition, stateToAdvance.advanceDot(), stateToAdvance.rule);
        if (stateToAdvance.position > resultingState.position || stateToAdvance.position != completedState.ruleStartPosition)
            throw new IssueRequest("Index failed. This is a bug.");
        final State.ViterbiScore newViterbiScore = getNewViterbiScore(completedState, completedViterbi, stateToAdvance, resultingState);
        boolean newViterbiIsBetter = newViterbiIsBetter(stateSets.viterbiScores.get(resultingState), newViterbiScore);


        final boolean isNewCompletedState = newViterbiIsBetter && resultingState.isCompleted();
        final boolean isNewState = !stateSets.contains(resultingState);
        return (isNewState || isNewCompletedState || newViterbiIsBetter)
                ? new ViterbiDelta(
                resultingState,
                isNewCompletedState,
                newViterbiIsBetter ? newViterbiScore : null,
                isNewState)
                : null;
    }

    private State.ViterbiScore getNewViterbiScore(State completedState, double completedViterbi, State stateToAdvance, State resultingState) {
        return new State.ViterbiScore(
                stateSets.grammar.semiring.times(
                        completedViterbi,
                        stateSets.viterbiScores.get(stateToAdvance).getScore() // must be set
                ),
                completedState,
                resultingState,
                stateSets.grammar.semiring
        );
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param i The index to make completions at.
     */
    void completeNoViterbi(
            final int i,
            final Grammar<T> grammar
    ) {
        final DeferredStateScoreComputations addForwardScores = new DeferredStateScoreComputations(grammar.semiring);
        final DeferredStateScoreComputations addInnerScores = new DeferredStateScoreComputations(grammar.semiring);

        completeNoViterbi(
                i,
                stateSets.completedStates.getCompletedStatesThatAreNotUnitProductions(i),
                addForwardScores,
                addInnerScores,
                stateSets
        );

        // Resolve and set forward & inner scores
        addForwardScores.states.forEach((s, score) ->
                stateSets.forwardScores.put(
                        stateSets.getOrCreate(s),
                        score.resolveFinal()
                )
        );

        addInnerScores.states.forEach((s, score) ->
                stateSets.innerScores.put(
                        stateSets.getOrCreate(s),
                        score.resolveFinal()
                )
        );
    }


    static class Delta {
        final State state;
        final Resolvable addForward;
        private final Resolvable addInner;
        private final boolean newCompletedStateNoUnitProduction;

        Delta(State state, Resolvable addInner, Resolvable addForward, boolean newCompletedStateNoUnitProduction) {
            this.state = state;
            this.addInner = addInner;
            this.addForward = addForward;
            this.newCompletedStateNoUnitProduction = newCompletedStateNoUnitProduction;
        }

        public State getState() {
            return state;
        }
    }

    public class ViterbiDelta {
        public final State resultingState;
        public final State.ViterbiScore newViterbiScore;
        public final boolean isNewState;
        private final boolean isNewCompletedState;

        ViterbiDelta(State resultingState, boolean isNewCompletedState, State.ViterbiScore newViterbiScore, boolean isNewState) {
            this.isNewCompletedState = isNewCompletedState;
            this.resultingState = resultingState;
            this.newViterbiScore = newViterbiScore;
            this.isNewState = isNewState;
        }

        boolean isNewCompletedState() {
            return isNewCompletedState;
        }

    }
}
