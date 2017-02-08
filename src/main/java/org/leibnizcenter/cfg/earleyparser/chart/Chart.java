package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.Complete;
import org.leibnizcenter.cfg.earleyparser.DeferredStateScoreComputations;
import org.leibnizcenter.cfg.earleyparser.Predict;
import org.leibnizcenter.cfg.earleyparser.Scan;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.ForwardScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.InnerScores;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.token.TokenWithCategories;
import org.leibnizcenter.cfg.util.MapEntry;
import org.leibnizcenter.cfg.util.StateInformationTriple;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * <p>
 * A chart produced by an Earley parser.
 * <p/>
 * <p>
 * Charts contain sets of {@link State states} mapped to the string indices where
 * they originate. Since the state sets are {@link Set sets}, an state can only
 * be added at a given index once (as sets do not permit duplicate members).
 * </p>
 */
public class Chart<T> {
    public final StateSets<T> stateSets;
    public final Grammar<T> grammar;
    private final ParseOptions<T> callbacks;

    private boolean parallelizeComplete = true;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart(Grammar<T> grammar) {
        this(grammar, null);
    }

    public Chart(Grammar<T> grammar, ParseOptions<T> callbacks) {
        this.stateSets = new StateSets<>(grammar);
        this.grammar = grammar;
        this.callbacks = callbacks;
    }

    /**
     * Function to calculate the new inner score from given values
     *
     * @param scanProbability The probability of scanning this particular token
     * @param sr              The semiring to calculate with
     * @param previousInner   The previous inner score
     * @return The inner score for the new state
     */

    private static double calculateInnerScore(double scanProbability, DblSemiring sr, double previousInner) {
        return Double.isNaN(scanProbability) ? previousInner : sr.times(previousInner, scanProbability);
    }

    /**
     * Function to compute the forward score for the new state after scanning the given token.
     *
     * @param scanProbability           The probability of scanning this particular token
     * @param sr                        The semiring to calculate with
     * @param previousStateForwardScore The previous forward score
     * @return Computed forward score for the new state
     */
    private static double calculateForwardScore(double scanProbability, DblSemiring sr, double previousStateForwardScore) {
        return Double.isNaN(scanProbability) ? previousStateForwardScore : sr.times(previousStateForwardScore, scanProbability);
    }

    private static boolean newViterbiIsBetter(State.ViterbiScore viterbiScore, State.ViterbiScore newViterbiScore) {
        return viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0;
    }

