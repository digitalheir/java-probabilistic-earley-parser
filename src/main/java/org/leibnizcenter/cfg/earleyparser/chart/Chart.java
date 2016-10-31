package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.expression.ScoreRefs;
import org.leibnizcenter.cfg.algebra.expression.AddableValuesContainer;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring.Value;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;


/**
 * A chart produced by an  Earley parser.
 * <p/>
 * Charts contain sets of {@link State states} mapped to the string indices where
 * they originate. Since the state sets are {@link Set sets}, an state can only
 * be added at a given index once (as sets do not permit duplicate members).
 * State sets are not guaranteed to maintain states in their order of insertion.
 */
public class Chart {
    public final StateSets stateSets;
    private final Grammar grammar;
    private final ExpressionSemiring semiring;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart(Grammar grammar) {
        this(grammar, new StateSets(grammar));
    }

    /**
     * Creates a new chart from the specified sorted map of indices mapped to
     * state sets.
     *
     * @param stateSets The map of integer-mapped state sets to use as this
     *                  chart's backing data structure.
     */
    private Chart(Grammar grammar, StateSets stateSets) {
        this.stateSets = stateSets;
        this.grammar = grammar;
        this.semiring = grammar.getSemiring();
    }

    /**
     * Makes predictions in the specified chart at the given index.
     * <p/>
     * For each state at position i, look at the the nonterminal at the dot position,
     * add a state that expands that nonterminal at position i, with the dot position at 0
     *
     * @param index The string index to make predictions at.
     */
    public void predict(int index) {
        DblSemiring sr = grammar.getSemiring();

        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...
        Collection<State> statesToPredictOn = stateSets.getStatesActiveOnNonTerminals(index);

        Collection<State.StateWithScore> newStates = new HashSet<>();

        for (State statePredecessor : statesToPredictOn) {
            final Category Z = statePredecessor.getActiveCategory();
            double prevForward = stateSets.getForwardScore(statePredecessor);

            // For all productions Y → v such that R(Z =*L> Y) is nonzero
            grammar.getLeftStarCorners()
                    .getNonZeroScores(Z).stream()
                    .flatMap(Y -> grammar.getRules(Y).stream()) // ?
                    // we predict state <code>i: Y<sub>i</sub> → ·v</code>
                    .forEach(Y_to_v -> {
                        Category Y = Y_to_v.getLeft();
                        State s = stateSets.get(index, index, 0, Y_to_v); // We might want to increment the probability of an existing state
                        // γ' = P(Y → v)
                        final double Y_to_vProbability = Y_to_v.getScore();

                        // α' = α * R(Z =*L> Y) * P(Y → v)
                        final double fw = sr.times(prevForward, grammar.getLeftStarScore(Z, Y), Y_to_vProbability);

                        double innerScore = stateSets.getInnerScore(s);
                        if (!(Y_to_vProbability == innerScore || sr.zero() == innerScore))
                            throw new Error(Y_to_vProbability + " != " + innerScore);

//                        System.out.println("predict, add " + semiring.toProbability(fw));
                        if (s != null) {
                            stateSets.addForwardScore(s, fw);
                            stateSets.setInnerScore(s, Y_to_vProbability);
                            stateSets.setViterbiScore(new State.ViterbiScore(Y_to_vProbability, statePredecessor, s, grammar.getSemiring()));
                        } else {
                            s = State.create(
                                    index,
                                    index,
                                    0,
                                    Y_to_v);
                            stateSets.setViterbiScore(new State.ViterbiScore(Y_to_vProbability, statePredecessor, s, grammar.getSemiring()));
                            newStates.add(new State.StateWithScore(s, fw, Y_to_vProbability, null));
                        }
                    });
        }

        newStates.forEach(ss -> {
            final State state = ss.getState();
            stateSets.add(state);
            stateSets.addForwardScore(state, ss.getForwardScore());
            stateSets.setInnerScore(state, ss.getInnerScore());
        });
    }

