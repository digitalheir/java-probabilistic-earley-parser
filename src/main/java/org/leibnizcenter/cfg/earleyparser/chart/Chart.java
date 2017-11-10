package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.Resolvable;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.KleeneClosure;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.Complete;
import org.leibnizcenter.cfg.earleyparser.DeferredStateScoreComputations;
import org.leibnizcenter.cfg.earleyparser.Scan;
import org.leibnizcenter.cfg.earleyparser.callbacks.ParseOptions;
import org.leibnizcenter.cfg.earleyparser.callbacks.ScanProbability;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.chart.statesets.StateSets;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.grammar.Grammar;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.TokenWithCategories;
import org.leibnizcenter.cfg.util.StateInformationTriple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.leibnizcenter.cfg.errors.IssueRequest.ensure;
import static org.leibnizcenter.cfg.util.Collections2.emptyIfNull;


public class Chart<T> {
    public final StateSets<T> stateSets;
    public final Grammar<T> grammar;
    public final ParseOptions<T> parseOptions;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart(Grammar<T> grammar) {
        this(
                grammar,
                null
        );
    }

    public Chart(Grammar<T> grammar, ParseOptions<T> parseOptions
    ) {
        this.stateSets = new StateSets<>(grammar);
        this.grammar = grammar;
        this.parseOptions = parseOptions == null ? new ParseOptions.Builder<T>().build() : parseOptions;
    }

