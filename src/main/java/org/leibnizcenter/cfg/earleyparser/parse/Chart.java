
package org.leibnizcenter.cfg.earleyparser.parse;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.ScannedTokenState;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A chart produced by an  Earley parser.
 * <p/>
 * Charts contain sets of {@link State states} mapped to the string indices where
 * they originate. Since the state sets are {@link Set sets}, an state can only
 * be added at a given index once (as sets do not permit duplicate members).
 * State sets are not guaranteed to maintain states in their order of insertion.
 */
public class Chart {
    //private static final Integer NULL_INDEX = -1;
//    /**
//     * Creates a new chart based on the specified chart. The newly created
//     * chart contains all the states as the specified chart at all the same
//     * indices.
//     *
//     * @param chart The chart to base the newly created chart upon.
//     */
//    public Chart(Chart chart) {
//        this(chart.grammar, chart.stateSets.copy());
//    }

    public final StateSets stateSets;
    private final Grammar grammar;

    /**
     * Creates a new chart, initializing its internal data structure.
     */
    public Chart(Grammar grammar) {
        this(grammar, new StateSets(grammar.getSemiring()));
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
        List<State.StateWithScore> newStates = new ArrayList<>();

        for (State statePredecessor : statesToPredictOn) {
            final Category Z = statePredecessor.getActiveCategory();
            double prevForward = stateSets.getForwardScore(statePredecessor);

            // For all productions Y → v such that R(Z =*L> Y) is nonzero
            grammar.getLeftStarCorners().getNonZeroScores(Z).stream()
                    .flatMap(Y -> grammar.getRules(Y).stream())
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
        if (token == null) throw new Error("null token at index " + index);//TODO errors?

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

                            double inner = stateSets.getInnerScore(prevState);
                            if (scanProbability != null) inner = sr.times(inner, scanProbability.getProbability(index));
                            stateSets.setInnerScore(newState, inner);
                            stateSets.setViterbiScore(new State.ViterbiScore(inner, prevState, newState, grammar.getSemiring()));
                        }
                );
    }

    /**
     * Makes completions in the specified chart at the given index. Not appropriate for finding a Viterbi path.
     *
     * @param index The index to make completions at.
     */
    public void completeTruncated(int index) {
        final DblSemiring sr = grammar.getSemiring();
        final Set<State> completedStates = new HashSet<>(stateSets.getCompletedStates(index));
        completeTruncated(index, sr, completedStates.stream().map(state ->
                new State.StateWithScore(state, stateSets.getForwardScore(state), stateSets.getInnerScore(state), null)
        ));
    }

    private void completeTruncated(int index, DblSemiring sr, Stream<State.StateWithScore> completedStates) {
        final List<State.StateWithScore> newStates = new ArrayList<>();
        final List<State.StateWithScore> incrementScoresForStates = new ArrayList<>();
        completedStates
                // O(|stateset(i)|) = O(|grammar|): For all states <code>i: Y<sub>k</sub> → μ·</code>, such that the production is not a unit production
                .filter(completedState ->
                        !completedState.getState().getRule().isUnitProduction())
                .forEach(completedState -> {
                            double completedInner = completedState.getInnerScore();
                            final NonTerminal Y = completedState.getState().getRule().getLeft();
                            //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Zμ</code>, where Z =*> Y is non-zero
                            stateSets.getStatesActiveOnNonTerminal().stream()
                                    .filter(stateToAdvance -> stateToAdvance.getPosition() == completedState.getState().getRuleStartPosition())
                                    .forEach(stateToAdvance -> {
                                        final Category Z = stateToAdvance.getActiveCategory();
                                        final double unitStarScore = grammar.getUnitStarScore(Z, Y);
                                        if (unitStarScore == sr.zero()) return;

                                        double prevForward = stateSets.getForwardScore(stateToAdvance);
                                        double prevInner = stateSets.getInnerScore(stateToAdvance);

//                                        State.ScoreRef scoreRefs = new State.ScoreRef(sr);
                                        final double fw = sr.times(prevForward, completedInner, unitStarScore);
                                        final double inner = sr.times(prevInner, completedInner, unitStarScore);
                                        State existingState = stateSets.get(
                                                index,
                                                stateToAdvance.getRuleStartPosition(),
                                                stateToAdvance.advanceDot(),
                                                stateToAdvance.getRule()
                                        );
//                                        System.out.println("-----");
//                                        System.out.println(completedState.getState());
//                                        System.out.println(stateToAdvance);
//                                        System.out.println("->");
//                                        System.out.println(stateSets.create(index,
//                                                stateToAdvance.getRuleStartPosition(),
//                                                stateToAdvance.advanceDot(),
//                                                stateToAdvance.getRule()));
//                                        System.out.println("-----");
                                        if (existingState != null)
                                            incrementScoresForStates.add(new State.StateWithScore(existingState, fw, inner, null));
                                        else {
                                            // TODO error?
                                            //System.out.println("State did not exist");
                                            State newState = stateSets.create(index,
                                                    stateToAdvance.getRuleStartPosition(),
                                                    stateToAdvance.advanceDot(),
                                                    stateToAdvance.getRule());
                                            final State.StateWithScore sws = new State.StateWithScore(newState, fw, inner, null);
                                            newStates.add(sws);
                                            incrementScoresForStates.add(sws);
                                        }
                                    });
                        }
                );
        newStates.forEach(ss -> stateSets.add(ss.getState()));
        incrementScoresForStates.forEach(ss -> {
            final State state = ss.getState();
            stateSets.addForwardScore(state, ss.getForwardScore());
            stateSets.addInnerScore(state, ss.getInnerScore());
        });

        if (incrementScoresForStates.size() > 0) {
            completeTruncated(
                    index,
                    sr,
                    incrementScoresForStates.stream()
                            .filter(s ->
                                    s.getState().isCompleted())
            );
        }
    }

    /**
     * For finding the Viterbi path, we can't conflate production recursions (ie can't use the left star corner),
     * exactly because we need to find the unique Viterbi path.
     * Luckily, we can avoid looping over unit productions because it only ever lowers probability
     * (assuming p = [0,1] and Occam's razor). This method does not guarantee a left most parse.
     */
    public void setViterbiScores(State completedState, Set<State> originPathTo, DblSemiring sr) {
        // = stateSets.getCompletedStates(index);
        List<State> newStates = new ArrayList<>();
        List<State> newResultingStates = new ArrayList<>();

        if (stateSets.getViterbiScore(completedState) == null)
            throw new Error("Expected Viterbi score to be set on completed state. This is an error.");

        double completedViterbi = stateSets.getViterbiScore(completedState).getScore();
//        System.out.println("" + completedState + " (" + sr.toProbability(completedViterbi) + ")");
        final NonTerminal Y = completedState.getRule().getLeft();
        //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Yμ</code>
        stateSets.getStatesActiveOnNonTerminal(Y).stream()
                .filter(stateToAdvance ->
                        completedState.getRuleStartPosition() == stateToAdvance.getPosition() &&
                                stateToAdvance.getPosition() <= completedState.getPosition())
                .forEach(stateToAdvance -> {
                    double prevViterbi = stateSets.getViterbiScore(stateToAdvance).getScore();

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
                        newStates.add(resultingState);
                    }
                    if (originPathTo.contains(resultingState)) {
                        System.out.println("Already went past " + resultingState);
                        return;
                    }
                    State.ViterbiScore viterbiScore = stateSets.getViterbiScore(resultingState);
                    State.ViterbiScore newViterbiScore = new State.ViterbiScore(sr.times(completedViterbi, prevViterbi), completedState, resultingState, sr);
//                    System.out.println("-> " + resultingState + " (" + sr.toProbability(newViterbiScore.getScore()) + ")");
                    if (viterbiScore == null || viterbiScore.compareTo(newViterbiScore) < 0) {
                        stateSets.setViterbiScore(newViterbiScore);
                        newResultingStates.add(resultingState);
//                    } else {
//                        System.out.println("(dropped, this seems to be a cycle)");
                    }
                });

        newStates.forEach(stateSets::add);
        newResultingStates.stream().filter(State::isCompleted).forEach(resultingState -> {
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

    public Set<State> getStates(int index) {
        return stateSets.getStates(index);
    }

    public double getForwardScore(State s) {
        return stateSets.getForwardScore(s);
    }

    public double getInnerScore(State s) {
        return stateSets.getInnerScore(s);
    }

    public Set<State> getCompletedStates(int i, NonTerminal s) {
        return stateSets.getCompletedStates(i).stream()
                .filter(state -> state.getRule().getLeft().equals(s))
                .collect(Collectors.toSet());
    }

    public State.ViterbiScore getViterbiScore(State s) {
        return stateSets.getViterbiScore(s);
    }


    public static class StateSets {
        private static final TIntObjectMap<TIntObjectMap<State>> EMPTY = new TIntObjectHashMap<>();

        private final Map<Rule,
                /*index*/
                TIntObjectMap<
                        /*rule start*/
                        TIntObjectMap<
                                /*dot position*/
                                TIntObjectMap<
                                        State
                                        >
                                >
                        >
                > states = new HashMap<>(500);

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
        private final TIntObjectHashMap<Set<State>> statesActiveOnNonTerminals = new TIntObjectHashMap<>(500);
        private final TIntObjectHashMap<Set<State>> statesActiveOnTerminals = new TIntObjectHashMap<>(500);
        private final DblSemiring semiring;
        private Multimap<Category, State> statesActiveOn = HashMultimap.create();


        public StateSets(DblSemiring sr) {
            this.semiring = sr;
            this.forwardScores = new TObjectDoubleHashMap<>(500, 0.5F, sr.zero());
            this.innerScores = new TObjectDoubleHashMap<>(500, 0.5F, sr.zero());
        }

        //TODO remove
//        /**
//         * Adds an state to this chart at the given index.
//         *
//         * @param index The index for <code>state</code>.
//         * @param state The state to add.
//         * @throws IndexOutOfBoundsException If <code>index < 0</code>.
//         * @throws NullPointerException      If <code>index</code> or
//         *                                   <code>state</code> is <code>null</code>.
//         */
//        public void addState(int index, State state, double forward, double inner) {
//            if (index < 0) throw new IndexOutOfBoundsException("invalid index: " + index);
//            if (state == null) throw new NullPointerException("null state");
//
//            addStateToIndex(stateSets, state, index, forward, inner);
//
//            if (state.isCompleted()) completedStates.add(state);
//        }
//
//        private void addStateToIndex(TIntObjectMap<Map<State, State.Score>> map, State state, int index,
//                                     double forward, double inner) {
//            Map<State, State.Score> states = map.get(index);
//            if (states == null) states = new HashMap<>();
//            if (states.containsKey(state)) {
//                System.out.println("State already found. Updating forward and inner probabilities.");
//                states.get(state).incrementForwardScore(forward);
//                states.get(state).incrementInnerScore(inner);
//            } else states.put(state, new State.Score(forward, inner));
//
//            map.put(index, states); // always true for new state set
//        }
//
//
//        /**
//         * Gets the set of indices at which this chart contains states. For any
//         * member of this set, {@link #getStates(int)} will return a non-empty
//         * set of states.
//         *
//         * @return A set containing every index in this chart where states have
//         * been added, sorted in ascending order (<code>0 ... <em>n</em></code>).
//         */
//        public int[] indices() {
//            return states.keys();
//        }
//
//        /**
//         * Tests whether this chart contains any states at a given string index. O(1)
//         *
//         * @param index The string index to check for states.
//         * @return <code>true</code> iff this chart contains an state set at
//         * <code>index</code>.
//         */
//        public boolean containsStates() {
//            return stateSets.containsKey();
//        }
//
//        public Set<State> getAtIndex(int index) {
//            return stateSets.get(index);
//        }
//
        public Set<State> getCompletedStates(int index) {
            if (!completedStates.containsKey(index)) completedStates.put(index, new HashSet<>());
            return completedStates.get(index);
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
            if (!states.containsKey(rule)) states.put(rule, new TIntObjectHashMap<>(30));
            TIntObjectMap<TIntObjectMap<TIntObjectMap<State>>> forRule = states.get(rule);

            if (!forRule.containsKey(ruleStart)) forRule.put(ruleStart, new TIntObjectHashMap<>(50));
            TIntObjectMap<TIntObjectMap<State>> forRuleStart = forRule.get(ruleStart);

            if (!forRuleStart.containsKey(index)) forRuleStart.put(index, new TIntObjectHashMap<>(50));
            TIntObjectMap<State> forInd = forRuleStart.get(index);

            if (!forInd.containsKey(dotPosition)) {
                State state = create(index, ruleStart, dotPosition, rule, token);
                addState(forInd, state);
            }

            return forInd.get(dotPosition);
        }

        private void addState(TIntObjectMap<State> forInd, State state) {
            int index = state.getPosition();
            int dotPosition = state.getRuleDotPosition();
            add(byIndex, index, state);
            forInd.put(dotPosition, state);

            if (state.isCompleted()) add(completedStates, index, state);
            if (state.isActive()) {
                statesActiveOn.put(state.getActiveCategory(), state);
                if (state.getActiveCategory() instanceof NonTerminal) add(statesActiveOnNonTerminals, index, state);
                else if (state.getActiveCategory() instanceof Terminal) add(statesActiveOnTerminals, index, state);
                else throw new Error("Neither Terminal nor NonToken...?");
            }
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
         * @param s
         * @return inner score so far
         */
        public double getInnerScore(State s) {
            return innerScores.get(s);
        }

        public Set<State> getStates(int index) {
            return byIndex.get(index);
        }

        public Collection<State> getStatesActiveOnNonTerminal(NonTerminal nonTerminal) {
            return statesActiveOn.get(nonTerminal);
        }

        public Collection<State> getStatesActiveOnNonTerminal() {
            return statesActiveOn.values();
        }

        public State.ViterbiScore getViterbiScore(State s) {
            return viterbiScores.get(s);
        }

        public void add(State state) {
            Rule rule = state.getRule();
            int ruleStart = state.getRuleStartPosition();
            int index = state.getPosition();

            if (!states.containsKey(rule)) states.put(rule, new TIntObjectHashMap<>(30));
            TIntObjectMap<TIntObjectMap<TIntObjectMap<State>>> forRule = states.get(rule);

            if (!forRule.containsKey(ruleStart)) forRule.put(ruleStart, new TIntObjectHashMap<>(50));
            TIntObjectMap<TIntObjectMap<State>> forRuleStart = forRule.get(ruleStart);

            if (!forRuleStart.containsKey(index)) forRuleStart.put(index, new TIntObjectHashMap<>(50));
            TIntObjectMap<State> forInd = forRuleStart.get(index);

            addState(forInd, state);
        }

        public State get(int index, int ruleStart, int ruleDot, Rule rule) {
            if (!states.containsKey(rule)) return null;
            TIntObjectMap<TIntObjectMap<TIntObjectMap<State>>> forRule = states.get(rule);

            if (!forRule.containsKey(ruleStart)) return null;
            TIntObjectMap<TIntObjectMap<State>> forRuleStart = forRule.get(ruleStart);

            if (!forRuleStart.containsKey(index)) return null;
            TIntObjectMap<State> forInd = forRuleStart.get(index);

            return forInd.get(ruleDot);
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