    @SuppressWarnings("unused")
    public void scan(int index, Token token) {
        scan(index, token, null);
    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param tokenPosition   The start index of the scan.
     * @param token           The token that was scanned.
     * @param scanProbability Function that provides the probability of scanning the given token at this position. Might be null for a probability of 1.0.
     */
    public <E> void scan(final int tokenPosition, final Token<E> token, final ScanProbability scanProbability) {


        if (token == null) throw new IssueRequest("null token at index " + tokenPosition + ". This is a bug");

        final double scanProb = scanProbability == null ? Double.NaN : scanProbability.getProbability(tokenPosition);
        final DblSemiring sr = grammar.getSemiring();
        /*
         * Get all states that are active on a terminal
         *   O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
         */
        // noinspection unchecked
        stateSets.getStatesActiveOnTerminals(tokenPosition).stream()//.parallel()
                // TODO can this be more efficient, ie have tokens make their category be explicit? (Do we want to maintain the possibility of such "fluid" categories?)
                //.sequential()
                .filter(state -> ((Terminal) state.getActiveCategory()).hasCategory(token))
                // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                .forEach(preScanState -> {
                    /*
                     * All these methods are synchronized
                     */
                            final double preScanForward = stateSets.getForwardScore(preScanState);
                            final double preScanInner = stateSets.getInnerScore(preScanState);
                            // Note that this state is unique for each preScanState
                            final State postScanState = stateSets.getOrCreate(
                                    tokenPosition + 1, preScanState.getRuleStartPosition(),
                                    preScanState.advanceDot(),
                                    preScanState.getRule(),
                                    token
                            );

                            // Set forward score //synchronized
                            stateSets.setForwardScore(
                                    postScanState,
                                    calculateForwardScore(scanProb, sr, preScanForward)
                            );

                            // Get inner score (no side effects)
                            final double postScanInner = calculateInnerScore(scanProb, sr, preScanInner);
                            // Set inner score //synchronized
                            stateSets.setInnerScore(
                                    postScanState,
                                    postScanInner
                            );

                            // Set Viterbi score//synchronized
                            stateSets.setViterbiScore(new State.ViterbiScore(postScanInner, preScanState, postScanState, sr));
                        }
                );
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
        if (Double.isNaN(scanProbability)) return previousInner;
        else return sr.times(previousInner, scanProbability);
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
        if (Double.isNaN(scanProbability)) return previousStateForwardScore;
        else return sr.times(previousStateForwardScore, scanProbability);
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param i The index to make completions at.
     */
    public void completeNoViterbi(int i) {
        final AddableValuesContainer addForwardScores = new AddableValuesContainer(50, semiring);
        final AddableValuesContainer addInnerScores = new AddableValuesContainer(50, semiring);
//        final ScoreRefs computationsInner = new ScoreRefs(1, semiring);
//        final ScoreRefs computationsForward = new ScoreRefs(1, semiring);

        completeNoViterbi(
                i,
                stateSets.getCompletedStatesThatAreNotUnitProductions(i),
                null,//new HashSet<>(),
                addForwardScores,
                addInnerScores,
                null,
                null
//                computationsForward,
//                computationsInner
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
     * Completes states exhaustively and makes resolvable expressions for the forward and inner scores.
     * Note that these expressions can only be resolved to actual values after finishing completion, because they may depend on one another.
     *
     * @param position                      State position
     * @param states                        Completed states to use for deducing what states to proceed
     * @param completedStatesAlreadyHandled The completed states that we don't want to reiterate.
     * @param addForwardScores              Container / helper for adding to forward score expressions
     * @param addInnerScores                Container / helper for adding to inner score expressions
     * @param computationsForward           Container for forward score expressions. TODO Probably superfluous.
     * @param computationsInner             Container for inner score expressions. TODO Probably superfluous.
     */
    private void completeNoViterbi(int position,
                                   Collection<State> states,
                                   Set<State> completedStatesAlreadyHandled,
                                   AddableValuesContainer addForwardScores,
                                   AddableValuesContainer addInnerScores,
                                   ScoreRefs computationsForward,
                                   ScoreRefs computationsInner
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

            Value unresolvedCompletedInner = addInnerScores.getOrCreate(completedState, stateSets.getInnerScore(completedState));

            //noinspection Convert2streamapi
            for (State stateToAdvance : stateSets.getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(j, Y)) {
                if (j != stateToAdvance.getPosition()) throw new IssueRequest("Index failed. This is a bug.");
                // Make i: X_k → lZ·m
//                Value prevInner = computationsForward.getOrCreate(stateToAdvance, stateSets.getInnerScore(stateToAdvance));
                Value prevInner = addInnerScores.getOrCreate(stateToAdvance, stateSets.getInnerScore(stateToAdvance));
//                Value prevForward = computationsInner.getOrCreate(stateToAdvance, stateSets.getForwardScore(stateToAdvance));
                Value prevForward = addForwardScores.getOrCreate(stateToAdvance, stateSets.getForwardScore(stateToAdvance));

                final Category Z = stateToAdvance.getActiveCategory();

                Value unitStarScore = semiring.dbl(grammar.getUnitStarScore(Z, Y));
                Value fw = unitStarScore.times(prevForward).times(unresolvedCompletedInner);
                Value inner = unitStarScore.times(prevInner).times(unresolvedCompletedInner);

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
                    completedStatesAlreadyHandled,
                    addForwardScores,
                    addInnerScores,
                    computationsForward,
                    computationsInner);
//        }
        }
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need it to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). ~This method does not guarantee a left most parse.~
     *
     * @param completedState Completed state to calculate Viterbi score for
     * @param originPathTo
     * @param sr             Semiring to use for calculating
     *///TODO write tests
    public void setViterbiScores(final State completedState, final Set<State> originPathTo, final DblSemiring sr) {
        Collection<State> newStates = null; // init as null to avoid arraylist creation
        Collection<State> newCompletedStates = null; // init as null to avoid arraylist creation

        if (stateSets.getViterbiScore(completedState) == null)
            throw new IssueRequest("Expected Viterbi score to be set on completed state. This is a bug.");

        final double completedViterbi = stateSets.getViterbiScore(completedState).getScore();
        // System.out.println("" + completedState + " (" + sr.toProbability(completedViterbi) + ")");
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
            final double prev = prevViterbi != null ? prevViterbi.getScore() : semiring.zero();
            final State.ViterbiScore newViterbiScore = new State.ViterbiScore(sr.times(completedViterbi, prev), completedState, resultingState, sr);

            // System.out.println("-> " + resultingState + " (" + sr.toProbability(newViterbiScore.getExpression()) + ")");
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
            setViterbiScores(resultingState, path, sr);
        });
    }

