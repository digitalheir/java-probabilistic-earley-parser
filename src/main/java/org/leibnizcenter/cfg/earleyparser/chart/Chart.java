package org.leibnizcenter.cfg.earleyparser.chart;

import org.leibnizcenter.cfg.Grammar;
import org.leibnizcenter.cfg.algebra.expression.AddableValuesContainer;
import org.leibnizcenter.cfg.algebra.semiring.dbl.DblSemiring;
import org.leibnizcenter.cfg.algebra.semiring.dbl.ExpressionSemiring;
import org.leibnizcenter.cfg.category.nonterminal.NonTerminal;
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
        Predict.predict(index, grammar, stateSets);
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
        Scan.scan(tokenPosition, token, scanProbability, grammar, stateSets);
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

        Complete.completeNoViterbi(
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