    private static <E> Complete.Delta completeNoViterbiForTriple(int position,
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
                new Complete.Delta(
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

    private Predict.Delta predictNextStateAndScores(int index, MapEntry<State, Rule> statePredecessor_Y_to_v) {
        final State statePredecessor = statePredecessor_Y_to_v.getKey();
        final Rule Y_to_v = statePredecessor_Y_to_v.getValue();

        final Category Z = statePredecessor.getActiveCategory();
        //noinspection SuspiciousNameCombination
        final Category Y = Y_to_v.left;

        final double prevForward = stateSets.forwardScores.get(statePredecessor);

        // γ' = P(Y → value)
        final double Y_to_vProbability = Y_to_v.getScore();

        // α' = α * R(Z =*L> Y) * P(Y → value)
        final double fw = grammar.semiring.times(prevForward, grammar.semiring.times(grammar.getLeftStarScore(Z, Y), Y_to_vProbability));

        State state = State.create(index, index, 0, Y_to_v);
        boolean isNew = !stateSets.contains(state);
        return new Predict.Delta(isNew, state, Y_to_vProbability, fw, statePredecessor);
    }

    public void predict(int i, TokenWithCategories<T> token) {
        Chart<T> chart = this;
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
    void predict(int index) {
        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...
        final Set<State> activeOnNonTerminals = stateSets.activeStates.getActiveOnNonTerminals(index);
        if (activeOnNonTerminals != null)
            // Copy set to avoid concurrent modification
            new HashSet<>(activeOnNonTerminals).stream().parallel()

                    // For all productions Y → value such that R(Z =*L> Y) is nonzero
                    .flatMap(grammar::streamNonZeroLeftStarRulesWithPrecedingState).parallel()

                    // we predict state <code>i: Y<sub>i</sub> → ·value</code>
                    .map(statePredecessor_Y_to_v -> predictNextStateAndScores(index, statePredecessor_Y_to_v))

                    // Now that we've calculated the scores, add to chart...
                    .sequential()
                    .forEach(stateSets::setScores);
    }

    public void scan(int i, TokenWithCategories<T> token) {
        final ScanProbability<T> scanProbability = callbacks != null ? callbacks.scanProbability : null;
        if (callbacks != null) callbacks.beforeScan(i, token, this);

        scan(
                i,
                token,
                scanProbability
        );

        if (callbacks != null) callbacks.onScan(i, token, this);
    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param tokenPosition       The start index of the scan.
     * @param tokenWithCategories The token that was scanned.
     * @param scanProbability     Function that provides the probability of scanning the given token at this position. Might be null for a probability of 1.0.
     */
    @SuppressWarnings("WeakerAccess")
    public void scan(
            final int tokenPosition,
            final TokenWithCategories<T> tokenWithCategories,
            final ScanProbability<T> scanProbability
    ) {
        if (tokenWithCategories == null)
            throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");

        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition, tokenWithCategories);
        final Token<T> token = tokenWithCategories.token;
        final ForwardScores forwardScores = stateSets.forwardScores;
        final InnerScores innerScores = stateSets.innerScores;
        final int nextPosition = tokenPosition + 1;

//        StateToXMap<State> checkNoNewStatesAreDoubles = new StateToXMap<>(10 + 100);

        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        tokenWithCategories.categories.stream()
                .parallel()
                .flatMap((final Terminal<T> terminalType) -> {
                    final Set<State> statesActiveOnTerminals = stateSets.activeStates.getActiveOn(tokenPosition, terminalType);
                    return statesActiveOnTerminals == null
                            ? Stream.empty()
                            : statesActiveOnTerminals.stream();
                })
                .parallel() // Parallellize for performance: everything we do in map does not mutate state
                .map(preScanState -> new Scan.Delta<>(
                        token,
                        preScanState,
                        calculateForwardScore(scanProb, grammar.semiring, forwardScores.get(preScanState)),
                        calculateInnerScore(scanProb, grammar.semiring, innerScores.get(preScanState)),
                        /* Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>. Note that this state is unique for each preScanState */
                        preScanState.rule, nextPosition, preScanState.ruleStartPosition, preScanState.advanceDot()
                ))

                // After we have calculated everything, we mutate the chart
                .sequential()
                .forEach(stateSets::createStateAndSetScores);
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
        if (parallelizeComplete) stream = stream.parallel();
        stream = stream.flatMap(stateSets.activeStates::streamAllStatesToAdvance);
        if (parallelizeComplete) stream = stream.parallel();

        List<Complete.Delta> deltas = stream
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
        for (Complete.Delta delta : deltas) {
            //todo these plus operation may be parallellized a little?
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

        final double completedViterbi = stateSets.viterbiScores.get(completedState).innerScore;
        final NonTerminal Yl = completedState.rule.left;
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        int completedPos = completedState.position;
        final Set<State> statesToAdvance = stateSets.activeStates.getStatesActiveOnNonTerminal(Yl, completedState.ruleStartPosition, completedPos);
        if (statesToAdvance != null && statesToAdvance.size() > 0) {
            Stream<State> stream = statesToAdvance.stream();
                    /* Safe to parallelize here */
            if (parallelizeComplete) stream = stream.parallel();
            Collection<Complete.ViterbiDelta> newStates = stream
                    .map((stateToAdvance) -> computeViterbiForState(completedState, completedViterbi, stateToAdvance))
                    .filter(d -> d != null)
                    .collect(Collectors.toSet());

            /* WARNING: shared mutable state mutated */
            newStates.forEach(stateSets::processDelta);

            // Recurse with new states that are completed
            newStates.stream()
                    .filter(Complete.ViterbiDelta::isNewCompletedState)
                    .map(d -> d.resultingState)
                    //recurse on newCompletedStates
                    .forEach(this::computeViterbiScoresForCompletedState);
        }

    }

    private Complete.ViterbiDelta computeViterbiForState(State completedState, double completedViterbi, State stateToAdvance) {
        final State resultingState = State.create(completedState.position, stateToAdvance.ruleStartPosition, stateToAdvance.advanceDot(), stateToAdvance.rule);
        if (stateToAdvance.position > resultingState.position || stateToAdvance.position != completedState.ruleStartPosition)
            throw new IssueRequest("Index failed. This is a bug.");
        final State.ViterbiScore newViterbiScore = getNewViterbiScore(completedState, completedViterbi, stateToAdvance, resultingState);
        boolean newViterbiIsBetter = newViterbiIsBetter(stateSets.viterbiScores.get(resultingState), newViterbiScore);


        final boolean isNewCompletedState = newViterbiIsBetter && resultingState.isCompleted();
        final boolean isNewState = !stateSets.contains(resultingState);
        return (isNewState || isNewCompletedState || newViterbiIsBetter)
                ? new Complete.ViterbiDelta(
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
                        stateSets.viterbiScores.get(stateToAdvance).innerScore // must be set
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
        ExpressionSemiring semiring = grammar.semiring;
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

    public void complete(int i, TokenWithCategories<T> token) {
        Chart<T> chart = this;

        if (callbacks != null) callbacks.beforeComplete(i, token, chart);


        final Set<State> completedStates = new HashSet<>(chart.stateSets.completedStates.getCompletedStates(i + 1));
        completeNoViterbi(i + 1);
        completedStates.forEach(this::computeViterbiScoresForCompletedState);

        if (callbacks != null) callbacks.onComplete(i, token, chart);
    }
}