//    /**
//     * Gets the last index in this chart that contains states.
//     *
//     * @return The maximal member of {@link #indices()}.
//     */
//    public int lastIndex() {
//        return stateSets.lastKey();
//    }
//
//    /**
//     * Gets a sub chart of this chart.
//     *
//     * @param from The low endpoint (inclusive) of the sub chart.
//     * @param to   The high endpoint (exclusive) of the subchart.
//     * @return A new chart containing only the state sets in this chart where
//     * <code>from &lt;= index &lt; to</code>.
//     * @throws NullPointerException If either <code>from</code> or
//     *                              <code>to</code> is <code>null</code>.
//     * @see java.util.SortedMap#subMap(Object, Object)
//     */
//    public Chart subChart(int from, int to) {
//        return new Chart(grammar, stateSets.subMap(from, to));
//    }
//
//    /**
//     * Gets a head chart of this chart (a chart containing only the indices
//     * from <code>0</code> to <code>to</code>).
//     *
//     * @param to The high endpoint (exclusive) of the new chart.
//     * @return A chart containing all the indices strictly less than
//     * <code>to</code>.
//     * @see java.util.SortedMap#headMap(Object)
//     */
//    public Chart headChart(int to) {
//        return new Chart(grammar,stateSets.headMap(to));
//    }
//
//    /**
//     * Gets a tail chart of this chart (a chart containing only the indices
//     * from <code>from</code> to the size of its {@link #indices()}).
//     *
//     * @param from The low endpoint (inclusive) of the new chart.
//     * @return A chart containing all the indices greater than or equal to
//     * <code>from</code>.
//     * @see java.util.SortedMap#tailMap(Object)
//     */
//    public Chart tailChart(int from) {
//        return new Chart(grammar, stateSets.tailMap(from));
//    }
//
//    /**
//     * Tests whether this chart contains the specified state.
//     *
//     * @param state The state to test whether this chart contains.
//     * @return true iff this chart contains the specified state at some index.
//     */
//    public boolean contains(State state) {
//        return !(NULL_INDEX.equals(indexOf(state)));
//    }
//
//    /**
//     * Gets the {@link #indices() index} of the specified state in this
//     * chart.
//     *
//     * @param state The state to find the index of.
//     * @return The index of the specified state, or <code>-1</code> if the
//     * specified state is <code>null</code> or is not contained in this chart.
//     */
//    public Integer indexOf(State state) {
////        if (state != null) {
////            for (Map.Entry<Integer, Set<State>> entry : stateSets.entrySet()) {
////                if (entry.getValue().contains(state)) {
////                    return entry.getKey();
////                }
////            }
////        }
//        state.getPosition()
//    }
//
//    /**
//     * Removes all states from this map at all indices (if any are present).
//     */
//    public void clear() {
//        stateSets.clear();
//    }
//
//    /**
//     * Tests whether this chart contains any states at any index.
//     *
//     * @return <code>true</code> if an state is present at some index,
//     * <code>false</code> otherwise.
//     */
//    public boolean isEmpty() {
//        return stateSets.isEmpty();
//    }
//
//
//

    /**
     * Counts the total number of states contained in this chart, at any
     * index.
     *
     * @return The total number of states contained.
     */
    @SuppressWarnings("unused")
    public int countStates() {
        return stateSets.countStates();
    }
