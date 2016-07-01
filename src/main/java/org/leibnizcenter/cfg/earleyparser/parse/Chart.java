
package org.leibnizcenter.cfg.earleyparser.parse;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.category.Category;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
import org.leibnizcenter.cfg.category.terminal.Terminal;
import org.leibnizcenter.cfg.earleyparser.chart.State;
import org.leibnizcenter.cfg.earleyparser.exception.ParseException;
import org.leibnizcenter.cfg.token.Token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
        this(grammar, new StateSets());
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
        new StatePredictor(index, stateSets);

    }

    /**
     * Handles a token scanned from the input string.
     *
     * @param index The start index of the scan.
     * @param token The token that was scanned.
     * @throws ParseException If <code>token</code> is </code>null</code>.
     */
    public void scan(int index, Token token) throws ParseException {
        if (token == null) throw new ParseException("null token at index " + index);

        this.getStates(index).entrySet().stream()
                // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → λ·tμ</code>, where t is a terminal that matches the given token...
                .filter(state -> state.getKey().isActive()
                        && state.getKey().getActiveCategory() instanceof Terminal
                        && ((Terminal) state.getKey().getActiveCategory()).hasCategory(token)) // TODO we might index on this
                // Create the state <code>i+1: X<sub>k</sub> → λt·μ</code>
                .map(state -> new State(
                        state.getKey().getRule(),
                        state.getKey().getRuleStartPosition(),
                        index + 1,
                        state.getKey().getRuleDotPosition() + 1))
                .forEach(state -> stateSets.addState(state.getPosition(), state, 0.0, 0.0));
    }

    /**
     * Makes completions in the specified chart at the given index.
     *
     * @param index The index to make completions at.
     */
    public void complete(int index) {
        // |grammar|
        Set<State> completedStates = stateSets.getCompletedStates(index).stream()
                // O(|stateset(i)|) = O(|grammar|): For all states <code>i: X<sub>k</sub> → μ·</code>...
                // ... Get all unique non-terminals X
                .collect(Collectors.toSet());

        // And for all states in j <= i, such that <code>j: Y<sub>k</sub> →  λ·Xμ</code>
        while (completedStates.size() > 0) {
            for (State completedState : completedStates) {
                Set<State> completedStatez =
                        getStatesWithActiveCategory(completedState.getRuleStartPosition(), completedState.getRule().getLeft())
                                // Create the new state <code>i: Y<sub>k</sub> →  λX·μ</code> if it does not exist yet
                                .map(state -> new State(state.getRule(),
                                        state.getRuleStartPosition(),
                                        completedState.getPosition(),
                                        state.getRuleDotPosition() + 1))
                                .filter(state -> !getCompletedStates(index).anyMatch(state::equals))
                                .collect(Collectors.toSet());
                completedStatez.forEach(completion -> stateSets.addState(completion.getPosition(), completion));
                completedStates = completedStatez.stream().filter(State::isCompleted).collect(Collectors.toSet());
            }
        }
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


    /**
     * Counts the total number of states contained in this chart, at any
     * index.
     *
     * @return The total number of states contained.
     */
    public int countStates() {
        return stateSets.size();
    }

    /**
     * Gets the states in this chart at a given index.
     *
     * @param index The index to return states for.
     * @return The {@link Set set} of {@link State states} this chart contains
     * at <code>index</code>, or <code>null</code> if no state set exists in
     * this chart for the given index. The state set returned by this
     * method is <em>not</em> guaranteed to contain the states in the order in
     * which they were added. This method
     * returns a set of states that is not modifiable.
     * @throws NullPointerException If <code>index</code> is <code>null</code>.
     * @see java.util.Collections#unmodifiableSet(Set)
     */
    public Map<State, State.Score> getStates(int index) {
        return stateSets.getAtIndex(index);
    }


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

    public Stream<State> getCompletedStates(int index) {
        // TODO store map with passive states
        return getStates(index).keySet().stream().filter(State::isCompleted);
    }

    public Stream<State> getStatesWithActiveCategory(int index, Category category) {
        // TODO store map with active states
        return getStates(index).keySet().stream()
                .filter(e -> (category.equals(e.getActiveCategory())));
    }

    public void addState(int i, State state, double forward, double inner) {
        stateSets.addState(i, state, forward, inner);
    }

    public boolean containsStates(int i) {
        return stateSets.containsStates(i);
    }

    static class StateSets {
        private final TIntObjectMap<Map<State, State.Score>> stateSets = new TIntObjectHashMap<>(500);
        private final TIntObjectMap<Map<State, State.Score>> completedStates = new TIntObjectHashMap<>(500);
        private final TIntObjectMap<Map<State, State.Score>> statesActiveOnNonTerminals = new TIntObjectHashMap<>(500);


        public StateSets() {
        }

        /**
         * Adds an state to this chart at the given index. If no other states
         * exist in this chart at the same index, a new state set is created before
         * adding the state.
         *
         * @param index The index for <code>state</code>.
         * @param state The state to add.
         * @throws IndexOutOfBoundsException If <code>index < 0</code>.
         * @throws NullPointerException      If <code>index</code> or
         *                                   <code>state</code> is <code>null</code>.
         */
        public void addState(int index, State state, double forward, double inner) {
            if (index < 0) throw new IndexOutOfBoundsException("invalid index: " + index);
            if (state == null) throw new NullPointerException("null state");
            addStateToIndex(stateSets, state, index, forward, inner);
            if (state.isCompleted()) addStateToIndex(completedStates, state, index, forward, inner);
            if (state.isActive() && state.getActiveCategory() instanceof NonTerminal)
                addStateToIndex(statesActiveOnNonTerminals, state, index, forward, inner);
        }

        private void addStateToIndex(TIntObjectMap<Map<State, State.Score>> map, State state, int index,
                                     double forward, double inner) {
            Map<State, State.Score> states = map.get(index);
            if (states == null) states = new HashMap<>();
            if (states.containsKey(state)) {
                System.out.println("State already found. Updating forward and inner probabilities.");
                states.get(state).incrementForwardScore(forward);
                states.get(state).incrementInnerScore(inner);
            } else states.put(state, new State.Score(forward, inner));

            map.put(index, states); // always true for new state set
        }


        /**
         * Gets the set of indices at which this chart contains states. For any
         * member of this set, {@link #getStates(int)} will return a non-empty
         * set of states.
         *
         * @return A set containing every index in this chart where states have
         * been added, sorted in ascending order (<code>0 ... <em>n</em></code>).
         */
        public int[] indices() {
            return stateSets.keys();
        }


        public int size() {
            //noinspection unchecked
            return Arrays.stream(stateSets.values(new Map[stateSets.size()]))
                    .mapToInt(m -> m.keySet().size())
                    .sum();
        }

        /**
         * Tests whether this chart contains any states at a given string index. O(1)
         *
         * @param index The string index to check for states.
         * @return <code>true</code> iff this chart contains an state set at
         * <code>index</code>.
         */
        public boolean containsStates(int index) {
            return stateSets.containsKey(index);
        }

        public Map<State, State.Score> getAtIndex(int index) {
            return stateSets.get(index);
        }

        public Map<State, State.Score> getCompletedStates(int index) {
            return completedStates.get(index);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StateSets stateSets1 = (StateSets) o;

            return stateSets.equals(stateSets1.stateSets)
                    && completedStates.equals(stateSets1.completedStates);

        }

        @Override
        public int hashCode() {
            int result = stateSets.hashCode();
            result = 31 * result + completedStates.hashCode();
            return result;
        }

        public void addState(int index, State state, State.Score score) {
            addState(index, state, score.getForwardScore(), score.getInnerScore());
        }

        public Map<State, State.Score> getStatesActiveOnNonTerminals(int index) {
            return statesActiveOnNonTerminals.get(index);
        }
    }
}
