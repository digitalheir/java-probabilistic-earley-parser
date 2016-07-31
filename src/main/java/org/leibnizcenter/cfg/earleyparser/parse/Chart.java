
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
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.rule.Rule;
import org.leibnizcenter.cfg.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.token.Token;

import java.util.*;


/**
 * A chart produced by an  Earley parser.
 * <p>
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
     * <p>
     * For each state at position i, look at the the nonterminal at the dot position,
     * add a state that expands that nonterminal at position i, with the dot position at 0
     *
     * @param index The string index to make predictions at.
     */
    public void predict(int index) {
        DblSemiring sr = grammar.getSemiring();

        // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·Zμ</code>...
        Collection<State> statesToPredictOn = stateSets.getStatesActiveOnNonTerminals(index);
        for (State statePredecessor : statesToPredictOn) {
            final Category Z = statePredecessor.getActiveCategory();
            double prevForward = stateSets.getForwardScore(statePredecessor);

            // For all productions Y → v such that R(Z =*L> Y) is nonzero
            grammar.getLeftStarCorners().getNonZeroScores(Z).stream()
                    .flatMap(Y -> grammar.getRules(Y).stream())
                    // we predict state <code>i: Y<sub>i</sub> → ·v</code>
                    .forEach(Y_to_v -> {
                        Category Y = Y_to_v.getLeft();
                        State s = stateSets.getOrCreate(index, index, 0, Y_to_v); // We might want to increment the probability of an existing state
                        // α' = α * R(Z =*L> Y) * P(Y → v)
                        stateSets.addForwardScore(s, sr.times(prevForward, grammar.getLeftStarScore(Z, Y), Y_to_v.getProbability()));

                        // γ' = P(Y → v)
                        double innerScore = stateSets.getInnerScore(s);
                        if (!(Y_to_v.getProbability() == innerScore || sr.zero() == innerScore))
                            throw new Error(Y_to_v.getProbability() + " != " + innerScore);
                        stateSets.setInnerScore(s, Y_to_v.getProbability());
                    });
        }
    }

    public void scan(int index, Token token) {
        scan(index, token, null);
    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param index The start index of the scan.
     * @param token The token that was scanned.
     */
    public void scan(int index, Token token, ScanProbability scanProbability) {
        if (token == null) throw new Error("null token at index " + index);//TODO exception?

        // Get all states that are active on a terminal
        //noinspection unchecked
        stateSets.getStatesActiveOnTerminals(index).stream()
                // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
                .filter(state -> ((Terminal) state.getActiveCategory()).hasCategory(token))

                // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                .forEach(prevState -> {
                            State newState = stateSets.getOrCreate(index + 1, prevState.getRuleStartPosition(), prevState.advanceDot(), prevState.getRule());

                            double fw = stateSets.getForwardScore(prevState);
                            DblSemiring sr = grammar.getSemiring();
                            if (scanProbability != null) fw = sr.times(fw, scanProbability.getProbability(index));
                            stateSets.setForwardScore(newState, fw);

                            double inner = stateSets.getInnerScore(prevState);
                            if (scanProbability != null) inner = sr.times(inner, scanProbability.getProbability(index));
                            stateSets.setInnerScore(newState, inner);
                        }
                );
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param index The index to make completions at.
     */
    public void complete(int index) {
        final DblSemiring sr = grammar.getSemiring();
        final Set<State> completedStates = new HashSet<>(stateSets.getCompletedStates(index));
        completedStates.stream()
                // O(|stateset(i)|) = O(|grammar|): For all states <code>i: Y<sub>k</sub> → μ·</code>, such that the production is not a unit production
                .filter(state -> !state.getRule().isUnitProduction())
                .forEach(completedState -> {
                            double completedInner = stateSets.getInnerScore(completedState);
                            final NonTerminal Y = completedState.getRule().getLeft();
                            //Get all states in j <= i, such that <code>j: X<sub>k</sub> →  λ·Zμ</code>, where Z =*> Y is non-zero
                            stateSets.getStatesActiveOnNonTerminal().stream()
                                    .filter(state -> state.getPosition() <= index)
                                    .forEach(stateToAdvance -> {
                                        final Category Z = stateToAdvance.getActiveCategory();
                                        final double leftStarScore = grammar.getLeftStarScore(Z, Y);
                                        if (leftStarScore == sr.zero()) return;

                                        double prevForward = stateSets.getForwardScore(stateToAdvance);
                                        double prevInner = stateSets.getInnerScore(stateToAdvance);

                                        State newState = stateSets.getOrCreate(
                                                index,
                                                stateToAdvance.getRuleStartPosition(),
                                                stateToAdvance.advanceDot(),
                                                stateToAdvance.getRule()
                                        );
                                        stateSets.addForwardScore(newState, sr.times(prevForward, completedInner, leftStarScore));
                                        stateSets.addInnerScore(newState, sr.times(prevInner, completedInner, leftStarScore));
                                    });
                        }
                );
    }