    private static boolean newViterbiIsBetter(State.ViterbiScore viterbiScore, double newViterbiScore) {
        return viterbiScore == null || viterbiScore.semiring.compare(viterbiScore.probabilityAsSemiringElement, newViterbiScore) < 0;
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
        final double unitStarScore = grammar.getUnitStarScore(Z, Yl);

        final Resolvable fw = grammar.semiring.times(unitStarScore, prevForward, t.completedInner);
        final Resolvable inner = grammar.semiring.times(unitStarScore, prevInner, t.completedInner);

        if (j != t.stateToAdvance.position) throw new IssueRequest("Index failed. This is a bug.");
        final State s = State.create(
                position,
                t.stateToAdvance.ruleStartPosition,
                t.stateToAdvance.advanceDot(),
                t.stateToAdvance.rule
        );
        return new Complete.Delta(
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


    public void predict(int i, TokenWithCategories<T> token) {
        Chart<T> chart = this;
        if (parseOptions != null) parseOptions.beforePredict(i, token, chart);

        predict(i);

        if (parseOptions != null) parseOptions.onPredict(i, token, chart);
    }

    /**
     * Makes predictions in the specified chart at the given index.
     * <p/>
     * For each state at position i, look at the the nonterminal at the dot position,
     * add a state that expands that nonterminal at position i, with the dot position at 0
     *
     * @param index The token index to make predictions at.
     */
    void predict(final int index) {
        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...
        final Set<State> activeOnNonTerminals = stateSets.activeStates.getActiveOnNonTerminals(index);
        if (activeOnNonTerminals != null && activeOnNonTerminals.size() > 0) {
            assert activeOnNonTerminals.stream()
                    .noneMatch(p -> p.position != index); // all on position == index
            // Copy set to avoid concurrent modification
            new HashSet<>(activeOnNonTerminals).forEach(this::predictStatesForState);
        }

        // Streamy:
//            (false ? activeOnNonTerminalsCp.parallelStream() : activeOnNonTerminalsCp.stream())
//                    // For all productions Y → v such that R(Z =*L> Y) is nonzero
//                    .flatMap(grammar::streamNonZeroLeftStarRulesWithPrecedingState)
//                    // we predict state <code>i: Y<sub>i</sub> → ·v</code>
//                    .map(statePredecessor_Y_to_v -> {
//                        final State statePredecessor = statePredecessor_Y_to_v.getKey();
//                        final Rule Y_to_v = statePredecessor_Y_to_v.getv();
//
//                        final Category Z = statePredecessor.getActiveCategory();
//                        //noinspection SuspiciousNameCombination
//                        final Category Y = Y_to_v.left;
//
//                        final double prevForward = stateSets.forwardScores.get(statePredecessor);
//
//                        // γ' = P(Y → v)
//                        final double Y_to_vProbability = Y_to_v.getScore();
//
//                        // α' = α * R(Z =*L> Y) * P(Y → v)
//                        final double fw = grammar.semiring.times(prevForward, grammar.semiring.times(grammar.getLeftStarScore(Z, Y), Y_to_vProbability));
//
//                        State state = State.create(index, index, 0, Y_to_v);
//                        boolean isNew = !stateSets.contains(state);
//                        return new Predict.Delta(isNew, state, Y_to_vProbability, fw, statePredecessor);
//                    })
//
//            // Now that we've calculated the scores, add to chart...
//                    .sequential()
//
//                    .forEach(stateSets::setScores);


    }

    public void predictError(Collection<State> justScannedErrors) {
        justScannedErrors.forEach(justScannedErrorState -> {
            final double prevForward = stateSets.forwardScores.get(justScannedErrorState);
            final double prevInner = stateSets.innerScores.get(justScannedErrorState);


            final State predictedState = State.create(
                    justScannedErrorState.position,
                    justScannedErrorState.ruleStartPosition,
                    justScannedErrorState.ruleDotPosition - 1,
                    justScannedErrorState.rule
            );

            //boolean isNewState =
            stateSets.addIfNew(predictedState);
            //todo
//            assert isNewState || (stateSets.innerScores.get(predicted) == ruleProbability || stateSets.innerScores.get(predicted) == grammar.semiring.zero());

            stateSets.setViterbiScore(new State.ViterbiScore(prevInner, justScannedErrorState, predictedState, grammar.semiring));
            stateSets.forwardScores.increment(predictedState, prevForward);
            stateSets.innerScores.put(predictedState, prevInner);
        });
    }


    private void predictStatesForState(State statePredecessor) {
        final Category Z = statePredecessor.getActiveCategory();
        // For all productions Y → v such that R(Z =*L> Y) is nonzero
        grammar.nonZeroLeftStartRules.get(Z).forEach(Y_to_v -> predictStatesForRule(
                statePredecessor,
                Z,
                Y_to_v
        ));
    }

    private void predictStatesForRule(State statePredecessor, Category activeOnPredecessor, Rule Y_to_v) {
        // we predict state <code>i: Y<sub>i</sub> → ·v</code>
        final double prevForward = stateSets.forwardScores.get(statePredecessor);

        // γ' = P(Y → v)
        final double Y_to_vProbability = Y_to_v.probabilityAsSemiringElement;

        // α' = α * R(Z =*L> Y) * P(Y → v)
        final double newForward = grammar.semiring.times(
                prevForward,
                grammar.getLeftStarScore(activeOnPredecessor, Y_to_v.left),
                Y_to_vProbability
        );

        State predicted = State.create(statePredecessor.position, statePredecessor.position, 0, Y_to_v);

        addPredictedStateToChart(statePredecessor, Y_to_vProbability, newForward, predicted);
    }

    public void addPredictedStateToChart(State statePredecessor, double inner, double forward, State predicted) {
//        boolean isNewState =
        stateSets.addIfNew(predicted);

        //todo
        //assert isNewState || (stateSets.innerScores.get(predicted) == inner || stateSets.innerScores.get(predicted) == grammar.semiring.zero());

        stateSets.setViterbiScore(new State.ViterbiScore(inner, statePredecessor, predicted, grammar.semiring));
        stateSets.forwardScores.increment(predicted, forward);
        stateSets.innerScores.put(predicted, inner);
    }

    public void scan(int i, TokenWithCategories<T> token) {
        final ScanProbability<T> scanProbability = parseOptions != null ? parseOptions.scanProbability : null;
        if (parseOptions != null) parseOptions.beforeScan(i, token, this);

        scan(i, token, scanProbability);

        if (parseOptions != null) parseOptions.onScan(i, token, this);
    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param chartPosition       The start index of the scan.
     * @param tokenWithCategories The token that was scanned.
     * @param scanProbability     Function that provides the probability of scanning the given token at this position. Might be null for a probability of 1.0.
     */
    @SuppressWarnings("WeakerAccess")
    void scan(
            final int chartPosition,
            final TokenWithCategories<T> tokenWithCategories,
            final ScanProbability<T> scanProbability
    ) {
        ensure(tokenWithCategories != null, "null token at chart index " + chartPosition + '.');
        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        final ExpressionSemiring semiring = grammar.semiring;
        for (Terminal<T> activeTerminalType : emptyIfNull(tokenWithCategories.categories)) {
            for (State preScanState : stateSets.activeStates.getActiveOn(chartPosition, activeTerminalType)) {
                final double scanProb = Scan.getScanProb(scanProbability, tokenWithCategories, chartPosition);

                final double previousForward = stateSets.forwardScores.get(preScanState);
                final double previousInner = stateSets.innerScores.get(preScanState);
                final boolean isKleeneContinuation = activeTerminalType instanceof KleeneClosure
                        && preScanState.position > (preScanState.ruleDotPosition + preScanState.ruleStartPosition);
                final double newInner = isKleeneContinuation
                        ? semiring.times(previousInner, preScanState.rule.probabilityAsSemiringElement)
                        : previousInner;
                // todo should we update forwardscore as well for continuations? yes:
                final double newForward = isKleeneContinuation
                        ? semiring.times(previousForward, preScanState.rule.probabilityAsSemiringElement)
                        : previousForward;
                final double postScanForward = Scan.calculateForwardScore(scanProb, semiring, newForward);
                final double postScanInner = Scan.calculateInnerScore(scanProb, semiring, newInner);

                // After we have calculated the delta, mutate the chart
                stateSets.createStateAndSetScores(
                        tokenWithCategories.token,
                        preScanState,
                        postScanForward,
                        postScanInner,
                        /* Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>. Note that this state is unique for each preScanState */
                        State.create(chartPosition + 1, preScanState.ruleStartPosition, preScanState.advanceDot(), preScanState.rule)
                );
            }
        }
    }

    double getScanProbability(int tokenPosition, TokenWithCategories<T> tokenWithCategories, ScanProbability<T> scanProbability) {
        return scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition, tokenWithCategories);
    }

    void scanError(
            final int tokenPosition,
            final TokenWithCategories<T> tokenWithCategories
    ) {
        if (tokenWithCategories == null)
            throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");
        /*
         * Get all states that are have just scanned an <error> token, advance them
         */
        final Collection<State> justScannedErrors = stateSets.activeStates.getJustScannedError(tokenPosition);
        if (justScannedErrors != null) justScannedErrors.forEach((State preScanState) ->
                stateSets.createStateAndSetScores(
                        // After we have calculated everything, we mutate the chart
                        tokenWithCategories.token,
                        preScanState,
                        stateSets.forwardScores.get(preScanState),
                        stateSets.innerScores.get(preScanState),
                    /* Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>. Note that this state is unique for each preScanState */
                        State.create(tokenPosition + 1, preScanState.ruleStartPosition, preScanState.ruleDotPosition, preScanState.rule)
                ));
    }

    /**
     * Completes states exhaustively and makes resolvable expressions for the forward and inner scores.
     * Note that these expressions can only be resolved to actual values after finishing completion, because they may depend on one another.
     *
     * @param position           State position
     * @param newCompletedStates Completed states to use for deducing what states to proceed
     * @param addForwardScores   Container / helper for adding to forward score expressions
     * @param addInnerScores     Container / helper for adding to inner score expressions
     *                           //     * @param completedStatesAlreadyHandled The completed states that we don't want to reiterate.
     *                           //     * @param computationsForward           Container for forward score expressions. Probably superfluous.
     *                           //     * @param computationsInner             Container for inner score expressions. Probably superfluous.
     */
    private void completeNoViterbi(final int position,
                                   Collection<State> newCompletedStates,
                                   final DeferredStateScoreComputations addForwardScores,
                                   final DeferredStateScoreComputations addInnerScores
    ) {
        if (newCompletedStates == null || newCompletedStates.size() <= 0)
            return;
        while (newCompletedStates.size() > 0) {
            newCompletedStates = newCompletedStates.stream()
                    // For all states
                    //      i: Y<sub>j</sub> → v·    [a",y"]
                    //      j: X<sub>k</suv> → l·Zm  [a',y']
                    //
                    //  such that the R*(Z =*> Y) is nonzero
                    //  and Y → v is not a unit production

                    // WARNING: shared mutated mutability
                    .map(completedState -> new StateInformationTriple(null,
                            completedState,
                            addInnerScores.getOrCreate(completedState, stateSets.innerScores.get(completedState))
                    ))
                    .flatMap(stateSets.activeStates::streamAllStatesToAdvance)
                    .map(stateInformation -> {
                                final double prevForward = stateSets.forwardScores.get(stateInformation.stateToAdvance);
                                return completeNoViterbiForTriple(
                                        position,
                                        addInnerScores.getOrCreate(stateInformation.stateToAdvance, stateSets.innerScores.get(stateInformation.stateToAdvance)),
                                        addForwardScores.getOrCreate(stateInformation.stateToAdvance, prevForward),
                                        stateSets,
                                        stateInformation
                                );
                            }
                    )
                    .peek(delta -> {
                        addForwardScores.plus(delta.state, delta.addForward);
                        addInnerScores.plus(delta.state, delta.addInner);
                    })
                    .filter(delta -> delta.newCompletedStateNoUnitProduction)
                    .map(Complete.Delta::getState)
                    /* Prepare next batch of new completed states; recurse until there are no more new completed states */
                    .collect(Collectors.toSet());

            newCompletedStates.forEach(stateSets::getOrCreate);
        }
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need it to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). ~This method does not guarantee a left most parse.~
     *
     * @param completedStates Completed state to calculate Viterbi score for
     */
    private void computeViterbiScoresForCompletedStates(Collection<State> completedStates) {
        while (completedStates.size() > 0)
            completedStates = completedStates.stream()
                    .map(completedState -> {
                        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
                        final Set<State> statesToAdvance = stateSets.activeStates.getStatesActiveOnNonTerminal(completedState.rule.left, completedState.ruleStartPosition, completedState.position);
                        if (statesToAdvance == null || statesToAdvance.size() <= 0) return null;
                        final State.ViterbiScore viterbiScore = ofNullable(stateSets.viterbiScores.get(completedState)).orElseThrow(() -> new IssueRequest("Expected Viterbi score to be set on completed state."));
                        return statesToAdvance.stream()
                                .map((stateToAdvance) -> computeViterbiForState(completedState,
                                        viterbiScore.probabilityAsSemiringElement,
                                        stateToAdvance))
                                .filter(Objects::nonNull)
                                .sequential()
                                .peek(o -> stateSets.processDelta((Complete.ViterbiDelta) o))
                                //recurse on newCompletedStates
                                .filter(Complete.ViterbiDelta::isNewCompletedState)
                                .map(d -> d.resultingState);
                    })
                    .filter(Objects::nonNull)
                    .flatMap(completedState -> completedState)
                    .collect(Collectors.toSet());
    }

    private Complete.ViterbiDelta computeViterbiForState(State completedState, double completedViterbi, State stateToAdvance) {
        final State resultingState = State.create(completedState.position, stateToAdvance.ruleStartPosition, stateToAdvance.advanceDot(), stateToAdvance.rule);
        if (stateToAdvance.position > resultingState.position || stateToAdvance.position != completedState.ruleStartPosition)
            throw new IssueRequest("Index failed. This is a bug.");
        final double oldViterbiScore = stateSets.viterbiScoresDbl.get(stateToAdvance);
        assert Double.isFinite(oldViterbiScore);
        double newViterbiScore = grammar.semiring.times(
                completedViterbi,
                oldViterbiScore // must be set
        );
        boolean newViterbiIsBetter = newViterbiIsBetter(stateSets.viterbiScores.get(resultingState), newViterbiScore);
        final State.ViterbiScore newViterbiScore_ = newViterbiIsBetter ? new State.ViterbiScore(
                newViterbiScore,
                completedState,
                resultingState,
                grammar.semiring
        ) : null;

        final boolean isNewCompletedState = newViterbiIsBetter && resultingState.isCompleted();
        final boolean isNewState = !stateSets.contains(resultingState);
        return (isNewState || isNewCompletedState || newViterbiIsBetter)
                ? new Complete.ViterbiDelta(
                resultingState,
                isNewCompletedState,
                newViterbiScore_,
                isNewState)
                : null;
    }


    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param i The index to make completions at.
     */
    private void completeNoViterbi(
            final int i
    ) {
        final DeferredStateScoreComputations addForwardScores = new DeferredStateScoreComputations(grammar);
        final DeferredStateScoreComputations addInnerScores = new DeferredStateScoreComputations(grammar);

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

        if (parseOptions != null) parseOptions.beforeComplete(i, token, chart);


        final Set<State> completedStates = new HashSet<>(chart.stateSets.completedStates.getCompletedStates(i + 1));
        completeNoViterbi(i + 1);
        computeViterbiScoresForCompletedStates(completedStates);

        if (parseOptions != null) parseOptions.onComplete(i, token, chart);
    }

    public int getJustCompletedErrorRulesCount(int index) {
        return stateSets.completedStates.getCompletedErrorRulesCount(index);
    }

}
