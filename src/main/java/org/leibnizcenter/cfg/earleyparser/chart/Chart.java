package org.leibnizcenter.cfg.earleyparser.chart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.expression.ScoreRefs;
import org.leibnizcenter.cfg.algebra.expression.AddableValue;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring.Value;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.state.ScannedTokenState;
import org.leibnizcenter.cfg.earleyparser.chart.state.State;
import org.leibnizcenter.cfg.earleyparser.parse.ScanProbability;
import org.leibnizcenter.cfg.errors.IssueRequest;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.token.Token;
import org.leibnizcenter.cfg.util.ArrayLists;

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
        List<State.StateWithScore> newStates = new ArrayList<>(); //used in stream

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
                            s = stateSets.create(
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
     * @param index The start index of the scan.
     * @param token The token that was scanned.
     */
    public <E> void scan(int index, Token<E> token, ScanProbability scanProbability) {
        if (token == null)
            throw new IssueRequest("null token at index " + index + ". This is a bug");

        // Get all states that are active on a terminal
        //noinspection unchecked
        stateSets.getStatesActiveOnTerminals(index).stream()
                // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
                .filter(state -> ((Terminal) state.getActiveCategory()).hasCategory(token))

                        // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                .forEach(prevState -> {
                            State newState = stateSets.getOrCreate(
                                    index + 1, prevState.getRuleStartPosition(),
                                    prevState.advanceDot(),
                                    prevState.getRule(),
                                    token
                            );

                            double fw = stateSets.getForwardScore(prevState);
                            DblSemiring sr = grammar.getSemiring();
                            if (scanProbability != null) fw = sr.times(fw, scanProbability.getProbability(index));
                            stateSets.setForwardScore(newState, fw);
//                            System.out.println("scan, set " + semiring.toProbability(fw));
                            double inner = stateSets.getInnerScore(prevState);
                            if (scanProbability != null) inner = sr.times(inner, scanProbability.getProbability(index));
                            stateSets.setInnerScore(newState, inner);
                            stateSets.setViterbiScore(new State.ViterbiScore(inner, prevState, newState, grammar.getSemiring()));
                        }
                );
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param i The index to make completions at.
     */
    public void completeNoViterbi(int i) {
        final AddableValue addForwardScores = new AddableValue(50, semiring);
        final AddableValue addInnerScores = new AddableValue(50, semiring);
        final ScoreRefs computationsInner = new ScoreRefs(50, semiring);
        final ScoreRefs computationsForward = new ScoreRefs(50, semiring);

        completeNoViterbi(i, stateSets.getCompletedStatesThatAreNotUnitProductions(i),
                new HashSet<>(),
                addForwardScores,
                addInnerScores,
                computationsForward,
                computationsInner
        );


        addForwardScores.getStates().forEach((rule, tIntObjectMapTIntObjectMap) -> {
            tIntObjectMapTIntObjectMap.forEachEntry((index, tIntDoubleMapTIntObjectMap) -> {
                tIntDoubleMapTIntObjectMap.forEachEntry((ruleStart, tIntDoubleMap) -> {
                    tIntDoubleMap.forEachEntry((dot, score) -> {
                        final State state = stateSets.getOrCreate(index, ruleStart, dot, rule);
//                        double prev = stateSets.getForwardScore(state);
//                        if (prev != semiring.zero())
//                            throw new IssueRequest("Expected forward score not to have been set yet");
//                        System.out.println(state+"a+"+score);
                        stateSets.setForwardScore(state, score.resolve());
                        return true;
                    });
                    return true;
                });
                return true;
            });
        });

        addInnerScores.getStates().forEach((rule, tIntObjectMapTIntObjectMap) -> {
            tIntObjectMapTIntObjectMap.forEachEntry((index, tIntDoubleMapTIntObjectMap) -> {
                tIntDoubleMapTIntObjectMap.forEachEntry((ruleStart, tIntDoubleMap) -> {
                    tIntDoubleMap.forEachEntry((dot, score) -> {
                        final State state = stateSets.getOrCreate(index, ruleStart, dot, rule);
//                        double prev = stateSets.getInnerScore(state);
//                        if (prev != semiring.zero())
//                            throw new IssueRequest("Expected inner score not to have been set yet");
                        //System.out.println(state+"y+"+score);
                        stateSets.setInnerScore(state, score.resolve());
                        return true;
                    });
                    return true;
                });
                return true;
            });
        });
    }

    private void completeNoViterbi(int i, Collection<State> states,
                                   /*TODO I think this Set is superfluous and can be deleted to make the algo a bit faster? (Note it was added a long time ago when I was still fleshing out the algorithm.)*/
                                   Set<State> completedStatesAlreadyHandled,
                                   AddableValue addForwardScores,
                                   AddableValue addInnerScores,
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
            completedStatesAlreadyHandled.add(completedState);
            final int j = completedState.getRuleStartPosition();
            final NonTerminal Y = completedState.getRule().getLeft();

            // TODO this one may not yet be completely added, so we need to resolve the actual value LATER
            //    2 seems fixed? remove "to do"?
            Value completedInner = addInnerScores.getOrCreate(completedState, stateSets.getInnerScore(completedState));

            //noinspection Convert2streamapi
            for (State stateToAdvance : stateSets.getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(j, Y)) {
                if (j != stateToAdvance.getPosition()) throw new IssueRequest("Index failed. This is a bug.");
                // Make i: X_k → lZ·m
                Value prevInner = computationsInner.getOrCreate(stateToAdvance, stateSets.getInnerScore(stateToAdvance));
                Value prevForward = computationsForward.getOrCreate(stateToAdvance, stateSets.getForwardScore(stateToAdvance));

                final Category Z = stateToAdvance.getActiveCategory();

                Value unitStarScore = semiring.dbl(grammar.getUnitStarScore(Z, Y));
                Value fw = unitStarScore.times(prevForward).times(completedInner);
                Value inner = unitStarScore.times(prevInner).times(completedInner);

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
                        i,
                        newStateRuleStart,
                        newStateDotPosition,
                        fw
                );

                // If this is a new completed state that is no unit production, make a note of it it because we want to recursively call *complete* on these states
                if (newStateRule.isPassive(newStateDotPosition)/*isCompleted?*/
                        && !newStateRule.isUnitProduction()
                        && stateSets.get(i, newStateRuleStart, newStateDotPosition, newStateRule) == null) {
                    if (possiblyNewStates == null) possiblyNewStates = new StateMap(20);
                    possiblyNewStates.add(
                            newStateRule,
                            i,
                            newStateRuleStart,
                            newStateDotPosition,
                            fw
                    );
                }

                addInnerScores.add(
                        newStateRule,
                        i,
                        newStateRuleStart,
                        newStateDotPosition,
                        inner
                );
            }
        }

        if (possiblyNewStates != null) {
            List<State> newCompletedStates = new ArrayList<>(possiblyNewStates.size());
            possiblyNewStates.states.forEach((rule, tIntObjectMapTIntObjectMap) ->
                    tIntObjectMapTIntObjectMap.forEachEntry((index, tIntDoubleMapTIntObjectMap) -> {
                        tIntDoubleMapTIntObjectMap.forEachEntry((ruleStart, tIntDoubleMap) -> {
                            tIntDoubleMap.forEachEntry((dot, score) -> {
                                boolean isnew = stateSets.get(index, ruleStart, dot, rule) == null;
                                final State state = stateSets.getOrCreate(index, ruleStart, dot, rule);
                                if (!isnew || !state.isCompleted() || state.rule.isUnitProduction())
                                    throw new IssueRequest("Unexpected state found in possible new states. This is a bug.");
                                if (!completedStatesAlreadyHandled.contains(state))
                                    newCompletedStates.add(state);
                                return true;
                            });
                            return true;
                        });
                        return true;
                    }));
            //noinspection ConstantConditions
            if (newCompletedStates != null && newCompletedStates.size() > 0) completeNoViterbi(i,
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
     * exactly because we need to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). This method does not guarantee a left most parse.
     *///TODO write tests
    public void setViterbiScores(State completedState, Set<State> originPathTo, DblSemiring sr) {
        List<State> newStates = null; // init as null to avoid arraylist creation
        List<State> newCompletedStates = null; // init as null to avoid arraylist creation

        if (stateSets.getViterbiScore(completedState) == null)
            throw new IssueRequest("Expected Viterbi score to be set on completed state. This is a bug.");

        double completedViterbi = stateSets.getViterbiScore(completedState).getScore();
//        System.out.println("" + completedState + " (" + sr.toProbability(completedViterbi) + ")");
        final NonTerminal Y = completedState.getRule().getLeft();
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        for (State stateToAdvance : stateSets.getStatesActiveOnNonTerminal(Y, completedState.getRuleStartPosition(), completedState.getPosition())) {
            if (stateToAdvance.getPosition() > completedState.getPosition() || stateToAdvance.getPosition() != completedState.getRuleStartPosition())
                throw new IssueRequest("Index failed. This is a bug.");
            State resultingState = stateSets.get(
                    completedState.getPosition(),
                    stateToAdvance.getRuleStartPosition(),
                    stateToAdvance.advanceDot(),
                    stateToAdvance.getRule()
            );
            if (resultingState == null) {
                resultingState = stateSets.create(
                        completedState.getPosition(),
                        stateToAdvance.getRuleStartPosition(),
                        stateToAdvance.advanceDot(),
                        stateToAdvance.getRule());
                newStates = ArrayLists.add(newStates, resultingState);
            }
            if (originPathTo.contains(resultingState)) {
                System.out.println("Already went past " + resultingState);
                return;
            }
            State.ViterbiScore viterbiScore = stateSets.getViterbiScore(resultingState);
            State.ViterbiScore prevViterbi = stateSets.getViterbiScore(stateToAdvance);
            double prev = prevViterbi != null ? prevViterbi.getScore() : semiring.zero();
            State.ViterbiScore newViterbiScore = new State.ViterbiScore(sr.times(completedViterbi, prev), completedState, resultingState, sr);
//                    System.out.println("-> " + resultingState + " (" + sr.toProbability(newViterbiScore.getExpression()) + ")");
            if (viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0) {
                stateSets.setViterbiScore(newViterbiScore);
                if (resultingState.isCompleted())
                    newCompletedStates = ArrayLists.add(newCompletedStates, resultingState);
                //                    } else {
//                        System.out.println("(dropped, this seems to be a cycle)");
            }

        }

        // Add new states to chart
        if (newStates != null) newStates.forEach(stateSets::add);

        // Recurse with new states that are completed
        if (newCompletedStates != null)
            newCompletedStates.stream().forEach(resultingState -> {
                Set<State> path = new HashSet<>(originPathTo);
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

    public Collection<State> getCompletedStates(int i, NonTerminal s) {
        Multimap<NonTerminal, State> m = stateSets.completedStatesFor.get(i);
        if (m != null && m.containsKey(s)) return m.get(s);
        return Collections.emptySet();
    }

    public State.ViterbiScore getViterbiScore(State s) {
        return stateSets.getViterbiScore(s);
    }


    @SuppressWarnings("WeakerAccess")
    public static class StateSets {
        private final StateIndex states = new StateIndex(500);

        private final TIntObjectHashMap<Set<State>> byIndex = new TIntObjectHashMap<>(500);

        /**
         * The forward probability <code>α_i</code> of a state is
         * the sum of the probabilities of
         * all constrained paths of length i that end in that state, do all
         * paths from start to position i. So this includes multiple
         * instances of the same history, which may happen because of recursion.
         */
        private final TObjectDoubleHashMap<State> forwardScores;

        /**
         * The inner probability <code>γ_{i}</code> of a state
         * is the sum of the probabilities of all
         * paths of length (i - k) that start at position k (the rule's start position),
         * and end at the current state and generate the input the input symbols up to k.
         * Note that this is conditional on the state happening at position k with
         * a certain non-terminal X
         */
        private final TObjectDoubleHashMap<State> innerScores;
        private final Map<State, State.ViterbiScore> viterbiScores = new HashMap<>(500);

        private final TIntObjectHashMap<Set<State>> completedStates = new TIntObjectHashMap<>(500);
        private final TIntObjectHashMap<Multimap<NonTerminal, State>> completedStatesFor = new TIntObjectHashMap<>(500);
        private final TIntObjectHashMap<Set<State>> completedStatesThatAreNotUnitProductions = new TIntObjectHashMap<>(500);
        private final TIntObjectHashMap<Set<State>> statesActiveOnNonTerminals = new TIntObjectHashMap<>(500);
        //        private final TIntObjectHashMap<Multimap<NonTerminal, State>> statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY = new TIntObjectHashMap<>(500);


        private final TIntObjectHashMap<Multimap<NonTerminal, State>> nonTerminalActiveAtIWithNonZeroUnitStarToY = new TIntObjectHashMap<>(500, 0.5F, -1);

        private final TIntObjectHashMap<Set<State>> statesActiveOnTerminals = new TIntObjectHashMap<>(500);
        private final DblSemiring semiring;
        private final Grammar grammar;
        private Map<NonTerminal, TIntObjectHashMap<Set<State>>> statesActiveOnNonTerminal = new HashMap<>(500);


        StateSets(Grammar grammar) {
            this.grammar = grammar;
            this.semiring = grammar.getSemiring();
            this.forwardScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
            this.innerScores = new TObjectDoubleHashMap<>(500, 0.5F, semiring.zero());
        }

        public Set<State> getCompletedStates(int index) {
            return getCompletedStates(index, true);
        }

        public Set<State> getCompletedStatesThatAreNotUnitProductions(int index) {
            return getCompletedStates(index, false);
        }

        public Set<State> getCompletedStates(int index, boolean allowUnitProductions) {
            if (allowUnitProductions) {
                if (!completedStates.containsKey(index)) completedStates.put(index, new HashSet<>());
                return completedStates.get(index);
            } else {
                if (!completedStatesThatAreNotUnitProductions.containsKey(index))
                    completedStatesThatAreNotUnitProductions.put(index, new HashSet<>());
                return completedStatesThatAreNotUnitProductions.get(index);
            }
        }

        public Collection<State> getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(int j, NonTerminal Y) {
            if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.contains(j)) return Collections.emptyList();
            else return nonTerminalActiveAtIWithNonZeroUnitStarToY.get(j).get(Y);
        }

//        public Collection<State> getStatesActiveOnNonTerminalWithNonZeroUnitStarScoreToY(int index, NonTerminal Y) {
//            if (!statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.containsKey(index))
//                statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.put(index, HashMultimap.create());
//            return statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.get(index).get(Y);
//        }


        public Set<State> getStatesActiveOnNonTerminal(NonTerminal y, int position, int beforeOrOnPosition) {
            // stateToAdvance.getPosition() <= beforeOrOnPosition;
            if (position <= beforeOrOnPosition) {
                TIntObjectHashMap<Set<State>> setTIntObjectHashMap = statesActiveOnNonTerminal.get(y);
                if (setTIntObjectHashMap != null && setTIntObjectHashMap.containsKey(position))
                    return setTIntObjectHashMap.get(position);
            }
            return Collections.emptySet();
        }

        public Set<State> getStatesActiveOnNonTerminals(int index) {
            if (!statesActiveOnNonTerminals.containsKey(index)) statesActiveOnNonTerminals.put(index, new HashSet<>());
            return statesActiveOnNonTerminals.get(index);
        }


        public Set<State> getStatesActiveOnTerminals(int index) {
            if (!statesActiveOnTerminals.containsKey(index)) statesActiveOnTerminals.put(index, new HashSet<>());
            return statesActiveOnTerminals.get(index);
        }

        /**
         * Default zero
         *
         * @param s state
         * @return forward score so far
         */
        public double getForwardScore(State s) {
            return forwardScores.get(s);
        }

        public State getOrCreate(int index, int ruleStart, int dotPosition, Rule rule) {
            return getOrCreate(index, ruleStart, dotPosition, rule, null);
        }

        public <E> State getOrCreate(int index, int ruleStart, int dotPosition, Rule rule, Token<E> token) {
            TIntObjectMap<State> dotToState = states.getDotToState(rule, index, ruleStart);
            if (!dotToState.containsKey(dotPosition)) {
                State state = create(index, ruleStart, dotPosition, rule, token);
                addState(dotToState, state);
            }
            return dotToState.get(dotPosition);
        }

        private void addState(TIntObjectMap<State> dotPositionToState, State state) {
            int index = state.getPosition();
            int dotPosition = state.getRuleDotPosition();
            add(byIndex, index, state);
            dotPositionToState.put(dotPosition, state);

            if (state.isCompleted()) {
                add(completedStates, index, state);
                if (!state.getRule().isUnitProduction()) add(completedStatesThatAreNotUnitProductions, index, state);
                addToCompletedStatesFor(state);
            }
            if (state.isActive()) {
                if (state.getActiveCategory() instanceof NonTerminal) {
                    addToStatesActiveOnNonTerminal(state);
                    add(statesActiveOnNonTerminals, index, state);

//                    Multimap<NonTerminal, State> mapp = statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.get(index);
//                    final Multimap<NonTerminal, State> map = mapp == null ? HashMultimap.create() //We expect this
//                            : mapp;
                    Collection<NonTerminal> scores = grammar.getUnitStar().getNonZeroNonTerminals(state.getActiveCategory());
                    scores.forEach(Y -> {
//                        map.put(Y, state);
                        if (!nonTerminalActiveAtIWithNonZeroUnitStarToY.containsKey(index))
                            nonTerminalActiveAtIWithNonZeroUnitStarToY.put(index, HashMultimap.create());
                        nonTerminalActiveAtIWithNonZeroUnitStarToY.get(index).put(Y, state);
                    });
//                    statesActiveOnNonTerminalsWithNonZeroUnitStarScoreToY.put(index, map);

                } else if (state.getActiveCategory() instanceof Terminal) add(statesActiveOnTerminals, index, state);
                else throw new IssueRequest("Neither Terminal nor NonToken...?");
            }
        }

        private void addToCompletedStatesFor(State state) {
            int index = state.getPosition();
            Multimap<NonTerminal, State> m = completedStatesFor.get(index);
            if (m == null) m = HashMultimap.create();
            m.put(state.getRule().getLeft(), state);
            completedStatesFor.putIfAbsent(index, m);
        }

        private void addToStatesActiveOnNonTerminal(State state) {
            int index = state.getPosition();
            final NonTerminal activeCategory = (NonTerminal) state.getActiveCategory();
            TIntObjectHashMap<Set<State>> mapForCategory = statesActiveOnNonTerminal.get(activeCategory);
            if (mapForCategory == null) mapForCategory = new TIntObjectHashMap<>(50, 0.5F, -1);
            Set<State> s = mapForCategory.get(index);
            if (s == null) {
                s = new HashSet<>();
                mapForCategory.put(index, s);
            }
            s.add(state);
            statesActiveOnNonTerminal.putIfAbsent(activeCategory, mapForCategory);
        }

        private void add(TIntObjectHashMap<Set<State>> states, int index, State state) {
            if (!states.containsKey(index)) states.put(index, new HashSet<>());
            states.get(index).add(state);
        }

        public void addForwardScore(State state, double increment) {
            forwardScores.put(state, semiring.plus(getForwardScore(state)/*default zero*/, increment));
        }

        public void addInnerScore(State state, double increment) {
            innerScores.put(state, semiring.plus(getInnerScore(state)/*default zero*/, increment));
        }

        public void setForwardScore(State s, double probability) {
            forwardScores.put(s, probability);
        }

        public void setInnerScore(State s, double probability) {
            innerScores.put(s, probability);
        }


        public void setViterbiScore(State.ViterbiScore v) {
            viterbiScores.put(v.getResultingState(), v);
        }

        /**
         * Default zero
         *
         * @param state State for which to get inner score
         * @return inner score so far
         */
        public double getInnerScore(State state) {
            return innerScores.get(state);
        }

        public Set<State> getStates(int index) {
            return byIndex.get(index);
        }

        public State.ViterbiScore getViterbiScore(State s) {
            return viterbiScores.get(s);
        }

        public void add(State state) {
            Rule rule = state.getRule();
            int ruleStart = state.getRuleStartPosition();
            int index = state.getPosition();

            TIntObjectMap<TIntObjectMap<State>> forRuleStart = states.getRuleStartToDotToState(rule, index);
            if (!forRuleStart.containsKey(ruleStart)) forRuleStart.put(ruleStart, new TIntObjectHashMap<>(50));
            TIntObjectMap<State> dotToState = forRuleStart.get(ruleStart);

            addState(dotToState, state);
        }

        public State get(int index, int ruleStart, int ruleDot, Rule rule) {
            return states.getState(rule, index, ruleStart, ruleDot);
        }

        public <E> State create(int index, int ruleStart, int dotPosition, Rule rule, Token<E> c) {
            if (c != null) return new ScannedTokenState<>(c, rule, ruleStart, index, dotPosition);
            else return new State(rule, ruleStart, index, dotPosition);
        }

        public State create(int index, int ruleStart, int dotPosition, Rule rule) {
            return create(index, ruleStart, dotPosition, rule, null);
        }

        public int countStates() {
            //noinspection unchecked
            return Arrays.stream(byIndex.values(new Set[byIndex.size()]))
                    .mapToInt(Set::size).sum();
        }

    }
}