//
//    /**
//     * Gets the states in this chart at a given index.
//     *
//     * @param index The index to return states for.
//     * @return The {@link Set set} of {@link State states} this chart contains
//     * at <code>index</code>, or <code>null</code> if no state set exists in
//     * this chart for the given index. The state set returned by this
//     * method is <em>not</em> guaranteed to contain the states in the order in
//     * which they were added. This method
//     * returns a set of states that is not modifiable.
//     * @throws NullPointerException If <code>index</code> is <code>null</code>.
//     * @see java.util.Collections#unmodifiableSet(Set)
//     */
//    public Map<State, State.Score> getStates(int index) {
//        return stateSets.getAtIndex(index);
//    }


    /**
     * Tests whether this chart is equal to another by comparing their
     * internal data structures.
     *
     * @return <code>true</code> iff the specified object is an instance of
     * <code>Chart</code> and it contains the same states at the same indices
     * as this chart.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Chart && stateSets.equals(((Chart) obj).stateSets));
    }

    /**
     * Computes a hash code for this chart based on its internal data
     * structure.
     */
    @Override
    public int hashCode() {
        return 37 * (1 + stateSets.hashCode());
    }

    /**
     * Gets a string representation of this chart.
     */
    @Override
    public String toString() {
        return stateSets.toString();
    }

    public void addState(int index, State state, double forward, double inner) {
        stateSets.getOrCreate(index, state.getRuleStartPosition(), state.getRuleDotPosition(), state.getRule());
        stateSets.setInnerScore(state, inner);
        stateSets.setForwardScore(state, forward);
        if (stateSets.getViterbiScore(state) == null)
            stateSets.setViterbiScore(new State.ViterbiScore(grammar.getSemiring().one(), null, state, grammar.getSemiring()));
    }

    @SuppressWarnings("unused")
    public Set<State> getStates(int index) {
        return stateSets.getStates(index);
    }

    public double getForwardScore(State s) {
        return stateSets.getForwardScore(s);
    }

    @SuppressWarnings("unused")
    public double getInnerScore(State s) {
        return stateSets.getInnerScore(s);
    }


    public State.ViterbiScore getViterbiScore(State s) {
        return stateSets.getViterbiScore(s);
    }


}