//    /**
//     * Makes predictions (adds states) in the specified chart for a given state
//     * at a given index. This method also make predictions for any newly added state.
//     *
//     * @param state     The state to make predictions for.
//     * @param index     The index in the string under consideration.
//     * @param newStates New states to add to the chart
//     */
//    public void getPredictedStates(State state,
//                                   int index,
//                                   Set<State> newStates,
//                                   boolean predictPreterm,
//                                   ParserListener listener) {
//        Category category = state.getActiveCategory(); // not present if passive
//
//        if (category != null && grammar.containsRules(category)) {
//            // get all rules with the active category on the left
//            for (Rule rule : grammar.getRules(category)) {
//                // TODO
////                if (!predictPreterm && rule.isPreterminal()) {
////                    // only predict for rules that aren't preterminals to avoid
////                    // filling up the chart with entries for every terminal
////                    continue;
////                }
//
//                // make new state at index with dotted rule at position 0
//                State newState = new State(rule, index);
//                // only predict for states the chart did not already contain
//                if (this.addState(index, newState)) {
//                    if (listener != null) listener.edgePredicted(new StateEvent(index, newState));
//                    // recursively predict for the new state
//                    getPredictedStates(newState, index, newStates, predictPreterm, listener);
//                }
//            }
//        }
//    }
    //TODO ?
//    /**
//     * Gets the first index in this chart that contains states.
//     *
//     * @return The minimal member of {@link #indices()}. In most cases, this
//     * will return <code>0</code> (unless this chart is a
//     * {@link #subChart(int, int) subchart} of another chart).
//     */
//    public int firstIndex() {
//        return stateSets.firstKey();
//    }
//
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
//    /**
//     * Counts the total number of states contained in this chart, at any
//     * index.
//     *
//     * @return The total number of states contained.
//     */
//    public int countStates() {
//        return stateSets.size();
//    }
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

    static class StateSets {
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
            return completedStates.get(index);
        }

        public Set<State> getStatesActiveOnNonTerminals(int index) {
            return statesActiveOnNonTerminals.get(index);
        }


        public Set<State> getStatesActiveOnTerminals(int index) {
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
            if (!states.containsKey(rule)) states.put(rule, new TIntObjectHashMap<>(30));
            TIntObjectMap<TIntObjectMap<TIntObjectMap<State>>> forRule = states.get(rule);

            if (!forRule.containsKey(ruleStart)) forRule.put(ruleStart, new TIntObjectHashMap<>(50));
            TIntObjectMap<TIntObjectMap<State>> forRuleStart = forRule.get(ruleStart);

            if (!forRuleStart.containsKey(index)) forRuleStart.put(index, new TIntObjectHashMap<>(50));
            TIntObjectMap<State> forInd = forRuleStart.get(index);

            if (!forInd.containsKey(dotPosition)) {
                State state = new State(rule, ruleStart, index, dotPosition);
                add(byIndex, index, state);
                forInd.put(dotPosition, state);

                if (state.isCompleted()) add(completedStates, index, state);
                if (state.isActive()) {
                    statesActiveOn.put(state.getActiveCategory(), state);
                    if (state.getActiveCategory() instanceof NonTerminal) add(statesActiveOnNonTerminals, index, state);
                    else if (state.getActiveCategory() instanceof Terminal) add(statesActiveOnTerminals, index, state);
                    else throw new Error("Neithor Terminal nor NonTerminal...?");
                }
            }

            return forInd.get(dotPosition);
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

    }
}
