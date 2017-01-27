package org.leibnizcenter.cfg.earleyparser;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseCallbacks;
import org.leibnizcenter.cfg.earleyparser.chart.Chart;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.token.TokenWithCategories;
import org.leibnizcenter.cfg.util.StateInformationTriple;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Complete stage
 * <p>
 * Created by maarten on 31/10/16.
 */
public class Complete<T> {
    private final StateSets<T> stateSets;
    private final boolean parallelize;
    private final ExpressionSemiring semiring;

    /**
     */
    Complete(StateSets<T> stateSets, boolean parallelize) {
        this.stateSets = stateSets;
        this.semiring = stateSets.grammar.semiring;
        this.parallelize = parallelize;
    }

    private static boolean newViterbiIsBetter(State.ViterbiScore viterbiScore, State.ViterbiScore newViterbiScore) {
        return viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0;
    }

    private static <E> Delta completeNoViterbiForTriple(int position,
                                                        Resolvable prevInner,
                                                        Resolvable prevForward,
                                                        StateSets<E> stateSets,
                                                        StateInformationTriple t) {
        final int j = t.completedState.ruleStartPosition;
        final NonTerminal Yl = t.completedState.rule.left;

        // Make i: X_k → lZ·m
        final Category Z = t.stateToAdvance.getActiveCategory();
        final Grammar<E> grammar = stateSets.grammar;
        final Resolvable unitStarScore = grammar.getUnitStarScore(Z, Yl);

        final Resolvable fw = grammar.semiring.times(unitStarScore, prevForward, t.completedInner);
        final Resolvable inner = grammar.semiring.times(unitStarScore, prevInner, t.completedInner);

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
    private void completeNoViterbi(final int position,
                                   final Collection<State> states,
                                   final DeferredStateScoreComputations addForwardScores,
                                   final DeferredStateScoreComputations addInnerScores
    ) {
        if (states == null || states.size() <= 0) return;


        final DeferredStateScoreComputations newStates = new DeferredStateScoreComputations(stateSets.grammar.semiring);
        Stream<StateInformationTriple> stream = states.stream()
                // For all states
                //      i: Y<sub>j</sub> → value·    [a",y"]
                //      j: X<sub>k</suv> → l·Zm  [a',y']
                //
                //  such that the R*(Z =*> Y) is nonzero
                //  and Y → value is not a unit production

                // WARNING: shared mutated mutability
                .sequential()
                .map(completedState -> new StateInformationTriple(null, completedState, addInnerScores.getOrCreate(completedState, stateSets.innerScores.getAtom(completedState))));

        /* Safe to parallelize here */
        if (parallelize) stream = stream.parallel();
        stream = stream.flatMap(stateSets.activeStates::streamAllStatesToAdvance);
        if (parallelize) stream = stream.parallel();

        List<Delta> deltas = stream
                .map(stateInformation -> completeNoViterbiForTriple(
                        position,
                        addInnerScores.getOrCreate(stateInformation.stateToAdvance, stateSets.innerScores.getAtom(stateInformation.stateToAdvance)),
                        addForwardScores.getOrCreate(stateInformation.stateToAdvance, stateSets.forwardScores.getAtom(stateInformation.stateToAdvance)),
                        stateSets,
                        stateInformation
                        )
                )
                .collect(Collectors.toList());

                /* */
        Collection<State> newCompletedStates = null;
        for (Delta delta : deltas) {
            //todo these plus operation may be parallelized a little?
            addForwardScores.plus(delta.state, delta.addForward);
            addInnerScores.plus(delta.state, delta.addInner);

            if (delta.newCompletedStateNoUnitProduction) {
                newStates.addForward(delta);
                if (newCompletedStates == null) newCompletedStates = new HashSet<>(deltas.size());
                newCompletedStates.add(delta.getState());
            }
        }
        // recurse
        if (newCompletedStates != null && newCompletedStates.size() > 0) {
            newCompletedStates.forEach(stateSets::getOrCreate);
            completeNoViterbi(
                    position,
                    newCompletedStates,
                    addForwardScores,
                    addInnerScores
            );
        }
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
            Stream<State> stream = statesToAdvance.stream();
                    /* Safe to parallelize here */
            if (parallelize) stream = stream.parallel();
            Collection<ViterbiDelta> newStates = stream
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
            final int i
    ) {
        final DeferredStateScoreComputations addForwardScores = new DeferredStateScoreComputations(semiring);
        final DeferredStateScoreComputations addInnerScores = new DeferredStateScoreComputations(semiring);

        completeNoViterbi(
                i,
                stateSets.completedStates.getCompletedStatesThatAreNotUnitProductions(i),
                addForwardScores,
                addInnerScores
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

    void complete(ParseCallbacks<T> callbacks, Chart<T> chart, int i, TokenWithCategories<T> token) {
        if (callbacks != null) callbacks.beforeComplete(i, token, chart);


        final Set<State> completedStates = new HashSet<>(chart.stateSets.completedStates.getCompletedStates(i + 1));
        completeNoViterbi(i + 1);
        completedStates.forEach(this::computeViterbiScoresForCompletedState);

        if (callbacks != null) callbacks.onComplete(i, token, chart);
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
